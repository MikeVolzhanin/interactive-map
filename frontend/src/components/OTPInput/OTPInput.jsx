import { useRef } from 'react'
import styles from './OTPInput.module.css'

const OTP_LENGTH = 6

/**
 * OTPInput — 6 отдельных полей для ввода кода.
 * Поддерживает:
 * - автопереход вперёд при вводе цифры
 * - возврат к предыдущему полю по Backspace
 * - вставку сразу 6 цифр (paste)
 */
export default function OTPInput({ value, onChange, disabled }) {
  const inputsRef = useRef([])

  // value — строка из 6 символов (может быть короче)
  const digits = Array.from({ length: OTP_LENGTH }, (_, i) => value[i] ?? '')

  function focusAt(index) {
    inputsRef.current[index]?.focus()
  }

  function handleChange(e, index) {
    const raw = e.target.value.replace(/\D/g, '') // только цифры
    if (!raw) return

    const char = raw[raw.length - 1] // берём последний введённый символ
    const next = digits.map((d, i) => (i === index ? char : d))
    onChange(next.join(''))

    if (index < OTP_LENGTH - 1) {
      focusAt(index + 1)
    }
  }

  function handleKeyDown(e, index) {
    if (e.key === 'Backspace') {
      if (digits[index]) {
        // Стираем текущую ячейку
        const next = digits.map((d, i) => (i === index ? '' : d))
        onChange(next.join(''))
      } else if (index > 0) {
        // Переходим к предыдущей и стираем её
        const next = digits.map((d, i) => (i === index - 1 ? '' : d))
        onChange(next.join(''))
        focusAt(index - 1)
      }
      e.preventDefault()
    }

    if (e.key === 'ArrowLeft' && index > 0) focusAt(index - 1)
    if (e.key === 'ArrowRight' && index < OTP_LENGTH - 1) focusAt(index + 1)
  }

  function handlePaste(e) {
    e.preventDefault()
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, OTP_LENGTH)
    if (!pasted) return

    const next = Array.from({ length: OTP_LENGTH }, (_, i) => pasted[i] ?? '')
    onChange(next.join(''))

    // Фокус на последнее заполненное поле
    const lastIndex = Math.min(pasted.length, OTP_LENGTH - 1)
    focusAt(lastIndex)
  }

  return (
    <div className={styles.container} role="group" aria-label="Поля ввода кода подтверждения">
      {digits.map((digit, index) => (
        <input
          key={index}
          ref={(el) => (inputsRef.current[index] = el)}
          type="text"
          inputMode="numeric"
          pattern="[0-9]*"
          maxLength={1}
          value={digit}
          disabled={disabled}
          className={`${styles.cell} ${digit ? styles.filled : ''}`}
          aria-label={`Цифра ${index + 1} из ${OTP_LENGTH}`}
          onChange={(e) => handleChange(e, index)}
          onKeyDown={(e) => handleKeyDown(e, index)}
          onPaste={handlePaste}
          // Предотвращаем стандартное поведение select-all при фокусе
          onFocus={(e) => e.target.select()}
          autoComplete="one-time-code"
        />
      ))}
    </div>
  )
}

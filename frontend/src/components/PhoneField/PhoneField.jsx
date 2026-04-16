import { useRef, useCallback } from 'react'
import styles from './PhoneField.module.css'

/**
 * Форматирует до 10 цифр в строку вида: +7 (XXX) XXX - XX - XX
 */
function formatDisplay(digits) {
  let out = '+7'
  if (digits.length === 0) return out
  out += ' (' + digits.slice(0, 3)
  if (digits.length < 3) return out
  out += ') ' + digits.slice(3, 6)
  if (digits.length < 6) return out
  out += ' - ' + digits.slice(6, 8)
  if (digits.length < 8) return out
  out += ' - ' + digits.slice(8, 10)
  return out
}

/**
 * Вытаскивает ровно 10 цифр из произвольной строки (для вставки).
 * Обрезает ведущую 7/8 если вставили полный номер (11 цифр).
 */
function extractPastedDigits(raw) {
  const all = raw.replace(/\D/g, '')
  if (all.length === 11 && (all[0] === '7' || all[0] === '8')) return all.slice(1, 11)
  return all.slice(0, 10)
}

/**
 * PhoneField — поле с живой маской +7 (XXX) XXX - XX - XX.
 *
 * Стратегия: полный контроль через onKeyDown + preventDefault.
 * Браузер никогда не меняет значение сам — мы сами обновляем digits.
 * Курсор всегда в конце строки (onSelect это обеспечивает).
 *
 * onChange вызывается с raw-форматом "+7XXXXXXXXXX" (для API).
 */
export default function PhoneField({ id, label, value, onChange, error, disabled }) {
  const inputRef = useRef(null)

  // Храним только цифры (10 шт.), остальное — display
  const digits = (value ?? '').replace(/\D/g, '').slice(1) // убираем «7» страны
  const displayed = formatDisplay(digits)

  // Всегда держим курсор в конце — предотвращает попадание в разделители
  const pushCursorToEnd = useCallback(() => {
    requestAnimationFrame(() => {
      const el = inputRef.current
      if (!el) return
      const end = el.value.length
      if (el.selectionStart !== end || el.selectionEnd !== end) {
        el.selectionStart = end
        el.selectionEnd = end
      }
    })
  }, [])

  function handleKeyDown(e) {
    // Пропускаем системные комбинации (Ctrl+C, Ctrl+V и т.д.)
    if (e.ctrlKey || e.metaKey) return
    // Пропускаем Tab
    if (e.key === 'Tab') return

    if (e.key === 'Backspace' || e.key === 'Delete') {
      e.preventDefault()
      if (digits.length > 0) {
        onChange('+7' + digits.slice(0, -1))
      }
      return
    }

    // Только цифры — всё остальное блокируем
    if (/^[0-9]$/.test(e.key)) {
      e.preventDefault()
      if (digits.length < 10) {
        onChange('+7' + digits + e.key)
      }
      return
    }

    // Стрелки/Home/End — разрешаем, но тут же возвращаем курсор в конец
    if (['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'].includes(e.key)) {
      pushCursorToEnd()
      return
    }

    // Всё остальное — блокируем
    e.preventDefault()
  }

  function handlePaste(e) {
    e.preventDefault()
    const pasted = e.clipboardData.getData('text')
    const pastedDigits = extractPastedDigits(pasted)
    // Дополняем уже введённые цифры вставленными (до 10)
    const combined = (digits + pastedDigits).slice(0, 10)
    onChange('+7' + combined)
  }

  // Мобильный fallback: onChange срабатывает на soft-клавиатуре
  // (там onKeyDown может не дать e.key)
  function handleChange(e) {
    const raw = e.target.value
    const newDigits = extractPastedDigits(raw.replace(/^\+7\s?/, ''))
    onChange('+7' + newDigits)
  }

  return (
    <div className={styles.group}>
      <label htmlFor={id} className={styles.label}>{label}</label>
      <input
        ref={inputRef}
        id={id}
        type="tel"
        inputMode="numeric"
        autoComplete="tel"
        value={displayed}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        onPaste={handlePaste}
        onFocus={pushCursorToEnd}
        onClick={pushCursorToEnd}
        // onSelect срабатывает при любом движении курсора (мышь, клавиши)
        onSelect={pushCursorToEnd}
        disabled={disabled}
        className={`${styles.input} ${error ? styles.inputError : ''}`}
        aria-describedby={error ? `${id}-error` : undefined}
        aria-invalid={!!error}
        placeholder="+7 (___) ___ - __ - __"
      />
      {error && (
        <span id={`${id}-error`} className={styles.error} role="alert">{error}</span>
      )}
    </div>
  )
}

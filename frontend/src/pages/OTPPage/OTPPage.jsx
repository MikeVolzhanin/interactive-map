import { useState, useEffect, useCallback } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import AuthCard from '../../components/AuthCard/AuthCard.jsx'
import OTPInput from '../../components/OTPInput/OTPInput.jsx'
import { verify, resendCode, login } from '../../api/auth.js'
import styles from './OTPPage.module.css'

const OTP_LENGTH = 6
const RESEND_TIMEOUT = 59

export default function OTPPage() {
  const navigate = useNavigate()
  const location = useLocation()

  const email    = location.state?.email    ?? ''
  const password = location.state?.password ?? ''
  const from     = location.state?.from     ?? 'register' // 'register' | 'forgot-password'

  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [serverError, setServerError] = useState('')
  const [secondsLeft, setSecondsLeft] = useState(RESEND_TIMEOUT)
  const [resending, setResending] = useState(false)

  // Если попали на страницу без email — редирект на регистрацию
  useEffect(() => {
    if (!email) navigate('/register', { replace: true })
  }, [email, navigate])

  // Таймер обратного отсчёта
  useEffect(() => {
    if (secondsLeft <= 0) return
    const id = setTimeout(() => setSecondsLeft((s) => s - 1), 1000)
    return () => clearTimeout(id)
  }, [secondsLeft])

  const formatted =
    String(Math.floor(secondsLeft / 60)).padStart(2, '0') +
    ':' +
    String(secondsLeft % 60).padStart(2, '0')

  const handleResend = useCallback(async () => {
    if (secondsLeft > 0 || resending) return
    setResending(true)
    setServerError('')
    try {
      await resendCode(email)
      setCode('')
      setSecondsLeft(RESEND_TIMEOUT)
    } catch (err) {
      setServerError(err.message)
    } finally {
      setResending(false)
    }
  }, [email, secondsLeft, resending])

  async function handleSubmit(e) {
    e.preventDefault()
    if (code.length !== OTP_LENGTH || loading) return

    setLoading(true)
    setServerError('')
    try {
      await verify(email, code)

      if (from === 'forgot-password') {
        // Только верификация — дальше пользователь сам задаёт новый пароль
        navigate('/reset-password', { replace: true, state: { email } })
      } else {
        // Регистрация: сразу логинимся и идём в анкету
        const { accessToken, refreshToken } = await login(email, password)
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        navigate('/onboarding', { replace: true })
      }
    } catch (err) {
      setServerError(err.message)
      setCode('')
    } finally {
      setLoading(false)
    }
  }

  const isDisabled = loading || code.length !== OTP_LENGTH

  return (
    <AuthCard title="Подтверждение почты">
      <form onSubmit={handleSubmit} noValidate>
        <p className={styles.subtitle}>
          Введите код из письма, отправленного на{' '}
          <strong className={styles.email}>{email}</strong>
        </p>

        <div className={styles.otpWrapper}>
          <OTPInput value={code} onChange={setCode} disabled={loading} />
        </div>

        {serverError && (
          <p className={styles.serverError} role="alert">{serverError}</p>
        )}

        <button
          type="submit"
          className={styles.submitBtn}
          disabled={isDisabled}
          aria-busy={loading}
        >
          {loading ? 'Проверка…' : 'Подтвердить'}
        </button>

        <div className={styles.resendRow}>
          {secondsLeft > 0 ? (
            <span className={styles.timer}>
              Отправить код повторно через{' '}
              <span className={styles.timerValue}>{formatted}</span>
            </span>
          ) : (
            <button
              type="button"
              className={styles.resendBtn}
              onClick={handleResend}
              disabled={resending}
            >
              {resending ? 'Отправка…' : 'Отправить код повторно'}
            </button>
          )}
        </div>
      </form>
    </AuthCard>
  )
}

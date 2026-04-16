import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthCard from '../../components/AuthCard/AuthCard.jsx'
import InputField from '../../components/InputField/InputField.jsx'
import { forgotPassword } from '../../api/auth.js'
import styles from './ForgotPasswordPage.module.css'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [emailError, setEmailError] = useState('')
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    setEmail(e.target.value)
    if (emailError) setEmailError('')
    if (serverError) setServerError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()

    if (!email.trim()) {
      setEmailError('Введите e-mail')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setEmailError('Некорректный формат e-mail')
      return
    }

    setLoading(true)
    setServerError('')
    try {
      await forgotPassword(email.trim())
      navigate('/otp', {
        state: { email: email.trim(), from: 'forgot-password' },
      })
    } catch (err) {
      setServerError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthCard title="Восстановление пароля">
      <form onSubmit={handleSubmit} noValidate>
        <p className={styles.hint}>
          Введите e-mail, указанный при регистрации. Мы отправим на него код подтверждения.
        </p>

        <div className={styles.fields}>
          <InputField
            id="email"
            name="email"
            label="E-mail"
            type="email"
            autoComplete="email"
            value={email}
            onChange={handleChange}
            error={emailError}
            disabled={loading}
            autoFocus
          />
        </div>

        {serverError && (
          <p className={styles.serverError} role="alert">{serverError}</p>
        )}

        <button
          type="submit"
          className={styles.submitBtn}
          disabled={loading || !email.trim()}
          aria-busy={loading}
        >
          {loading ? 'Отправка…' : 'Восстановить пароль'}
        </button>

        <div className={styles.links}>
          <Link to="/login">← Вернуться ко входу</Link>
        </div>
      </form>
    </AuthCard>
  )
}

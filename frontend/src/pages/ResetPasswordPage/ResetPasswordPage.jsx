import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import AuthCard from '../../components/AuthCard/AuthCard.jsx'
import PasswordField from '../../components/PasswordField/PasswordField.jsx'
import { resetPassword, login } from '../../api/auth.js'
import styles from './ResetPasswordPage.module.css'

const MIN_LENGTH = 6

function validate(password, passwordRepeat) {
  const errors = {}
  if (!password) {
    errors.password = 'Введите новый пароль'
  } else if (password.length < MIN_LENGTH) {
    errors.password = `Минимальная длина — ${MIN_LENGTH} символов`
  }
  if (!passwordRepeat) {
    errors.passwordRepeat = 'Повторите пароль'
  } else if (password !== passwordRepeat) {
    errors.passwordRepeat = 'Пароли не совпадают'
  }
  return errors
}

export default function ResetPasswordPage() {
  const navigate = useNavigate()
  const location = useLocation()

  const email = location.state?.email ?? ''

  const [form, setForm] = useState({ password: '', passwordRepeat: '' })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  // Без email нечего делать — редиректим на восстановление
  useEffect(() => {
    if (!email) navigate('/forgot-password', { replace: true })
  }, [email, navigate])

  function handleChange(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: undefined }))
    if (serverError) setServerError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate(form.password, form.passwordRepeat)
    if (Object.keys(errs).length) {
      setErrors(errs)
      return
    }

    setLoading(true)
    setServerError('')
    try {
      await resetPassword(email, form.password)
      // После сброса — сразу логинимся с новым паролем
      const { accessToken, refreshToken } = await login(email, form.password)
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      navigate('/onboarding', { replace: true })
    } catch (err) {
      setServerError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const isDisabled = loading || !form.password || !form.passwordRepeat

  return (
    <AuthCard title="Новый пароль">
      <form onSubmit={handleSubmit} noValidate>
        <p className={styles.hint}>
          Придумайте новый пароль для аккаунта{' '}
          <strong className={styles.email}>{email}</strong>
        </p>

        <div className={styles.fields}>
          <PasswordField
            id="password"
            name="password"
            label="Новый пароль"
            autoComplete="new-password"
            value={form.password}
            onChange={handleChange}
            error={errors.password}
            disabled={loading}
            autoFocus
          />

          <PasswordField
            id="passwordRepeat"
            name="passwordRepeat"
            label="Повторите пароль"
            autoComplete="new-password"
            value={form.passwordRepeat}
            onChange={handleChange}
            error={errors.passwordRepeat}
            disabled={loading}
          />
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
          {loading ? 'Сохранение…' : 'Сохранить пароль'}
        </button>
      </form>
    </AuthCard>
  )
}

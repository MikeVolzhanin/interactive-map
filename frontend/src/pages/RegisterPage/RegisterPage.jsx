import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthCard from '../../components/AuthCard/AuthCard.jsx'
import InputField from '../../components/InputField/InputField.jsx'
import PasswordField from '../../components/PasswordField/PasswordField.jsx'
import { signup } from '../../api/auth.js'
import styles from './RegisterPage.module.css'

const MIN_PASSWORD_LENGTH = 6

function validate(form) {
  const errors = {}

  if (!form.email.trim()) {
    errors.email = 'Введите e-mail'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = 'Некорректный формат e-mail'
  }

  if (!form.password) {
    errors.password = 'Введите пароль'
  } else if (form.password.length < MIN_PASSWORD_LENGTH) {
    errors.password = `Минимальная длина пароля — ${MIN_PASSWORD_LENGTH} символов`
  }

  if (!form.passwordRepeat) {
    errors.passwordRepeat = 'Повторите пароль'
  } else if (form.password !== form.passwordRepeat) {
    errors.passwordRepeat = 'Пароли не совпадают'
  }

  return errors
}

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', passwordRepeat: '' })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: undefined }))
    if (serverError) setServerError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length) {
      setErrors(errs)
      return
    }

    setLoading(true)
    setServerError('')
    try {
      await signup(form.email.trim(), form.password)
      // Передаём email в OTP-страницу через router state
      navigate('/otp', { state: { email: form.email.trim(), password: form.password, from: 'register' } })
    } catch (err) {
      setServerError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const isDisabled = loading || !form.email || !form.password || !form.passwordRepeat

  return (
    <AuthCard title="Регистрация">
      <form onSubmit={handleSubmit} noValidate>
        <div className={styles.fields}>
          <InputField
            id="email"
            name="email"
            label="E-mail"
            type="email"
            autoComplete="email"
            value={form.email}
            onChange={handleChange}
            error={errors.email}
            disabled={loading}
            autoFocus
          />

          <PasswordField
            id="password"
            name="password"
            label="Пароль"
            autoComplete="new-password"
            value={form.password}
            onChange={handleChange}
            error={errors.password}
            disabled={loading}
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
          {loading ? 'Регистрация…' : 'Зарегистрироваться'}
        </button>

        <div className={styles.links}>
          <span className={styles.hint}>Уже есть аккаунт?</span>
          <Link to="/login">Войти</Link>
        </div>
      </form>
    </AuthCard>
  )
}

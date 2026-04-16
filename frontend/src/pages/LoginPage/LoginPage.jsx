import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthCard from '../../components/AuthCard/AuthCard.jsx'
import InputField from '../../components/InputField/InputField.jsx'
import PasswordField from '../../components/PasswordField/PasswordField.jsx'
import { login } from '../../api/auth.js'
import styles from './LoginPage.module.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
    if (serverError) setServerError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setServerError('')
    try {
      const { accessToken, refreshToken } = await login(form.email.trim(), form.password)
      // Сохраняем токены
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      navigate('/onboarding')
    } catch (err) {
      setServerError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const isDisabled = loading || !form.email.trim() || !form.password

  return (
    <AuthCard title="Авторизация">
      <form onSubmit={handleSubmit} noValidate>
        <div className={styles.fields}>
          <InputField
            id="email"
            name="email"
            label="E-mail"
            type="email"
            autoComplete="username"
            value={form.email}
            onChange={handleChange}
            disabled={loading}
            autoFocus
          />

          <PasswordField
            id="password"
            name="password"
            label="Пароль"
            autoComplete="current-password"
            value={form.password}
            onChange={handleChange}
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
          {loading ? 'Вход…' : 'Войти'}
        </button>

        <div className={styles.links}>
          <Link to="/register">Зарегистрироваться</Link>
          <span className={styles.divider} aria-hidden="true" />
          <Link to="/forgot-password">Восстановить пароль</Link>
        </div>
      </form>
    </AuthCard>
  )
}

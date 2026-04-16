import { useNavigate } from 'react-router-dom'
import styles from './MapPage.module.css'

export default function MapPage() {
  const navigate = useNavigate()

  function handleLogout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    navigate('/login', { replace: true })
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <span className={styles.logo}>ВШЭ</span>
        <span className={styles.title}>Интерактивная карта</span>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          Выйти
        </button>
      </header>

      <main className={styles.main}>
        {/* TODO: здесь будет карта */}
        <p className={styles.placeholder}>Карта появится здесь</p>
      </main>
    </div>
  )
}

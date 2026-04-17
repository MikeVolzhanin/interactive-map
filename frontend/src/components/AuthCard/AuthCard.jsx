import styles from './AuthCard.module.css'

/**
 * AuthCard — оболочка-карточка для всех страниц аутентификации.
 * Содержит центрированный контейнер + нижний футер с логотипом ВШЭ.
 */
export default function AuthCard({ title, children }) {
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <div className={styles.card} role="main">
          <h1 className={styles.title}>{title}</h1>
          {children}
        </div>

        <p className={styles.support}>
          Если у вас возникли технические сложности, свяжитесь с нами по email{' '}
          <a href="mailto:mrvolzhanin@edu.hse.ru">mrvolzhanin@edu.hse.ru</a>
        </p>
      </main>

      <footer className={styles.footer}>
        <img src="/hse-logo-footer.svg" alt="НИУ ВШЭ" className={styles.logo} />
      </footer>
    </div>
  )
}

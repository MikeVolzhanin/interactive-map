import styles from './SelectField.module.css'

/**
 * SelectField — обёртка над <select> в едином стиле с InputField.
 * Автоматически применяет стиль placeholder когда value пустое.
 */
export default function SelectField({ id, label, error, children, className, value, ...selectProps }) {
  const isEmpty = value === '' || value === undefined || value === null

  return (
    <div className={`${styles.group} ${className ?? ''}`}>
      <label htmlFor={id} className={styles.label}>
        {label}
      </label>
      <div className={styles.wrapper}>
        <select
          id={id}
          value={value}
          className={[
            styles.select,
            isEmpty ? styles.selectEmpty : '',
            error ? styles.selectError : '',
          ].join(' ')}
          aria-describedby={error ? `${id}-error` : undefined}
          aria-invalid={!!error}
          {...selectProps}
        >
          {children}
        </select>
        <svg
          className={styles.arrow}
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M6 9l6 6 6-6"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </div>
      {error && (
        <span id={`${id}-error`} className={styles.error} role="alert">
          {error}
        </span>
      )}
    </div>
  )
}

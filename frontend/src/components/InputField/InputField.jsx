import styles from './InputField.module.css'

/**
 * InputField — универсальное поле ввода с label и сообщением об ошибке.
 */
export default function InputField({
  id,
  label,
  error,
  className,
  ...inputProps
}) {
  return (
    <div className={`${styles.group} ${className ?? ''}`}>
      <label htmlFor={id} className={styles.label}>
        {label}
      </label>
      <input
        id={id}
        className={`${styles.input} ${error ? styles.inputError : ''}`}
        aria-describedby={error ? `${id}-error` : undefined}
        aria-invalid={!!error}
        {...inputProps}
      />
      {error && (
        <span id={`${id}-error`} className={styles.error} role="alert">
          {error}
        </span>
      )}
    </div>
  )
}

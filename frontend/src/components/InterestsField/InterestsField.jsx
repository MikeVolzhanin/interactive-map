import styles from './InterestsField.module.css'

/**
 * InterestsField — группа чекбоксов для множественного выбора интересов.
 * items: Array<{ id: number, name: string }>
 * value: number[]  — массив выбранных id
 */
export default function InterestsField({ items, value, onChange, disabled, error, loading }) {
  function toggle(id) {
    if (value.includes(id)) {
      onChange(value.filter((v) => v !== id))
    } else {
      onChange([...value, id])
    }
  }

  return (
    <div className={styles.group}>
      <span className={styles.label}>Сферы интересов</span>

      {loading ? (
        <p className={styles.hint}>Загрузка…</p>
      ) : items.length === 0 ? (
        <p className={styles.hint}>Нет доступных вариантов</p>
      ) : (
        <div className={styles.grid}>
          {items.map((item) => {
            const checked = value.includes(item.id)
            return (
              <label
                key={item.id}
                className={`${styles.chip} ${checked ? styles.chipChecked : ''} ${disabled ? styles.chipDisabled : ''}`}
              >
                <input
                  type="checkbox"
                  className={styles.hidden}
                  checked={checked}
                  onChange={() => toggle(item.id)}
                  disabled={disabled}
                  aria-label={item.name}
                />
                {item.name}
              </label>
            )
          })}
        </div>
      )}

      {error && (
        <span className={styles.error} role="alert">{error}</span>
      )}
    </div>
  )
}

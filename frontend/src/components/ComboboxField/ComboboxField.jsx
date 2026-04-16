import { useState, useRef, useEffect, useId } from 'react'
import styles from './ComboboxField.module.css'

/**
 * ComboboxField — поле с живым поиском по списку.
 *
 * Props:
 *   items       — Array<{ id: number|string, name: string }>
 *   value       — id выбранного элемента (число/строка) или ''
 *   onChange    — (id) => void
 *   label       — string
 *   placeholder — string
 *   error       — string | undefined
 *   disabled    — boolean
 *   loading     — boolean
 */
export default function ComboboxField({
  items = [],
  value,
  onChange,
  label,
  placeholder = 'Начните вводить…',
  error,
  disabled,
  loading,
}) {
  const uid = useId()
  const listId = `${uid}-list`

  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)

  const inputRef = useRef(null)
  const listRef = useRef(null)
  const containerRef = useRef(null)

  // Находим выбранный элемент по id
  const selectedItem = items.find((it) => String(it.id) === String(value)) ?? null

  // Фильтруем список по введённому тексту
  const filtered = query.trim()
    ? items.filter((it) => it.name.toLowerCase().includes(query.trim().toLowerCase()))
    : items

  // Закрыть при клике вне компонента
  useEffect(() => {
    function onPointerDown(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        closeDropdown()
      }
    }
    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [])

  // Прокручиваем активный пункт в видимую область
  useEffect(() => {
    if (activeIndex < 0 || !listRef.current) return
    const item = listRef.current.children[activeIndex]
    item?.scrollIntoView({ block: 'nearest' })
  }, [activeIndex])

  function openDropdown() {
    setOpen(true)
    setActiveIndex(-1)
  }

  function closeDropdown() {
    setOpen(false)
    setActiveIndex(-1)
    // Восстанавливаем query = имя выбранного (или пусто)
    setQuery('')
  }

  function selectItem(item) {
    onChange(item.id)
    setQuery('')
    setOpen(false)
    inputRef.current?.blur()
  }

  function handleInputChange(e) {
    setQuery(e.target.value)
    setOpen(true)
    setActiveIndex(-1)
    // Если пользователь стёр текст — сбрасываем значение
    if (!e.target.value) onChange('')
  }

  function handleKeyDown(e) {
    if (!open) {
      if (e.key === 'ArrowDown' || e.key === 'Enter') openDropdown()
      return
    }

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault()
        setActiveIndex((i) => Math.min(i + 1, filtered.length - 1))
        break
      case 'ArrowUp':
        e.preventDefault()
        setActiveIndex((i) => Math.max(i - 1, 0))
        break
      case 'Enter':
        e.preventDefault()
        if (activeIndex >= 0 && filtered[activeIndex]) {
          selectItem(filtered[activeIndex])
        }
        break
      case 'Escape':
        closeDropdown()
        break
    }
  }

  // Что показываем в поле: если дропдаун открыт — то, что печатает пользователь;
  // если закрыт — имя выбранного элемента или пусто
  const inputValue = open ? query : (selectedItem?.name ?? '')

  return (
    <div ref={containerRef} className={styles.group}>
      <label htmlFor={uid} className={styles.label}>{label}</label>

      <div className={`${styles.wrapper} ${open ? styles.wrapperOpen : ''}`}>
        <input
          ref={inputRef}
          id={uid}
          type="text"
          role="combobox"
          aria-expanded={open}
          aria-controls={listId}
          aria-autocomplete="list"
          aria-activedescendant={activeIndex >= 0 ? `${listId}-${activeIndex}` : undefined}
          aria-invalid={!!error}
          aria-describedby={error ? `${uid}-error` : undefined}
          autoComplete="off"
          value={inputValue}
          placeholder={loading ? 'Загрузка…' : placeholder}
          disabled={disabled || loading}
          className={`${styles.input} ${error ? styles.inputError : ''}`}
          onChange={handleInputChange}
          onFocus={openDropdown}
          onKeyDown={handleKeyDown}
        />

        {/* Иконка: крестик если что-то выбрано, иначе стрелка */}
        <button
          type="button"
          tabIndex={-1}
          className={styles.iconBtn}
          onClick={() => {
            if (selectedItem && !open) {
              onChange('')
              setQuery('')
              inputRef.current?.focus()
            } else {
              open ? closeDropdown() : openDropdown()
              inputRef.current?.focus()
            }
          }}
          aria-label={selectedItem && !open ? 'Очистить' : 'Открыть список'}
          disabled={disabled || loading}
        >
          {selectedItem && !open ? <ClearIcon /> : <ChevronIcon open={open} />}
        </button>

        {open && (
          <ul
            ref={listRef}
            id={listId}
            role="listbox"
            className={styles.dropdown}
          >
            {filtered.length === 0 ? (
              <li className={styles.empty}>Ничего не найдено</li>
            ) : (
              filtered.map((item, index) => (
                <li
                  key={item.id}
                  id={`${listId}-${index}`}
                  role="option"
                  aria-selected={String(item.id) === String(value)}
                  className={[
                    styles.option,
                    String(item.id) === String(value) ? styles.optionSelected : '',
                    index === activeIndex ? styles.optionActive : '',
                  ].join(' ')}
                  onPointerDown={(e) => {
                    // pointerDown чтобы сработало до onBlur у input
                    e.preventDefault()
                    selectItem(item)
                  }}
                >
                  {item.name}
                </li>
              ))
            )}
          </ul>
        )}
      </div>

      {error && (
        <span id={`${uid}-error`} className={styles.error} role="alert">{error}</span>
      )}
    </div>
  )
}

function ChevronIcon({ open }) {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
      style={{ transform: open ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }}
    >
      <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function ClearIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

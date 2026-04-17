import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import InputField from '../../components/InputField/InputField.jsx'
import SelectField from '../../components/SelectField/SelectField.jsx'
import PhoneField from '../../components/PhoneField/PhoneField.jsx'
import ComboboxField from '../../components/ComboboxField/ComboboxField.jsx'
import InterestsField from '../../components/InterestsField/InterestsField.jsx'
import { fetchRegions, fetchEducationLevels, fetchInterests } from '../../api/catalog.js'
import { getUserInfo, addUserInfo } from '../../api/users.js'
import { logout } from '../../api/auth.js'
import styles from './OnboardingPage.module.css'

const PHONE_RE = /^\+7\d{10}$/
const CURRENT_YEAR = new Date().getFullYear()

const EMPTY_FORM = {
  lastName: '',
  firstName: '',
  middleName: '',
  phoneNumber: '',
  regionId: '',
  educationLevelId: '',
  yearOfAdmission: '',
  interestIds: [],
  consent: false,
}

/** Преобразует ответ GET /api/users/get-info в состояние формы */
function infoToForm(info) {
  return {
    lastName: info.lastName ?? '',
    firstName: info.firstName ?? '',
    middleName: info.middleName ?? '',
    phoneNumber: info.phoneNumber ?? '',
    // null от бэка → '' для контролируемых инпутов
    regionId: info.regionId ?? '',
    educationLevelId: info.educationLevelId ?? '',
    yearOfAdmission: info.yearOfAdmission != null ? String(info.yearOfAdmission) : '',
    interestIds: info.interestIds ?? [],
    // Уже давал согласие — считаем подтверждённым
    consent: true,
  }
}

function validate(form) {
  const errors = {}

  if (!form.lastName.trim()) errors.lastName = 'Введите фамилию'
  if (!form.firstName.trim()) errors.firstName = 'Введите имя'

  if (!form.phoneNumber.trim()) {
    errors.phoneNumber = 'Введите номер телефона'
  } else if (!PHONE_RE.test(form.phoneNumber.trim())) {
    errors.phoneNumber = 'Формат: +7XXXXXXXXXX (10 цифр после +7)'
  }

  if (!form.regionId) errors.regionId = 'Выберите регион'
  if (!form.educationLevelId) errors.educationLevelId = 'Выберите уровень образования'

  if (!form.yearOfAdmission) {
    errors.yearOfAdmission = 'Введите год поступления'
  } else {
    const year = Number(form.yearOfAdmission)
    if (!Number.isInteger(year) || year < CURRENT_YEAR || year > 2100) {
      errors.yearOfAdmission = `Год не может быть раньше ${CURRENT_YEAR}`
    }
  }

  if (!form.consent) errors.consent = 'Необходимо ваше согласие'

  return errors
}

export default function OnboardingPage() {
  const navigate = useNavigate()

  const [catalog, setCatalog] = useState({ regions: [], educationLevels: [], interests: [] })
  const [form, setForm] = useState(EMPTY_FORM)
  const [profileCompleted, setProfileCompleted] = useState(false)
  const [initialLoading, setInitialLoading] = useState(true)
  const [catalogError, setCatalogError] = useState('')
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)

  // Загружаем каталог и профиль параллельно
  useEffect(() => {
    Promise.all([
      fetchRegions(),
      fetchEducationLevels(),
      fetchInterests(),
      getUserInfo().catch(() => null), // 404/401 — просто null, не блокируем
    ])
      .then(([regions, educationLevels, interests, userInfo]) => {
        setCatalog({ regions, educationLevels, interests })

        if (userInfo?.profileCompleted) {
          setForm(infoToForm(userInfo))
          setProfileCompleted(true)
        }
      })
      .catch((err) => setCatalogError(err.message))
      .finally(() => setInitialLoading(false))
  }, [])

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    const next = type === 'checkbox' ? checked : value
    setForm((prev) => ({ ...prev, [name]: next }))
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: undefined }))
    if (serverError) setServerError('')
  }

  function handleInterests(ids) {
    setForm((prev) => ({ ...prev, interestIds: ids }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length) {
      setErrors(errs)
      document.querySelector('[aria-invalid="true"]')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
      return
    }

    setLoading(true)
    setServerError('')
    try {
      await addUserInfo({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        middleName: form.middleName.trim() || null,
        phoneNumber: form.phoneNumber.trim(),
        yearOfAdmission: Number(form.yearOfAdmission),
        educationLevelId: Number(form.educationLevelId),
        // Отправляем null если не выбрано, а не 0
        regionId: form.regionId !== '' ? Number(form.regionId) : null,
        interestIds: form.interestIds,
      })
      setSuccess(true)
      await logout()
      setTimeout(() => navigate('/map', { replace: true }), 1500)
    } catch (err) {
      setServerError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const canSubmit =
    !loading &&
    form.lastName.trim() &&
    form.firstName.trim() &&
    form.phoneNumber.trim() &&
    form.regionId &&
    form.educationLevelId &&
    form.yearOfAdmission &&
    form.consent

  if (initialLoading) {
    return (
      <div className={styles.page}>
        <main className={styles.main}>
          <div className={styles.skeletonCard}>
            <div className={styles.skeletonTitle} />
            <div className={styles.skeletonLine} />
            <div className={styles.skeletonLine} style={{ width: '70%' }} />
            <div className={styles.skeletonLine} />
          </div>
        </main>
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <div className={styles.card}>
          <h1 className={styles.title}>
            {profileCompleted ? 'Редактировать анкету' : 'Заполните анкету'}
          </h1>
          <p className={styles.subtitle}>
            {profileCompleted
              ? 'Измените нужные поля и сохраните'
              : 'Это поможет нам персонализировать ваш опыт в системе'}
          </p>

          {catalogError && (
            <p className={styles.catalogError} role="alert">
              Не удалось загрузить справочные данные: {catalogError}
            </p>
          )}

          <form onSubmit={handleSubmit} noValidate>

            {/* ФИО */}
            <fieldset className={styles.fieldset} disabled={loading}>
              <legend className={styles.legend}>ФИО</legend>
              <div className={styles.nameGrid}>
                <InputField
                  id="lastName"
                  name="lastName"
                  label="Фамилия *"
                  type="text"
                  autoComplete="family-name"
                  value={form.lastName}
                  onChange={handleChange}
                  error={errors.lastName}
                />
                <InputField
                  id="firstName"
                  name="firstName"
                  label="Имя *"
                  type="text"
                  autoComplete="given-name"
                  value={form.firstName}
                  onChange={handleChange}
                  error={errors.firstName}
                />
                <InputField
                  id="middleName"
                  name="middleName"
                  label="Отчество"
                  type="text"
                  autoComplete="additional-name"
                  value={form.middleName}
                  onChange={handleChange}
                  placeholder="Необязательно"
                />
              </div>
            </fieldset>

            {/* Контактные данные */}
            <fieldset className={styles.fieldset} disabled={loading}>
              <legend className={styles.legend}>Контактные данные</legend>
              <div className={styles.fields}>
                <PhoneField
                  id="phoneNumber"
                  label="Телефон *"
                  value={form.phoneNumber}
                  onChange={(val) => {
                    setForm((prev) => ({ ...prev, phoneNumber: val }))
                    if (errors.phoneNumber) setErrors((prev) => ({ ...prev, phoneNumber: undefined }))
                    if (serverError) setServerError('')
                  }}
                  error={errors.phoneNumber}
                  disabled={loading}
                />
              </div>
            </fieldset>

            {/* Учебные данные */}
            <fieldset className={styles.fieldset} disabled={loading}>
              <legend className={styles.legend}>Учебные данные</legend>
              <div className={styles.fields}>
                <ComboboxField
                  label="Регион *"
                  items={catalog.regions}
                  value={form.regionId}
                  onChange={(id) => {
                    setForm((prev) => ({ ...prev, regionId: id }))
                    if (errors.regionId) setErrors((prev) => ({ ...prev, regionId: undefined }))
                  }}
                  error={errors.regionId}
                  disabled={loading}
                  loading={initialLoading}
                  placeholder="Начните вводить название региона…"
                />

                <SelectField
                  id="educationLevelId"
                  name="educationLevelId"
                  label="Уровень образования *"
                  value={form.educationLevelId}
                  onChange={handleChange}
                  error={errors.educationLevelId}
                  disabled={initialLoading || loading}
                >
                  <option value="">Выберите уровень</option>
                  {catalog.educationLevels.map((l) => (
                    <option key={l.id} value={l.id}>{l.level}</option>
                  ))}
                </SelectField>

                <InputField
                  id="yearOfAdmission"
                  name="yearOfAdmission"
                  label="Год поступления *"
                  type="number"
                  min={CURRENT_YEAR}
                  max="2100"
                  placeholder={String(CURRENT_YEAR)}
                  value={form.yearOfAdmission}
                  onChange={handleChange}
                  error={errors.yearOfAdmission}
                />
              </div>
            </fieldset>

            {/* Интересы */}
            <fieldset className={styles.fieldset} disabled={loading}>
              <legend className={styles.legend}>Интересы</legend>
              <InterestsField
                items={catalog.interests}
                value={form.interestIds}
                onChange={handleInterests}
                disabled={loading}
                loading={initialLoading}
              />
            </fieldset>

            {/* Согласие */}
            <div className={styles.consentBlock}>
              <label className={`${styles.consentLabel} ${errors.consent ? styles.consentError : ''}`}>
                <input
                  type="checkbox"
                  name="consent"
                  className={styles.checkbox}
                  checked={form.consent}
                  onChange={handleChange}
                  disabled={loading}
                  aria-required="true"
                />
                <span className={styles.consentText}>
                  Я подтверждаю, что лично ознакомился с{' '}
                  <a href="https://www.hse.ru/data_protection_regulation" target="_blank" rel="noopener noreferrer">
                    Положением об обработке персональных данных НИУ ВШЭ
                  </a>
                  , вправе предоставлять свои персональные данные и давать{' '}
                  <a href="https://www.hse.ru/consent" target="_blank" rel="noopener noreferrer">
                    согласие на их обработку
                  </a>
                  .
                </span>
              </label>
              {errors.consent && (
                <span className={styles.consentErrorText} role="alert">{errors.consent}</span>
              )}
            </div>

            {serverError && (
              <p className={styles.serverError} role="alert">{serverError}</p>
            )}

            {success && (
              <p className={styles.successMsg} role="status">
                {profileCompleted ? 'Изменения сохранены! Переход на карту…' : 'Анкета сохранена! Переход на карту…'}
              </p>
            )}

            <button
              type="submit"
              className={styles.submitBtn}
              disabled={!canSubmit}
              aria-busy={loading}
            >
              {loading
                ? 'Сохранение…'
                : profileCompleted
                  ? 'Сохранить изменения'
                  : 'Сохранить и продолжить'}
            </button>
          </form>
        </div>

        <p className={styles.support}>
          Если у вас возникли технические сложности, свяжитесь с нами по email{' '}
          <a href="mailto:digital@hse.ru">digital@hse.ru</a>
        </p>
      </main>

      <footer className={styles.footer}>
        <span className={styles.logo}>ВШЭ</span>
        <span className={styles.footerText}>
          Высшая школа экономики | Национальный исследовательский университет
        </span>
      </footer>
    </div>
  )
}

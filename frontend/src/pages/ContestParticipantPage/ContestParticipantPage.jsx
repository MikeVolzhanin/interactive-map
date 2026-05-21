import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AuthCard from '../../components/AuthCard/AuthCard.jsx'
import SelectField from '../../components/SelectField/SelectField.jsx'
import { fetchContests } from '../../api/map.js'
import { hasActiveSession } from '../../utils/authSession.js'
import styles from './ContestParticipantPage.module.css'

export default function ContestParticipantPage() {
  const navigate = useNavigate()
  const [contests, setContests] = useState([])
  const [loadingList, setLoadingList] = useState(true)
  const [listError, setListError] = useState('')
  const [selectedId, setSelectedId] = useState('')
  const [fieldError, setFieldError] = useState('')

  useEffect(() => {
    let ignore = false
    setLoadingList(true)
    setListError('')

    fetchContests()
      .then(data => {
        if (ignore) return
        const items = Array.isArray(data) ? data : []
        setContests(items)
        if (items.length === 1) {
          setSelectedId(String(items[0].id))
        }
      })
      .catch(err => {
        if (!ignore) setListError(err.message || 'Не удалось загрузить список конкурсов')
      })
      .finally(() => {
        if (!ignore) setLoadingList(false)
      })

    return () => {
      ignore = true
    }
  }, [])

  function handleParticipate() {
    setFieldError('')

    if (!selectedId) {
      setFieldError('Выберите конкурс из списка')
      return
    }

    const contest = contests.find(item => String(item.id) === selectedId)
    const contestPayload = contest
      ? { contestId: contest.id, contestTitle: contest.title }
      : { contestId: Number(selectedId) }

    if (hasActiveSession()) {
      navigate('/onboarding', {
        state: { from: 'contest-participant', ...contestPayload },
      })
      return
    }

    navigate('/register', {
      state: { from: 'contest-participant', ...contestPayload },
    })
  }

  const listReady = !loadingList && !listError
  const canSubmit = listReady && contests.length > 0

  return (
    <AuthCard title="Участие в конкурсе">
      <p className={styles.hint}>
        Выберите конкурс и подтвердите участие. Если личного кабинета ещё нет, откроется регистрация.
      </p>

      {loadingList && <p className={styles.loading}>Загрузка конкурсов…</p>}

      {listError && (
        <p className={styles.serverError} role="alert">
          {listError}
        </p>
      )}

      {listReady && contests.length === 0 && (
        <p className={styles.empty}>
          В системе пока нет конкурсов. Обратитесь к администратору или зайдите позже.
        </p>
      )}

      {listReady && contests.length > 0 && (
        <div className={styles.fields}>
          <SelectField
            id="contest"
            label="Конкурс"
            value={selectedId}
            onChange={e => {
              setSelectedId(e.target.value)
              if (fieldError) setFieldError('')
            }}
          >
            <option value="">Выберите конкурс</option>
            {contests.map(contest => (
              <option key={contest.id} value={String(contest.id)}>
                {contest.title}
                {contest.status ? ` — ${contest.status}` : ''}
              </option>
            ))}
          </SelectField>
          {fieldError && (
            <p className={styles.fieldError} role="alert">
              {fieldError}
            </p>
          )}
        </div>
      )}

      <button
        type="button"
        className={styles.submitBtn}
        disabled={!canSubmit}
        onClick={handleParticipate}
      >
        Я участник
      </button>
    </AuthCard>
  )
}

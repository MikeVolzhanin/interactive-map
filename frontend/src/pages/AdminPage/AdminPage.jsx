import { useState, useEffect, useRef } from 'react'
import { createPortal } from 'react-dom'
import { useNavigate } from 'react-router-dom'
import {
  adminFetchEducationLevels, adminCreateEducationLevel,
  adminUpdateEducationLevel, adminDeleteEducationLevel,
  adminFetchInterests, adminCreateInterest,
  adminUpdateInterest, adminDeleteInterest,
  adminFetchRegions, adminCreateRegion,
  adminUpdateRegion, adminDeleteRegion,
  adminExportUsers,
} from '../../api/admin.js'
import { logout } from '../../api/auth.js'
import styles from './AdminPage.module.css'

const EXPORT_FIELDS = [
  { key: 'lastName',        label: 'Фамилия' },
  { key: 'firstName',       label: 'Имя' },
  { key: 'middleName',      label: 'Отчество' },
  { key: 'email',           label: 'E-mail' },
  { key: 'phoneNumber',     label: 'Телефон' },
  { key: 'region',          label: 'Регион' },
  { key: 'educationLevel',  label: 'Уровень образования' },
  { key: 'yearOfAdmission', label: 'Год поступления' },
  { key: 'interests',       label: 'Интересы' },
  { key: 'registeredAt',    label: 'Дата регистрации' },
]

function CatalogSection({ title, items, labelKey, onCreate, onUpdate, onDelete, loading, error, searchable, scrollable }) {
  const [editingId, setEditingId]   = useState(null)
  const [editValue, setEditValue]   = useState('')
  const [addingNew, setAddingNew]   = useState(false)
  const [newValue,  setNewValue]    = useState('')
  const [busy,      setBusy]        = useState(false)
  const [localErr,  setLocalErr]    = useState('')
  const [search,    setSearch]      = useState('')
  const editRef = useRef(null)
  const addRef  = useRef(null)

  useEffect(() => { if (editingId !== null) editRef.current?.focus() }, [editingId])
  useEffect(() => { if (addingNew)          addRef.current?.focus()  }, [addingNew])

  function startEdit(item) {
    setEditingId(item.id)
    setEditValue(item[labelKey])
    setLocalErr('')
  }
  function cancelEdit() { setEditingId(null); setEditValue('') }

  async function saveEdit() {
    const val = editValue.trim()
    if (!val) { setLocalErr('Значение не может быть пустым'); return }
    setBusy(true); setLocalErr('')
    try   { await onUpdate(editingId, { [labelKey]: val }); setEditingId(null) }
    catch (e) { setLocalErr(e.message) }
    finally   { setBusy(false) }
  }

  async function handleDelete(id) {
    if (!confirm('Удалить запись?')) return
    setBusy(true); setLocalErr('')
    try   { await onDelete(id) }
    catch (e) { setLocalErr(e.message) }
    finally   { setBusy(false) }
  }

  async function saveNew() {
    const val = newValue.trim()
    if (!val) { setLocalErr('Значение не может быть пустым'); return }
    setBusy(true); setLocalErr('')
    try   { await onCreate({ [labelKey]: val }); setNewValue(''); setAddingNew(false) }
    catch (e) { setLocalErr(e.message) }
    finally   { setBusy(false) }
  }

  function cancelNew() { setAddingNew(false); setNewValue(''); setLocalErr('') }

  const displayedItems = searchable && search.trim()
    ? items.filter(item => item[labelKey].toLowerCase().includes(search.toLowerCase()))
    : items

  return (
    <section className={styles.section}>
      <div className={styles.sectionHeader}>
        <h2 className={styles.sectionTitle}>{title}</h2>
        {!loading && <span className={styles.badge}>{items.length}</span>}
      </div>

      {searchable && (
        <div className={styles.searchWrap}>
          <span className={styles.searchIcon}>🔍</span>
          <input
            className={styles.searchInput}
            placeholder="Поиск по списку…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          {search && (
            <button className={styles.searchClear} onClick={() => setSearch('')} aria-label="Очистить">✕</button>
          )}
        </div>
      )}

      {(error || localErr) && (
        <p className={styles.errorMsg} role="alert">{error || localErr}</p>
      )}

      {loading ? (
        <div className={styles.skeleton}>
          {[1, 2, 3].map(i => <div key={i} className={styles.skeletonLine} />)}
        </div>
      ) : (
        <ul className={`${styles.itemList} ${scrollable ? styles.itemListScrollable : ''}`}>
          {displayedItems.length === 0 && search && (
            <li className={styles.emptySearch}>Ничего не найдено</li>
          )}
          {displayedItems.map(item => (
            <li key={item.id} className={styles.item}>
              {editingId === item.id ? (
                <div className={styles.editRow}>
                  <input
                    ref={editRef}
                    className={styles.itemInput}
                    value={editValue}
                    onChange={e => setEditValue(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter') saveEdit(); if (e.key === 'Escape') cancelEdit() }}
                    disabled={busy}
                  />
                  <button className={styles.btnSave}   onClick={saveEdit}  disabled={busy}>Сохранить</button>
                  <button className={styles.btnCancel} onClick={cancelEdit} disabled={busy}>Отмена</button>
                </div>
              ) : (
                <>
                  <span className={styles.itemText}>{item[labelKey]}</span>
                  <div className={styles.itemActions}>
                    <button
                      className={styles.btnIcon}
                      onClick={() => startEdit(item)}
                      disabled={busy}
                      aria-label="Редактировать"
                      title="Редактировать"
                    >✏</button>
                    <button
                      className={`${styles.btnIcon} ${styles.btnIconDanger}`}
                      onClick={() => handleDelete(item.id)}
                      disabled={busy}
                      aria-label="Удалить"
                      title="Удалить"
                    >✕</button>
                  </div>
                </>
              )}
            </li>
          ))}

          {addingNew && (
            <li className={styles.item}>
              <div className={styles.editRow}>
                <input
                  ref={addRef}
                  className={styles.itemInput}
                  placeholder="Введите значение…"
                  value={newValue}
                  onChange={e => setNewValue(e.target.value)}
                  onKeyDown={e => { if (e.key === 'Enter') saveNew(); if (e.key === 'Escape') cancelNew() }}
                  disabled={busy}
                />
                <button className={styles.btnSave}   onClick={saveNew}  disabled={busy}>Добавить</button>
                <button className={styles.btnCancel} onClick={cancelNew} disabled={busy}>Отмена</button>
              </div>
            </li>
          )}
        </ul>
      )}

      {!addingNew && (
        <button
          className={styles.btnAdd}
          onClick={() => { setAddingNew(true); setLocalErr('') }}
          disabled={loading || busy}
        >
          + Добавить
        </button>
      )}
    </section>
  )
}

function InterestModal({ title, nameValue, descValue, onNameChange, onDescChange, onSave, onCancel, busy, error, descRef }) {
  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') onCancel() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onCancel])

  return createPortal(
    <div className={styles.modalOverlay} onClick={onCancel}>
      <div className={styles.modalBox} onClick={e => e.stopPropagation()}>
        <h3 className={styles.modalTitle}>{title}</h3>

        <label className={styles.modalLabel}>Название</label>
        <input
          className={styles.modalInput}
          value={nameValue}
          onChange={e => onNameChange(e.target.value)}
          placeholder="Название…"
          disabled={busy}
        />

        <label className={styles.modalLabel}>Описание</label>
        <textarea
          ref={descRef}
          className={`${styles.modalInput} ${styles.modalTextarea}`}
          value={descValue}
          onChange={e => onDescChange(e.target.value)}
          placeholder="Описание…"
          disabled={busy}
        />

        {error && <p className={styles.modalError} role="alert">{error}</p>}

        <div className={styles.modalActions}>
          <button className={styles.btnSave}   onClick={onSave}   disabled={busy}>Сохранить</button>
          <button className={styles.btnCancel} onClick={onCancel} disabled={busy}>Отмена</button>
        </div>
      </div>
    </div>,
    document.body,
  )
}

function InterestsSection({ items, onCreate, onUpdate, onDelete, loading, error }) {
  const [editItem,  setEditItem]  = useState(null)
  const [editName,  setEditName]  = useState('')
  const [editDesc,  setEditDesc]  = useState('')
  const [addingNew, setAddingNew] = useState(false)
  const [newName,   setNewName]   = useState('')
  const [newDesc,   setNewDesc]   = useState('')
  const [busy,      setBusy]      = useState(false)
  const [localErr,  setLocalErr]  = useState('')
  const [search,    setSearch]    = useState('')
  const editDescRef = useRef(null)
  const newDescRef  = useRef(null)

  useEffect(() => { if (editItem)   editDescRef.current?.focus() }, [editItem])
  useEffect(() => { if (addingNew)  newDescRef.current?.focus()  }, [addingNew])

  function startEdit(item) { setEditItem(item); setEditName(item.name ?? ''); setEditDesc(item.description ?? ''); setLocalErr('') }
  function cancelEdit()    { setEditItem(null); setEditName(''); setEditDesc('') }

  async function saveEdit() {
    setBusy(true); setLocalErr('')
    try   { await onUpdate(editItem.id, { name: editName.trim(), description: editDesc.trim() }); setEditItem(null) }
    catch (e) { setLocalErr(e.message) }
    finally   { setBusy(false) }
  }

  async function handleDelete(id) {
    if (!confirm('Удалить запись?')) return
    setBusy(true); setLocalErr('')
    try   { await onDelete(id) }
    catch (e) { setLocalErr(e.message) }
    finally   { setBusy(false) }
  }

  async function saveNew() {
    const name = newName.trim()
    if (!name) { setLocalErr('Название не может быть пустым'); return }
    setBusy(true); setLocalErr('')
    try   { await onCreate({ name, description: newDesc.trim() }); setNewName(''); setNewDesc(''); setAddingNew(false) }
    catch (e) { setLocalErr(e.message) }
    finally   { setBusy(false) }
  }

  function cancelNew() { setAddingNew(false); setNewName(''); setNewDesc(''); setLocalErr('') }

  const displayed = search.trim()
    ? items.filter(i => i.name.toLowerCase().includes(search.toLowerCase()))
    : items

  return (
    <section className={styles.section}>
      <div className={styles.sectionHeader}>
        <h2 className={styles.sectionTitle}>Сферы интересов</h2>
        {!loading && <span className={styles.badge}>{items.length}</span>}
      </div>

      <div className={styles.searchWrap}>
        <span className={styles.searchIcon}>🔍</span>
        <input
          className={styles.searchInput}
          placeholder="Поиск по списку…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        {search && <button className={styles.searchClear} onClick={() => setSearch('')} aria-label="Очистить">✕</button>}
      </div>

      {(error || (!editItem && !addingNew && localErr)) && (
        <p className={styles.errorMsg} role="alert">{error || localErr}</p>
      )}

      {loading ? (
        <div className={styles.skeleton}>
          {[1, 2, 3].map(i => <div key={i} className={styles.skeletonLine} />)}
        </div>
      ) : (
        <ul className={`${styles.itemList} ${styles.itemListScrollable}`}>
          {displayed.length === 0 && search && <li className={styles.emptySearch}>Ничего не найдено</li>}
          {displayed.map(item => (
            <li key={item.id} className={styles.item}>
              <div className={styles.interestText}>
                <span className={styles.itemText}>{item.name}</span>
                {item.description && <span className={styles.interestDesc}>{item.description}</span>}
              </div>
              <div className={styles.itemActions}>
                <button className={styles.btnIcon} onClick={() => startEdit(item)} disabled={busy} aria-label="Редактировать" title="Редактировать">✏</button>
                <button className={`${styles.btnIcon} ${styles.btnIconDanger}`} onClick={() => handleDelete(item.id)} disabled={busy} aria-label="Удалить" title="Удалить">✕</button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {!addingNew && (
        <button className={styles.btnAdd} onClick={() => { setAddingNew(true); setLocalErr('') }} disabled={loading || busy}>
          + Добавить
        </button>
      )}

      {editItem && (
        <InterestModal
          title="Редактировать интерес"
          nameValue={editName}
          descValue={editDesc}
          onNameChange={setEditName}
          onDescChange={setEditDesc}
          onSave={saveEdit}
          onCancel={cancelEdit}
          busy={busy}
          error={localErr}
          descRef={editDescRef}
        />
      )}

      {addingNew && (
        <InterestModal
          title="Новый интерес"
          nameValue={newName}
          descValue={newDesc}
          onNameChange={setNewName}
          onDescChange={setNewDesc}
          onSave={saveNew}
          onCancel={cancelNew}
          busy={busy}
          error={localErr}
          descRef={newDescRef}
        />
      )}
    </section>
  )
}

export default function AdminPage() {
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  const [eduLevels,   setEduLevels]   = useState([])
  const [interests,   setInterests]   = useState([])
  const [regions,     setRegions]     = useState([])
  const [loadingEdu,  setLoadingEdu]  = useState(true)
  const [loadingInt,  setLoadingInt]  = useState(true)
  const [loadingReg,  setLoadingReg]  = useState(true)
  const [eduError,    setEduError]    = useState('')
  const [intError,    setIntError]    = useState('')
  const [regError,    setRegError]    = useState('')

  const [exportFields,  setExportFields]  = useState(() => new Set(EXPORT_FIELDS.map(f => f.key)))
  const [exporting,     setExporting]     = useState(false)
  const [exportError,   setExportError]   = useState('')
  const [exportSuccess, setExportSuccess] = useState(false)

  useEffect(() => {
    adminFetchEducationLevels()
      .then(setEduLevels)
      .catch(e => setEduError(e.message))
      .finally(() => setLoadingEdu(false))

    adminFetchInterests()
      .then(setInterests)
      .catch(e => setIntError(e.message))
      .finally(() => setLoadingInt(false))

    adminFetchRegions()
      .then(setRegions)
      .catch(e => setRegError(e.message))
      .finally(() => setLoadingReg(false))
  }, [])

  async function createEdu(data)       { const item = await adminCreateEducationLevel(data);    setEduLevels(p => [...p, item]) }
  async function updateEdu(id, data)   { const item = await adminUpdateEducationLevel(id, data); setEduLevels(p => p.map(e => e.id === id ? item : e)) }
  async function deleteEdu(id)         { await adminDeleteEducationLevel(id);                    setEduLevels(p => p.filter(e => e.id !== id)) }

  async function createInt(data)       { const item = await adminCreateInterest(data);    setInterests(p => [...p, item]) }
  async function updateInt(id, data)   { const item = await adminUpdateInterest(id, data); setInterests(p => p.map(i => i.id === id ? item : i)) }
  async function deleteInt(id)         { await adminDeleteInterest(id);                    setInterests(p => p.filter(i => i.id !== id)) }

  async function createReg(data)       { const item = await adminCreateRegion(data);       setRegions(p => [...p, item]) }
  async function updateReg(id, data)   { const item = await adminUpdateRegion(id, data);   setRegions(p => p.map(r => r.id === id ? item : r)) }
  async function deleteReg(id)         { await adminDeleteRegion(id);                      setRegions(p => p.filter(r => r.id !== id)) }

  function toggleField(key) {
    setExportFields(prev => {
      const next = new Set(prev)
      next.has(key) ? next.delete(key) : next.add(key)
      return next
    })
  }

  async function handleExport() {
    if (exportFields.size === 0) { setExportError('Выберите хотя бы одно поле'); return }
    setExporting(true); setExportError(''); setExportSuccess(false)
    try {
      await adminExportUsers([...exportFields])
      setExportSuccess(true)
      setTimeout(() => setExportSuccess(false), 4000)
    } catch (e) {
      setExportError(e.message)
    } finally {
      setExporting(false)
    }
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <img src="/hse-logo-header.svg" alt="НИУ ВШЭ" className={styles.headerLogo} />
        <h1 className={styles.headerTitle}>Панель администратора</h1>
        <button className={styles.logoutBtn} onClick={handleLogout}>Выйти</button>
      </header>

      <main className={styles.main}>
        <div className={styles.grid}>
          <div className={styles.card}>
            <CatalogSection
              title="Регионы"
              items={regions}
              labelKey="name"
              onCreate={createReg}
              onUpdate={updateReg}
              onDelete={deleteReg}
              loading={loadingReg}
              error={regError}
              searchable
              scrollable
            />
          </div>

          <div className={styles.card}>
            <InterestsSection
              items={interests}
              onCreate={createInt}
              onUpdate={updateInt}
              onDelete={deleteInt}
              loading={loadingInt}
              error={intError}
            />
          </div>

          <div className={`${styles.card} ${styles.cardWide}`}>
            <CatalogSection
              title="Уровни образования"
              items={eduLevels}
              labelKey="level"
              onCreate={createEdu}
              onUpdate={updateEdu}
              onDelete={deleteEdu}
              loading={loadingEdu}
              error={eduError}
            />
          </div>
        </div>

        <div className={styles.card}>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>Выгрузка пользователей</h2>
            <p className={styles.exportHint}>Выберите поля для включения в XLSX-файл:</p>

            <div className={styles.exportGrid}>
              {EXPORT_FIELDS.map(f => (
                <label key={f.key} className={styles.checkLabel}>
                  <input
                    type="checkbox"
                    className={styles.checkbox}
                    checked={exportFields.has(f.key)}
                    onChange={() => toggleField(f.key)}
                    disabled={exporting}
                  />
                  {f.label}
                </label>
              ))}
            </div>

            {exportError   && <p className={styles.errorMsg}   role="alert">{exportError}</p>}
            {exportSuccess && <p className={styles.successMsg} role="status">Файл сформирован и загружен</p>}

            <button
              className={styles.exportBtn}
              onClick={handleExport}
              disabled={exporting || exportFields.size === 0}
              aria-busy={exporting}
            >
              {exporting ? 'Формирование…' : 'Выгрузить XLSX'}
            </button>
          </section>
        </div>
      </main>

    </div>
  )
}

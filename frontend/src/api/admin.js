import { authorizedRequest, authorizedFetch } from './http.js'

async function throwApiError(res, fallback) {
  let message = fallback ?? `Ошибка ${res.status}`
  try {
    const contentType = res.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const body = await res.json()
      message = body.message ?? body.error ?? message
    } else {
      const text = await res.text()
      if (text) message = text
    }
  } catch (_) {
    // оставляем fallback
  }
  throw new Error(message)
}

// ── Education Levels ──────────────────────────────────────────────────────────
const EDU_BASE = '/api/education-levels'

export const adminFetchEducationLevels = () =>
  authorizedRequest(EDU_BASE)

export const adminCreateEducationLevel = ({ level }) =>
  authorizedRequest(EDU_BASE, {
    method: 'POST',
    body: JSON.stringify({ level }),
  })

export const adminUpdateEducationLevel = (id, { level }) =>
  authorizedRequest(`${EDU_BASE}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ level }),
  })

export const adminDeleteEducationLevel = (id) =>
  authorizedRequest(`${EDU_BASE}/${id}`, { method: 'DELETE' })

// ── Interests ─────────────────────────────────────────────────────────────────
const INT_BASE = '/api/interests'

export const adminFetchInterests = () =>
  authorizedRequest(INT_BASE)

export const adminCreateInterest = ({ name, description }) =>
  authorizedRequest(INT_BASE, {
    method: 'POST',
    body: JSON.stringify({ name, description }),
  })

export const adminUpdateInterest = (id, { name, description }) =>
  authorizedRequest(`${INT_BASE}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ name, description }),
  })

export const adminDeleteInterest = (id) =>
  authorizedRequest(`${INT_BASE}/${id}`, { method: 'DELETE' })

// ── Regions ───────────────────────────────────────────────────────────────────
const REG_BASE = '/api/regions'

export const adminFetchRegions = () =>
  authorizedRequest(REG_BASE)

export const adminCreateRegion = ({ name, description }) =>
  authorizedRequest(REG_BASE, {
    method: 'POST',
    body: JSON.stringify({ name, description }),
  })

export const adminUpdateRegion = (id, { name, description }) =>
  authorizedRequest(`${REG_BASE}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ name, description }),
  })

export const adminDeleteRegion = (id) =>
  authorizedRequest(`${REG_BASE}/${id}`, { method: 'DELETE' })

// ── Export ────────────────────────────────────────────────────────────────────
// POST /api/users/export    body: { fields: string[] }
// Response: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
export const adminExportUsers = async (fields) => {
  const res = await authorizedFetch('/api/users/export', {
    method: 'POST',
    body: JSON.stringify({ fields }),
  })

  if (!res.ok) await throwApiError(res)

  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'users.xlsx'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

// ── Contests ──────────────────────────────────────────────────────────────────
export const adminFetchContestExportFields = () =>
  authorizedRequest('/api/admin/contests/export-fields')

// POST /api/admin/contests/export  body: { fields: string[] }
// POST /api/admin/contests/import  → multipart file
export const adminExportContests = async (fields) => {
  let res
  try {
    res = await authorizedFetch('/api/admin/contests/export', {
      method: 'POST',
      body: JSON.stringify({ fields: fields ?? [] }),
    })
  } catch {
    throw new Error('Не удалось выгрузить файл. Проверьте, что сервер запущен и вы вошли как администратор.')
  }

  if (!res.ok) await throwApiError(res)

  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'contests.xlsx'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export const adminImportContests = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  return authorizedRequest('/api/admin/contests/import', {
    method: 'POST',
    body: formData,
  })
}

export const adminClearContests = () =>
  authorizedRequest('/api/admin/contests', { method: 'DELETE' })

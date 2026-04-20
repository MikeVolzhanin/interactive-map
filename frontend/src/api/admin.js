import { authorizedRequest, authorizedFetch } from './http.js'

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

  if (!res.ok) throw new Error(`Ошибка ${res.status}`)

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

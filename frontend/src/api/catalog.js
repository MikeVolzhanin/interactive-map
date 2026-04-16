/**
 * Справочные данные — регионы, уровни образования, интересы.
 * Все запросы требуют авторизации.
 */

function authHeaders() {
  return { Authorization: `Bearer ${localStorage.getItem('accessToken') ?? ''}` }
}

async function getJson(url) {
  const res = await fetch(url, { headers: authHeaders() })
  if (res.ok) return res.json()
  throw new Error(`Ошибка загрузки ${url}: ${res.status}`)
}

export const fetchRegions = () => getJson('/api/regions')
export const fetchEducationLevels = () => getJson('/api/education-levels')
export const fetchInterests = () => getJson('/api/interests')

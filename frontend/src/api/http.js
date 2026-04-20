let refreshPromise = null // предотвращает параллельные попытки рефреша

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return null
  }
}

function isExpired(token) {
  const payload = parseJwt(token)
  if (!payload?.exp) return true
  return Date.now() / 1000 >= payload.exp
}

export async function tryRefresh() {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) throw new Error('no_refresh_token')

  const res = await fetch('/api/auth/refresh-token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })

  if (!res.ok) throw new Error('refresh_failed')

  const data = await res.json()
  localStorage.setItem('accessToken', data.accessToken)
  localStorage.setItem('refreshToken', data.refreshToken)
  return data.accessToken
}

function logout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  window.location.href = '/login'
}

async function getValidToken() {
  const token = localStorage.getItem('accessToken') ?? ''
  if (!isExpired(token)) return token

  // Токен истёк — рефрешим проактивно, не дожидаясь 401 от бэкенда
  if (!refreshPromise) {
    refreshPromise = tryRefresh().finally(() => { refreshPromise = null })
  }
  return refreshPromise
}

export async function authorizedFetch(url, options = {}) {
  const makeRequest = (token) =>
    fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        ...options.headers,
      },
    })

  let token
  try {
    token = await getValidToken()
  } catch {
    logout()
    throw new Error('Сессия истекла. Войдите снова.')
  }

  let res = await makeRequest(token)

  if (res.status !== 401) return res

  // 401 после свежего токена — рефрешим ещё раз как страховка
  if (!refreshPromise) {
    refreshPromise = tryRefresh().finally(() => { refreshPromise = null })
  }

  let newToken
  try {
    newToken = await refreshPromise
  } catch {
    logout()
    throw new Error('Сессия истекла. Войдите снова.')
  }

  res = await makeRequest(newToken)
  if (res.status === 401) {
    logout()
    throw new Error('Сессия истекла. Войдите снова.')
  }

  return res
}

/**
 * authorizedFetch + обработка ошибок и парсинг JSON/text.
 * Аналог request() из auth.js, но с авторизацией и рефрешем.
 */
export async function authorizedRequest(url, options = {}) {
  const res = await authorizedFetch(url, options)

  if (res.ok) {
    const ct = res.headers.get('content-type') ?? ''
    if (ct.includes('application/json')) return res.json()
    return res.text()
  }

  let message = `Ошибка ${res.status}`
  try {
    const ct = res.headers.get('content-type') ?? ''
    if (ct.includes('application/json')) {
      const body = await res.json()
      message = body.message ?? body.error ?? message
    } else {
      const text = await res.text()
      if (text) message = text
    }
  } catch (_) { /* игнорируем */ }

  throw new Error(message)
}

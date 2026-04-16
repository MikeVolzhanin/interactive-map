async function apiRequest(url, options = {}) {
  const res = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${localStorage.getItem('accessToken') ?? ''}`,
      ...options.headers,
    },
    ...options,
  })

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
  } catch (_) { /* ignore */ }

  throw new Error(message)
}

/** GET /api/users/get-info */
export function getUserInfo() {
  return apiRequest('/api/users/get-info')
}

/**
 * POST /api/users/add-info
 * Сохраняет анкету пользователя.
 */
export function addUserInfo(data) {
  return apiRequest('/api/users/add-info', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

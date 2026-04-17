const BASE = '/api/auth'

/**
 * Общая обёртка над fetch.
 * При не-2xx ответе пытается прочитать тело и бросает Error с понятным сообщением.
 */
async function request(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })

  if (res.ok) {
    // 204 No Content или текстовые ответы — возвращаем как есть
    const contentType = res.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      return res.json()
    }
    return res.text()
  }

  // Пытаемся достать сообщение об ошибке из тела
  let message = `Ошибка ${res.status}`
  try {
    const contentType = res.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const body = await res.json()
      message = body.message ?? body.error ?? message
    } else {
      const text = await res.text()
      if (text) message = text
    }
  } catch (_) { /* игнорируем ошибку парсинга */ }

  throw new Error(message)
}

/** POST /api/auth/signup */
export function signup(email, password) {
  return request(`${BASE}/signup`, {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

/** POST /api/auth/verify */
export function verify(email, verificationCode) {
  return request(`${BASE}/verify`, {
    method: 'POST',
    body: JSON.stringify({ email, verificationCode }),
  })
}

/** POST /api/auth/login — возвращает { accessToken, refreshToken } */
export function login(email, password) {
  return request(`${BASE}/login`, {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

/** POST /api/auth/resend?email=... */
export function resendCode(email) {
  return request(`${BASE}/resend?email=${encodeURIComponent(email)}`, {
    method: 'POST',
  })
}

/** POST /api/auth/forgot-password?email=... */
export function forgotPassword(email) {
  return request(`${BASE}/forgot-password?email=${encodeURIComponent(email)}`, {
    method: 'POST',
  })
}

/** POST /api/auth/reset-password */
export function resetPassword(email, password) {
  return request(`${BASE}/reset-password`, {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

/** POST /api/auth/logout — инвалидирует accessToken на сервере */
export async function logout() {
  const accessToken = localStorage.getItem('accessToken')
  try {
    await fetch(`${BASE}/logout`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${accessToken ?? ''}` },
    })
  } finally {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }
}

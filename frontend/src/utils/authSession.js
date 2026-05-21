function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return null
  }
}

/** Есть ли неистёкший access-токен (личный кабинет в смысле авторизации). */
export function hasActiveSession() {
  const token = localStorage.getItem('accessToken') ?? ''
  if (!token) return false
  const payload = parseJwt(token)
  if (!payload?.exp) return false
  return Date.now() / 1000 < payload.exp
}

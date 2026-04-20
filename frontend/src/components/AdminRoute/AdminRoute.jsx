import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { tryRefresh } from '../../api/http.js'

function parseJwt(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return null
  }
}

function hasAdminRole(payload) {
  if (!payload) return false
  const roles = payload.roles ?? payload.authorities ?? payload.role ?? []
  if (Array.isArray(roles)) return roles.includes('ROLE_ADMIN')
  if (typeof roles === 'string') return roles === 'ROLE_ADMIN'
  return false
}

function isExpired(payload) {
  if (!payload?.exp) return true
  return Date.now() / 1000 >= payload.exp
}

// 'checking' → 'allowed' | 'denied'
export default function AdminRoute({ children }) {
  const [status, setStatus] = useState('checking')

  useEffect(() => {
    let cancelled = false

    async function check() {
      const token = localStorage.getItem('accessToken')
      if (!token) { setStatus('denied'); return }

      const payload = parseJwt(token)

      if (!isExpired(payload)) {
        setStatus(hasAdminRole(payload) ? 'allowed' : 'denied')
        return
      }

      // Токен истёк — пробуем обновить
      try {
        const newToken = await tryRefresh()
        if (cancelled) return
        const newPayload = parseJwt(newToken)
        setStatus(hasAdminRole(newPayload) ? 'allowed' : 'denied')
      } catch {
        if (!cancelled) setStatus('denied')
      }
    }

    check()
    return () => { cancelled = true }
  }, [])

  if (status === 'checking') return null
  if (status === 'denied') return <Navigate to="/login" replace />
  return children
}

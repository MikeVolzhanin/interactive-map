export async function fetchRegionStats() {
  return publicRequest('/api/map/regions')
}

export async function fetchNews() {
  return publicRequest('/api/news')
}

export async function fetchInterestStats(regionId) {
  const query = regionId ? `?regionId=${encodeURIComponent(regionId)}` : ''
  return publicRequest(`/api/map/interests${query}`)
}

async function publicRequest(url) {
  const res = await fetch(url)

  if (res.ok) {
    const contentType = res.headers.get('content-type') ?? ''
    return contentType.includes('application/json') ? res.json() : []
  }

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
  } catch (_) {
    // Ignore response parsing errors and keep the status-based message.
  }

  throw new Error(message)
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export async function apiRequest(
  path,
  { authenticated = false, responseType = 'json', ...options } = {},
) {
  const headers = new Headers(options.headers)

  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (authenticated) {
    const accessToken = localStorage.getItem('amtpilot_access_token')

    if (!accessToken) {
      window.location.assign('/login')
      throw new Error('Authentication is required')
    }

    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  const contentType = response.headers.get('content-type')
  let body = null

  if (contentType?.includes('application/json')) {
    body = await response.json()
  } else if (responseType === 'blob' && response.ok) {
    body = await response.blob()
  }

  if (response.status === 401 && authenticated) {
    localStorage.removeItem('amtpilot_access_token')
    window.location.assign('/login')

    throw new Error('Your session has expired. Please log in again.')
  }

  if (!response.ok) {
    const error = new Error(body?.error?.message || 'Something went wrong')

    error.code = body?.error?.code
    error.fieldErrors = body?.error?.fieldErrors || {}

    throw error
  }

  return responseType === 'blob' ? body : body?.data
}

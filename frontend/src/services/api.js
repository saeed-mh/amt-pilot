const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  const body = await response.json()

  if (!response.ok) {
    const error = new Error(body.error?.message || 'Something went wrong')

    error.code = body.error?.code
    error.fieldErrors = body.error?.fieldErrors || {}

    throw error
  }

  return body.data
}

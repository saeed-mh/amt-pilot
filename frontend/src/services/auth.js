import { apiRequest } from './api'

export function registerUser(email, password) {
  return apiRequest('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify({
      email,
      password,
    }),
  })
}

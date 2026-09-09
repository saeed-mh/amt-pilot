import { apiRequest } from './api'

export function getCurrentUser() {
  return apiRequest('/api/v1/users/me', {
    authenticated: true,
  })
}

import { apiRequest } from './api'

export function getCurrentUser() {
  return apiRequest('/api/v1/users/me', {
    authenticated: true,
  })
}

export function updateCurrentUser(profile) {
  return apiRequest('/api/v1/users/me', {
    authenticated: true,
    method: 'PATCH',
    body: JSON.stringify(profile),
  })
}

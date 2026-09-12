import { apiRequest } from './api'

export function createApplication(processId) {
  return apiRequest('/api/v1/applications', {
    authenticated: true,
    method: 'POST',
    body: JSON.stringify({ processId }),
  })
}

export function getApplications() {
  return apiRequest('/api/v1/applications', {
    authenticated: true,
  })
}

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

export function getApplicationChecklist(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}/checklist`, {
    authenticated: true,
  })
}

export function updateChecklistItem(checklistItemId, completed) {
  return apiRequest(`/api/v1/applications/checklist/${checklistItemId}`, {
    authenticated: true,
    method: 'PATCH',
    body: JSON.stringify({ completed }),
  })
}

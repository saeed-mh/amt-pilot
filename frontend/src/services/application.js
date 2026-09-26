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

export function getApplication(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}`, {
    authenticated: true,
  })
}

export function deleteApplication(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}`, {
    authenticated: true,
    method: 'DELETE',
  })
}

export function analyzeApplication(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}/analyze`, {
    authenticated: true,
    method: 'POST',
  })
}

export function getApplicationAnalyses(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}/analyses`, {
    authenticated: true,
  })
}

export function getApplicationAdvice(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}/advice`, {
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

export function getApplicationDocuments(applicationId) {
  return apiRequest(`/api/v1/applications/${applicationId}/documents`, {
    authenticated: true,
  })
}

export function uploadApplicationDocument(applicationId, file, checklistItemId) {
  const formData = new FormData()
  formData.append('file', file)

  const checklistQuery = checklistItemId
    ? `?checklistItemId=${encodeURIComponent(checklistItemId)}`
    : ''

  return apiRequest(`/api/v1/applications/${applicationId}/documents${checklistQuery}`, {
    authenticated: true,
    method: 'POST',
    body: formData,
  })
}

export function deleteApplicationDocument(applicationId, documentId) {
  return apiRequest(`/api/v1/applications/${applicationId}/documents/${documentId}`, {
    authenticated: true,
    method: 'DELETE',
  })
}

export function downloadApplicationDocument(applicationId, documentId) {
  return apiRequest(`/api/v1/applications/${applicationId}/documents/${documentId}`, {
    authenticated: true,
    responseType: 'blob',
  })
}

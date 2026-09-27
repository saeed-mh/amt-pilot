import { apiRequest } from './api'

export function getProcesses(city = 'Dortmund') {
  const query = new URLSearchParams({
    city,
  })

  return apiRequest('/api/v1/processes?' + query, {
    authenticated: true,
  })
}

export function getProcessRequirements(processId) {
  return apiRequest('/api/v1/processes/' + processId + '/requirements', {
    authenticated: true,
  })
}

export function getProcessGuide(processId, language = 'en') {
  const query = new URLSearchParams({
    language,
  })

  return apiRequest('/api/v1/processes/' + processId + '/guide?' + query, {
    authenticated: true,
  })
}

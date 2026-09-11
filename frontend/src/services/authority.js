import { apiRequest } from './api'

export function getAuthorities(city = 'Dortmund') {
  const query = new URLSearchParams({
    city,
  })

  return apiRequest(`/api/v1/authorities?${query}`, {
    authenticated: true,
  })
}

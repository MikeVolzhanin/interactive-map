import { authorizedRequest } from './http'

export function getUserInfo() {
  return authorizedRequest('/api/users/get-info')
}

export function addUserInfo(data) {
  return authorizedRequest('/api/users/add-info', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

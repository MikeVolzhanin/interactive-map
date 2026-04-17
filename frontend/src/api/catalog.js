import { authorizedRequest } from './http'

export const fetchRegions = () => authorizedRequest('/api/regions')
export const fetchEducationLevels = () => authorizedRequest('/api/education-levels')
export const fetchInterests = () => authorizedRequest('/api/interests')

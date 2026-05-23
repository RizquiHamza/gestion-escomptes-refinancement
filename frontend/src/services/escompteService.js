import api from './api'

export const getAll        = (params)        => api.get('/escomptes', { params })
export const getById       = (id)            => api.get(`/escomptes/${id}`)
export const create        = (data)          => api.post('/escomptes', data)
export const update        = (id, data)      => api.put(`/escomptes/${id}`, data)
export const changerStatut = (id, statut)    => api.patch(`/escomptes/${id}/statut`, null, { params: { statut } })
export const remove        = (id)            => api.delete(`/escomptes/${id}`)

import api from './api'

export const getAll        = (params)     => api.get('/refinancements', { params })
export const getById       = (id)         => api.get(`/refinancements/${id}`)
export const create        = (data)       => api.post('/refinancements', data)
export const update        = (id, data)   => api.put(`/refinancements/${id}`, data)
export const changerStatut = (id, statut) => api.patch(`/refinancements/${id}/statut`, null, { params: { statut } })
export const remove        = (id)         => api.delete(`/refinancements/${id}`)

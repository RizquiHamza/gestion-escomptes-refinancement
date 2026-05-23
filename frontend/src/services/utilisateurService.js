import api from './api'

export const getAll       = (params) => api.get('/utilisateurs', { params })
export const getById      = (id)     => api.get(`/utilisateurs/${id}`)
export const create       = (data)   => api.post('/utilisateurs', data)
export const update       = (id, data) => api.put(`/utilisateurs/${id}`, data)
export const toggleActif  = (id)     => api.patch(`/utilisateurs/${id}/toggle-actif`)
export const remove       = (id)     => api.delete(`/utilisateurs/${id}`)

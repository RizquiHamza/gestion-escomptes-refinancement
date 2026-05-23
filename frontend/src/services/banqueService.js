import api from './api'

export const getAll   = (params)   => api.get('/banques', { params })
export const getById  = (id)       => api.get(`/banques/${id}`)
export const create   = (data)     => api.post('/banques', data)
export const update   = (id, data) => api.put(`/banques/${id}`, data)
export const remove   = (id)       => api.delete(`/banques/${id}`)

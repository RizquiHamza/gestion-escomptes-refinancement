import api from './api'

export const getAll    = (params)    => api.get('/partenaires', { params })
export const getById   = (id)        => api.get(`/partenaires/${id}`)
export const create    = (data)      => api.post('/partenaires', data)
export const update    = (id, data)  => api.put(`/partenaires/${id}`, data)
export const remove    = (id)        => api.delete(`/partenaires/${id}`)

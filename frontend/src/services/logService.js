import api from './api'

export const getRecents        = ()       => api.get('/logs')
export const getByUtilisateur  = (id)     => api.get(`/logs/utilisateur/${id}`)
export const getByEntite       = (entite) => api.get(`/logs/entite/${entite}`)
export const remove            = (id)     => api.delete(`/logs/${id}`)
export const removeAll         = ()       => api.delete('/logs')

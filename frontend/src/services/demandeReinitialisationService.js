import api from './api'

export const getAll      = ()   => api.get('/demandes-reinitialisation')
export const getEnAttente = ()  => api.get('/demandes-reinitialisation/en-attente')
export const approuver   = (id) => api.post(`/demandes-reinitialisation/${id}/approuver`)
export const refuser     = (id) => api.post(`/demandes-reinitialisation/${id}/refuser`)

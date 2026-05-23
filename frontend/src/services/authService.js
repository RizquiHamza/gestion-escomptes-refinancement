import api from './api'

export const login    = (email, motDePasse) => api.post('/auth/login',    { email, motDePasse })
export const register = (data)              => api.post('/auth/register',  data)

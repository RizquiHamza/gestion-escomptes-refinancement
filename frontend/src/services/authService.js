import api from './api'

export const login    = (email, motDePasse) => api.post('/auth/login',    { email, motDePasse })
export const register = (data)              => api.post('/auth/register',  data)

export const demanderReinitialisation = (email, message) =>
  api.post('/auth/demande-reinitialisation', { email, message })

export const changerMotDePasse = (nouveauMotDePasse) =>
  api.post('/auth/changer-mot-de-passe', { nouveauMotDePasse })

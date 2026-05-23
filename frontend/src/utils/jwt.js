import { TOKEN_KEY, USER_KEY } from './constants'

export const saveToken = (token) => localStorage.setItem(TOKEN_KEY, token)
export const getToken  = ()      => localStorage.getItem(TOKEN_KEY)
export const removeToken = ()    => localStorage.removeItem(TOKEN_KEY)

export const saveUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user))
export const getUser  = ()     => {
  try { return JSON.parse(localStorage.getItem(USER_KEY)) }
  catch { return null }
}
export const removeUser = () => localStorage.removeItem(USER_KEY)

export const clearAuth = () => { removeToken(); removeUser() }

export const isTokenExpired = (token) => {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

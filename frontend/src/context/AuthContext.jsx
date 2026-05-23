import { createContext, useContext, useState, useCallback } from 'react'
import { saveToken, saveUser, clearAuth, getToken, getUser, isTokenExpired } from '../utils/jwt'
import * as authService from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]         = useState(() => getUser())
  const [token, setToken]       = useState(() => {
    const t = getToken()
    return t && !isTokenExpired(t) ? t : null
  })
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)

  const login = useCallback(async (email, motDePasse) => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await authService.login(email, motDePasse)
      saveToken(data.token)
      const userData = {
        id:      data.utilisateurId,
        email:   data.email,
        nom:     data.nom,
        prenom:  data.prenom,
        role:    data.role,
      }
      saveUser(userData)
      setToken(data.token)
      setUser(userData)
      return true
    } catch (err) {
      const msg = err.response?.data?.message || 'Email ou mot de passe incorrect'
      setError(msg)
      return false
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    clearAuth()
    setToken(null)
    setUser(null)
  }, [])

  const isAuthenticated = !!token && !isTokenExpired(token)

  const hasRole = useCallback((...roles) => {
    return roles.includes(user?.role)
  }, [user])

  return (
    <AuthContext.Provider value={{ user, token, loading, error, isAuthenticated, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth doit être utilisé dans AuthProvider')
  return ctx
}

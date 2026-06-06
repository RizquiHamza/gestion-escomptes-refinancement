import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function PrivateRoute({ roles, allowForceChange = false }) {
  const { isAuthenticated, hasRole, user } = useAuth()

  if (!isAuthenticated) return <Navigate to="/login" replace />

  // Si l'utilisateur doit changer son mot de passe et que la route n'est pas exemptée
  if (user?.doitChangerMotDePasse && !allowForceChange) {
    return <Navigate to="/changer-mot-de-passe" replace />
  }

  if (roles && !hasRole(...roles)) return <Navigate to="/dashboard" replace />

  return <Outlet />
}

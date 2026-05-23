import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function PrivateRoute({ roles }) {
  const { isAuthenticated, hasRole } = useAuth()

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (roles && !hasRole(...roles)) return <Navigate to="/dashboard" replace />

  return <Outlet />
}

import { FiLogOut, FiUser } from 'react-icons/fi'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

export default function Navbar({ title }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6">
      <h1 className="text-lg font-semibold text-gray-800">{title}</h1>
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <FiUser className="h-4 w-4" />
          <span>{user?.prenom} {user?.nom}</span>
        </div>
        <button onClick={handleLogout}
          className="flex items-center gap-2 text-sm text-red-600 hover:text-red-700 font-medium px-3 py-1.5 rounded-lg hover:bg-red-50 transition-colors">
          <FiLogOut className="h-4 w-4" />
          Déconnexion
        </button>
      </div>
    </header>
  )
}

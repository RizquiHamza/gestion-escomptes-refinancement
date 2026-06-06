import { FiLogOut, FiChevronRight } from 'react-icons/fi'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

const ROLE_LABELS = {
  ADMIN:           'Administrateur',
  RESPONSABLE:     'Responsable',
  AGENT_FINANCIER: 'Agent Financier',
}

export default function Navbar({ title }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const initials = user
    ? `${(user.prenom?.[0] ?? '').toUpperCase()}${(user.nom?.[0] ?? '').toUpperCase()}`
    : '?'

  return (
    <header className="h-16 bg-white border-b border-gray-100 flex items-center justify-between px-6 flex-shrink-0">
      {/* Titre avec fil d'Ariane */}
      <div className="flex items-center gap-2 text-sm">
        <span className="text-gray-400 font-medium">UNIMAGEC</span>
        <FiChevronRight className="h-3.5 w-3.5 text-gray-300" />
        <span className="text-gray-800 font-semibold">{title}</span>
      </div>

      {/* Zone droite */}
      <div className="flex items-center gap-3">
        {/* Info utilisateur */}
        <div className="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-xl bg-gray-50 border border-gray-100">
          <div className="h-7 w-7 rounded-lg bg-emerald-600 flex items-center justify-center text-white text-[10px] font-bold flex-shrink-0">
            {initials}
          </div>
          <div className="leading-tight">
            <p className="text-xs font-semibold text-gray-700">{user?.prenom} {user?.nom}</p>
            <p className="text-[10px] text-gray-400">{ROLE_LABELS[user?.role] ?? user?.role}</p>
          </div>
        </div>

        {/* Séparateur */}
        <div className="h-6 w-px bg-gray-100 hidden sm:block" />

        {/* Bouton déconnexion */}
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-sm font-medium px-3 py-1.5 rounded-xl
                     text-gray-500 hover:text-red-600 hover:bg-red-50
                     border border-transparent hover:border-red-100
                     transition-all duration-200"
        >
          <FiLogOut className="h-4 w-4" />
          <span className="hidden sm:inline">Déconnexion</span>
        </button>
      </div>
    </header>
  )
}

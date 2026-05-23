import { NavLink } from 'react-router-dom'
import {
  FiGrid, FiUsers, FiBriefcase, FiCreditCard,
  FiFileText, FiRepeat, FiActivity, FiBarChart2,
} from 'react-icons/fi'
import { useAuth } from '../context/AuthContext'

const NAV = [
  { to: '/dashboard',       label: 'Dashboard',        icon: FiGrid,      roles: null },
  { to: '/escomptes',       label: 'Escomptes',         icon: FiFileText,  roles: null },
  { to: '/refinancements',  label: 'Refinancements',    icon: FiRepeat,    roles: null },
  { to: '/partenaires',     label: 'Partenaires',       icon: FiBriefcase, roles: null },
  { to: '/banques',         label: 'Banques',           icon: FiCreditCard,roles: null },
  { to: '/utilisateurs',    label: 'Utilisateurs',      icon: FiUsers,     roles: ['ADMIN'] },
  { to: '/logs',            label: 'Logs',              icon: FiActivity,  roles: ['ADMIN', 'RESPONSABLE'] },
]

export default function Sidebar() {
  const { hasRole, user } = useAuth()

  return (
    <aside className="w-64 min-h-screen bg-gray-900 text-white flex flex-col">
      <div className="flex items-center gap-3 px-6 py-5 border-b border-gray-700">
        <div className="h-8 w-8 rounded-lg bg-blue-600 flex items-center justify-center">
          <FiBarChart2 className="h-5 w-5 text-white" />
        </div>
        <div>
          <p className="font-semibold text-sm leading-tight">Gestion Financement</p>
          <p className="text-xs text-gray-400">{user?.role}</p>
        </div>
      </div>

      <nav className="flex-1 py-4 px-3 space-y-1">
        {NAV.map(({ to, label, icon: Icon, roles }) => {
          if (roles && !hasRole(...roles)) return null
          return (
            <NavLink key={to} to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                }`
              }>
              <Icon className="h-4 w-4 shrink-0" />
              {label}
            </NavLink>
          )
        })}
      </nav>

      <div className="px-4 pb-4 text-xs text-gray-500 text-center">
        Gestion Financement v1.0
      </div>
    </aside>
  )
}

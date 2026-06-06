import { NavLink } from 'react-router-dom'
import {
  FiGrid, FiUsers, FiBriefcase, FiCreditCard,
  FiFileText, FiRepeat, FiActivity, FiBarChart2,
} from 'react-icons/fi'
import { useAuth } from '../context/AuthContext'

const NAV = [
  { to: '/dashboard',      label: 'Tableau de bord',        icon: FiGrid,       roles: null },
  { to: '/escomptes',      label: 'Escomptes',               icon: FiFileText,   roles: null },
  { to: '/refinancements', label: 'Refinancements',          icon: FiRepeat,     roles: null },
  { to: '/partenaires',    label: 'Partenaires',             icon: FiBriefcase,  roles: null },
  { to: '/banques',        label: 'Banques',                 icon: FiCreditCard, roles: null },
  { to: '/utilisateurs',   label: 'Utilisateurs',            icon: FiUsers,      roles: ['ADMIN'] },
  { to: '/logs',           label: 'Journaux',                icon: FiActivity,   roles: ['ADMIN'] },
  { to: '/statistiques',   label: 'Statistiques & Rapports', icon: FiBarChart2,  roles: ['ADMIN', 'RESPONSABLE'] },
]

const ROLE_LABELS = {
  ADMIN:           'Administrateur',
  RESPONSABLE:     'Responsable',
  AGENT_FINANCIER: 'Agent Financier',
}

const ROLE_COLORS = {
  ADMIN:           'bg-emerald-500/20 text-emerald-300 ring-1 ring-emerald-500/30',
  RESPONSABLE:     'bg-sky-500/20     text-sky-300     ring-1 ring-sky-500/30',
  AGENT_FINANCIER: 'bg-amber-500/20   text-amber-300   ring-1 ring-amber-500/30',
}

export default function Sidebar() {
  const { hasRole, user } = useAuth()
  const initials = user
    ? `${(user.prenom?.[0] ?? '').toUpperCase()}${(user.nom?.[0] ?? '').toUpperCase()}`
    : '?'

  return (
    <aside
      className="w-64 min-h-screen flex flex-col"
      style={{ background: 'linear-gradient(180deg, #071a0e 0%, #0c2716 50%, #071a0e 100%)' }}
    >
      {/* ── En-tête logo ── */}
      <div className="px-5 py-6 border-b border-white/5">
        <div className="flex items-center gap-3">
          <div className="h-9 w-9 rounded-xl flex items-center justify-center flex-shrink-0 overflow-hidden bg-white/10">
            <img
              src="/unimagec-logo.png"
              alt="U"
              className="h-7 w-7 object-contain"
              onError={e => {
                e.target.style.display = 'none'
                e.target.nextSibling.style.display = 'flex'
              }}
            />
            <span
              style={{ display: 'none' }}
              className="text-white font-black text-sm w-full h-full items-center justify-center"
            >U</span>
          </div>
          <div className="min-w-0">
            <p className="text-white font-bold text-sm tracking-wide leading-tight">UNIMAGEC</p>
            <p className="text-white/35 text-[10px] tracking-widest uppercase truncate">
              Gestion financière
            </p>
          </div>
        </div>
      </div>

      {/* ── Navigation ── */}
      <nav className="flex-1 px-3 py-5 space-y-0.5">
        <p className="text-white/25 text-[10px] font-semibold tracking-[0.15em] uppercase px-3 mb-3">
          Menu
        </p>
        {NAV.map(({ to, label, icon: Icon, roles }) => {
          if (roles && !hasRole(...roles)) return null
          return (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `group flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium
                 transition-all duration-200 relative
                 ${isActive
                   ? 'bg-emerald-500/15 text-emerald-300'
                   : 'text-white/50 hover:text-white/90 hover:bg-white/5'
                 }`
              }
            >
              {({ isActive }) => (
                <>
                  {/* Indicateur gauche */}
                  {isActive && (
                    <span className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-5 bg-emerald-400 rounded-r-full" />
                  )}
                  <span className={`p-1.5 rounded-lg transition-colors duration-200 flex-shrink-0
                    ${isActive
                      ? 'bg-emerald-500/20 text-emerald-400'
                      : 'text-white/40 group-hover:text-white/70 group-hover:bg-white/5'
                    }`}>
                    <Icon className="h-3.5 w-3.5" />
                  </span>
                  {label}
                </>
              )}
            </NavLink>
          )
        })}
      </nav>

      {/* ── Profil utilisateur ── */}
      <div className="px-3 pb-5 border-t border-white/5 pt-4">
        <div className="flex items-center gap-3 px-3 py-3 rounded-xl bg-white/4">
          {/* Avatar initiales */}
          <div className="h-8 w-8 rounded-lg flex items-center justify-center flex-shrink-0
                          bg-emerald-600 text-white text-xs font-bold">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-white/80 text-xs font-semibold truncate leading-tight">
              {user?.prenom} {user?.nom}
            </p>
            <span className={`inline-block text-[9px] font-semibold px-1.5 py-0.5 rounded-md mt-0.5
              ${ROLE_COLORS[user?.role] ?? 'bg-white/10 text-white/50'}`}>
              {ROLE_LABELS[user?.role] ?? user?.role}
            </span>
          </div>
        </div>

        <p className="text-white/15 text-[10px] text-center mt-4 tracking-wide">
          UNIMAGEC · v1.0
        </p>
      </div>
    </aside>
  )
}

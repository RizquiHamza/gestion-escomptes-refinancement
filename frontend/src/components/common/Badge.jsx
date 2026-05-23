import { STATUT_LABELS } from '../../utils/constants'

const STATUT_CLASS = {
  EN_ATTENTE: 'badge-warning',
  APPROUVE:   'badge-success',
  REJETE:     'badge-danger',
  ANNULE:     'badge-gray',
}

export function StatutBadge({ statut }) {
  return (
    <span className={STATUT_CLASS[statut] || 'badge-gray'}>
      {STATUT_LABELS[statut] || statut}
    </span>
  )
}

export function RoleBadge({ role }) {
  const cls = role === 'ADMIN' ? 'badge-danger' : role === 'RESPONSABLE' ? 'badge-info' : 'badge-gray'
  const labels = { ADMIN: 'Admin', RESPONSABLE: 'Responsable', AGENT_FINANCIER: 'Agent Financier' }
  return <span className={cls}>{labels[role] || role}</span>
}

export function ActiveBadge({ actif }) {
  return actif
    ? <span className="badge-success">Actif</span>
    : <span className="badge-danger">Inactif</span>
}

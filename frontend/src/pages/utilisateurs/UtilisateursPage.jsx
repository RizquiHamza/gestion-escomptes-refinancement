import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import {
  FiPlus, FiEdit2, FiTrash2, FiToggleLeft, FiToggleRight,
  FiKey, FiCheckCircle, FiXCircle, FiClock, FiRefreshCw, FiCopy,
} from 'react-icons/fi'
import toast from 'react-hot-toast'
import * as utilisateurService from '../../services/utilisateurService'
import * as demandeService from '../../services/demandeReinitialisationService'
import { usePagination } from '../../hooks/usePagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Pagination from '../../components/common/Pagination'
import Modal from '../../components/common/Modal'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { RoleBadge, ActiveBadge } from '../../components/common/Badge'
import { ROLES } from '../../utils/constants'

// ── Formulaire utilisateur ────────────────────────────────────────────────────
function UserForm({ onSubmit, defaultValues, loading, isEdit }) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm({ defaultValues })
  useEffect(() => { reset(defaultValues) }, [defaultValues, reset])

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
          <input className={`input-field ${errors.nom ? 'border-red-400' : ''}`}
            {...register('nom', { required: 'Nom requis' })} />
          {errors.nom && <p className="text-xs text-red-600 mt-1">{errors.nom.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Prénom *</label>
          <input className={`input-field ${errors.prenom ? 'border-red-400' : ''}`}
            {...register('prenom', { required: 'Prénom requis' })} />
          {errors.prenom && <p className="text-xs text-red-600 mt-1">{errors.prenom.message}</p>}
        </div>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
        <input type="email" className={`input-field ${errors.email ? 'border-red-400' : ''}`}
          {...register('email', { required: 'Email requis', pattern: { value: /\S+@\S+\.\S+/, message: 'Email invalide' } })} />
        {errors.email && <p className="text-xs text-red-600 mt-1">{errors.email.message}</p>}
      </div>
      {!isEdit && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Mot de passe *</label>
          <input type="password" className={`input-field ${errors.motDePasse ? 'border-red-400' : ''}`}
            {...register('motDePasse', { required: !isEdit && 'Mot de passe requis', minLength: { value: 6, message: 'Minimum 6 caractères' } })} />
          {errors.motDePasse && <p className="text-xs text-red-600 mt-1">{errors.motDePasse.message}</p>}
        </div>
      )}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Rôle *</label>
        <select className={`input-field ${errors.role ? 'border-red-400' : ''}`}
          {...register('role', { required: 'Rôle requis' })}>
          <option value="">— Sélectionner —</option>
          {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
        </select>
        {errors.role && <p className="text-xs text-red-600 mt-1">{errors.role.message}</p>}
      </div>
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={loading} className="btn-primary text-sm">
          {loading ? 'Enregistrement…' : 'Enregistrer'}
        </button>
      </div>
    </form>
  )
}

// ── Badge statut demande ──────────────────────────────────────────────────────
function StatutDemandeBadge({ statut }) {
  const cfg = {
    EN_ATTENTE: { label: 'En attente', icon: FiClock,        cls: 'bg-amber-100 text-amber-700'  },
    APPROUVEE:  { label: 'Approuvée',  icon: FiCheckCircle,  cls: 'bg-emerald-100 text-emerald-700' },
    REFUSEE:    { label: 'Refusée',    icon: FiXCircle,      cls: 'bg-red-100 text-red-700'       },
  }[statut] ?? { label: statut, icon: FiClock, cls: 'bg-gray-100 text-gray-600' }
  const Icon = cfg.icon
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold ${cfg.cls}`}>
      <Icon className="h-3 w-3" />
      {cfg.label}
    </span>
  )
}

// ── Formatage date ────────────────────────────────────────────────────────────
const fmtDate = (d) => d ? new Date(d).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' }) : '—'

// ── Page principale ───────────────────────────────────────────────────────────
export default function UtilisateursPage() {
  const [users, setUsers]       = useState([])
  const [loading, setLoading]   = useState(true)
  const [saving, setSaving]     = useState(false)
  const [selected, setSelected] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [showDel, setShowDel]   = useState(false)
  const [toDelete, setToDelete] = useState(null)
  const [filterRole, setFilterRole] = useState('')
  const pag = usePagination(10)

  // ── Demandes de réinitialisation ──────────────────────────────────────────
  const [demandes, setDemandes]           = useState([])
  const [loadingDemandes, setLoadingDem]  = useState(true)
  const [tempPassword, setTempPassword]   = useState(null)   // modal mot de passe temporaire
  const [showTempPwd, setShowTempPwd]     = useState(false)
  const [onglet, setOnglet]               = useState('utilisateurs') // 'utilisateurs' | 'demandes'

  // ── Chargement utilisateurs ───────────────────────────────────────────────
  const load = useCallback(() => {
    setLoading(true)
    const params = { page: pag.page, size: pag.size, sort: 'nom,asc' }
    if (filterRole) params.role = filterRole
    utilisateurService.getAll(params)
      .then(r => { setUsers(r.data.content); pag.updateFromPage(r.data) })
      .catch(() => toast.error('Erreur lors du chargement'))
      .finally(() => setLoading(false))
  }, [pag.page, filterRole])

  // ── Chargement demandes ───────────────────────────────────────────────────
  const loadDemandes = useCallback(() => {
    setLoadingDem(true)
    demandeService.getAll()
      .then(r => setDemandes(r.data))
      .catch(() => toast.error('Erreur lors du chargement des demandes'))
      .finally(() => setLoadingDem(false))
  }, [])

  useEffect(() => { pag.reset() }, [filterRole])
  useEffect(() => { load() }, [pag.page, filterRole])
  useEffect(() => { loadDemandes() }, [])

  const nbEnAttente = demandes.filter(d => d.statut === 'EN_ATTENTE').length

  // ── Actions utilisateurs ──────────────────────────────────────────────────
  const handleSave = async (data) => {
    setSaving(true)
    try {
      if (selected) { await utilisateurService.update(selected.id, data); toast.success('Utilisateur modifié') }
      else          { await utilisateurService.create(data);               toast.success('Utilisateur créé') }
      setShowForm(false); load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur lors de l\'enregistrement')
    } finally { setSaving(false) }
  }

  const handleToggle = async (u) => {
    try {
      await utilisateurService.toggleActif(u.id)
      toast.success(u.actif ? 'Compte désactivé' : 'Compte activé')
      load()
    } catch { toast.error('Erreur') }
  }

  const handleDelete = async () => {
    try {
      await utilisateurService.remove(toDelete.id)
      toast.success('Utilisateur supprimé'); load()
    } catch { toast.error('Impossible de supprimer cet utilisateur') }
  }

  // ── Actions demandes ──────────────────────────────────────────────────────
  const handleApprouver = async (id) => {
    try {
      const { data } = await demandeService.approuver(id)
      setTempPassword(data.motDePasseTemporaire)
      setShowTempPwd(true)
      loadDemandes()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur lors de l\'approbation')
    }
  }

  const handleRefuser = async (id) => {
    try {
      await demandeService.refuser(id)
      toast.success('Demande refusée')
      loadDemandes()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur')
    }
  }

  const copierMDP = () => {
    navigator.clipboard.writeText(tempPassword).then(() => toast.success('Mot de passe copié !'))
  }

  return (
    <div className="space-y-4">

      {/* ── Onglets ── */}
      <div className="flex items-center gap-1 border-b border-gray-200">
        {[
          { key: 'utilisateurs', label: 'Utilisateurs' },
          { key: 'demandes',     label: 'Demandes de réinitialisation', badge: nbEnAttente },
        ].map(({ key, label, badge }) => (
          <button key={key} onClick={() => setOnglet(key)}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-colors flex items-center gap-2
              ${onglet === key
                ? 'border-emerald-600 text-emerald-700'
                : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            {label}
            {badge > 0 && (
              <span className="h-5 min-w-5 px-1.5 rounded-full bg-amber-500 text-white text-[10px] font-bold flex items-center justify-center">
                {badge}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* ═══════════════════════════════════════════════════════ */}
      {/* ── ONGLET UTILISATEURS ── */}
      {/* ═══════════════════════════════════════════════════════ */}
      {onglet === 'utilisateurs' && (
        <>
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <select value={filterRole} onChange={e => setFilterRole(e.target.value)}
                className="input-field w-48 text-sm">
                <option value="">Tous les rôles</option>
                {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              <p className="text-sm text-gray-500">{pag.totalElements} utilisateur(s)</p>
            </div>
            <button className="btn-primary text-sm flex items-center gap-2"
              onClick={() => { setSelected(null); setShowForm(true) }}>
              <FiPlus className="h-4 w-4" /> Nouvel utilisateur
            </button>
          </div>

          <div className="card p-0 overflow-hidden">
            {loading ? <LoadingSpinner /> : (
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    {['Nom', 'Prénom', 'Email', 'Rôle', 'Statut', 'Actions'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {users.length === 0 ? (
                    <tr><td colSpan={6} className="text-center py-12 text-gray-400">Aucun utilisateur</td></tr>
                  ) : users.map(u => (
                    <tr key={u.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-4 py-3 font-medium text-gray-900">{u.nom}</td>
                      <td className="px-4 py-3 text-gray-700">{u.prenom}</td>
                      <td className="px-4 py-3 text-gray-500 text-xs">{u.email}</td>
                      <td className="px-4 py-3"><RoleBadge role={u.role} /></td>
                      <td className="px-4 py-3"><ActiveBadge actif={u.actif} /></td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <button onClick={() => { setSelected(u); setShowForm(true) }}
                            className="p-1.5 rounded hover:bg-blue-50 text-blue-600" title="Modifier">
                            <FiEdit2 className="h-4 w-4" />
                          </button>
                          <button onClick={() => handleToggle(u)}
                            className={`p-1.5 rounded transition-colors ${u.actif ? 'hover:bg-yellow-50 text-yellow-600' : 'hover:bg-green-50 text-green-600'}`}
                            title={u.actif ? 'Désactiver' : 'Activer'}>
                            {u.actif ? <FiToggleRight className="h-4 w-4" /> : <FiToggleLeft className="h-4 w-4" />}
                          </button>
                          <button onClick={() => { setToDelete(u); setShowDel(true) }}
                            className="p-1.5 rounded hover:bg-red-50 text-red-600" title="Supprimer">
                            <FiTrash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            <Pagination page={pag.page} totalPages={pag.totalPages} totalElements={pag.totalElements}
              size={pag.size} onPage={pag.goTo} />
          </div>
        </>
      )}

      {/* ═══════════════════════════════════════════════════════ */}
      {/* ── ONGLET DEMANDES ── */}
      {/* ═══════════════════════════════════════════════════════ */}
      {onglet === 'demandes' && (
        <>
          <div className="flex items-center justify-between">
            <p className="text-sm text-gray-500">{demandes.length} demande(s) au total</p>
            <button onClick={loadDemandes} className="btn-secondary text-sm flex items-center gap-2">
              <FiRefreshCw className="h-4 w-4" /> Actualiser
            </button>
          </div>

          <div className="card p-0 overflow-hidden">
            {loadingDemandes ? <LoadingSpinner /> : demandes.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-gray-400">
                <FiKey className="h-10 w-10 mb-3 opacity-30" />
                <p className="font-medium">Aucune demande de réinitialisation</p>
                <p className="text-sm mt-1">Les demandes soumises par les utilisateurs apparaîtront ici.</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    {['Utilisateur', 'Rôle', 'Message', 'Date demande', 'Statut', 'Actions'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {demandes.map(d => (
                    <tr key={d.id} className="hover:bg-gray-50 transition-colors">

                      {/* Utilisateur */}
                      <td className="px-4 py-3">
                        <p className="font-medium text-gray-900">{d.utilisateurPrenom} {d.utilisateurNom}</p>
                        <p className="text-xs text-gray-400">{d.utilisateurEmail}</p>
                      </td>

                      {/* Rôle */}
                      <td className="px-4 py-3">
                        <RoleBadge role={d.utilisateurRole} />
                      </td>

                      {/* Message */}
                      <td className="px-4 py-3 max-w-xs">
                        <p className="text-xs text-gray-600 leading-relaxed line-clamp-2">
                          {d.message || <span className="text-gray-300 italic">Aucun message</span>}
                        </p>
                      </td>

                      {/* Date */}
                      <td className="px-4 py-3 whitespace-nowrap">
                        <p className="text-xs text-gray-700">{fmtDate(d.dateCreation)}</p>
                        {d.dateTraitement && (
                          <p className="text-[10px] text-gray-400">Traité : {fmtDate(d.dateTraitement)}</p>
                        )}
                      </td>

                      {/* Statut */}
                      <td className="px-4 py-3">
                        <StatutDemandeBadge statut={d.statut} />
                      </td>

                      {/* Actions */}
                      <td className="px-4 py-3">
                        {d.statut === 'EN_ATTENTE' ? (
                          <div className="flex items-center gap-2">
                            <button onClick={() => handleApprouver(d.id)}
                              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                                         bg-emerald-600 text-white hover:bg-emerald-700 transition-colors">
                              <FiCheckCircle className="h-3.5 w-3.5" /> Approuver
                            </button>
                            <button onClick={() => handleRefuser(d.id)}
                              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                                         bg-red-50 text-red-600 hover:bg-red-100 transition-colors border border-red-200">
                              <FiXCircle className="h-3.5 w-3.5" /> Refuser
                            </button>
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400">—</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      {/* ── Modal utilisateur ── */}
      <Modal isOpen={showForm} onClose={() => setShowForm(false)} size="lg"
        title={selected ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur'}>
        <UserForm onSubmit={handleSave} loading={saving} isEdit={!!selected}
          defaultValues={selected
            ? { nom: selected.nom, prenom: selected.prenom, email: selected.email, role: selected.role }
            : { nom: '', prenom: '', email: '', motDePasse: '', role: '' }} />
      </Modal>

      {/* ── Modal mot de passe temporaire ── */}
      <Modal isOpen={showTempPwd} onClose={() => { setShowTempPwd(false); setTempPassword(null) }}
        title="Demande approuvée — Mot de passe temporaire">
        <div className="space-y-4">
          <div className="flex items-start gap-3 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3">
            <FiKey className="h-5 w-5 text-amber-600 mt-0.5 flex-shrink-0" />
            <p className="text-sm text-amber-800 leading-relaxed">
              Communiquez ce mot de passe temporaire à l'utilisateur <strong>par téléphone ou en personne</strong>.
              Il devra le changer à la prochaine connexion.
            </p>
          </div>

          <div>
            <p className="text-xs text-gray-500 mb-2 font-medium uppercase tracking-wide">
              Mot de passe temporaire
            </p>
            <div className="flex items-center gap-3 bg-gray-50 border border-gray-200 rounded-xl px-4 py-3">
              <code className="flex-1 text-lg font-mono font-bold text-gray-900 tracking-widest">
                {tempPassword}
              </code>
              <button onClick={copierMDP}
                className="p-2 rounded-lg hover:bg-gray-200 text-gray-500 transition-colors" title="Copier">
                <FiCopy className="h-4 w-4" />
              </button>
            </div>
          </div>

          <button onClick={() => { setShowTempPwd(false); setTempPassword(null) }}
            className="w-full btn-primary text-sm">
            Fermer
          </button>
        </div>
      </Modal>

      <ConfirmDialog isOpen={showDel} onClose={() => setShowDel(false)} onConfirm={handleDelete}
        title="Supprimer l'utilisateur" message={`Supprimer ${toDelete?.prenom} ${toDelete?.nom} ?`} />
    </div>
  )
}

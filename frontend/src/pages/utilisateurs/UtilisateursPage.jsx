import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { FiPlus, FiEdit2, FiTrash2, FiToggleLeft, FiToggleRight } from 'react-icons/fi'
import toast from 'react-hot-toast'
import * as utilisateurService from '../../services/utilisateurService'
import { usePagination } from '../../hooks/usePagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Pagination from '../../components/common/Pagination'
import Modal from '../../components/common/Modal'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { RoleBadge, ActiveBadge } from '../../components/common/Badge'
import { ROLES } from '../../utils/constants'

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

  const load = useCallback(() => {
    setLoading(true)
    const params = { page: pag.page, size: pag.size, sort: 'nom,asc' }
    if (filterRole) params.role = filterRole
    utilisateurService.getAll(params)
      .then(r => { setUsers(r.data.content); pag.updateFromPage(r.data) })
      .catch(() => toast.error('Erreur lors du chargement'))
      .finally(() => setLoading(false))
  }, [pag.page, filterRole])

  useEffect(() => { pag.reset() }, [filterRole])
  useEffect(() => { load() }, [pag.page, filterRole])

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
      toast.success('Utilisateur supprimé')
      load()
    } catch { toast.error('Impossible de supprimer cet utilisateur') }
  }

  return (
    <div className="space-y-4">
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
                        className="p-1.5 rounded hover:bg-blue-50 text-blue-600"><FiEdit2 className="h-4 w-4" /></button>
                      <button onClick={() => handleToggle(u)}
                        className={`p-1.5 rounded transition-colors ${u.actif ? 'hover:bg-yellow-50 text-yellow-600' : 'hover:bg-green-50 text-green-600'}`}>
                        {u.actif ? <FiToggleRight className="h-4 w-4" /> : <FiToggleLeft className="h-4 w-4" />}
                      </button>
                      <button onClick={() => { setToDelete(u); setShowDel(true) }}
                        className="p-1.5 rounded hover:bg-red-50 text-red-600"><FiTrash2 className="h-4 w-4" /></button>
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

      <Modal isOpen={showForm} onClose={() => setShowForm(false)} size="lg"
        title={selected ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur'}>
        <UserForm onSubmit={handleSave} loading={saving} isEdit={!!selected}
          defaultValues={selected ? { nom: selected.nom, prenom: selected.prenom, email: selected.email, role: selected.role } : { nom: '', prenom: '', email: '', motDePasse: '', role: '' }} />
      </Modal>

      <ConfirmDialog isOpen={showDel} onClose={() => setShowDel(false)} onConfirm={handleDelete}
        title="Supprimer l'utilisateur" message={`Supprimer ${toDelete?.prenom} ${toDelete?.nom} ?`} />
    </div>
  )
}

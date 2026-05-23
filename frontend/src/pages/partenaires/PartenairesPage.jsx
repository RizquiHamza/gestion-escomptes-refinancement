import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { FiPlus, FiEdit2, FiTrash2, FiSearch } from 'react-icons/fi'
import toast from 'react-hot-toast'
import * as partenaireService from '../../services/partenaireService'
import { usePagination } from '../../hooks/usePagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Pagination from '../../components/common/Pagination'
import Modal from '../../components/common/Modal'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { TYPES_PARTENAIRE } from '../../utils/constants'

function PartenaireForm({ onSubmit, defaultValues, loading }) {
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
          <label className="block text-sm font-medium text-gray-700 mb-1">Type *</label>
          <select className={`input-field ${errors.type ? 'border-red-400' : ''}`}
            {...register('type', { required: 'Type requis' })}>
            <option value="">— Sélectionner —</option>
            {TYPES_PARTENAIRE.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
          {errors.type && <p className="text-xs text-red-600 mt-1">{errors.type.message}</p>}
        </div>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
        <input type="email" className={`input-field ${errors.email ? 'border-red-400' : ''}`}
          {...register('email', {
            required: 'Email requis',
            pattern: { value: /\S+@\S+\.\S+/, message: 'Email invalide' },
          })} />
        {errors.email && <p className="text-xs text-red-600 mt-1">{errors.email.message}</p>}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
          <input className="input-field" {...register('telephone')} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">ICE</label>
          <input className="input-field" {...register('ice')} />
        </div>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Adresse</label>
        <input className="input-field" {...register('adresse')} />
      </div>
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={loading} className="btn-primary text-sm">
          {loading ? 'Enregistrement…' : 'Enregistrer'}
        </button>
      </div>
    </form>
  )
}

export default function PartenairesPage() {
  const [partenaires, setPartenaires] = useState([])
  const [loading, setLoading]         = useState(true)
  const [saving, setSaving]           = useState(false)
  const [selected, setSelected]       = useState(null)
  const [showForm, setShowForm]       = useState(false)
  const [showDel, setShowDel]         = useState(false)
  const [toDelete, setToDelete]       = useState(null)
  const [filterType, setFilterType]   = useState('')
  const pag = usePagination(10)

  const load = useCallback(() => {
    setLoading(true)
    const params = { page: pag.page, size: pag.size, sort: 'nom,asc' }
    if (filterType) params.type = filterType
    partenaireService.getAll(params)
      .then(r => { setPartenaires(r.data.content); pag.updateFromPage(r.data) })
      .catch(() => toast.error('Erreur lors du chargement'))
      .finally(() => setLoading(false))
  }, [pag.page, filterType])

  useEffect(() => { pag.reset(); }, [filterType])
  useEffect(() => { load() }, [pag.page, filterType])

  const handleSave = async (data) => {
    setSaving(true)
    try {
      if (selected) { await partenaireService.update(selected.id, data); toast.success('Partenaire modifié') }
      else          { await partenaireService.create(data);               toast.success('Partenaire créé') }
      setShowForm(false); load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur lors de l\'enregistrement')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try {
      await partenaireService.remove(toDelete.id)
      toast.success('Partenaire supprimé')
      load()
    } catch { toast.error('Impossible de supprimer ce partenaire') }
  }

  const typeBadge = (t) =>
    t === 'CLIENT' ? <span className="badge-info">Client</span> : <span className="badge-gray">Fournisseur</span>

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <select value={filterType} onChange={e => setFilterType(e.target.value)}
            className="input-field w-44 text-sm">
            <option value="">Tous les types</option>
            {TYPES_PARTENAIRE.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
          <p className="text-sm text-gray-500">{pag.totalElements} résultat(s)</p>
        </div>
        <button className="btn-primary text-sm flex items-center gap-2"
          onClick={() => { setSelected(null); setShowForm(true) }}>
          <FiPlus className="h-4 w-4" /> Nouveau partenaire
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? <LoadingSpinner /> : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Nom', 'Type', 'Email', 'Téléphone', 'ICE', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {partenaires.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-12 text-gray-400">Aucun partenaire</td></tr>
              ) : partenaires.map(p => (
                <tr key={p.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-900">{p.nom}</td>
                  <td className="px-4 py-3">{typeBadge(p.type)}</td>
                  <td className="px-4 py-3 text-gray-600 text-xs">{p.email}</td>
                  <td className="px-4 py-3 text-gray-600">{p.telephone || '—'}</td>
                  <td className="px-4 py-3 text-gray-600 font-mono text-xs">{p.ice || '—'}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button onClick={() => { setSelected(p); setShowForm(true) }}
                        className="p-1.5 rounded hover:bg-blue-50 text-blue-600"><FiEdit2 className="h-4 w-4" /></button>
                      <button onClick={() => { setToDelete(p); setShowDel(true) }}
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
        title={selected ? 'Modifier le partenaire' : 'Nouveau partenaire'}>
        <PartenaireForm onSubmit={handleSave} loading={saving}
          defaultValues={selected || { nom: '', type: '', email: '', telephone: '', ice: '', adresse: '' }} />
      </Modal>

      <ConfirmDialog isOpen={showDel} onClose={() => setShowDel(false)} onConfirm={handleDelete}
        title="Supprimer le partenaire" message={`Supprimer "${toDelete?.nom}" ?`} />
    </div>
  )
}

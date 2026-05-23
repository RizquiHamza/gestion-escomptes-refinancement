import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { FiPlus, FiEdit2, FiTrash2, FiRefreshCw } from 'react-icons/fi'
import toast from 'react-hot-toast'
import * as escompteService from '../../services/escompteService'
import * as partenaireService from '../../services/partenaireService'
import * as banqueService from '../../services/banqueService'
import { usePagination } from '../../hooks/usePagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Pagination from '../../components/common/Pagination'
import Modal from '../../components/common/Modal'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { StatutBadge } from '../../components/common/Badge'
import { STATUTS } from '../../utils/constants'
import { useAuth } from '../../context/AuthContext'

const fmt = (n) => n == null ? '—' : Number(n).toLocaleString('fr-MA', { minimumFractionDigits: 2 })

function EscompteForm({ onSubmit, defaultValues, loading, partenaires, banques }) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm({ defaultValues })
  useEffect(() => { reset(defaultValues) }, [defaultValues, reset])

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Référence *</label>
        <input className={`input-field ${errors.reference ? 'border-red-400' : ''}`}
          {...register('reference', { required: 'Référence requise' })} />
        {errors.reference && <p className="text-xs text-red-600 mt-1">{errors.reference.message}</p>}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Partenaire *</label>
          <select className={`input-field ${errors.partenaireId ? 'border-red-400' : ''}`}
            {...register('partenaireId', { required: 'Partenaire requis', valueAsNumber: true })}>
            <option value="">— Sélectionner —</option>
            {partenaires.map(p => <option key={p.id} value={p.id}>{p.nom}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Banque *</label>
          <select className={`input-field ${errors.banqueId ? 'border-red-400' : ''}`}
            {...register('banqueId', { required: 'Banque requise', valueAsNumber: true })}>
            <option value="">— Sélectionner —</option>
            {banques.map(b => <option key={b.id} value={b.id}>{b.nom}</option>)}
          </select>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Montant (MAD) *</label>
          <input type="number" step="0.01" className={`input-field ${errors.montant ? 'border-red-400' : ''}`}
            {...register('montant', { required: 'Montant requis', min: { value: 0.01, message: 'Doit être positif' }, valueAsNumber: true })} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Taux (%) *</label>
          <input type="number" step="0.01" className={`input-field ${errors.taux ? 'border-red-400' : ''}`}
            {...register('taux', { required: 'Taux requis', min: 0, valueAsNumber: true })} />
        </div>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Durée (jours) *</label>
          <input type="number" className={`input-field ${errors.duree ? 'border-red-400' : ''}`}
            {...register('duree', { required: 'Durée requise', min: { value: 1, message: 'Min 1 jour' }, valueAsNumber: true })} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Date création</label>
          <input type="date" className="input-field" {...register('dateCreation')} />
        </div>
      </div>
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={loading} className="btn-primary text-sm">
          {loading ? 'Enregistrement…' : 'Enregistrer'}
        </button>
      </div>
    </form>
  )
}

function StatutSelector({ escompte, onChanged }) {
  const { hasRole } = useAuth()
  const [loading, setLoading] = useState(false)

  const change = async (statut) => {
    setLoading(true)
    try {
      await escompteService.changerStatut(escompte.id, statut)
      toast.success('Statut mis à jour')
      onChanged()
    } catch { toast.error('Erreur changement statut') }
    finally { setLoading(false) }
  }

  if (!hasRole('ADMIN', 'RESPONSABLE')) return <StatutBadge statut={escompte.statut} />

  return (
    <select value={escompte.statut} onChange={e => change(e.target.value)}
      disabled={loading}
      className="text-xs border border-gray-200 rounded px-1.5 py-1 focus:outline-none focus:ring-1 focus:ring-blue-500">
      {STATUTS.map(s => <option key={s} value={s}>{s}</option>)}
    </select>
  )
}

export default function EscomptesPage() {
  const [escomptes, setEscomptes]   = useState([])
  const [partenaires, setPartenaires] = useState([])
  const [banques, setBanques]       = useState([])
  const [loading, setLoading]       = useState(true)
  const [saving, setSaving]         = useState(false)
  const [selected, setSelected]     = useState(null)
  const [showForm, setShowForm]     = useState(false)
  const [showDel, setShowDel]       = useState(false)
  const [toDelete, setToDelete]     = useState(null)
  const [filterStatut, setFilterStatut] = useState('')
  const pag = usePagination(10)

  const loadRefs = useCallback(() => {
    partenaireService.getAll({ size: 200 }).then(r => setPartenaires(r.data.content)).catch(() => {})
    banqueService.getAll({ size: 200 }).then(r => setBanques(r.data.content)).catch(() => {})
  }, [])

  const load = useCallback(() => {
    setLoading(true)
    const params = { page: pag.page, size: pag.size }
    if (filterStatut) params.statut = filterStatut
    escompteService.getAll(params)
      .then(r => { setEscomptes(r.data.content); pag.updateFromPage(r.data) })
      .catch(() => toast.error('Erreur lors du chargement'))
      .finally(() => setLoading(false))
  }, [pag.page, filterStatut])

  useEffect(() => { loadRefs() }, [])
  useEffect(() => { pag.reset() }, [filterStatut])
  useEffect(() => { load() }, [pag.page, filterStatut])

  const handleSave = async (data) => {
    setSaving(true)
    try {
      if (selected) { await escompteService.update(selected.id, data); toast.success('Escompte modifié') }
      else          { await escompteService.create(data);                toast.success('Escompte créé') }
      setShowForm(false); load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur lors de l\'enregistrement')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try {
      await escompteService.remove(toDelete.id)
      toast.success('Escompte supprimé')
      load()
    } catch { toast.error('Impossible de supprimer') }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <select value={filterStatut} onChange={e => setFilterStatut(e.target.value)}
            className="input-field w-44 text-sm">
            <option value="">Tous les statuts</option>
            {STATUTS.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
          <p className="text-sm text-gray-500">{pag.totalElements} résultat(s)</p>
        </div>
        <button className="btn-primary text-sm flex items-center gap-2"
          onClick={() => { setSelected(null); setShowForm(true) }}>
          <FiPlus className="h-4 w-4" /> Nouvel escompte
        </button>
      </div>

      <div className="card p-0 overflow-auto">
        {loading ? <LoadingSpinner /> : (
          <table className="w-full text-sm min-w-max">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Référence', 'Partenaire', 'Banque', 'Montant', 'Taux', 'Agios', 'Net reçu', 'Durée', 'Statut', 'Actions'].map(h => (
                  <th key={h} className="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {escomptes.length === 0 ? (
                <tr><td colSpan={10} className="text-center py-12 text-gray-400">Aucun escompte</td></tr>
              ) : escomptes.map(e => (
                <tr key={e.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-3 py-3 font-mono text-xs text-gray-700">{e.reference}</td>
                  <td className="px-3 py-3 text-gray-700">{e.partenaireNom || '—'}</td>
                  <td className="px-3 py-3 text-gray-600">{e.banqueNom || '—'}</td>
                  <td className="px-3 py-3 font-medium text-gray-900 whitespace-nowrap">{fmt(e.montant)}</td>
                  <td className="px-3 py-3 text-gray-600">{e.taux}%</td>
                  <td className="px-3 py-3 text-gray-600 whitespace-nowrap">{fmt(e.agios)}</td>
                  <td className="px-3 py-3 text-green-700 font-medium whitespace-nowrap">{fmt(e.netRecu)}</td>
                  <td className="px-3 py-3 text-gray-600">{e.duree}j</td>
                  <td className="px-3 py-3"><StatutSelector escompte={e} onChanged={load} /></td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-1">
                      <button onClick={() => { setSelected(e); setShowForm(true) }}
                        className="p-1.5 rounded hover:bg-blue-50 text-blue-600"><FiEdit2 className="h-3.5 w-3.5" /></button>
                      <button onClick={() => { setToDelete(e); setShowDel(true) }}
                        className="p-1.5 rounded hover:bg-red-50 text-red-600"><FiTrash2 className="h-3.5 w-3.5" /></button>
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
        title={selected ? 'Modifier l\'escompte' : 'Nouvel escompte'}>
        <EscompteForm onSubmit={handleSave} loading={saving} partenaires={partenaires} banques={banques}
          defaultValues={selected || { reference: '', partenaireId: '', banqueId: '', montant: '', taux: '', duree: '', dateCreation: '' }} />
      </Modal>

      <ConfirmDialog isOpen={showDel} onClose={() => setShowDel(false)} onConfirm={handleDelete}
        title="Supprimer l'escompte" message={`Supprimer l'escompte "${toDelete?.reference}" ?`} />
    </div>
  )
}

import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi'
import toast from 'react-hot-toast'
import * as refinancementService from '../../services/refinancementService'
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

function RefinancementForm({ onSubmit, defaultValues, loading, partenaires, banques }) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm({ defaultValues })
  useEffect(() => { reset(defaultValues) }, [defaultValues, reset])

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Référence *</label>
        <input className={`input-field ${errors.reference ? 'border-red-400' : ''}`}
          {...register('reference', { required: 'Référence requise' })} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Partenaire *</label>
          <select className="input-field"
            {...register('partenaireId', { required: true, valueAsNumber: true })}>
            <option value="">— Sélectionner —</option>
            {partenaires.map(p => <option key={p.id} value={p.id}>{p.nom}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Banque *</label>
          <select className="input-field"
            {...register('banqueId', { required: true, valueAsNumber: true })}>
            <option value="">— Sélectionner —</option>
            {banques.map(b => <option key={b.id} value={b.id}>{b.nom}</option>)}
          </select>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Montant (MAD) *</label>
          <input type="number" step="0.01" className="input-field"
            {...register('montant', { required: true, min: 0.01, valueAsNumber: true })} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Taux (%)</label>
          <input type="number" step="0.01" className="input-field"
            {...register('taux', { min: 0, valueAsNumber: true })} />
        </div>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Durée (mois) *</label>
          <input type="number" className="input-field"
            {...register('duree', { required: true, min: 1, valueAsNumber: true })} />
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

function StatutSelector({ refinancement, onChanged }) {
  const { hasRole } = useAuth()
  const [loading, setLoading] = useState(false)

  const change = async (statut) => {
    setLoading(true)
    try {
      await refinancementService.changerStatut(refinancement.id, statut)
      toast.success('Statut mis à jour')
      onChanged()
    } catch { toast.error('Erreur changement statut') }
    finally { setLoading(false) }
  }

  if (!hasRole('ADMIN', 'RESPONSABLE')) return <StatutBadge statut={refinancement.statut} />

  return (
    <select value={refinancement.statut} onChange={e => change(e.target.value)} disabled={loading}
      className="text-xs border border-gray-200 rounded px-1.5 py-1 focus:outline-none focus:ring-1 focus:ring-blue-500">
      {STATUTS.map(s => <option key={s} value={s}>{s}</option>)}
    </select>
  )
}

export default function RefinancementsPage() {
  const [items, setItems]           = useState([])
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
    refinancementService.getAll(params)
      .then(r => { setItems(r.data.content); pag.updateFromPage(r.data) })
      .catch(() => toast.error('Erreur lors du chargement'))
      .finally(() => setLoading(false))
  }, [pag.page, filterStatut])

  useEffect(() => { loadRefs() }, [])
  useEffect(() => { pag.reset() }, [filterStatut])
  useEffect(() => { load() }, [pag.page, filterStatut])

  const handleSave = async (data) => {
    setSaving(true)
    try {
      if (selected) { await refinancementService.update(selected.id, data); toast.success('Refinancement modifié') }
      else          { await refinancementService.create(data);                toast.success('Refinancement créé') }
      setShowForm(false); load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur lors de l\'enregistrement')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try {
      await refinancementService.remove(toDelete.id)
      toast.success('Refinancement supprimé')
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
          <FiPlus className="h-4 w-4" /> Nouveau refinancement
        </button>
      </div>

      <div className="card p-0 overflow-auto">
        {loading ? <LoadingSpinner /> : (
          <table className="w-full text-sm min-w-max">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Référence', 'Partenaire', 'Banque', 'Montant', 'Taux', 'Intérêts', 'Durée', 'Statut', 'Actions'].map(h => (
                  <th key={h} className="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {items.length === 0 ? (
                <tr><td colSpan={9} className="text-center py-12 text-gray-400">Aucun refinancement</td></tr>
              ) : items.map(r => (
                <tr key={r.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-3 py-3 font-mono text-xs text-gray-700">{r.reference}</td>
                  <td className="px-3 py-3 text-gray-700">{r.partenaireNom || '—'}</td>
                  <td className="px-3 py-3 text-gray-600">{r.banqueNom || '—'}</td>
                  <td className="px-3 py-3 font-medium text-gray-900 whitespace-nowrap">{fmt(r.montant)}</td>
                  <td className="px-3 py-3 text-gray-600">{r.taux != null ? r.taux + '%' : '—'}</td>
                  <td className="px-3 py-3 text-gray-600 whitespace-nowrap">{fmt(r.interets)}</td>
                  <td className="px-3 py-3 text-gray-600">{r.duree != null ? r.duree + 'm' : '—'}</td>
                  <td className="px-3 py-3"><StatutSelector refinancement={r} onChanged={load} /></td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-1">
                      <button onClick={() => { setSelected(r); setShowForm(true) }}
                        className="p-1.5 rounded hover:bg-blue-50 text-blue-600"><FiEdit2 className="h-3.5 w-3.5" /></button>
                      <button onClick={() => { setToDelete(r); setShowDel(true) }}
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
        title={selected ? 'Modifier le refinancement' : 'Nouveau refinancement'}>
        <RefinancementForm onSubmit={handleSave} loading={saving} partenaires={partenaires} banques={banques}
          defaultValues={selected || { reference: '', partenaireId: '', banqueId: '', montant: '', taux: '', duree: '', dateCreation: '' }} />
      </Modal>

      <ConfirmDialog isOpen={showDel} onClose={() => setShowDel(false)} onConfirm={handleDelete}
        title="Supprimer le refinancement" message={`Supprimer le refinancement "${toDelete?.reference}" ?`} />
    </div>
  )
}

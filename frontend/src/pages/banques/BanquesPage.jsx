import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { FiPlus, FiEdit2, FiTrash2, FiSearch } from 'react-icons/fi'
import toast from 'react-hot-toast'
import * as banqueService from '../../services/banqueService'
import { usePagination } from '../../hooks/usePagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Pagination from '../../components/common/Pagination'
import Modal from '../../components/common/Modal'
import ConfirmDialog from '../../components/common/ConfirmDialog'

function BanqueForm({ onSubmit, defaultValues, loading }) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm({ defaultValues })
  useEffect(() => { reset(defaultValues) }, [defaultValues, reset])

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
        <input className={`input-field ${errors.nom ? 'border-red-400' : ''}`}
          {...register('nom', { required: 'Nom requis' })} />
        {errors.nom && <p className="text-xs text-red-600 mt-1">{errors.nom.message}</p>}
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Code *</label>
        <input className={`input-field ${errors.code ? 'border-red-400' : ''}`}
          {...register('code', { required: 'Code requis' })} />
        {errors.code && <p className="text-xs text-red-600 mt-1">{errors.code.message}</p>}
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Adresse</label>
        <input className="input-field" {...register('adresse')} />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
        <input className="input-field" {...register('telephone')} />
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <button type="submit" disabled={loading} className="btn-primary text-sm">
          {loading ? 'Enregistrement…' : 'Enregistrer'}
        </button>
      </div>
    </form>
  )
}

export default function BanquesPage() {
  const [banques, setBanques]     = useState([])
  const [loading, setLoading]     = useState(true)
  const [saving, setSaving]       = useState(false)
  const [selected, setSelected]   = useState(null)
  const [showForm, setShowForm]   = useState(false)
  const [showDel, setShowDel]     = useState(false)
  const [toDelete, setToDelete]   = useState(null)
  const pag = usePagination(10)

  const load = useCallback(() => {
    setLoading(true)
    banqueService.getAll({ page: pag.page, size: pag.size, sort: 'nom,asc' })
      .then(r => { setBanques(r.data.content); pag.updateFromPage(r.data) })
      .catch(() => toast.error('Erreur lors du chargement des banques'))
      .finally(() => setLoading(false))
  }, [pag.page])

  useEffect(() => { load() }, [pag.page])

  const handleSave = async (data) => {
    setSaving(true)
    try {
      if (selected) { await banqueService.update(selected.id, data); toast.success('Banque modifiée') }
      else          { await banqueService.create(data);              toast.success('Banque créée') }
      setShowForm(false)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erreur lors de l\'enregistrement')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try {
      await banqueService.remove(toDelete.id)
      toast.success('Banque supprimée')
      load()
    } catch { toast.error('Impossible de supprimer cette banque') }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{pag.totalElements} banque(s) au total</p>
        <button className="btn-primary text-sm flex items-center gap-2"
          onClick={() => { setSelected(null); setShowForm(true) }}>
          <FiPlus className="h-4 w-4" /> Nouvelle banque
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? <LoadingSpinner /> : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Nom', 'Code', 'Adresse', 'Téléphone', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {banques.length === 0 ? (
                <tr><td colSpan={5} className="text-center py-12 text-gray-400">Aucune banque</td></tr>
              ) : banques.map(b => (
                <tr key={b.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-900">{b.nom}</td>
                  <td className="px-4 py-3 text-gray-600 font-mono text-xs">{b.code}</td>
                  <td className="px-4 py-3 text-gray-600">{b.adresse || '—'}</td>
                  <td className="px-4 py-3 text-gray-600">{b.telephone || '—'}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button onClick={() => { setSelected(b); setShowForm(true) }}
                        className="p-1.5 rounded hover:bg-blue-50 text-blue-600 transition-colors">
                        <FiEdit2 className="h-4 w-4" />
                      </button>
                      <button onClick={() => { setToDelete(b); setShowDel(true) }}
                        className="p-1.5 rounded hover:bg-red-50 text-red-600 transition-colors">
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

      <Modal isOpen={showForm} onClose={() => setShowForm(false)}
        title={selected ? 'Modifier la banque' : 'Nouvelle banque'}>
        <BanqueForm onSubmit={handleSave} loading={saving}
          defaultValues={selected || { nom: '', code: '', adresse: '', telephone: '' }} />
      </Modal>

      <ConfirmDialog isOpen={showDel} onClose={() => setShowDel(false)} onConfirm={handleDelete}
        title="Supprimer la banque" message={`Supprimer "${toDelete?.nom}" ?`} />
    </div>
  )
}

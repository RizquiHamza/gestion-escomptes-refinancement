import Modal from './Modal'

export default function ConfirmDialog({ isOpen, onClose, onConfirm, title, message, danger = true }) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} size="sm">
      <p className="text-sm text-gray-600 mb-6">{message}</p>
      <div className="flex justify-end gap-3">
        <button onClick={onClose} className="btn-secondary text-sm">Annuler</button>
        <button onClick={() => { onConfirm(); onClose() }}
          className={danger ? 'btn-danger text-sm' : 'btn-primary text-sm'}>
          Confirmer
        </button>
      </div>
    </Modal>
  )
}

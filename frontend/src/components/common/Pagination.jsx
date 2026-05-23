import { FiChevronLeft, FiChevronRight } from 'react-icons/fi'

export default function Pagination({ page, totalPages, totalElements, size, onPage }) {
  if (totalPages <= 1) return null

  const pages = []
  const start = Math.max(0, page - 2)
  const end   = Math.min(totalPages - 1, page + 2)
  for (let i = start; i <= end; i++) pages.push(i)

  const from = page * size + 1
  const to   = Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex items-center justify-between px-2 py-3 border-t border-gray-200 mt-4">
      <p className="text-sm text-gray-500">
        {from}–{to} sur <span className="font-medium">{totalElements}</span>
      </p>
      <div className="flex items-center gap-1">
        <button onClick={() => onPage(page - 1)} disabled={page === 0}
          className="p-1.5 rounded hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed">
          <FiChevronLeft className="h-4 w-4" />
        </button>
        {pages.map((p) => (
          <button key={p} onClick={() => onPage(p)}
            className={`px-3 py-1 rounded text-sm font-medium ${p === page ? 'bg-blue-600 text-white' : 'hover:bg-gray-100 text-gray-700'}`}>
            {p + 1}
          </button>
        ))}
        <button onClick={() => onPage(page + 1)} disabled={page >= totalPages - 1}
          className="p-1.5 rounded hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed">
          <FiChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}

export default function LoadingSpinner({ size = 'md', text = '' }) {
  const sz = { sm: 'h-4 w-4', md: 'h-8 w-8', lg: 'h-12 w-12' }[size]
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-12">
      <div className={`${sz} animate-spin rounded-full border-4 border-blue-200 border-t-blue-600`} />
      {text && <p className="text-sm text-gray-500">{text}</p>}
    </div>
  )
}

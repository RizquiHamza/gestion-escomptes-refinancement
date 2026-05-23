import { useState } from 'react'

export function usePagination(initialSize = 10) {
  const [page, setPage]     = useState(0)
  const [size]              = useState(initialSize)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const updateFromPage = (pageData) => {
    setTotalPages(pageData.totalPages)
    setTotalElements(pageData.totalElements)
  }

  const goTo   = (p) => setPage(p)
  const prev   = ()  => setPage((p) => Math.max(0, p - 1))
  const next   = ()  => setPage((p) => Math.min(totalPages - 1, p + 1))
  const reset  = ()  => setPage(0)

  return { page, size, totalPages, totalElements, updateFromPage, goTo, prev, next, reset }
}

import { useCallback, useEffect, useRef, type MutableRefObject } from 'react'

type QueryParams = {
  page: number
  size: number
  sort: string
  search: string
  organization: string
}

type PaginatedResult = {
  content?: any[]
  totalElements?: number
}

type TableRef = MutableRefObject<any>

type UseAdminTableQuerySyncArgs = {
  organization: string | undefined
  tableRef: TableRef
  isRefreshing: boolean
  currentPage: number
  subscribedData?: PaginatedResult
}

const buildSignature = (params: { page: number; organization: string | undefined }, result: PaginatedResult) => {
  return JSON.stringify({
    page: params.page,
    organization: params.organization,
    totalCount: result.totalElements || 0,
    content: result.content || [],
  })
}

export const useAdminTableQuerySync = ({
  organization,
  tableRef,
  isRefreshing,
  currentPage,
  subscribedData,
}: UseAdminTableQuerySyncArgs) => {
  const currentQueryRef = useRef<QueryParams | null>(null)
  const lastRenderedDataSignatureRef = useRef<string | null>(null)

  const markTableRenderedData = useCallback((params: QueryParams, result: PaginatedResult) => {
    lastRenderedDataSignatureRef.current = buildSignature(params, result)
  }, [])

  const handleRefresh = useCallback(() => {
    if (tableRef.current && tableRef.current.onQueryChange) {
      tableRef.current.onQueryChange()
    }
  }, [tableRef])

  useEffect(() => {
    if (!subscribedData || !tableRef.current?.onQueryChange || isRefreshing) {
      return
    }

    const nextSignature = buildSignature(
      {
        page: currentPage,
        organization,
      },
      subscribedData
    )

    if (lastRenderedDataSignatureRef.current !== nextSignature) {
      lastRenderedDataSignatureRef.current = nextSignature
      tableRef.current.onQueryChange()
    }
  }, [subscribedData, organization, currentPage, isRefreshing, tableRef])

  return {
    currentQueryRef,
    markTableRenderedData,
    handleRefresh,
  }
}

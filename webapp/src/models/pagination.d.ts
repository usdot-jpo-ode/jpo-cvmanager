export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface PaginatedQueryParams {
  page?: number
  size?: number
  sort?: string
  search?: string
}

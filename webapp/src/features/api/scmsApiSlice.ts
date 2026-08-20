import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'

/**
 * API slice for SCMS health status.
 * Provides RSU certificate health information for a given organization.
 */

// Types matching the API response structure

// Individual SCMS health record for an RSU
export type ScmsHealthDto = {
  health: boolean | null
  // Certificate expiration timestamp as ISO-8601 UTC, e.g. "2026-04-10T13:28:01Z". Parse with new Date() and format for display with toLocaleString().
  expiration: string | null
}

// Map of RSU IPv4 addresses to their health status. Null values indicate no health record.
export type ScmsHealthStatus = {
  [ip: string]: ScmsHealthDto | null
}

// Raw API response wrapper - the API returns the map inside a scmsHealthByIp field
type ScmsHealthResponse = {
  scmsHealthByIp: ScmsHealthStatus
}

// Tag type constants
const SCMS_API_STATUS_TAG = 'ScmsStatus' as const

export const scmsApiSlice = createApi({
  reducerPath: 'scmsApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/devices/scms`,
    prepareHeaders: (headers, { getState }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  tagTypes: [SCMS_API_STATUS_TAG],
  endpoints: (builder) => ({
    getScmsStatus: builder.query<ScmsHealthStatus, string>({
      query: (organization) => ({
        url: '/status',
        headers: {
          Organization: organization,
        },
      }),
      // Unwrap the response so consumers get the map directly without needing to access .scmsHealthByIp
      transformResponse: (response: ScmsHealthResponse) => response.scmsHealthByIp,
      providesTags: [SCMS_API_STATUS_TAG],
    }),
  }),
})

export const { useGetScmsStatusQuery, useLazyGetScmsStatusQuery } = scmsApiSlice

// Formats an ISO-8601 expiration timestamp as "MM/DD/YYYY hh:mm:ss AM/PM" in the viewer's local timezone.
// Returns empty string for null/undefined input.
export const formatScmsExpiration = (iso: string | null | undefined): string => {
  if (!iso) return ''
  return new Date(iso).toLocaleString('en-US', {
    month: '2-digit',
    day: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: true,
  })
}


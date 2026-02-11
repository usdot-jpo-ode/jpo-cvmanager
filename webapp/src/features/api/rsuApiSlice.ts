import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { getQueryString } from './intersectionApiSlice'
import { AdminRsu, AdminRsuAllowedSelections } from '../../models/Rsu'

export interface PaginatedRsusResponse {
  content: AdminRsu[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface GetAllRsusParams {
  organization: string
  page?: number
  size?: number
  sort?: string
}

export const rsuApiSlice = createApi({
  reducerPath: 'rsuApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/devices/rsus`,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      // Endpoint names must match the keys in the endpoints objects below
      const endpointsWithoutToken = []
      if (token && !endpointsWithoutToken.includes(endpoint)) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  tagTypes: ['Rsu', 'AllowedSelections'],
  endpoints: (builder) => ({
    getAllRsus: builder.query<PaginatedRsusResponse, GetAllRsusParams>({
      query: ({ organization, page = 0, size = 100, sort = '' }) => {
        return {
          url: `${getQueryString({
            page: page.toString(),
            size: size.toString(),
            sort: sort,
          })}`,
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: (result) =>
        result
          ? [...result.content.map(({ ip }) => ({ type: 'Rsu' as const, id: ip })), { type: 'Rsu', id: 'LIST' }]
          : [{ type: 'Rsu', id: 'LIST' }],
      // When getAllRsus loads, populate individual RSU caches
      async onQueryStarted(args, { dispatch, queryFulfilled }) {
        try {
          const { data } = await queryFulfilled
          // Populate each individual RSU cache entry
          data.content.forEach((rsu) => {
            dispatch(rsuApiSlice.util.upsertQueryData('getRsu', rsu.ip, rsu))
          })
        } catch {}
      },
    }),
    getRsu: builder.query<AdminRsu, string>({
      query: (rsuIp) => {
        return {
          url: `${getQueryString({
            rsu_ip: rsuIp,
          })}`,
        }
      },
      providesTags: (result, error, rsuIp) => [{ type: 'Rsu', id: rsuIp }],
      // When getRsu loads, update the corresponding item in getAllRsus
      async onQueryStarted(rsuIp, { dispatch, queryFulfilled, getState }) {
        try {
          const { data: updatedRsu } = await queryFulfilled

          // Update the getAllRsus cache for all organizations
          const state = getState() as RootState

          // Get all active getAllRsus queries across all organizations
          Object.keys(state.rsuApi.queries).forEach((queryKey) => {
            const query = state.rsuApi.queries[queryKey]
            if (query?.endpointName === 'getAllRsus' && query?.status === 'fulfilled') {
              const args = query.originalArgs as GetAllRsusParams

              dispatch(
                rsuApiSlice.util.updateQueryData('getAllRsus', args, (draft) => {
                  const index = draft.content.findIndex((rsu) => rsu.ip === rsuIp)
                  if (index !== -1) {
                    draft.content[index] = updatedRsu
                  }
                })
              )
            }
          })
        } catch {}
      },
    }),
    getRsuAllowedSelections: builder.query<AdminRsuAllowedSelections, void>({
      query: () => {
        return {
          url: 'allowed-selections',
        }
      },
      providesTags: (result, error) => ['AllowedSelections'],
    }),
    patchRsu: builder.mutation<void, { rsuIp: string; patch: Partial<AdminRsu> }>({
      query: ({ rsuIp, patch }) => ({
        url: `${getQueryString({
          rsu_ip: rsuIp,
        })}`,
        method: 'PATCH',
        body: { origin_ip: rsuIp, ...patch },
      }),
      invalidatesTags: (result, error, { rsuIp }) => [
        { type: 'Rsu', id: rsuIp },
        { type: 'Rsu', id: 'LIST' },
      ],
    }),
    deleteRsu: builder.mutation<void, string>({
      query: (rsuIp) => ({
        url: `${getQueryString({
          rsu_ip: rsuIp,
        })}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, rsuIp) => [
        { type: 'Rsu', id: rsuIp },
        { type: 'Rsu', id: 'LIST' },
      ],
    }),
    deleteMultipleRsus: builder.mutation<void, string[]>({
      query: (rsuIps) => ({
        url: '/batch',
        method: 'DELETE',
        body: rsuIps,
      }),
      invalidatesTags: (result, error, rsuIps) => [
        ...rsuIps.map((rsuIp) => ({ type: 'Rsu' as const, id: rsuIp })),
        { type: 'Rsu', id: 'LIST' },
      ],
    }),
  }),
})

export const {
  useGetAllRsusQuery,
  useLazyGetAllRsusQuery,
  useGetRsuQuery,
  useLazyGetRsuQuery,
  useGetRsuAllowedSelectionsQuery,
  useLazyGetRsuAllowedSelectionsQuery,
  usePatchRsuMutation,
  useDeleteRsuMutation,
  useDeleteMultipleRsusMutation,
} = rsuApiSlice

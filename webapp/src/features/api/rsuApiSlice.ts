import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { getQueryString } from './intersectionApiSlice'
import { AdminRsu, AdminRsuAllowedSelections } from '../../models/Rsu'
import { AdminRsuCreationBody } from '../adminAddRsu/AdminAddRsu'

export interface PaginatedRsusResponse {
  content: AdminRsu[]
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

export interface GetAllRsusParams extends PaginatedQueryParams {
  organization: string
}

export const rsuApiSlice = createApi({
  reducerPath: 'rsuApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/devices/rsus`,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      headers.set('Accept', 'application/json')

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
      query: ({ organization, page = 0, size = 100, sort = 'ip,asc', search = '' }) => {
        return {
          url: `${getQueryString({
            page: page.toString(),
            size: size.toString(),
            sort: sort,
            search: search,
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
    }),
    getRsuAllowedSelections: builder.query<AdminRsuAllowedSelections, void>({
      query: () => {
        return {
          url: 'allowed-selections',
        }
      },
      providesTags: (result, error) => ['AllowedSelections'],
    }),
    createRsu: builder.mutation<void, AdminRsuCreationBody>({
      query: (rsu) => ({
        url: '',
        method: 'POST',
        body: rsu,
      }),
      invalidatesTags: (result, error, vars) => [{ type: 'Rsu', id: 'LIST' }],
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
  useCreateRsuMutation,
  usePatchRsuMutation,
  useDeleteRsuMutation,
  useDeleteMultipleRsusMutation,
} = rsuApiSlice

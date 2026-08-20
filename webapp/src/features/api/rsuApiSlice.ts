import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { getQueryString } from './intersectionConfigSlice'
import { AdminRsu, AdminRsuAllowedSelections, AdminRsuPatch } from '../../models/Rsu'
import { PaginatedQueryParams, PaginatedResponse } from '../../models/pagination'
import { AdminRsuCreationBody } from '../adminAddRsu/AdminAddRsu'

export interface GetAllRsusParams extends PaginatedQueryParams {
  organization: string
}

// Tag type constants
export const RSU_API_RSU_TAG = 'Rsu' as const
export const RSU_API_RSU_ALLOWED_SELECTIONS_TAG = 'AllowedSelections' as const
export const RSU_API_RSU_LIST_ID = 'LIST' as const

export const rsuApiSlice = createApi({
  reducerPath: 'rsuApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/devices/rsus`,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      headers.set('Accept', 'application/json')

      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  tagTypes: [RSU_API_RSU_TAG, RSU_API_RSU_ALLOWED_SELECTIONS_TAG],
  endpoints: (builder) => ({
    getAllRsus: builder.query<PaginatedResponse<AdminRsu>, GetAllRsusParams>({
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
          ? [
              ...result.content.map(({ ip }) => ({ type: RSU_API_RSU_TAG, id: ip })),
              { type: RSU_API_RSU_TAG, id: RSU_API_RSU_LIST_ID },
            ]
          : [{ type: RSU_API_RSU_TAG, id: RSU_API_RSU_LIST_ID }],
    }),
    getRsu: builder.query<AdminRsu, string>({
      query: (rsuIp) => {
        return {
          url: `${getQueryString({
            rsu_ip: rsuIp,
          })}`,
        }
      },
      providesTags: (result, error, rsuIp) => [{ type: RSU_API_RSU_TAG, id: rsuIp }],
    }),
    getRsuAllowedSelections: builder.query<AdminRsuAllowedSelections, void>({
      query: () => {
        return {
          url: 'allowed-selections',
        }
      },
      providesTags: (result, error) => [RSU_API_RSU_ALLOWED_SELECTIONS_TAG],
    }),
    createRsu: builder.mutation<void, AdminRsuCreationBody>({
      query: (rsu) => ({
        url: '',
        method: 'POST',
        body: rsu,
      }),
      invalidatesTags: (result, error, vars) => [{ type: RSU_API_RSU_TAG, id: RSU_API_RSU_LIST_ID }],
    }),
    patchRsu: builder.mutation<void, { rsuIp: string; patch: AdminRsuPatch }>({
      query: ({ rsuIp, patch }) => ({
        url: `${getQueryString({
          rsu_ip: rsuIp,
        })}`,
        method: 'PATCH',
        body: { ip: rsuIp, ...patch },
      }),
      invalidatesTags: (result, error, { rsuIp }) => [
        { type: RSU_API_RSU_TAG, id: rsuIp },
        { type: RSU_API_RSU_TAG, id: RSU_API_RSU_LIST_ID },
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
        { type: RSU_API_RSU_TAG, id: rsuIp },
        { type: RSU_API_RSU_TAG, id: RSU_API_RSU_LIST_ID },
      ],
    }),
    deleteMultipleRsus: builder.mutation<void, string[]>({
      query: (rsuIps) => ({
        url: '/batch',
        method: 'DELETE',
        body: rsuIps,
      }),
      invalidatesTags: (result, error, rsuIps) => [
        ...rsuIps.map((rsuIp) => ({ type: RSU_API_RSU_TAG, id: rsuIp })),
        { type: RSU_API_RSU_TAG, id: RSU_API_RSU_LIST_ID },
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

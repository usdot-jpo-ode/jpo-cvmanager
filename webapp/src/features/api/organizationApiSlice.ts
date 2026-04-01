import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { getQueryString } from './intersectionApiSlice'

export const organizationApiSlice = createApi({
  reducerPath: 'organizationApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/organizations`,
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
  tagTypes: ['RsuList', 'Rsu'],
  endpoints: (builder) => ({
    getAllRsuIps: builder.query<string[], string>({
      query: (organization) => {
        return {
          url: 'rsus',
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: ['RsuList'],
    }),
    getRsuOrganizations: builder.query<string[], string>({
      query: (rsuIp) => {
        return {
          url: `rsus${getQueryString({
            rsu_ip: rsuIp,
          })}`,
        }
      },
      providesTags: (result, error, rsuIp) => [{ type: 'Rsu', id: rsuIp }],
    }),
  }),
})

export const {
  useGetAllRsuIpsQuery,
  useLazyGetAllRsuIpsQuery,
  useGetRsuOrganizationsQuery,
  useLazyGetRsuOrganizationsQuery,
} = organizationApiSlice

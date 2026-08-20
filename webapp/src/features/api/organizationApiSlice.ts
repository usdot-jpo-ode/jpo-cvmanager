import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { AdminRsu } from '../../models/Rsu'

// Tag type constants
export const ORGANIZATION_API_RSU_LIST_TAG = 'RsuList' as const
export const ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG = 'AvailableRsuList' as const
export const ORGANIZATION_API_RSU_TAG = 'Rsu' as const
export const ORGANIZATION_API_USER_LIST_TAG = 'UserList' as const
export const ORGANIZATION_API_AVAILABLE_USER_LIST_TAG = 'AvailableUserList' as const
export const ORGANIZATION_API_USER_TAG = 'User' as const

export const organizationApiSlice = createApi({
  reducerPath: 'organizationApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/organizations`,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  tagTypes: [
    ORGANIZATION_API_RSU_LIST_TAG,
    ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG,
    ORGANIZATION_API_RSU_TAG,
    ORGANIZATION_API_USER_LIST_TAG,
    ORGANIZATION_API_AVAILABLE_USER_LIST_TAG,
    ORGANIZATION_API_USER_TAG,
  ],
  endpoints: (builder) => ({
    getAllRsuIpsInOrganization: builder.query<string[], string>({
      query: (organization) => {
        return {
          url: 'rsus',
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: [ORGANIZATION_API_RSU_LIST_TAG],
    }),
    getRsuOrganizations: builder.query<string[], string>({
      query: (rsuIp) => {
        return {
          url: `rsus/${rsuIp}`,
        }
      },
      providesTags: (result, error, rsuIp) => [{ type: ORGANIZATION_API_RSU_TAG, id: rsuIp }],
    }),
    getAllRsusNotInOrganization: builder.query<AdminRsu[], string>({
      query: (organization) => {
        return {
          url: 'rsus/available',
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: [ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG],
    }),
    getAllUserEmailsInOrganization: builder.query<string[], string>({
      query: (organization) => {
        return {
          url: 'users',
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: [ORGANIZATION_API_USER_LIST_TAG],
    }),
    getUserOrganizations: builder.query<string[], string>({
      query: (email) => {
        return {
          url: `users/${email}`,
        }
      },
      providesTags: (result, error, email) => [{ type: ORGANIZATION_API_USER_TAG, id: email }],
    }),
    getAllUsersNotInOrganization: builder.query<AdminUser[], string>({
      query: (organization) => {
        return {
          url: 'users/available',
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: [ORGANIZATION_API_AVAILABLE_USER_LIST_TAG],
    }),
  }),
})

export const {
  useGetAllRsuIpsInOrganizationQuery,
  useLazyGetAllRsuIpsInOrganizationQuery,
  useGetAllRsusNotInOrganizationQuery,
  useLazyGetAllRsusNotInOrganizationQuery,
  useGetRsuOrganizationsQuery,
  useLazyGetRsuOrganizationsQuery,
  useGetAllUserEmailsInOrganizationQuery,
  useLazyGetAllUserEmailsInOrganizationQuery,
  useGetAllUsersNotInOrganizationQuery,
  useLazyGetAllUsersNotInOrganizationQuery,
  useGetUserOrganizationsQuery,
  useLazyGetUserOrganizationsQuery,
} = organizationApiSlice

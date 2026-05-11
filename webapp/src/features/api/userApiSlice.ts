import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { getQueryString } from './intersectionApiSlice'
import { PaginatedQueryParams, PaginatedResponse } from '../../models/pagination'

export interface GetUsersParams extends PaginatedQueryParams {
  organization: string
}

// Tag type constants
const USER_API_USER_TAG = 'User' as const
const USER_API_ALLOWED_SELECTIONS_TAG = 'AllowedSelections' as const
const USER_API_USER_LIST_ID = 'LIST' as const

export const userApiSlice = createApi({
  reducerPath: 'userApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/users`,
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
  tagTypes: [USER_API_USER_TAG, USER_API_ALLOWED_SELECTIONS_TAG],
  endpoints: (builder) => ({
    getUsers: builder.query<PaginatedResponse<AdminUser>, GetUsersParams>({
      query: ({ organization, page = 0, size = 100, sort = 'first_name,asc', search = '' }) => {
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
              ...result.content.map(({ email }) => ({ type: USER_API_USER_TAG, id: email })),
              { type: USER_API_USER_TAG, id: USER_API_USER_LIST_ID },
            ]
          : [{ type: USER_API_USER_TAG, id: USER_API_USER_LIST_ID }],
    }),
    getUser: builder.query<AdminUser, string>({
      query: (email) => {
        return {
          url: `/${email}`,
        }
      },
      providesTags: (result, error, email) => [{ type: USER_API_USER_TAG, id: email }],
    }),
    getUserAllowedSelections: builder.query<AdminUserAllowedSelections, void>({
      query: () => {
        return {
          url: '/allowed-selections',
        }
      },
      providesTags: (result, error) => [USER_API_ALLOWED_SELECTIONS_TAG],
    }),
    getRoles: builder.query<string[], void>({
      query: () => {
        return {
          url: '/roles',
        }
      },
    }),
    createUser: builder.mutation<void, AdminUserCreationBody>({
      query: (user) => ({
        url: '',
        method: 'POST',
        body: user,
      }),
      invalidatesTags: (result, error, vars) => [{ type: USER_API_USER_TAG, id: USER_API_USER_LIST_ID }],
    }),
    patchUser: builder.mutation<void, { email: string; patch: Partial<AdminUser> }>({
      query: ({ email, patch }) => ({
        url: `/${email}`,
        method: 'PATCH',
        body: { origin_ip: email, ...patch },
      }),
      invalidatesTags: (result, error, { email }) => [
        { type: USER_API_USER_TAG, id: email },
        { type: USER_API_USER_TAG, id: USER_API_USER_LIST_ID },
      ],
    }),
    deleteUser: builder.mutation<void, string>({
      query: (email) => ({
        url: `/${email}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, email) => [
        { type: USER_API_USER_TAG, id: email },
        { type: USER_API_USER_TAG, id: USER_API_USER_LIST_ID },
      ],
    }),
    deleteMultipleUsers: builder.mutation<void, string[]>({
      query: (emails) => ({
        url: '/batch',
        method: 'DELETE',
        body: emails,
      }),
      invalidatesTags: (result, error, emails) => [
        ...emails.map((email) => ({ type: USER_API_USER_TAG, id: email })),
        { type: USER_API_USER_TAG, id: USER_API_USER_LIST_ID },
      ],
    }),
  }),
})

export const {
  useGetUsersQuery,
  useLazyGetUsersQuery,
  useGetUserQuery,
  useLazyGetUserQuery,
  useGetUserAllowedSelectionsQuery,
  useLazyGetUserAllowedSelectionsQuery,
  useGetRolesQuery,
  useLazyGetRolesQuery,
  useCreateUserMutation,
  usePatchUserMutation,
  useDeleteUserMutation,
  useDeleteMultipleUsersMutation,
} = userApiSlice

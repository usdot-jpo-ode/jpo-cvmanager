import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { AdminIntersection } from '../../models/Intersection'
import { AdminIntersectionCreationInfo, AdminIntersectionCreationBody } from '../adminAddIntersection/adminAddIntersectionSlice'
import { AdminEditIntersectionBody, adminEditIntersectionData } from '../adminEditIntersection/adminEditIntersectionSlice'

// Tag type constants
export const ADMIN_INTERSECTION_TAG = 'AdminIntersection' as const
export const ADMIN_INTERSECTION_LIST_ID = 'LIST' as const

export const adminIntersectionApiSlice = createApi({
  reducerPath: 'adminIntersectionApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/admin/intersections`,
    prepareHeaders: (headers, { getState }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
        headers.set('Content-Type', 'application/json')
        headers.set('Accept', 'application/json')
      }

      return headers
    },
  }),
  tagTypes: [ADMIN_INTERSECTION_TAG],
  endpoints: (builder) => ({
    getIntersections: builder.query<{ intersection_data: AdminIntersection[] }, string>({
      query: (organization) => {
        return {
          url: '',
          headers: {
            Organization: organization,
          },
        }
      },
      providesTags: (result) =>
        result
          ? [
              ...result.intersection_data.map(({ intersection_id }) => ({
                type: ADMIN_INTERSECTION_TAG,
                id: intersection_id,
              })),
              { type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_LIST_ID },
            ]
          : [{ type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_LIST_ID }],
    }),
    getIntersection: builder.query<adminEditIntersectionData, string>({
      query: (intersectionId) => {
        return {
          url: `${intersectionId}`,
        }
      },
      providesTags: (result, error, intersectionId) => [{ type: ADMIN_INTERSECTION_TAG, id: intersectionId }],
    }),
    getIntersectionAllowedSelections: builder.query<AdminIntersectionCreationInfo, void>({
      query: () => {
        return {
          url: 'allowed-selections',
        }
      },
    }),
    createIntersection: builder.mutation<{ success: boolean; message: string }, AdminIntersectionCreationBody>({
      query: (body) => ({
        url: '',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_LIST_ID }],
    }),
    patchIntersection: builder.mutation<{ success: boolean; message: string }, AdminEditIntersectionBody>({
      query: (body) => ({
        url: '',
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (result, error, { intersection_id }) => [
        { type: ADMIN_INTERSECTION_TAG, id: intersection_id },
        { type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_LIST_ID },
      ],
    }),
    deleteIntersection: builder.mutation<{ success: boolean; message: string }, string>({
      query: (intersectionId) => ({
        url: `${intersectionId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, intersectionId) => [
        { type: ADMIN_INTERSECTION_TAG, id: intersectionId },
        { type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_LIST_ID },
      ],
    }),
  }),
})

export const {
  useGetIntersectionsQuery,
  useLazyGetIntersectionsQuery,
  useGetIntersectionQuery,
  useLazyGetIntersectionQuery,
  useGetIntersectionAllowedSelectionsQuery,
  useLazyGetIntersectionAllowedSelectionsQuery,
  useCreateIntersectionMutation,
  usePatchIntersectionMutation,
  useDeleteIntersectionMutation,
} = adminIntersectionApiSlice

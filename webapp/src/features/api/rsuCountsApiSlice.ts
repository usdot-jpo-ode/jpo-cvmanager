// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { RsuCounts } from '../../models/RsuApi'
import { getQueryString } from './intersectionConfigSlice'

// Define a service using a base URL and expected endpoints
export const rsuCountsApiSlice = createApi({
  reducerPath: 'rsuCountsApi',
  baseQuery: fetchBaseQuery({
    baseUrl: EnvironmentVars.cvmanagerBaseEndpoint,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      if (token) {
        headers.set('Authorization', `${token}`)
      }

      return headers
    },
  }),
  endpoints: (builder) => ({
    getRsuCounts: builder.query<RsuCounts, { organization: string; startDate: Date; endDate: Date }>({
      query: ({ organization, startDate, endDate }) => {
        return {
          url: `/rsucounts${getQueryString({
            start: startDate.toISOString(),
            end: endDate.toISOString(),
          })}`,
          headers: {
            Organization: organization,
          },
        }
      },
    }),
  }),
})

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const { useGetRsuCountsQuery, useLazyGetRsuCountsQuery, util: rsuCountsApiUtil } = rsuCountsApiSlice

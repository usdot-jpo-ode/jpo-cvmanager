// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { RsuCounts } from '../../models/RsuApi'
import { getQueryString } from './intersectionApiSlice'

// Define a service using a base URL and expected endpoints
export const rsuCountsApiSlice = createApi({
  reducerPath: 'rsuCountsApi',
  baseQuery: fetchBaseQuery({
    baseUrl: EnvironmentVars.cvmanagerBaseEndpoint,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      // Specify endpoints that do not require a token or organization. These names must match the keys in the endpoints object below.
      const endpointsWithoutToken = []

      if (token && !endpointsWithoutToken.includes(endpoint)) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  endpoints: (builder) => ({
    getRsuCounts: builder.query<RsuCounts, { organization: string; startDate: Date; endDate: Date }>({
      query: ({ organization, startDate, endDate }) => {
        return {
          url: `/rsucounts${getQueryString({
            start_time_utc_millis: startDate.toISOString(),
            end_time_utc_millis: endDate.toISOString(),
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

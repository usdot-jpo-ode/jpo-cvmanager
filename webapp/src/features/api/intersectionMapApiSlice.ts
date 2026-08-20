// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'
import { combineUrlPaths } from '../../apis/intersections/api-helper-cviz'
import {
  addSrmTimestampsAndSortAscending,
  addSsmTimestampsAndSortAscending,
} from '../intersections/map/utilities/message-utils'
import { MAP_QUERY_PARAMS } from '../intersections/map/map-slice'
import { getQueryString } from './intersectionConfigSlice'

export type LocationParams = {
  longitude: number
  latitude: number
  distance: number
}

export type TimeWindow = {
  startMillis: number
  endMillis: number
}

// Define a service using a base URL and expected endpoints
export const intersectionMapApiSlice = createApi({
  reducerPath: 'intersectionMapApi',
  baseQuery: fetchBaseQuery({
    baseUrl: combineUrlPaths(EnvironmentVars.CVIZ_API_SERVER_URL, '/data'),
    prepareHeaders: (headers, { getState, endpoint }) => {
      const token = selectToken(getState() as RootState)

      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  tagTypes: ['defaultConfigs', 'intersectionConfigs'],
  endpoints: (builder) => ({
    getSsmWithinTimeWindow: builder.query<ProcessedSsm[], { intersectionId: number; timeWindow: TimeWindow }>({
      query: ({ intersectionId, timeWindow }) => {
        return `/processed-ssm${getQueryString({
          intersection_id: intersectionId.toString(),
          start_time_utc_millis: timeWindow.startMillis.toString(),
          end_time_utc_millis: timeWindow.endMillis.toString(),
        })}`
      },
      transformResponse: (response: { content: ProcessedSsm[] }) => addSsmTimestampsAndSortAscending(response.content),
    }),
    getSrmWithinTimeWindow: builder.query<ProcessedSrmFeature[], { intersectionId: number; timeWindow: TimeWindow }>({
      query: ({ intersectionId, timeWindow }) => {
        return `/processed-srm${getQueryString({
          intersection_id: intersectionId.toString(),
          start_time_utc_millis: timeWindow.startMillis.toString(),
          end_time_utc_millis: timeWindow.endMillis.toString(),
        })}`
      },
      transformResponse: (response: { content: ProcessedSrmFeature[] }) =>
        addSrmTimestampsAndSortAscending(response.content),
    }),
  }),
})

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const {
  useGetSsmWithinTimeWindowQuery,
  useGetSrmWithinTimeWindowQuery,
  endpoints: { getSsmWithinTimeWindow, getSrmWithinTimeWindow },
  util: intersectionMapApiUtil,
} = intersectionMapApiSlice

// Helper function to manually fetch SSM data
export const fetchSsmWithinTimeWindow = (queryParams: MAP_QUERY_PARAMS, dispatch: any) => {
  const intersectionId = queryParams.intersectionId
  const timeWindow = getTimeWindowFromQueryParams(queryParams)
  return dispatch(
    intersectionMapApiSlice.endpoints.getSsmWithinTimeWindow.initiate({
      intersectionId,
      timeWindow,
    })
  )
}

// Helper function to manually fetch SSM data
export const fetchSrmWithinTimeWindow = (queryParams: MAP_QUERY_PARAMS, dispatch: any) => {
  const intersectionId = queryParams.intersectionId
  const timeWindow = getTimeWindowFromQueryParams(queryParams)
  return dispatch(
    intersectionMapApiSlice.endpoints.getSrmWithinTimeWindow.initiate({
      intersectionId,
      timeWindow,
    })
  )
}

function upperBound<T>(messages: T[], target: number, getValue: (message: T) => number) {
  if (messages.length == 0) return 0
  // Finds the index of the first message with timestamp greater than target
  let lo = 0
  let hi = messages.length
  while (lo < hi) {
    const mid = (lo + hi) >> 1
    if (getValue(messages[mid]) <= target) lo = mid + 1
    else hi = mid
  }
  return lo
}

function lowerBound<T>(messages: T[], target: number, getValue: (message: T) => number, hi: number) {
  if (messages.length == 0) return 0
  let lo = 0
  while (lo < hi) {
    const mid = (lo + hi) >> 1 // integer divide by 2
    if (getValue(messages[mid]) < target) lo = mid + 1
    else hi = mid
  }
  return lo
}

export const getTimeWindowFromQueryParams = (queryParams: MAP_QUERY_PARAMS): TimeWindow => ({
  startMillis: queryParams.startDate.getTime(),
  endMillis: queryParams.endDate.getTime(),
})

export const getTimeWindowFromRenderInterval = (renderTimeInterval: number[]): TimeWindow => ({
  startMillis: renderTimeInterval[0] * 1000,
  endMillis: renderTimeInterval[1] * 1000,
})

export const filterSsms = (ssms: ProcessedSsm[], timeWindow: TimeWindow) => {
  const upper = upperBound(ssms, timeWindow.endMillis, (ssm) => ssm.timeStampEpochMillis)
  const lower = lowerBound(ssms, timeWindow.startMillis, (ssm) => ssm.timeStampEpochMillis, upper)
  return ssms.slice(lower, upper)
}

export const filterSrms = (srms: ProcessedSrmFeature[], timeWindow: TimeWindow) => {
  const upper = upperBound(srms, timeWindow.endMillis, (srm) => srm.properties.timeStampEpochMillis)
  const lower = lowerBound(srms, timeWindow.startMillis, (srm) => srm.properties.timeStampEpochMillis, upper)
  return srms.slice(lower, upper)
}

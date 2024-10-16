// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectOrganizationName, selectToken } from '../../generalSlices/userSlice'

const getQueryString = (query_params: Record<string, string>) => {
  // filter out undefined values from query params
  const filteredQueryParams: Record<string, string> = { ...query_params }
  Object.keys(filteredQueryParams).forEach((key) => query_params[key] === undefined && delete query_params[key])
  const queryString = new URLSearchParams(query_params).toString()
  return `${queryString ? `?${queryString}` : ''}`
}

// Define a service using a base URL and expected endpoints
export const intersectionConfigParamApiSlice = createApi({
  reducerPath: 'intersectionConfigParamApi',
  baseQuery: fetchBaseQuery({
    baseUrl: EnvironmentVars.CVIZ_API_SERVER_URL,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const token = selectToken(getState() as RootState)

      // Specify endpoints that do not require a token or organization. These names must match the keys in the endpoints object below.
      const endpointsWithoutToken = []

      if (token && !endpointsWithoutToken.includes(endpoint)) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  tagTypes: ['defaultCongifs', 'intersectionConfigs'],
  endpoints: (builder) => ({
    getGeneralParameters: builder.query<Config[], undefined>({
      query: () => {
        return `config/default/all`
      },
      providesTags: ['defaultCongifs'],
    }),
    getIntersectionParameters: builder.query<IntersectionConfig[], number>({
      query: (intersectionId) => {
        return `config/intersection/unique${getQueryString({
          intersection_id: intersectionId.toString(),
          road_regulator_id: '-1',
        })}`
      },
      providesTags: ['intersectionConfigs'],
    }),
    updateDefaultParameter: builder.mutation<Config | undefined, Config>({
      query: (body) => ({
        url: 'config/default',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
      }),
      transformResponse: (response: any, meta: any) => response as Config,
      invalidatesTags: ['defaultCongifs', 'intersectionConfigs'],
    }),
    updateIntersectionParameter: builder.mutation<IntersectionConfig | undefined, Config>({
      query: (body) => ({
        url: 'config/intersection',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
      }),
      transformResponse: (response: any, meta: any) => response as IntersectionConfig,
      invalidatesTags: ['intersectionConfigs'],
    }),
    removeOverriddenParameter: builder.mutation<
      IntersectionConfig | undefined,
      { name: string; config: IntersectionConfig }
    >({
      query: ({ name, config }) => ({
        url: `config/intersection/create/${name}`,
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: config,
      }),
      transformResponse: (response: any, meta: any) => response as IntersectionConfig,
      invalidatesTags: ['intersectionConfigs'],
    }),
  }),
})

export const filterParameter = (
  key: string,
  intersectionParameters: IntersectionConfig[],
  generalParameters: Config[]
): Config | undefined =>
  intersectionParameters?.find(
    (p) => p.key === key && p.intersectionID !== null && p.intersectionID !== 0 && p.intersectionID !== -1
  ) ?? generalParameters?.find((p) => p.key === key)

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const {
  useGetGeneralParametersQuery,
  useGetIntersectionParametersQuery,
  useUpdateDefaultParameterMutation,
  useUpdateIntersectionParameterMutation,
  useRemoveOverriddenParameterMutation,

  useLazyGetGeneralParametersQuery,
  useLazyGetIntersectionParametersQuery,
} = intersectionConfigParamApiSlice

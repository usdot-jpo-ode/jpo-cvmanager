// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { combineUrlPaths } from '../../apis/intersections/api-helper-cviz'
import { EmailSubscription, EmailUnsubscribeGetResponse } from '../../models/email-subscriptions'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'

// Define a service using a base URL and expected endpoints
export const subscriptionManagementApiSlice = createApi({
  reducerPath: 'subscriptionManagementApiSlice',
  baseQuery: fetchBaseQuery({
    baseUrl: combineUrlPaths(EnvironmentVars.CVIZ_API_SERVER_URL, '/users/subscriptions'),
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
  tagTypes: ['subscriptions'],
  endpoints: (builder) => ({
    getEmailSubscriptions: builder.query<EmailUnsubscribeGetResponse, void>({
      query: () => {
        return '/email-subscriptions'
      },
      providesTags: ['subscriptions'],
      transformResponse: (response: any) => response as EmailUnsubscribeGetResponse,
    }),
    updateEmailSubscriptions: builder.mutation<null, EmailSubscription[]>({
      query: (subscriptions) => ({
        url: '/email-subscriptions',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: subscriptions,
      }),
      invalidatesTags: ['subscriptions'],
    }),
  }),
})

export const { useGetEmailSubscriptionsQuery, useUpdateEmailSubscriptionsMutation } = subscriptionManagementApiSlice

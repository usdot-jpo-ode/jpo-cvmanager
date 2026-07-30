// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { combineUrlPaths } from '../../apis/intersections/api-helper-cviz'
import { EmailSubscription, EmailUnsubscribeGetResponse } from '../../models/email-subscriptions'

// Define a service using a base URL and expected endpoints
export const unsubscribeApiSlice = createApi({
  reducerPath: 'unsubscribeApiSlice',
  baseQuery: fetchBaseQuery({
    baseUrl: combineUrlPaths(EnvironmentVars.CVIZ_API_SERVER_URL, '/users/unsubscribe'),
  }),
  tagTypes: ['subscriptions'],
  endpoints: (builder) => ({
    getEmailSubscriptions: builder.query<EmailUnsubscribeGetResponse, string>({
      query: (token) => ({
        url: '/email-subscriptions',
        headers: {
          Authorization: token,
        },
      }),
      providesTags: ['subscriptions'],
      transformResponse: (response: any) => response as EmailUnsubscribeGetResponse,
    }),
    updateEmailSubscriptions: builder.mutation<null, { token: string; subscriptions: EmailSubscription[] }>({
      query: ({ token, subscriptions }) => ({
        url: '/email-subscriptions',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token,
        },
        body: subscriptions,
      }),
      invalidatesTags: ['subscriptions'],
    }),
  }),
})

export const { useGetEmailSubscriptionsQuery, useUpdateEmailSubscriptionsMutation } = unsubscribeApiSlice

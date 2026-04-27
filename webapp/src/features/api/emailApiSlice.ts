import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
import { selectToken } from '../../generalSlices/userSlice'

export const emailApiSlice = createApi({
  reducerPath: 'emailApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${EnvironmentVars.CVIZ_API_SERVER_URL}/emails`,
    prepareHeaders: (headers, { getState, endpoint }) => {
      const currentState = getState() as RootState
      const token = selectToken(currentState)

      headers.set('Accept', 'application/json')

      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }

      return headers
    },
  }),
  endpoints: (builder) => ({
    sendContactSupportEmail: builder.mutation<EmailApiResponse, SupportRequestEmailContents>({
      query: (emailContents) => {
        return {
          url: '/support-requests',
          method: 'POST',
          body: emailContents,
        }
      },
    }),
    sendRsuErrorSummaryEmail: builder.mutation<EmailApiResponse, RsuErrorSummaryEmailContents>({
      query: (emailContents) => {
        return {
          url: '/rsu-errors',
          method: 'POST',
          body: emailContents,
        }
      },
    }),
  }),
})

export const { useSendContactSupportEmailMutation, useSendRsuErrorSummaryEmailMutation } = emailApiSlice

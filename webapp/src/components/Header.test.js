import React from 'react'
import { render } from '@testing-library/react'
import Header from './Header'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import EnvironmentVars from '../EnvironmentVars'
import { GoogleOAuthProvider } from '@react-oauth/google'
import { replaceChaoticIds } from '../utils/test-utils'
import ContactSupportMenu from './ContactSupportMenu'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider
      store={setupStore({
        user: {
          value: {
            loginFailure: true,
            atuhLoginData: {
              data: {
                organizations: [{ name: 'org1', role: 'role1' }],
              },
            },
          },
        },
      })}
    >
      <GoogleOAuthProvider clientId={EnvironmentVars.GOOGLE_CLIENT_ID}>
        <Header />
      </GoogleOAuthProvider>

      <br />
      <ContactSupportMenu />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

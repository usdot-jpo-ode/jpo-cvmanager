import React from 'react'
import { render } from '@testing-library/react'
import Header from './Header'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import EnvironmentVars from '../EnvironmentVars'
import { useKeycloak } from '@react-keycloak/web'
import { replaceChaoticIds } from '../utils/test-utils'
import ContactSupportMenu from './ContactSupportMenu'

jest.mock('@react-keycloak/web')

const mockKeycloak = {
  authenticated: true,
  login: jest.fn(),
  logout: jest.fn(),
  register: jest.fn(),
  accountManagement: jest.fn(),
  loadUserProfile: jest.fn(),
}

describe('<Header />', () => {
  beforeEach(() => {
    useKeycloak.mockReturnValue([mockKeycloak])
  })

  afterEach(() => {
    jest.clearAllMocks()
  })

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
        <Header />
        <br />
        <ContactSupportMenu />
      </Provider>
    )

    expect(replaceChaoticIds(container)).toMatchSnapshot()
  })
})

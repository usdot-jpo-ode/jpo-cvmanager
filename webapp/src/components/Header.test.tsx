import React from 'react'
import { render } from '@testing-library/react'
import Header from './Header'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
import { setupStore } from '../store'
import { useKeycloak } from '@react-keycloak/web'
import { replaceChaoticIds } from '../utils/test-utils'
import ContactSupportMenu from './ContactSupportMenu'

import { vi } from 'vitest'

vi.mock('@react-keycloak/web')

const mockKeycloak = {
  authenticated: true,
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
  accountManagement: vi.fn(),
  loadUserProfile: vi.fn(),
}

describe('<Header />', () => {
  beforeEach(() => {
    ;(useKeycloak as any).mockReturnValue({ keycloak: mockKeycloak })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('should take a snapshot', () => {
    const { container } = render(
      <ThemeProvider theme={testTheme}>
        <Provider
          store={setupStore({
            user: {
              value: {
                loginFailure: true,
                loginMessage: 'User Unauthorized',
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
      </ThemeProvider>
    )

    expect(replaceChaoticIds(container)).toMatchSnapshot()
  })
})

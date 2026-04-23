import '@testing-library/jest-dom'
import { render, screen } from '@testing-library/react'
import RsuFirmwareMenu from './RsuFirmwareMenu'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'

const configWithMsg = (firmwareUpgradeMsg: string, firmwareUpgradeErr: boolean) => ({
  config: {
    value: {
      firmwareUpgradeAvailable: false,
      firmwareUpgradeName: '',
      firmwareUpgradeMsg,
      firmwareUpgradeErr,
    },
  },
})

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <RsuFirmwareMenu type={''} rsuIpList={[]} />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

describe('RsuFirmwareMenu message block (single_rsu type)', () => {
  it('renders role="status" when firmwareUpgradeMsg is a success string', () => {
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(configWithMsg('Upgrade submitted successfully.', false))}>
          <RsuFirmwareMenu type="single_rsu" rsuIpList={['10.0.0.1']} />
        </Provider>
      </ThemeProvider>
    )

    const statusEl = screen.getByRole('status')
    expect(statusEl).toBeInTheDocument()
    expect(statusEl).toHaveTextContent('Upgrade submitted successfully.')
  })

  it('renders role="alert" when firmwareUpgradeErr is true', () => {
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(configWithMsg('Upgrade failed: timeout', true))}>
          <RsuFirmwareMenu type="single_rsu" rsuIpList={['10.0.0.1']} />
        </Provider>
      </ThemeProvider>
    )

    const alertEl = screen.getByRole('alert')
    expect(alertEl).toBeInTheDocument()
    expect(alertEl).toHaveTextContent('Upgrade failed: timeout')
  })

  it('does not render the message block when firmwareUpgradeMsg is empty string', () => {
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(configWithMsg('', false))}>
          <RsuFirmwareMenu type="single_rsu" rsuIpList={['10.0.0.1']} />
        </Provider>
      </ThemeProvider>
    )

    expect(screen.queryByRole('status')).not.toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })
})

describe('RsuFirmwareMenu message block (multi_rsu type)', () => {
  it('renders role="status" when firmwareUpgradeMsg is a success string', () => {
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(configWithMsg('Batch upgrade submitted.', false))}>
          <RsuFirmwareMenu type="multi_rsu" rsuIpList={['10.0.0.1', '10.0.0.2']} />
        </Provider>
      </ThemeProvider>
    )

    const statusEl = screen.getByRole('status')
    expect(statusEl).toBeInTheDocument()
    expect(statusEl).toHaveTextContent('Batch upgrade submitted.')
  })

  it('renders role="alert" when firmwareUpgradeErr is true', () => {
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(configWithMsg('Batch upgrade failed.', true))}>
          <RsuFirmwareMenu type="multi_rsu" rsuIpList={['10.0.0.1', '10.0.0.2']} />
        </Provider>
      </ThemeProvider>
    )

    const alertEl = screen.getByRole('alert')
    expect(alertEl).toHaveTextContent('Batch upgrade failed.')
  })

  it('does not render the message block when firmwareUpgradeMsg is empty string', () => {
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(configWithMsg('', false))}>
          <RsuFirmwareMenu type="multi_rsu" rsuIpList={['10.0.0.1']} />
        </Provider>
      </ThemeProvider>
    )

    expect(screen.queryByRole('status')).not.toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })
})

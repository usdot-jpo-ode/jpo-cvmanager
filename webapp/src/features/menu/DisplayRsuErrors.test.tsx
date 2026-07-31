import '@testing-library/jest-dom'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import DisplayRsuErrors from './DisplayRsuErrors'
import { RsuInfo, RsuProperties } from '../../models/RsuApi'
import { replaceChaoticIds } from '../../utils/test-utils'
import RsuApi from '../../apis/rsu-api'

vi.mock('../api/scmsApiSlice', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api/scmsApiSlice')>()
  return {
    ...actual,
    useGetScmsStatusQuery: vi.fn(),
  }
})

import { useGetScmsStatusQuery } from '../api/scmsApiSlice'
const mockUseGetScmsStatusQuery = vi.mocked(useGetScmsStatusQuery)

const TEST_IP = '10.0.0.1'
const VALID_EXPIRATION_ISO = '2026-12-31T23:59:59Z'

const testRsu: RsuInfo = {
  id: 1,
  type: 'Feature',
  geometry: { type: 'Point', coordinates: [-104.9903, 39.7392] },
  properties: {
    rsu_id: 1,
    milepost: 1,
    geography: 'POINT (39.7392 -104.9903)',
    model_name: 'model',
    ipv4_address: TEST_IP,
    primary_route: 'I-25',
    serial_number: 'SN001',
    manufacturer_name: 'Acme',
    tim_deposit: false,
  } as RsuProperties,
}

const rsuPreloadedState = {
  rsu: {
    value: {
      selectedRsu: null,
      rsuData: [testRsu],
      rsuOnlineStatus: {
        [TEST_IP]: { current_status: 'online', last_online: '2026-04-10T10:00:00Z' },
      },
      geoMsgType: 'BSM',
      rsuMapData: {},
      mapList: [],
      mapDate: '',
      displayMap: false,
      geoMsgStart: '',
      geoMsgEnd: '',
      addGeoMsgPoint: false,
      geoMsgCoordinates: [],
      geoMsgData: [],
      geoMsgDateError: false,
      geoMsgFilter: false,
      geoMsgFilterStep: 60,
      geoMsgFilterOffset: 0,
      ssmDisplay: false,
      srmSsmList: [],
      selectedSrm: [],
    },
  },
  user: {
    value: {
      authLoginData: { token: 'test-token' },
      organization: { organization: 'test-org', role: 'admin' },
    },
  },
}

const mockRsuOnline = () =>
  vi.spyOn(RsuApi, 'getRsuOnline').mockResolvedValue({
    ip: TEST_IP,
    current_status: 'online',
    last_online: '2026-04-10T10:00:00Z',
  } as any)

function renderComponent(preloadedState = rsuPreloadedState) {
  return render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore(preloadedState)}>
        <DisplayRsuErrors />
      </Provider>
    </ThemeProvider>
  )
}

describe('DisplayRsuErrors table view (no RSU selected)', () => {
  beforeEach(() => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    mockUseGetScmsStatusQuery.mockClear()
    mockRsuOnline()
  })

  it('shows Healthy status when health === true', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({
      data: { [TEST_IP]: { health: true, expiration: VALID_EXPIRATION_ISO } },
    } as any)

    renderComponent()

    expect(screen.getAllByText('Healthy').length).toBeGreaterThan(0)
  })

  it('shows Unhealthy status when health === false', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({
      data: { [TEST_IP]: { health: false, expiration: '2025-01-01T00:00:00Z' } },
    } as any)

    renderComponent()

    expect(screen.getAllByText('Unhealthy').length).toBeGreaterThan(0)
  })

  it('shows Unhealthy status when health === null', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({
      data: { [TEST_IP]: null },
    } as any)

    renderComponent()

    expect(screen.getAllByText('Unhealthy').length).toBeGreaterThan(0)
  })

  it('shows Never downloaded certificates when expiration is absent (no SCMS record)', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({ data: {} } as any)

    renderComponent()

    expect(screen.getAllByText('Never downloaded certificates').length).toBeGreaterThan(0)
  })

  it('calls useGetScmsStatusQuery with skip:true when no organization is selected', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({ data: {} } as any)

    const stateWithoutOrg = {
      ...rsuPreloadedState,
      user: {
        value: {
          authLoginData: { token: 'test-token' },
          organization: { organization: '', role: 'admin' },
        },
      },
    }
    renderComponent(stateWithoutOrg)

    // The hook is called with skip: !organization — empty string → skip: true
    const lastCallArgs = mockUseGetScmsStatusQuery.mock.calls.at(-1)!
    expect(lastCallArgs[1]).toMatchObject({ skip: true })
  })
})

describe('DisplayRsuErrors detail view (RSU selected)', () => {
  beforeEach(() => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    mockUseGetScmsStatusQuery.mockClear()
    mockRsuOnline()
  })

  const renderDetail = (overrideState = rsuPreloadedState) =>
    render(
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore(overrideState)}>
          <DisplayRsuErrors initialSelectedRsu={testRsu} />
        </Provider>
      </ThemeProvider>
    )

  it('renders Healthy in SCMS status accordion when health === true', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({
      data: { [TEST_IP]: { health: true, expiration: VALID_EXPIRATION_ISO } },
    } as any)

    renderDetail()

    expect(screen.getAllByText('Healthy').length).toBeGreaterThan(0)
  })

  it('renders Unhealthy in SCMS status accordion when health === false', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({
      data: { [TEST_IP]: { health: false, expiration: '2025-01-01T00:00:00Z' } },
    } as any)

    renderDetail()

    expect(screen.getAllByText('Unhealthy').length).toBeGreaterThan(0)
  })

  it('renders Never downloaded certificates when no SCMS record exists', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({ data: {} } as any)

    renderDetail()

    expect(screen.getAllByText('Never downloaded certificates').length).toBeGreaterThan(0)
  })
})

describe('DisplayRsuErrors snapshot', () => {
  beforeEach(() => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-04-20T00:00:00Z'))
    mockUseGetScmsStatusQuery.mockClear()
    mockRsuOnline()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('should match snapshot', () => {
    mockUseGetScmsStatusQuery.mockReturnValue({
      data: { [TEST_IP]: { health: true, expiration: VALID_EXPIRATION_ISO } },
    } as any)

    const { container } = renderComponent()
    expect(replaceChaoticIds(container)).toMatchSnapshot()
  })
})

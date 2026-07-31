import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { vi } from 'vitest'
import fetchMock from 'jest-fetch-mock'
import toast from 'react-hot-toast'
import AdminOrganizationTabIntersection from './AdminOrganizationTabIntersection'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import EnvironmentVars from '../../EnvironmentVars'
import { AdminOrgIntersection } from '../adminOrganizationTab/adminOrganizationTabSlice'

vi.mock('react-confirm-alert', () => ({
  confirmAlert: (options: { buttons: { label: string; onClick: () => void }[] }) =>
    options.buttons[0].onClick(),
}))

vi.mock('../../components/AdminTable', () => ({
  default: ({ actions, data }: any) => (
    <div>
      {actions.map((action: any, i: number) => (
        <div key={i}>
          {typeof action.icon === 'function' && action.icon()}
          <button
            data-testid={`action-${action.position}-${i}`}
            onClick={() => action.onClick(null, action.position === 'row' ? data[0] : data)}
          >
            {`action-${action.position}-${i}`}
          </button>
        </div>
      ))}
    </div>
  ),
}))

vi.mock('react-widgets/cjs', () => ({
  Multiselect: ({ data, value, onChange, placeholder }: any) => (
    <div>
      <div data-testid="multiselect-placeholder">{placeholder}</div>
      <button
        data-testid="multiselect-add-all"
        onClick={() => onChange([...(data ?? [])])}
      >
        add-all
      </button>
      <button data-testid="multiselect-clear" onClick={() => onChange([])}>
        clear
      </button>
      <div data-testid="multiselect-value">
        {(value ?? []).map((item: any) => item.intersection_id).join(',')}
      </div>
    </div>
  ),
}))

const ADMIN_INT_URL = `${EnvironmentVars.CVIZ_API_SERVER_URL}/admin/intersections`

const preloadedAuth = {
  user: {
    loading: false,
    value: {
      authLoginData: { token: 'test-token' },
      organization: { name: 'selectedOrg', role: 'admin' },
    },
  },
}

const row = (id: string, name = `Intersection ${id}`): AdminOrgIntersection => ({
  intersection_id: id,
  intersection_name: name,
  ref_pt: { latitude: '0', longitude: '0' },
})

const renderWithRows = (tableData: AdminOrgIntersection[]) => {
  const updateTableData = vi.fn()
  const utils = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore(preloadedAuth)}>
        <AdminOrganizationTabIntersection
          selectedOrg="selectedOrg"
          selectedOrgEmail="email@test.com"
          tableData={tableData}
          updateTableData={updateTableData}
        />
      </Provider>
    </ThemeProvider>
  )
  return { ...utils, updateTableData }
}

const getIntersectionBody = (organizations: string[]) =>
  JSON.stringify({
    intersection_data: { organizations },
    allowed_selections: {},
  })

const emptyAvailableBody = JSON.stringify({ intersection_data: [] })

const findPatchCall = () =>
  fetchMock.mock.calls.find(([, opts]: any) => opts?.method === 'PATCH')

const findRequestCalls = () =>
  fetchMock.mock.calls.filter(([input]: any) => {
    const url = typeof input === 'string' ? input : (input as Request).url
    return !url.endsWith('/admin/intersections/available')
  })

describe('AdminOrganizationTabIntersection — delete actions', () => {
  beforeEach(() => {
    fetchMock.resetMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('single delete', () => {
    it('dispatches editOrg and shows success toast when intersection has multiple orgs', async () => {
      const toastSuccess = vi.spyOn(toast, 'success').mockReturnValue('id' as any)
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return emptyAvailableBody
        }
        if (req.method === 'GET') {
          return getIntersectionBody(['org1', 'org2'])
        }
        return JSON.stringify({ message: 'ok' })
      })

      const { updateTableData } = renderWithRows([row('1')])

      fireEvent.click(screen.getByTestId('action-row-0'))

      await waitFor(() => expect(findRequestCalls().length).toBeGreaterThanOrEqual(2))

      const [getCall, ...rest] = findRequestCalls()
      const getReq = getCall[0] as Request
      expect(getReq.url).toBe(`${ADMIN_INT_URL}/1`)
      expect(getReq.method).toBe('GET')
      expect(rest.length).toBeGreaterThan(0)

      const patchCall = findPatchCall()
      expect(patchCall).toBeDefined()
      const body = JSON.parse((patchCall![1] as any).body)
      expect(body).toMatchObject({
        name: 'selectedOrg',
        email: 'email@test.com',
        intersections_to_remove: ['1'],
      })

      await waitFor(() => expect(updateTableData).toHaveBeenCalledWith('selectedOrg'))
      await waitFor(() =>
        expect(toastSuccess).toHaveBeenCalledWith('Intersection deleted successfully')
      )
    })

    it('alerts and skips editOrg when intersection belongs to only one org', async () => {
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return emptyAvailableBody
        }
        return getIntersectionBody(['only-org'])
      })

      const { updateTableData } = renderWithRows([row('1')])

      fireEvent.click(screen.getByTestId('action-row-0'))

      await waitFor(() =>
        expect(alertSpy).toHaveBeenCalledWith(
          'Cannot remove Intersection 1 from selectedOrg because it must belong to at least one organization.'
        )
      )
      expect(findPatchCall()).toBeUndefined()
      expect(updateTableData).not.toHaveBeenCalled()
    })

    it('shows error toast when editOrg fails', async () => {
      const toastError = vi.spyOn(toast, 'error').mockReturnValue('id' as any)
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return emptyAvailableBody
        }
        if (req.method === 'GET') {
          return getIntersectionBody(['org1', 'org2'])
        }
        return { status: 500, body: JSON.stringify({ message: 'nope' }) }
      })

      renderWithRows([row('1')])

      fireEvent.click(screen.getByTestId('action-row-0'))

      await waitFor(() =>
        expect(toastError).toHaveBeenCalledWith('Failed to delete Intersection')
      )
    })
  })

  describe('multi delete', () => {
    it('dispatches editOrg with all ids when all intersections have multiple orgs', async () => {
      const toastSuccess = vi.spyOn(toast, 'success').mockReturnValue('id' as any)
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return emptyAvailableBody
        }
        if (req.method === 'GET') {
          return getIntersectionBody(['org1', 'org2'])
        }
        return JSON.stringify({ message: 'ok' })
      })

      const { updateTableData } = renderWithRows([row('1'), row('2'), row('3')])

      fireEvent.click(screen.getByTestId('action-toolbarOnSelect-1'))

      await waitFor(() =>
        expect(toastSuccess).toHaveBeenCalledWith('Intersection(s) deleted successfully')
      )

      const patchCall = findPatchCall()
      expect(patchCall).toBeDefined()
      const body = JSON.parse((patchCall![1] as any).body)
      expect(body.intersections_to_remove).toEqual(['1', '2', '3'])
      expect(body.name).toBe('selectedOrg')
      expect(body.email).toBe('email@test.com')
      expect(updateTableData).toHaveBeenCalledWith('selectedOrg')
    })

    it('alerts with invalid ids and skips editOrg when any intersection has only one org', async () => {
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return emptyAvailableBody
        }
        if (req.method !== 'GET') {
          return JSON.stringify({ message: 'ok' })
        }
        if (req.url.endsWith('/1')) {
          return getIntersectionBody(['org1', 'org2'])
        }
        return getIntersectionBody(['only-org'])
      })

      const { updateTableData } = renderWithRows([row('1'), row('2'), row('3')])

      fireEvent.click(screen.getByTestId('action-toolbarOnSelect-1'))

      await waitFor(() =>
        expect(alertSpy).toHaveBeenCalledWith(
          'Cannot remove Intersection(s) 2, 3 from selectedOrg because they must belong to at least one organization.'
        )
      )
      expect(findPatchCall()).toBeUndefined()
      expect(updateTableData).not.toHaveBeenCalled()
    })
  })

  describe('multi add', () => {
    const availableBody = (ids: string[]) =>
      JSON.stringify({
        intersection_data: ids.map((id) => ({
          intersection_id: id,
          intersection_name: `Intersection ${id}`,
          ref_pt: { latitude: '0', longitude: '0' },
          organizations: [],
          rsus: [],
        })),
      })

    // The Add button is the 4th action (index 3) in the intersectionActions array:
    // 0: row delete, 1: toolbarOnSelect multi-delete, 2: toolbar Multiselect, 3: toolbar Add button.
    const ADD_BUTTON_TESTID = 'action-toolbar-3'

    it('shows error toast when no intersections selected', async () => {
      const toastError = vi.spyOn(toast, 'error').mockReturnValue('id' as any)
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return availableBody(['10', '20'])
        }
        return JSON.stringify({ message: 'ok' })
      })

      renderWithRows([])

      fireEvent.click(screen.getByTestId(ADD_BUTTON_TESTID))

      await waitFor(() =>
        expect(toastError).toHaveBeenCalledWith('Please select Intersections to add')
      )
      expect(findPatchCall()).toBeUndefined()
    })

    it('dispatches editOrg with selected intersection ids and shows success toast', async () => {
      const toastSuccess = vi.spyOn(toast, 'success').mockReturnValue('id' as any)
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return availableBody(['10', '20'])
        }
        return JSON.stringify({ message: 'ok' })
      })

      const { updateTableData } = renderWithRows([])

      await waitFor(() => expect(screen.getByTestId('multiselect-add-all')).toBeTruthy())

      fireEvent.click(screen.getByTestId('multiselect-add-all'))

      await waitFor(() =>
        expect(screen.getByTestId('multiselect-value').textContent).toBe('10,20')
      )

      fireEvent.click(screen.getByTestId(ADD_BUTTON_TESTID))

      await waitFor(() => expect(findPatchCall()).toBeDefined())
      const patchCall = findPatchCall()
      const body = JSON.parse((patchCall![1] as any).body)
      expect(body).toMatchObject({
        name: 'selectedOrg',
        email: 'email@test.com',
        intersections_to_add: ['10', '20'],
      })

      await waitFor(() => expect(updateTableData).toHaveBeenCalledWith('selectedOrg'))
      await waitFor(() =>
        expect(toastSuccess).toHaveBeenCalledWith('Intersection(s) added successfully')
      )
      await waitFor(() =>
        expect(screen.getByTestId('multiselect-value').textContent).toBe('')
      )
    })

    it('shows error toast when editOrg fails', async () => {
      const toastError = vi.spyOn(toast, 'error').mockReturnValue('id' as any)
      fetchMock.mockResponse(async (req) => {
        if (req.url.endsWith('/admin/intersections/available')) {
          return availableBody(['10'])
        }
        return { status: 500, body: JSON.stringify({ message: 'nope' }) }
      })

      renderWithRows([])

      await waitFor(() => expect(screen.getByTestId('multiselect-add-all')).toBeTruthy())

      fireEvent.click(screen.getByTestId('multiselect-add-all'))
      await waitFor(() =>
        expect(screen.getByTestId('multiselect-value').textContent).toBe('10')
      )

      fireEvent.click(screen.getByTestId(ADD_BUTTON_TESTID))

      await waitFor(() =>
        expect(toastError).toHaveBeenCalledWith('Failed to add Intersection(s)')
      )
    })
  })
})

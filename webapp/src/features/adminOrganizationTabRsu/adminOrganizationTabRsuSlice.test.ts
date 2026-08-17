import reducer from './adminOrganizationTabRsuSlice'
import {
  // async thunks
  rsuDeleteSingle,
  rsuDeleteMultiple,
  rsuAddMultiple,
  refresh,

  // reducers
  setSelectedRsuList,

  // selectors
  selectLoading,
  selectSelectedRsuList,
} from './adminOrganizationTabRsuSlice'
import { RootState } from '../../store'
import { AdminOrgRsuWithId } from './AdminOrganizationTabRsuTypes'

// Mock the organizationApiSlice
const mockInitiate = jest.fn()
const mockInvalidateTags = jest.fn()

jest.mock('../api/organizationApiSlice', () => ({
  organizationApiSlice: {
    endpoints: {
      getRsuOrganizations: {
        initiate: mockInitiate,
      },
    },
    util: {
      invalidateTags: mockInvalidateTags,
    },
  },
}))

describe('admin organization tab RSU reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        selectedRsuList: [],
      },
    })
  })
})

describe('async thunks', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('rsuDeleteSingle', () => {
    it('returns and calls the api correctly when RSU has multiple organizations', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        organizationApi: {},
      })
      const rsu = { ip: '1.1.1.1' } as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValue({
        data: ['org1', 'org2'],
        payload: { success: true },
      })

      const action = rsuDeleteSingle({ rsu, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        const result = await action(dispatch, getState, undefined)

        expect(dispatch).toHaveBeenCalledTimes(2 + 4)
        expect(window.alert).not.toHaveBeenCalled()
        expect(result.payload).toEqual({ success: true, message: 'RSU deleted successfully' })
      } finally {
        window.alert = jsdomAlert
      }
    })

    it('shows alert when RSU has only one organization', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        organizationApi: {},
      })
      const rsu = { ip: '1.1.1.1' } as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValue({
        data: ['org1'],
      })

      const action = rsuDeleteSingle({ rsu, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        await action(dispatch, getState, undefined)

        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove RSU 1.1.1.1 from selectedOrg because it must belong to at least one organization.'
        )
      } finally {
        window.alert = jsdomAlert
      }
    })

    it('handles undefined or empty organization data', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        organizationApi: {},
      })
      const rsu = { ip: '1.1.1.1' } as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValue({
        data: undefined,
      })

      const action = rsuDeleteSingle({ rsu, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        await action(dispatch, getState, undefined)

        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove RSU 1.1.1.1 from selectedOrg because it must belong to at least one organization.'
        )
      } finally {
        window.alert = jsdomAlert
      }
    })
  })

  describe('rsuDeleteMultiple', () => {
    it('returns and calls the api correctly when all RSUs have multiple organizations', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        organizationApi: {},
      })
      const rows = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }, { ip: '1.1.1.3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValueOnce(undefined) // First call
      dispatch.mockResolvedValueOnce({ data: ['org1', 'org2', 'org3'], unsubscribe: jest.fn() })
      dispatch.mockResolvedValueOnce({ data: ['org1', 'org2'], unsubscribe: jest.fn() })
      dispatch.mockResolvedValueOnce({ data: ['org1', 'org2'], unsubscribe: jest.fn() })
      dispatch.mockResolvedValueOnce({ payload: { success: true } })

      const action = rsuDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        const result = await action(dispatch, getState, undefined)

        expect(dispatch).toHaveBeenCalledTimes(2 + 6)
        expect(window.alert).not.toHaveBeenCalled()
        expect(result.payload).toEqual({ success: true, message: 'RSU(s) deleted successfully' })
      } finally {
        window.alert = jsdomAlert
      }
    })

    it('shows alert when some RSUs have only one organization', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rows = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }, { ip: '1.1.1.3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValueOnce(undefined) // First call
      dispatch.mockResolvedValueOnce({ data: ['org1', 'org2'], unsubscribe: jest.fn() })
      dispatch.mockResolvedValueOnce({ data: ['org1'], unsubscribe: jest.fn() })
      dispatch.mockResolvedValueOnce({ data: ['org1'], unsubscribe: jest.fn() })

      const action = rsuDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        await action(dispatch, getState, undefined)

        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove RSU(s) 1.1.1.2, 1.1.1.3 from selectedOrg because they must belong to at least one organization.'
        )
      } finally {
        window.alert = jsdomAlert
      }
    })

    it('handles mixed valid and invalid RSUs', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rows = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValueOnce(undefined) // First call
      dispatch.mockResolvedValueOnce({ data: ['org1', 'org2'], unsubscribe: jest.fn() })
      dispatch.mockResolvedValueOnce({ data: ['org1'], unsubscribe: jest.fn() })

      const action = rsuDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        await action(dispatch, getState, undefined)

        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove RSU(s) 1.1.1.2 from selectedOrg because they must belong to at least one organization.'
        )
      } finally {
        window.alert = jsdomAlert
      }
    })
  })

  describe('rsuAddMultiple', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rsuList = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }, { ip: '1.1.1.3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockImplementation((action: any) => {
        if (typeof action === 'function') {
          return action(dispatch, getState, undefined)
        }
        return Promise.resolve({ payload: { success: true } })
      })

      const action = rsuAddMultiple({ rsuList, selectedOrg, selectedOrgEmail, updateTableData })

      await action(dispatch, getState, undefined)

      // Should dispatch editOrg and refresh
      expect(dispatch).toHaveBeenCalled()
    })

    it('handles successful addition', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rsuList = [{ ip: '1.1.1.1' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValueOnce(undefined) // First call
      dispatch.mockResolvedValueOnce({ payload: { success: true } })

      const action = rsuAddMultiple({ rsuList, selectedOrg, selectedOrgEmail, updateTableData })
      const result = await action(dispatch, getState, undefined)

      expect(result.payload).toEqual({
        success: true,
        message: 'RSU(s) added successfully',
      })
    })

    it('handles failed addition', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rsuList = [{ ip: '1.1.1.1' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      dispatch.mockResolvedValueOnce(undefined) // First call
      dispatch.mockResolvedValueOnce({ payload: { success: false } })

      const action = rsuAddMultiple({ rsuList, selectedOrg, selectedOrgEmail, updateTableData })
      const result = await action(dispatch, getState, undefined)

      expect(result.payload).toEqual({
        success: false,
        message: 'Failed to add RSU(s)',
      })
    })
  })

  describe('refresh', () => {
    it('returns and calls the update function correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const selectedOrg = 'selectedOrg'
      const updateTableData = jest.fn()

      const action = refresh({ selectedOrg, updateTableData })

      await action(dispatch, getState, undefined)

      expect(updateTableData).toHaveBeenCalledTimes(1)
      expect(updateTableData).toHaveBeenCalledWith(selectedOrg)
    })

    it('clears selectedRsuList in extraReducers', () => {
      const stateWithSelectedRsus = {
        loading: false,
        value: {
          selectedRsuList: [{ id: 1, ip: '1.1.1.1' } as AdminOrgRsuWithId],
        },
      }

      const newState = reducer(stateWithSelectedRsus, {
        type: refresh.fulfilled.type,
      })

      expect(newState.value.selectedRsuList).toEqual([])
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminOrganizationTabRsu'] = {
    loading: null,
    value: {
      selectedRsuList: null,
    },
  }

  it('setSelectedRsuList reducer updates state correctly', async () => {
    const selectedRsuList = [{ id: 1, ip: '1.1.1.1' }] as any
    expect(reducer(initialState, setSelectedRsuList(selectedRsuList))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsuList },
    })
  })

  it('setSelectedRsuList with empty array', () => {
    const result = reducer(initialState, setSelectedRsuList([]))
    expect(result.value.selectedRsuList).toEqual([])
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      selectedRsuList: 'selectedRsuList',
    },
  }
  const state = { adminOrganizationTabRsu: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectSelectedRsuList(state)).toEqual('selectedRsuList')
  })

  it('selectLoading returns false when loading is false', () => {
    const state = {
      adminOrganizationTabRsu: {
        loading: false,
        value: { selectedRsuList: [] },
      },
    } as any

    expect(selectLoading(state)).toBe(false)
  })

  it('selectSelectedRsuList returns empty array when no RSUs selected', () => {
    const state = {
      adminOrganizationTabRsu: {
        loading: false,
        value: { selectedRsuList: [] },
      },
    } as any

    expect(selectSelectedRsuList(state)).toEqual([])
  })
})

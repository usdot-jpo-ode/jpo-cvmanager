import reducer from './adminEditOrganizationSlice'
import {
  // async thunks
  editOrganization,

  // functions
  updateStates,

  // reducers
  setSuccessMsg,

  // selectors
  selectSuccessMsg,
  selectLoading,
} from './adminEditOrganizationSlice'
import { RootState } from '../../store'

describe('admin add User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminEditOrganization'] = {
    loading: null,
    value: {
      successMsg: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('editOrganization', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest
        .fn()
        .mockReturnValue(
          new Promise((resolve) =>
            resolve({ payload: { success: true, message: 'Changes were successfully applied!' } })
          )
        )
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminOrganizationTab: {
          value: {
            selectedOrg: {
              name: 'prevSelectedOrg',
            },
          },
        },
      })
      const json = { name: 'orgName', email: 'name@email.com' }
      const selectedOrg = 'selectedOrg'
      let setValue = jest.fn()
      const action = editOrganization({ json, selectedOrg, setValue })

      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        const resp = await action(dispatch, getState, undefined)
        console.error(JSON.stringify(resp))
        expect(resp.payload).toEqual({ success: true, message: 'Changes were successfully applied!' })
        expect(global.setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(4 + 2)
        expect(setValue).toHaveBeenCalledTimes(3)
        expect(setValue).toHaveBeenCalledWith('orig_name', 'orgName')
        expect(setValue).toHaveBeenCalledWith('name', 'orgName')
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }

      dispatch = jest
        .fn()
        .mockReturnValue(new Promise((resolve) => resolve({ payload: { success: false, message: 'message' } })))
      setValue = jest.fn()
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        const resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(global.setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(setValue).not.toHaveBeenCalled()
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let successMsg = 'message'

      let state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/fulfilled',
        payload: { message: 'message', success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg },
      })

      // Error Case
      successMsg = ''

      state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })
})

describe('functions', () => {
  it('updateStates', async () => {
    const setValue = jest.fn()
    const selectedOrgName = 'selectedOrgName'
    const selectedOrgEmail = 'name@email.com'

    updateStates(setValue, selectedOrgName, selectedOrgEmail)

    expect(setValue).toHaveBeenCalledTimes(3)
    expect(setValue).toHaveBeenCalledWith('orig_name', selectedOrgName)
    expect(setValue).toHaveBeenCalledWith('name', selectedOrgName)
  })
})

describe('reducers', () => {
  const initialState: RootState['adminEditOrganization'] = {
    loading: null,
    value: {
      successMsg: null,
    },
  }

  it('setSuccessMsg reducer updates state correctly', async () => {
    const successMsg = 'successMsg'
    expect(reducer(initialState, setSuccessMsg(successMsg))).toEqual({
      ...initialState,
      value: { ...initialState.value, successMsg },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      successMsg: 'successMsg',
    },
  }
  const state = { adminEditOrganization: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectSuccessMsg(state)).toEqual('successMsg')
  })
})

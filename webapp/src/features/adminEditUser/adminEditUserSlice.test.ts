import reducer from './adminEditUserSlice'
import {
  // async thunks
  getUserData,
  editUser,
  submitForm,

  // functions
  organizationParser,

  // reducers
  updateOrganizations,
  setSuccessMsg,
  updateStates,
  setSelectedRole,

  // selectors
  selectLoading,
  selectSuccessMsg,
  selectSelectedOrganizationNames,
  selectSelectedOrganizations,
  selectOrganizationNames,
  selectAvailableRoles,
  selectApiData,
  selectErrorState,
  selectErrorMsg,
  selectSubmitAttempt,
} from './adminEditUserSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin edit User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        selectedOrganizationNames: [],
        selectedOrganizations: [],
        organizationNames: [],
        availableRoles: [],
        apiData: {},
        errorState: false,
        errorMsg: '',
        submitAttempt: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminEditUser'] = {
    loading: null,
    value: {
      successMsg: null,
      selectedOrganizationNames: null,
      selectedOrganizations: null,
      organizationNames: null,
      availableRoles: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      submitAttempt: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getUserData', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const user_email = 'test@gmail.com'
      const action = getUserData(user_email)

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'body' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminUser,
        token: 'token',
        query_params: { user_email },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminUser,
        token: 'token',
        query_params: { user_email },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditUser/getUserData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const apiData = 'apiData' as any
      let successMsg = 'message'
      let errorMsg = ''
      let errorState = false

      let state = reducer(
        { ...initialState, value: { ...initialState.value, apiData } },
        {
          type: 'adminEditUser/getUserData/fulfilled',
          payload: { message: 'message', success: true, data: apiData },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState, apiData },
      })

      // Error Case
      successMsg = ''
      errorMsg = 'message'
      errorState = true

      state = reducer(initialState, {
        type: 'adminEditUser/getUserData/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditUser/getUserData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('editUser', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { data: 'data' }
      let updateUserData = jest.fn()
      let action = editUser({ json, updateUserData })

      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: true, message: 'Changes were successfully applied!' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminUser,
          token: 'token',
          body: JSON.stringify(json),
        })
        expect(setTimeout).toHaveBeenCalledTimes(1)
        expect(updateUserData).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }

      dispatch = jest.fn()
      updateUserData = jest.fn()
      action = editUser({ json, updateUserData })
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminUser,
          token: 'token',
          body: JSON.stringify(json),
        })
        expect(setTimeout).not.toHaveBeenCalled()
        expect(updateUserData).not.toHaveBeenCalled()
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditUser/editUser/pending',
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
      let errorMsg = ''
      let errorState = false

      let state = reducer(
        { ...initialState, value: { ...initialState.value } },
        {
          type: 'adminEditUser/editUser/fulfilled',
          payload: { message: 'message', success: true },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })

      // Error Case
      successMsg = ''
      errorMsg = 'message'
      errorState = true

      state = reducer(initialState, {
        type: 'adminEditUser/editUser/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditUser/editUser/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('submitForm', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      let getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditUser: {
          value: {
            selectedOrganizations: [{ id: 0, name: 'org1' }],
            apiData: {
              user_data: {
                organizations: [
                  { role: 'role2', name: 'org1' },
                  { role: 'role3', name: 'org3' },
                ],
              },
            },
          },
        },
      })
      const data = { data: 'data' } as any
      let updateUserData = jest.fn()

      let action = submitForm({ data, updateUserData })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(false)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      // empty selectedOrganizations
      dispatch = jest.fn()
      getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditUser: {
          value: {
            selectedOrganizations: [],
          },
        },
      })
      action = submitForm({ data, updateUserData })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(true)
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly fulfilled', async () => {
      const submitAttempt = 'submitAttempt'

      const state = reducer(initialState, {
        type: 'adminEditUser/submitForm/fulfilled',
        payload: submitAttempt,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, submitAttempt },
      })
    })
  })
})

describe('functions', () => {
  it('organizationParser', async () => {
    const submitOrgs = [
      { role: 'role1', name: 'org1' },
      { role: 'role2', name: 'org2' },
    ]
    const apiData = {
      user_data: {
        organizations: [
          { role: 'role2', name: 'org1' },
          { role: 'role3', name: 'org3' },
        ],
      },
    } as any

    const expected = {
      organizations_to_add: [{ role: 'role2', name: 'org2' }],
      organizations_to_modify: [{ role: 'role1', name: 'org1' }],
      organizations_to_remove: [{ role: 'role3', name: 'org3' }],
    }

    expect(organizationParser({} as any, submitOrgs, apiData)).toEqual(expected)
  })
})

describe('reducers', () => {
  const initialState: RootState['adminEditUser'] = {
    loading: null,
    value: {
      successMsg: null,
      selectedOrganizationNames: null,
      selectedOrganizations: null,
      organizationNames: null,
      availableRoles: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      submitAttempt: null,
    },
  }

  it('updateOrganizations reducer updates state correctly', async () => {
    const availableRoles = [{ role: 'role1' }]
    const selectedOrganizations = [
      { id: 0, name: 'org1', role: 'role3' },
      { id: 1, name: 'org2', role: 'role4' },
    ]
    const payload = [
      { id: 0, name: 'org1' },
      { id: 0, name: 'org3' },
    ]
    expect(
      reducer(
        { ...initialState, value: { ...initialState.value, selectedOrganizations, availableRoles } },
        updateOrganizations(payload)
      )
    ).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        selectedOrganizations: [
          { id: 0, name: 'org1', role: 'role3' },
          { id: 0, name: 'org3', role: 'role1' },
        ],
        selectedOrganizationNames: payload,
        availableRoles,
      },
    })
  })

  it('setSuccessMsg reducer updates state correctly', async () => {
    const successMsg = 'successMsg'
    expect(reducer(initialState, setSuccessMsg(successMsg))).toEqual({
      ...initialState,
      value: { ...initialState.value, successMsg },
    })
  })

  it('setSelectedRole reducer updates state correctly', async () => {
    const selectedOrganizations = [
      { id: 0, name: 'org1', role: 'role3' },
      { id: 1, name: 'org2', role: 'role4' },
    ]
    const payload = { name: 'org1', role: 'role1' }
    expect(
      reducer({ ...initialState, value: { ...initialState.value, selectedOrganizations } }, setSelectedRole(payload))
    ).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        selectedOrganizations: [
          { id: 0, name: 'org1', role: 'role1' },
          { id: 1, name: 'org2', role: 'role4' },
        ],
      },
    })
  })

  it('updateStates', async () => {
    const apiData = {
      allowed_selections: {
        organizations: ['org1', 'org2'],
        roles: ['role1', 'role2'],
      },
      user_data: {
        organizations: [
          { name: 'org3', role: 'role3' },
          { name: 'org4', role: 'role4' },
        ],
      },
    }

    const values = {
      organizationNames: [
        { id: 0, name: 'org1' },
        { id: 1, name: 'org2' },
      ],
      availableRoles: [{ role: 'role1' }, { role: 'role2' }],
      selectedOrganizations: [
        { id: 0, name: 'org3', role: 'role3' },
        { id: 1, name: 'org4', role: 'role4' },
      ],
      selectedOrganizationNames: [
        { id: 0, name: 'org3' },
        { id: 1, name: 'org4' },
      ],
    }
    expect(reducer(initialState, updateStates(apiData))).toEqual({
      ...initialState,
      value: { ...initialState.value, ...values },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      successMsg: 'successMsg',
      selectedOrganizationNames: 'selectedOrganizationNames',
      selectedOrganizations: 'selectedOrganizations',
      organizationNames: 'organizationNames',
      availableRoles: 'availableRoles',
      apiData: 'apiData',
      errorState: 'errorState',
      errorMsg: 'errorMsg',
      submitAttempt: 'submitAttempt',
    },
  }
  const state = { adminEditUser: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectSuccessMsg(state)).toEqual('successMsg')
    expect(selectSelectedOrganizationNames(state)).toEqual('selectedOrganizationNames')
    expect(selectSelectedOrganizations(state)).toEqual('selectedOrganizations')
    expect(selectOrganizationNames(state)).toEqual('organizationNames')
    expect(selectAvailableRoles(state)).toEqual('availableRoles')
    expect(selectApiData(state)).toEqual('apiData')
    expect(selectErrorState(state)).toEqual('errorState')
    expect(selectErrorMsg(state)).toEqual('errorMsg')
    expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
  })
})

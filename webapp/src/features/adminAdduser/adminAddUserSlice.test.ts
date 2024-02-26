import reducer from './adminAddUserSlice'
import {
  // async thunks
  getUserData,
  createUser,
  resetForm,
  submitForm,

  // reducers
  updateOrganizationNamesApiData,
  updateAvailableRolesApiData,
  updateOrganizations,
  setSuccessMsg,
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
} from './adminAddUserSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin add User reducer', () => {
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
  var initialState: RootState['adminAddUser'] = {
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
      const action = getUserData()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'body' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddUser,
        token: 'token',
        additional_headers: { 'Content-Type': 'application/json' },
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message', body: 'body' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddUser,
        token: 'token',
        additional_headers: { 'Content-Type': 'application/json' },
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddUser/getUserData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const apiData = 'data'
      let errorMsg = ''
      let errorState = false

      let state = reducer(initialState, {
        type: 'adminAddUser/getUserData/fulfilled',
        payload: { data: apiData, success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, errorMsg, errorState, apiData },
      })

      // Error Case
      errorMsg = 'message'
      errorState = true

      state = reducer(initialState, {
        type: 'adminAddUser/getUserData/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminAddUser/getUserData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('createUser', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { data: 'data' } as any

      let reset = jest.fn()
      let action = createUser({ json, reset })
      apiHelper._postData = jest.fn().mockReturnValue({ status: 200, message: 'User Creation is successful.' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: 'User Creation is successful.' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddUser,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)

      // Error Code Other
      dispatch = jest.fn()
      action = createUser({ json, reset })
      apiHelper._postData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddUser,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddUser/createUser/pending',
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

      let state = reducer(initialState, {
        type: 'adminAddUser/createUser/fulfilled',
        payload: { message: 'message', success: true },
      })

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
        type: 'adminAddUser/createUser/fulfilled',
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
        type: 'adminAddUser/createUser/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('resetForm', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      let getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })

      let reset = jest.fn()
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        let action = resetForm(reset)
        await action(dispatch, getState, undefined)
        expect(reset).toHaveBeenCalledTimes(1)
        expect(global.setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly fulfilled', async () => {
      const selectedOrganizations = [] as any
      const selectedOrganizationNames = [] as any

      const state = reducer(initialState, {
        type: 'adminAddUser/resetForm/fulfilled',
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, selectedOrganizations, selectedOrganizationNames },
      })
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
        adminAddUser: {
          value: {
            selectedOrganizations: [{ id: 0, name: 'org1' }],
          },
        },
      })
      const data = { data: 'data' } as any

      let reset = jest.fn()
      let action = submitForm({ data, reset })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(false)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      // invalid checkForm
      dispatch = jest.fn()
      getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminAddUser: {
          value: {
            selectedOrganizations: [],
          },
        },
      })
      action = submitForm({ data, reset })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(true)
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly fulfilled', async () => {
      const submitAttempt = 'submitAttempt'

      const state = reducer(initialState, {
        type: 'adminAddUser/submitForm/fulfilled',
        payload: submitAttempt,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, submitAttempt },
      })
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminAddUser'] = {
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

  it('updateOrganizationNamesApiData reducer updates state correctly', async () => {
    const apiData = {
      organizations: ['org1', 'org2'],
    } as any
    expect(
      reducer({ ...initialState, value: { ...initialState.value, apiData } }, updateOrganizationNamesApiData())
    ).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        organizationNames: [
          { id: 0, name: 'org1' },
          { id: 1, name: 'org2' },
        ],
        apiData,
      },
    })
  })

  it('updateAvailableRolesApiData reducer updates state correctly', async () => {
    const apiData = {
      roles: ['role1', 'role2'],
    } as any
    expect(
      reducer({ ...initialState, value: { ...initialState.value, apiData } }, updateAvailableRolesApiData())
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, availableRoles: [{ role: 'role1' }, { role: 'role2' }], apiData },
    })
  })

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
    const payload = { name: 'org1', role: 'role1' } as any
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
  const state = { adminAddUser: initialState } as any

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

import reducer from './adminOrganizationTabUserSlice'
import {
  // async thunks
  getAvailableRoles,
  getAvailableUsers,
  userDeleteSingle,
  userDeleteMultiple,
  userAddMultiple,
  userBulkEdit,
  refresh,

  // functions
  getUserData,

  // reducers
  setSelectedUserList,
  setSelectedUserRole,

  // selectors
  selectLoading,
  selectAvailableUserList,
  selectSelectedUserList,
  selectAvailableRoles,
} from './adminOrganizationTabUserSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin organization tab User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        availableUserList: [],
        selectedUserList: [],
        availableRoles: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminOrganizationTabUser'] = {
    loading: null,
    value: {
      availableUserList: null,
      selectedUserList: null,
      availableRoles: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getAvailableRoles', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = getAvailableRoles()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'data' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddUser,
        token: 'token',
        additional_headers: { 'Content-Type': 'application/json' },
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
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
        type: 'adminOrganizationTabUser/getAvailableRoles/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let orgName = 'org2'
      const data = {
        roles: ['role1', 'role2'],
      }
      let state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableRoles/fulfilled',
        payload: { data, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, availableRoles: [{ role: 'role1' }, { role: 'role2' }] },
      })

      state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableRoles/fulfilled',
        payload: { data, orgName, success: false },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableRoles/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('getAvailableUsers', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const orgName = 'orgName'
      const action = getAvailableUsers(orgName)

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'data', orgName })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminUser,
        token: 'token',
        query_params: { user_email: 'all' },
        additional_headers: { 'Content-Type': 'application/json' },
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminUser,
        token: 'token',
        query_params: { user_email: 'all' },
        additional_headers: { 'Content-Type': 'application/json' },
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableUsers/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let orgName = 'org3'
      const data = {
        user_data: [
          { organizations: ['org1', 'org2'], email: 'test@gmail.com' },
          { organizations: ['org1', 'org2'], email: 'test2@gmail.com' },
        ],
      }
      let state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableUsers/fulfilled',
        payload: { data, orgName, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
          availableUserList: [
            { id: 0, email: 'test@gmail.com', role: 'user' },
            { id: 1, email: 'test2@gmail.com', role: 'user' },
          ],
        },
      })

      // not successful
      state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableUsers/fulfilled',
        payload: { data, orgName, success: false },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminOrganizationTabUser/getAvailableUsers/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('userDeleteSingle', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const user = { email: 'test@gmail.com', role: 'role1' }
      const selectedOrg = 'selectedOrg'
      const updateTableData = jest.fn()

      let userData = { user_data: { organizations: ['org1', 'org2'] } }

      let action = userDeleteSingle({ user, selectedOrg, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ body: userData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(window.alert).not.toHaveBeenCalled()

        // Only 1 organization
        dispatch = jest.fn()
        userData = { user_data: { organizations: ['org1'] } }

        action = userDeleteSingle({ user, selectedOrg, updateTableData })

        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ body: userData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove User test@gmail.com from selectedOrg because they must belong to at least one organization.'
        )
      } catch (e) {
        window.alert = jsdomAlert
        throw e
      }
    })
  })

  describe('userDeleteMultiple', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const users = [
        { email: 'test@gmail.com', role: 'role1' },
        { email: 'test2@gmail.com', role: 'role2' },
        { email: 'test3@gmail.com', role: 'role3' },
      ]
      const selectedOrg = 'selectedOrg'
      let fetchPatchOrganization = jest.fn()
      const updateTableData = jest.fn()

      const userData = { user_data: { organizations: ['org1', 'org2', 'org3'] } }
      const invalidUserData = { user_data: { organizations: ['org1'] } }

      let action = userDeleteMultiple({ users, selectedOrg, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest
          .fn()
          .mockReturnValueOnce({ body: userData })
          .mockReturnValueOnce({ body: userData })
          .mockReturnValueOnce({ body: userData })
        await action(dispatch, getState, undefined)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)

        // Only 1 organization
        dispatch = jest.fn()

        action = userDeleteMultiple({ users, selectedOrg, updateTableData })

        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest
          .fn()
          .mockReturnValueOnce({ body: userData })
          .mockReturnValueOnce({ body: invalidUserData })
          .mockReturnValueOnce({ body: invalidUserData })
        await action(dispatch, getState, undefined)
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove User(s) test2@gmail.com, test3@gmail.com from selectedOrg because they must belong to at least one organization.'
        )
      } catch (e) {
        window.alert = jsdomAlert
        throw e
      }
    })
  })

  describe('userAddMultiple', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const userList = [
        { email: 'test@gmail.com', role: 'role1' },
        { email: 'test2@gmail.com', role: 'role2' },
      ]
      const selectedOrg = 'selectedOrg'
      let fetchPatchOrganization = jest.fn()
      const updateTableData = jest.fn()

      let action = userAddMultiple({ userList, selectedOrg, updateTableData })

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)
    })
  })

  describe('userBulkEdit', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const userList = [
        { email: 'test@gmail.com', role: 'role1' },
        { email: 'test2@gmail.com', role: 'role2' },
      ]
      const json = {
        obj1: { newData: { email: 'test@gmail.com', role: 'role1' } },
        obj2: { newData: { email: 'test2@gmail.com', role: 'role2' } },
      }
      const selectedOrg = 'selectedOrg'
      const selectedUser = 'selectedUser'
      const fetchPatchOrganization = jest.fn()
      const updateTableData = jest.fn()

      const action = userBulkEdit({ json, selectedOrg, selectedUser, updateTableData })

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)
    })
  })

  describe('refresh', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const selectedOrg = 'selectedOrg'
      const updateTableData = jest.fn()

      let action = refresh({ selectedOrg, updateTableData })

      await action(dispatch, getState, undefined)
      expect(updateTableData).toHaveBeenCalledTimes(1)
      expect(updateTableData).toHaveBeenCalledWith(selectedOrg)
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminOrganizationTabUser'] = {
    loading: null,
    value: {
      availableUserList: null,
      selectedUserList: null,
      availableRoles: null,
    },
  }

  it('setSelectedUserList reducer updates state correctly', async () => {
    const selectedUserList = 'selectedUserList'
    expect(reducer(initialState, setSelectedUserList(selectedUserList))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedUserList },
    })
  })

  it('setSelectedUserRole reducer updates state correctly', async () => {
    const email = 'test@gmail.com'
    const role = 'role1'
    const selectedUserList = [{ email, role: 'role2' }] as any
    expect(
      reducer(
        { ...initialState, value: { ...initialState.value, selectedUserList } },
        setSelectedUserRole({ email, role })
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedUserList: [{ email, role: 'role1' }] },
    })
  })
})

describe('functions', () => {
  it('getUserData', async () => {
    const user_email = 'test@gmail.com'
    const token = 'token'
    apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ data: 'data' })
    const resp = await getUserData(user_email, token)
    expect(resp).toEqual({ data: 'data' })
    expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
      url: EnvironmentVars.adminUser,
      token,
      query_params: { user_email },
      additional_headers: { 'Content-Type': 'application/json' },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      availableUserList: 'availableUserList',
      selectedUserList: 'selectedUserList',
      availableRoles: 'availableRoles',
    },
  }
  const state = { adminOrganizationTabUser: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectAvailableUserList(state)).toEqual('availableUserList')
    expect(selectSelectedUserList(state)).toEqual('selectedUserList')
    expect(selectAvailableRoles(state)).toEqual('availableRoles')
  })
})

import reducer from './adminUserTabSlice'
import {
  // async thunks
  getAvailableUsers,
  deleteUsers,

  // functions
  getUserData,
  deleteUser,

  // reducers
  updateTitle,
  setActiveDiv,
  setEditUserRowData,

  // selectors
  selectLoading,
  selectActiveDiv,
  selectTableData,
  selectTitle,
  selectEditUserRowData,
} from './adminUserTabSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'

describe('admin User tab reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        activeDiv: 'user_table',
        tableData: [],
        title: 'Users',
        editUserRowData: {},
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    value: {
      activeDiv: null,
      tableData: null,
      title: null,
      editUserRowData: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
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
      const action = getAvailableUsers()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'data' })

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminUserTab/getAvailableUsers/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let success = true
      let data = { user_data: [{ name: 'username1' }, { name: 'username2' }] }
      let tableData = [
        { name: 'username1', id: 0 },
        { name: 'username2', id: 1 },
      ]
      let state = reducer(initialState, {
        type: 'adminUserTab/getAvailableUsers/fulfilled',
        payload: { data, success },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, tableData },
      })

      success = false
      state = reducer(initialState, {
        type: 'adminUserTab/getAvailableUsers/fulfilled',
        payload: { success },
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
        type: 'adminUserTab/getAvailableUsers/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteUsers', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const data = [{ email: 'test1@gmail.com' }, { email: 'test2@gmail.com' }, { email: 'test3@gmail.com' }]

      let action = deleteUsers(data)

      apiHelper._deleteData = jest
        .fn()
        .mockReturnValueOnce({ status: 200 })
        .mockReturnValueOnce({ status: 200 })
        .mockReturnValueOnce({ status: 500 })
      await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledTimes(3)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)
    })
  })
})

describe('reducers', () => {
  const initialState = {
    loading: null,
    value: {
      activeDiv: null,
      tableData: null,
      title: null,
      editUserRowData: null,
    },
  }

  it('updateTitle reducer updates state correctly', async () => {
    let activeDiv = 'user_table'
    let title = 'CV Manager Users'
    expect(reducer({ ...initialState, value: { ...initialState.value, activeDiv } }, updateTitle())).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title },
    })

    activeDiv = 'edit_user'
    title = 'Edit User'
    expect(reducer({ ...initialState, value: { ...initialState.value, activeDiv } }, updateTitle())).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title },
    })

    activeDiv = 'add_user'
    title = 'Add User'
    expect(reducer({ ...initialState, value: { ...initialState.value, activeDiv } }, updateTitle())).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title },
    })
  })

  it('setActiveDiv reducer updates state correctly', async () => {
    const activeDiv = 'activeDiv'
    expect(reducer(initialState, setActiveDiv(activeDiv))).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv },
    })
  })

  it('setEditUserRowData reducer updates state correctly', async () => {
    const editUserRowData = 'editUserRowData'
    expect(reducer(initialState, setEditUserRowData(editUserRowData))).toEqual({
      ...initialState,
      value: { ...initialState.value, editUserRowData },
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

  it('deleteUser', async () => {
    const user_email = 'test@gmail.com'
    const token = 'token'
    apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, data: 'data' })
    await deleteUser(user_email, token)
    expect(apiHelper._deleteData).toHaveBeenCalledWith({
      url: EnvironmentVars.adminUser,
      token,
      query_params: { user_email },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      activeDiv: 'activeDiv',
      tableData: 'tableData',
      title: 'title',
      editUserRowData: 'editUserRowData',
    },
  }
  const state = { adminUserTab: initialState }

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectActiveDiv(state)).toEqual('activeDiv')
    expect(selectTableData(state)).toEqual('tableData')
    expect(selectTitle(state)).toEqual('title')
    expect(selectEditUserRowData(state)).toEqual('editUserRowData')
  })
})

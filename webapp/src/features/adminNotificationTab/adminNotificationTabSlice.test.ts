import reducer from './adminNotificationTabSlice'
import {
  // async thunks
  getNotificationData,
  deleteNotifications,

  // functions
  getUserNotifications,
  deleteNotification,

  // reducers
  updateTitle,
  setActiveDiv,
  setEditNotificationRowData,

  // selectors
  selectLoading,
  selectActiveDiv,
  selectTableData,
  selectTitle,
  selectEditNotificationRowData,
} from './adminNotificationTabSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin Notification tab reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        activeDiv: 'notification_table',
        tableData: [],
        title: 'Email Notifications',
        editNotificationRowData: {},
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminNotificationTab'] = {
    loading: null,
    value: {
      activeDiv: null,
      tableData: null,
      title: null,
      editNotificationRowData: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getUserNotifications', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = getUserNotifications()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('data')
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminNotificationTab/getUserNotifications/pending',
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
      let notification_data = [
        { first_name: 'first-name', last_name: 'last-name', email: 'test@gmail.com', email_type: 'some-type' },
        { first_name: 'first-name', last_name: 'last-name', email: 'test2@gmail.com', email_type: 'some-type-2' },
      ]
      let tableData = [
        {
          email: 'test@gmail.com',
          email_type: 'some-type',
          first_name: 'first-name',
          last_name: 'last-name',
        },
        {
          email: 'test2@gmail.com',
          email_type: 'some-type-2',
          first_name: 'first-name',
          last_name: 'last-name',
        },
      ]
      let state = reducer(initialState, {
        type: 'adminNotificationTab/getUserNotifications/fulfilled',
        payload: { notification_data },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, tableData },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminNotificationTab/getUserNotifications/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteNotifications', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const data = [
        {
          email: 'test1@gmail.com',
          email_type: 'some_type',
          first_name: 'first_name',
          last_name: 'last_name',
        },
        {
          email: 'test2@gmail.com',
          email_type: 'some-type-2',
          first_name: 'first_name',
          last_name: 'last_name',
        },
        {
          email: 'test3@gmail.com',
          email_type: 'some-type-3',
          first_name: 'first_name',
          last_name: 'last_name',
        },
      ]

      let action = deleteNotifications(data)

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
  const initialState: RootState['adminNotificationTab'] = {
    loading: null,
    value: {
      activeDiv: null,
      tableData: null,
      title: null,
      editNotificationRowData: null,
    },
  }

  it('updateTitle reducer updates state correctly', async () => {
    let activeDiv = 'notification_table'
    let title = 'CV Manager Email Notifications'
    expect(reducer({ ...initialState, value: { ...initialState.value, activeDiv } }, updateTitle())).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title },
    })

    activeDiv = 'edit_notification'
    title = 'Edit Email Notification'
    expect(reducer({ ...initialState, value: { ...initialState.value, activeDiv } }, updateTitle())).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title },
    })

    activeDiv = 'add_notification'
    title = 'Add Email Notification'
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
    const editNotificationRowData = 'editNotificationRowData'
    expect(reducer(initialState, setEditNotificationRowData(editNotificationRowData))).toEqual({
      ...initialState,
      value: { ...initialState.value, editNotificationRowData },
    })
  })
})

describe('functions', () => {
  it('getUserNotifications', async () => {
    const user_email = 'test@gmail.com'
    const token = 'token'
    apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ data: 'data' })
    const resp = await getNotificationData(user_email, token)
    expect(resp).toEqual({ data: 'data' })
    expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
      url: EnvironmentVars.adminNotification,
      token,
      query_params: { user_email },
      additional_headers: { 'Content-Type': 'application/json' },
    })
  })

  it('deleteUser', async () => {
    const email = 'test@gmail.com'
    const token = 'token'
    const email_type = 'email_type'
    apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, data: 'data' })
    await deleteNotification(email, email_type, token)
    expect(apiHelper._deleteData).toHaveBeenCalledWith({
      url: EnvironmentVars.adminNotification,
      token,
      query_params: { email, email_type },
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
      editNotificationRowData: 'editNotificationRowData',
    },
  }
  const state = { adminNotificationTab: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectActiveDiv(state)).toEqual('activeDiv')
    expect(selectTableData(state)).toEqual('tableData')
    expect(selectTitle(state)).toEqual('title')
    expect(selectEditNotificationRowData(state)).toEqual('editNotificationRowData')
  })
})

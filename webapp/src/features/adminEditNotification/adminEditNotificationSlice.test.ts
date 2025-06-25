import reducer, {
  editNotification,
  getNotificationData,
  selectSelectedType,
  setSelectedType,
  updateEmailTypesApiData,
} from './adminEditNotificationSlice'
import {
  // async thunks
  submitForm,

  // reducers
  setSuccessMsg,

  // selectors
  selectLoading,
  selectSuccessMsg,
  selectApiData,
  selectErrorState,
  selectErrorMsg,
  selectSubmitAttempt,
} from './adminEditNotificationSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin edit Notification reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        availableTypes: [] as { type: string }[],
        apiData: {},
        errorState: false,
        errorMsg: '',
        submitAttempt: false,
        selectedType: { type: '' },
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminEditNotification'] = {
    loading: null,
    value: {
      successMsg: null,
      availableTypes: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      submitAttempt: null,
      selectedType: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getNotificationData', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token', data: { email: 'test@gmail.com' } },
          },
        },
      })
      const user_email = 'test@gmail.com'
      const action = getNotificationData()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'body' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddNotification,
        token: 'token',
        query_params: { user_email },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddNotification,
        token: 'token',
        query_params: { user_email },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditNotification/getNotificationData/pending',
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
      let successMsg = ''
      let errorMsg = ''
      let errorState = false

      let state = reducer(
        { ...initialState, value: { ...initialState.value, apiData, successMsg: '' } },
        {
          type: 'adminEditNotification/getNotificationData/fulfilled',
          payload: { message: 'message', success: true, data: apiData },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, errorMsg, errorState, apiData, successMsg },
      })

      // Error Case
      successMsg = ''
      errorMsg = 'message'
      errorState = true

      state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, successMsg: '' },
        },
        {
          type: 'adminEditNotification/getNotificationData/fulfilled',
          payload: { message: 'message', success: false },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditNotification/getNotificationData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('editNotification', () => {
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
      let action = editNotification({ json })

      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: true, message: 'Changes were successfully applied!' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminNotification,
          token: 'token',
          body: JSON.stringify(json),
        })
        expect(setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }

      dispatch = jest.fn()
      action = editNotification({ json })
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminNotification,
          token: 'token',
          body: JSON.stringify(json),
        })
        expect(setTimeout).not.toHaveBeenCalled()
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditNotification/editNotification/pending',
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
          type: 'adminEditNotification/editNotification/fulfilled',
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
        type: 'adminEditNotification/editNotification/fulfilled',
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
        type: 'adminEditNotification/editNotification/rejected',
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
        adminEditNotification: {
          value: {
            selectedType: { type: 'type1' },
          },
        },
        adminNotificationTab: {
          value: {
            editNotificationRowData: { email: 'email' },
          },
        },
      })
      const data = { data: 'data' } as any
      let updateEmailTypesApiData = jest.fn()

      let action = submitForm({ data })
      let resp = await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      // empty selectedOrganizations
      dispatch = jest.fn()
      getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditNotification: {
          value: {
            selectedType: { type: '' },
          },
        },
        adminNotificationTab: {
          value: {
            editNotificationRowData: { email: 'email' },
          },
        },
      })
      action = submitForm({ data })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({
        message: 'Please fill out all required fields',
        submitAttempt: true,
        success: false,
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly fulfilled', async () => {
      const submitAttempt = 'submitAttempt'

      const state = reducer(initialState, {
        type: 'adminEditNotification/submitForm/fulfilled',
        payload: { submitAttempt: submitAttempt },
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, submitAttempt },
      })
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminEditNotification'] = {
    loading: null,
    value: {
      successMsg: null,
      availableTypes: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      submitAttempt: null,
      selectedType: null,
    },
  }

  it('setSuccessMsg reducer updates state correctly', async () => {
    const successMsg = 'successMsg'
    expect(reducer(initialState, setSuccessMsg(successMsg))).toEqual({
      ...initialState,
      value: { ...initialState.value, successMsg },
    })
  })

  it('setSelectedType reducer updates state correctly', async () => {
    const selectedType = { type: 'type1' }
    const payload = { type: 'type1' }
    expect(
      reducer({ ...initialState, value: { ...initialState.value, selectedType } }, setSelectedType(payload))
    ).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        selectedType: { type: 'type1' },
      },
    })
  })

  it('updateEmailTypesApiData', async () => {
    const apiData = {
      email_types: ['type1', 'type2'],
    }

    expect(
      reducer({ ...initialState, value: { ...initialState.value, apiData: apiData } }, updateEmailTypesApiData())
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, apiData: apiData, availableTypes: [{ type: 'type1' }, { type: 'type2' }] },
    })
  })

  describe('selectors', () => {
    const initialState = {
      loading: 'loading',
      value: {
        successMsg: 'successMsg',
        apiData: 'apiData',
        errorState: 'errorState',
        errorMsg: 'errorMsg',
        submitAttempt: 'submitAttempt',
        selectedType: 'selectedType',
        user_email: 'user_email',
      },
    }
    const state = { adminEditNotification: initialState } as any

    it('selectors return the correct value', async () => {
      expect(selectLoading(state)).toEqual('loading')
      expect(selectSuccessMsg(state)).toEqual('successMsg')
      expect(selectApiData(state)).toEqual('apiData')
      expect(selectErrorState(state)).toEqual('errorState')
      expect(selectErrorMsg(state)).toEqual('errorMsg')
      expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
      expect(selectSelectedType(state)).toEqual('selectedType')
    })
  })
})

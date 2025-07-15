import reducer, {
  // async thunks
  resetForm,
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
  selectUserEmail,
  selectSelectedType,
  selectAvailableTypes,
  updateEmailTypesApiData,
  setSelectedType,
  createNotification,
  getNotificationData,
} from '../adminAddNotification/adminAddNotificationSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'
describe('admin add User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        availableTypes: [],
        apiData: {},
        errorState: false,
        errorMsg: '',
        submitAttempt: false,
        selectedType: { type: '' },
        user_email: '',
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminAddNotification'] = {
    loading: null,
    value: {
      successMsg: null,
      availableTypes: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      submitAttempt: null,
      selectedType: null,
      user_email: null,
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
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = getNotificationData()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'body' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddNotification,
        token: 'token',
        query_params: { user_email: undefined },
        additional_headers: { 'Content-Type': 'application/json' },
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message', body: 'body' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddNotification,
        token: 'token',
        query_params: { user_email: undefined },
        additional_headers: { 'Content-Type': 'application/json' },
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddNotification/getNotificationData/pending',
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
        type: 'adminAddNotification/getNotificationData/fulfilled',
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
        type: 'adminAddNotification/getNotificationData/fulfilled',
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
        type: 'adminAddNotification/getNotificationData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('createNotification', () => {
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

      const reset = jest.fn()
      let action = createNotification({ json, reset })
      apiHelper._postData = jest
        .fn()
        .mockReturnValue({ status: 200, message: 'Email Notification Creation is successful.' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: 'Email Notification Creation is successful.' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddNotification,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)

      // Error Code Other
      dispatch = jest.fn()
      action = createNotification({ json, reset })
      apiHelper._postData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddNotification,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddNotification/createNotification/pending',
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
        type: 'adminAddNotification/createNotification/fulfilled',
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
        type: 'adminAddNotification/createNotification/fulfilled',
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
        type: 'adminAddNotification/createNotification/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('resetForm', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })

      const reset = jest.fn()
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        const action = resetForm(reset)
        await action(dispatch, getState, undefined)
        expect(reset).toHaveBeenCalledTimes(1)
        expect(global.setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
      } catch (e) {
        (global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly fulfilled', async () => {
      const selectedType = { type: '' }

      const state = reducer(initialState, {
        type: 'adminAddNotification/resetForm/fulfilled',
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, selectedType },
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
        adminAddNotification: {
          value: {
            selectedType: { type: 'type1' },
          },
        },
      })
      const data = { data: 'data' } as any

      const reset = jest.fn()
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
        adminAddNotification: {
          value: {
            selectedType: { type: '' },
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
        type: 'adminAddNotification/submitForm/fulfilled',
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
  const initialState: RootState['adminAddNotification'] = {
    loading: null,
    value: {
      successMsg: null,
      availableTypes: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      submitAttempt: null,
      selectedType: null,
      user_email: null,
    },
  }

  it('updateEmailTypesApiData reducer updates state correctly', async () => {
    const apiData = {
      email_types: ['type1', 'type2'],
    } as any
    expect(reducer({ ...initialState, value: { ...initialState.value, apiData } }, updateEmailTypesApiData())).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        availableTypes: [{ type: 'type1' }, { type: 'type2' }],
        apiData,
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

  it('setSelectedType reducer updates state correctly', async () => {
    const payload = { type: 'type1' } as any
    expect(reducer({ ...initialState, value: { ...initialState.value } }, setSelectedType(payload))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        selectedType: { type: 'type1' },
      },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      successMsg: 'successMsg',
      availableTypes: 'availableTypes',
      apiData: 'apiData',
      errorState: 'errorState',
      errorMsg: 'errorMsg',
      submitAttempt: 'submitAttempt',
      selectedType: 'selectedType',
      user_email: 'user_email',
    },
  }
  const state = { adminAddNotification: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectSuccessMsg(state)).toEqual('successMsg')
    expect(selectApiData(state)).toEqual('apiData')
    expect(selectErrorState(state)).toEqual('errorState')
    expect(selectErrorMsg(state)).toEqual('errorMsg')
    expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
    expect(selectAvailableTypes(state)).toEqual('availableTypes')
    expect(selectSelectedType(state)).toEqual('selectedType')
    expect(selectUserEmail(state)).toEqual('user_email')
  })
})

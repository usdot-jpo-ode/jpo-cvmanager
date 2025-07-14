import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectEmail, selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { getUserNotifications } from '../adminNotificationTab/adminNotificationTabSlice'
import { RootState } from '../../store'

export type AdminNotificationForm = {
  email: string
  email_type: string
}

export type AdminNotificationApiData = {
  email_types: string[]
}

export type AdminNotificationRow = {
  first_name: string
  last_name: string
  email: string
  email_type: string
}

const initialState = {
  successMsg: '',
  availableTypes: [] as { type: string }[],
  apiData: {} as AdminNotificationApiData,
  errorState: false,
  errorMsg: '',
  submitAttempt: false,
  selectedType: { type: '' },
  user_email: '',
}

export const getNotificationData = createAsyncThunk(
  'adminAddNotification/getNotificationData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const user_email = selectEmail(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminAddNotification,
      token,
      query_params: { user_email },
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const createNotification = createAsyncThunk(
  'adminAddNotification/createNotification',
  async (payload: { json: AdminNotificationForm; reset: () => void }, { getState, dispatch }) => {
    const { json, reset } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._postData({
      url: EnvironmentVars.adminAddNotification,
      token,
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        dispatch(getUserNotifications())
        dispatch(resetForm(reset))
        return { success: true, message: 'Email Notification Creation is successful.' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const resetForm = createAsyncThunk('adminAddNotification/resetForm', async (reset: () => void, { dispatch }) => {
  reset()
  dispatch(adminAddNotificationSlice.actions.setSelectedType({ type: '' }))
  setTimeout(() => dispatch(adminAddNotificationSlice.actions.setSuccessMsg('')), 5000)
})

export const submitForm = createAsyncThunk(
  'adminAddNotification/submitForm',
  async (payload: { data: AdminNotificationForm; reset: () => void }, { getState, dispatch }) => {
    const { data, reset } = payload
    data.email_type = (getState() as RootState).adminAddNotification.value.selectedType.type
    var res = await dispatch(createNotification({ json: data, reset }))
    if ((res as any).payload && (res as any).payload.success) {
      return { submitAttempt: false, success: true, message: 'Notification Added Successfully' }
    } else {
      return { submitAttempt: false, success: false, message: (res as any).payload?.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminAddNotificationSlice = createSlice({
  name: 'adminAddNotification',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateEmailTypesApiData: (state) => {
      if (Object.keys(state.value.apiData).length !== 0) {
        let typeData = [] as { type: string }[]
        state.value.apiData.email_types.forEach((type) => typeData.push({ type }))
        state.value.availableTypes = [...typeData]
      }
    },
    setSuccessMsg: (state, action) => {
      state.value.successMsg = action.payload
    },
    setSelectedType: (
      state,
      action: {
        payload: {
          type: string
        }
      }
    ) => {
      state.value.selectedType = { type: action.payload.type }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getNotificationData.pending, (state) => {
        state.loading = true
      })
      .addCase(getNotificationData.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.apiData = action.payload.data
          state.value.errorMsg = ''
          state.value.errorState = false
        } else {
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(getNotificationData.rejected, (state) => {
        state.loading = false
      })
      .addCase(createNotification.pending, (state) => {
        state.loading = true
      })
      .addCase(createNotification.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.errorMsg = ''
          state.value.errorState = false
          state.value.successMsg = action.payload.message
        } else {
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
          state.value.successMsg = ''
        }
      })
      .addCase(createNotification.rejected, (state) => {
        state.loading = false
      })
      .addCase(resetForm.fulfilled, (state) => {
        state.value.selectedType = { type: '' }
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const { setSuccessMsg, setSelectedType, updateEmailTypesApiData } = adminAddNotificationSlice.actions

export const selectLoading = (state: RootState) => state.adminAddNotification.loading
export const selectSuccessMsg = (state: RootState) => state.adminAddNotification.value.successMsg
export const selectApiData = (state: RootState) => state.adminAddNotification.value.apiData
export const selectErrorState = (state: RootState) => state.adminAddNotification.value.errorState
export const selectErrorMsg = (state: RootState) => state.adminAddNotification.value.errorMsg
export const selectSubmitAttempt = (state: RootState) => state.adminAddNotification.value.submitAttempt
export const selectSelectedType = (state: RootState) => state.adminAddNotification.value.selectedType
export const selectAvailableTypes = (state: RootState) => state.adminAddNotification.value.availableTypes
export const selectUserEmail = (state: RootState) => state.adminAddNotification.value.user_email

export default adminAddNotificationSlice.reducer

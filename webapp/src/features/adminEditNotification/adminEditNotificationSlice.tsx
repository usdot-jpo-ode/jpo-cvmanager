import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectEmail, selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import { getUserNotifications } from '../adminNotificationTab/adminNotificationTabSlice'
import { AdminNotificationForm } from '../adminAddNotification/adminAddNotificationSlice'

export type AdminNotificationApiData = {
  email_types: string[]
}

const initialState = {
  successMsg: '',
  availableTypes: [] as { type: string }[],
  apiData: {} as AdminNotificationApiData,
  errorState: false,
  errorMsg: '',
  submitAttempt: false,
  selectedType: { type: '' },
}

export const getNotificationData = createAsyncThunk(
  'adminEditNotification/getNotificationData',
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

export const editNotification = createAsyncThunk(
  'adminEditNotification/editNotification',
  async (payload: { json: Object }, { getState, dispatch }) => {
    const { json } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminNotification,
      token,
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        dispatch(getUserNotifications())
        setTimeout(() => dispatch(adminEditNotificationSlice.actions.setSuccessMsg('')), 5000)
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const submitForm = createAsyncThunk(
  'adminEditNotification/submitForm',
  async (payload: { data: AdminNotificationForm }, { getState, dispatch }) => {
    const { data } = payload
    const currentState = getState() as RootState

    var tmpData = {
      email: data.email,
      old_email_type: currentState.adminNotificationTab.value.editNotificationRowData.email_type,
      new_email_type: currentState.adminEditNotification.value.selectedType.type,
    }

    const res = await dispatch(editNotification({ json: tmpData }))
    if ((res as any).payload && (res as any).payload.success) {
      return { submitAttempt: false, success: true, message: 'Notification Updated Successfully' }
    } else {
      return { submitAttempt: false, success: false, message: (res as any).payload?.message }
    }
  }
)

export const adminEditNotificationSlice = createSlice({
  name: 'adminEditNotification',
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
      .addCase(editNotification.pending, (state) => {
        state.loading = true
      })
      .addCase(editNotification.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.successMsg = action.payload.message
          state.value.errorMsg = ''
          state.value.errorState = false
        } else {
          state.value.successMsg = ''
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(editNotification.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const { setSuccessMsg, setSelectedType, updateEmailTypesApiData } = adminEditNotificationSlice.actions

export const selectLoading = (state: RootState) => state.adminEditNotification.loading
export const selectSuccessMsg = (state: RootState) => state.adminEditNotification.value.successMsg
export const selectAvailableTypes = (state: RootState) => state.adminEditNotification.value.availableTypes
export const selectApiData = (state: RootState) => state.adminEditNotification.value.apiData
export const selectErrorState = (state: RootState) => state.adminEditNotification.value.errorState
export const selectErrorMsg = (state: RootState) => state.adminEditNotification.value.errorMsg
export const selectSubmitAttempt = (state: RootState) => state.adminEditNotification.value.submitAttempt
export const selectSelectedType = (state: RootState) => state.adminEditNotification.value.selectedType

export default adminEditNotificationSlice.reducer

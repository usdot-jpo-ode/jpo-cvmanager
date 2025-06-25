import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectEmail, selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import { AdminEmailNotification } from '../../models/Notifications'
import { BreakfastDining } from '@mui/icons-material'

const initialState = {
  activeDiv: 'notification_table',
  tableData: [] as AdminEmailNotification[],
  title: 'Email Notifications',
  editNotificationRowData: {} as AdminEmailNotification,
}

export const getNotificationData = async (user_email: string, token: string) => {
  return await apiHelper._getDataWithCodes({
    url: EnvironmentVars.adminNotification,
    token,
    query_params: { user_email },
    additional_headers: { 'Content-Type': 'application/json' },
  })
}

export const deleteNotification = async (email: string, email_type: string, token: string) => {
  const data = await apiHelper._deleteData({
    url: EnvironmentVars.adminNotification,
    token,
    query_params: { email, email_type },
  })

  var return_val = {}

  switch (data.status) {
    case 200:
      console.debug(`Successfully deleted Notification: ${email_type} for ${email}`)
      return_val = { success: true, message: 'Successfully deleted Notification' }
      break
    default:
      console.error(data.message)
      return_val = { success: false, message: data.message }
      break
  }
  return return_val
}

export const getUserNotifications = createAsyncThunk(
  'adminNotificationTab/getUserNotifications',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const email = selectEmail(currentState)

    const data = await getNotificationData(email, token)

    switch (data.status) {
      case 200:
        return data.body
      default:
        console.error(data.message)
        return
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const deleteNotifications = createAsyncThunk(
  'adminUserTab/deleteNotification',
  async (
    data: Array<{ email: string; email_type: string; first_name: string; last_name: string }>,
    { getState, dispatch }
  ) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    let promises = []
    for (const user of data) {
      promises.push(deleteNotification(user.email, user.email_type, token))
    }
    var res = await Promise.all(promises)
    dispatch(getUserNotifications())
    for (const r of res) {
      if (!r.success) {
        console.error(`Failed to delete Notification: ${r.payload.message}`)
        return { success: false, message: 'Failed to delete one or more Notification(s)' }
      }
    }
    return { success: true, message: 'Notifications Deleted Successfully' }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminNotificationTabSlice = createSlice({
  name: 'adminNotificationTab',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateTitle: (state) => {
      if (state.value.activeDiv === 'notification_table') {
        state.value.title = 'CV Manager Email Notifications'
      } else if (state.value.activeDiv === 'edit_notification') {
        state.value.title = 'Edit Email Notification'
      } else if (state.value.activeDiv === 'add_notification') {
        state.value.title = 'Add Email Notification'
      }
    },
    setActiveDiv: (state, action) => {
      state.value.activeDiv = action.payload
    },
    setEditNotificationRowData: (state, action) => {
      state.value.editNotificationRowData = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getUserNotifications.pending, (state) => {
        state.loading = true
      })
      .addCase(getUserNotifications.fulfilled, (state, action) => {
        state.loading = false
        state.value.tableData = action.payload?.notification_data
      })
      .addCase(getUserNotifications.rejected, (state) => {
        state.loading = false
      })
  },
})

export const { updateTitle, setActiveDiv, setEditNotificationRowData } = adminNotificationTabSlice.actions

export const selectLoading = (state: RootState) => state.adminNotificationTab.loading
export const selectActiveDiv = (state: RootState) => state.adminNotificationTab.value.activeDiv
export const selectTableData = (state: RootState) => state.adminNotificationTab.value.tableData
export const selectTitle = (state: RootState) => state.adminNotificationTab.value.title
export const selectEditNotificationRowData = (state: RootState) =>
  state.adminNotificationTab.value.editNotificationRowData

export default adminNotificationTabSlice.reducer

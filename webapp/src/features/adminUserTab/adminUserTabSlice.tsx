import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'

const initialState = {
  activeDiv: 'user_table',
  tableData: [],
  title: 'Users',
  editUserRowData: {},
}

export const getUserData = async (user_email, token) => {
  return await apiHelper._getDataWithCodes({
    url: EnvironmentVars.adminUser,
    token,
    query_params: { user_email },
    additional_headers: { 'Content-Type': 'application/json' },
  })
}

export const deleteUser = async (user_email, token) => {
  const data = await apiHelper._deleteData({
    url: EnvironmentVars.adminUser,
    token,
    query_params: { user_email },
  })

  switch (data.status) {
    case 200:
      console.debug(`Successfully deleted User: ${user_email}`)
      break
    default:
      console.error(data.message)
      break
  }
}

export const getAvailableUsers = createAsyncThunk(
  'adminUserTab/getAvailableUsers',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await getUserData('all', token)

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const deleteUsers = createAsyncThunk(
  'adminUserTab/deleteUser',
  async (data, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    let promises = []
    for (const user of data) {
      promises.push(deleteUser(user.email, token))
    }
    await Promise.all(promises)
    dispatch(getAvailableUsers())
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const adminUserTabSlice = createSlice({
  name: 'adminUserTab',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateTitle: (state) => {
      if (state.value.activeDiv === 'user_table') {
        state.value.title = 'CV Manager Users'
      } else if (state.value.activeDiv === 'edit_user') {
        state.value.title = 'Edit User'
      } else if (state.value.activeDiv === 'add_user') {
        state.value.title = 'Add User'
      }
    },
    setActiveDiv: (state, action) => {
      state.value.activeDiv = action.payload
    },
    setEditUserRowData: (state, action) => {
      state.value.editUserRowData = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getAvailableUsers.pending, (state) => {
        state.loading = true
      })
      .addCase(getAvailableUsers.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.tableData = (action.payload.data?.user_data ?? []).map((user, index) => ({ ...user, id: index }))
        }
      })
      .addCase(getAvailableUsers.rejected, (state) => {
        state.loading = false
      })
  },
})

export const { updateTitle, setActiveDiv, setEditUserRowData } = adminUserTabSlice.actions

export const selectLoading = (state: RootState) => state.adminUserTab.loading
export const selectActiveDiv = (state: RootState) => state.adminUserTab.value.activeDiv
export const selectTableData = (state: RootState) => state.adminUserTab.value.tableData
export const selectTitle = (state: RootState) => state.adminUserTab.value.title
export const selectEditUserRowData = (state: RootState) => state.adminUserTab.value.editUserRowData

export default adminUserTabSlice.reducer

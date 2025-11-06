import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { getRsuInfoOnly } from '../../generalSlices/rsuSlice'
import { RootState } from '../../store'
import { AdminEditRsuFormType } from '../adminEditRsu/AdminEditRsu'

const initialState = {
  tableData: [] as AdminEditRsuFormType[],
  title: 'RSUs',
  columns: [
    { title: 'Milepost', field: 'milepost', id: 0 },
    { title: 'IP Address', field: 'ip', id: 1 },
    { title: 'Primary Route', field: 'primary_route', id: 2 },
    { title: 'RSU Model', field: 'model', id: 3 },
    { title: 'Serial Number', field: 'serial_number', id: 4 },
  ],
  editRsuRowData: {} as AdminEditRsuFormType,
}

export const updateTableData = createAsyncThunk(
  'adminRsuTab/updateTableData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    dispatch(getRsuInfoOnly())

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip: 'all' },
      additional_headers: { 'Content-Type': 'application/json' },
      tag: 'rsu',
    })

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

export const deleteRsu = createAsyncThunk(
  'adminRsuTab/deleteRsu',
  async (payload: { rsu_ip: string; shouldUpdateTableData: boolean }, { getState, dispatch }) => {
    const { rsu_ip, shouldUpdateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._deleteData({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip },
      tag: 'rsu',
    })

    let return_val = {}

    switch (data.status) {
      case 200:
        console.debug('Successfully deleted RSU: ' + rsu_ip)
        return_val = { success: true, message: 'Successfully deleted RSU: ' + rsu_ip }
        break
      default:
        return_val = { success: false, message: data.message }
        break
    }
    if (shouldUpdateTableData) {
      dispatch(updateTableData())
    }
    return return_val
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const deleteMultipleRsus = createAsyncThunk(
  'adminRsuTabSlice/deleteMultipleRsus',
  async (rows: AdminEditRsuFormType[], { dispatch }) => {
    const promises = []
    for (const row of rows) {
      promises.push(dispatch(deleteRsu({ rsu_ip: row.ip, shouldUpdateTableData: false })))
    }
    const res = await Promise.all(promises)
    dispatch(updateTableData())
    for (const r of res) {
      if (!r.payload.success) {
        return { success: false, message: 'Failed to delete one or more RSU(s)' }
      }
    }
    return { success: true, message: 'RSUs Deleted Successfully' }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminRsuTabSlice = createSlice({
  name: 'adminRsuTab',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setTitle: () => {},
    setEditRsuRowData: (state, action) => {
      state.value.editRsuRowData = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(updateTableData.pending, (state) => {
        state.loading = true
      })
      .addCase(updateTableData.fulfilled, (state, action) => {
        state.loading = false
        state.value.tableData = action.payload?.rsu_data
      })
      .addCase(updateTableData.rejected, (state) => {
        state.loading = false
      })
      .addCase(deleteRsu.pending, (state) => {
        state.loading = true
      })
      .addCase(deleteRsu.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(deleteRsu.rejected, (state) => {
        state.loading = false
      })
  },
})

export const { setEditRsuRowData } = adminRsuTabSlice.actions

export const selectLoading = (state: RootState) => state.adminRsuTab.loading
export const selectTableData = (state: RootState) => state.adminRsuTab.value.tableData
export const selectColumns = (state: RootState) => state.adminRsuTab.value.columns
export const selectEditRsuRowData = (state: RootState) => state.adminRsuTab.value.editRsuRowData

export default adminRsuTabSlice.reducer

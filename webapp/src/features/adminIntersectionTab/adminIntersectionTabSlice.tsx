import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { getIntersections } from '../../generalSlices/intersectionSlice'
import { RootState } from '../../store'
import { AdminEditIntersectionFormType } from '../adminEditIntersection/AdminEditIntersection'
import { AdminIntersection } from '../../models/Intersection'

const initialState = {
  tableData: [] as AdminEditIntersectionFormType[],
  title: 'Intersections',
  columns: [
    { title: 'Intersection ID', field: 'intersection_id', id: 0 },
    { title: 'Intersection Name', field: 'intersection_name', id: 1 },
    { title: 'Origin IP', field: 'origin_ip', id: 2 },
    { title: 'Linked RSUs', field: 'rsus', id: 3 },
  ],
  editIntersectionRowData: {} as AdminEditIntersectionFormType,
}

export const updateTableData = createAsyncThunk(
  'adminIntersectionTab/updateTableData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminIntersection,
      token,
      query_params: { intersection_id: 'all' },
      additional_headers: { 'Content-Type': 'application/json' },
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

export const deleteIntersection = createAsyncThunk(
  'adminIntersectionTab/deleteIntersection',
  async (payload: { intersection_id: string; shouldUpdateTableData: boolean }, { getState, dispatch }) => {
    const { intersection_id, shouldUpdateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._deleteData({
      url: EnvironmentVars.adminIntersection,
      token,
      query_params: { intersection_id },
    })

    var return_val = {}

    switch (data.status) {
      case 200:
        console.debug('Successfully deleted Intersection: ' + intersection_id)
        return_val = { success: true, message: 'Successfully deleted Intersection: ' + intersection_id }
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

export const deleteMultipleIntersections = createAsyncThunk(
  'adminIntersectionTabSlice/deleteMultipleIntersections',
  async (rows: AdminEditIntersectionFormType[], { dispatch }) => {
    let promises = []
    for (const row of rows) {
      promises.push(
        dispatch(deleteIntersection({ intersection_id: row.intersection_id, shouldUpdateTableData: false }))
      )
    }
    var res = await Promise.all(promises)
    dispatch(updateTableData())
    for (const r of res) {
      if (!r.payload.success) {
        return { success: false, message: 'Failed to delete one or more Intersection(s)' }
      }
    }
    return { success: true, message: 'Intersections Deleted Successfully' }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminIntersectionTabSlice = createSlice({
  name: 'adminIntersectionTab',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setTitle: (state) => {},
    setEditIntersectionRowData: (state, action) => {
      state.value.editIntersectionRowData = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(updateTableData.pending, (state) => {
        state.loading = true
      })
      .addCase(updateTableData.fulfilled, (state, action) => {
        state.loading = false
        state.value.tableData = action.payload?.intersection_data
        state.value.tableData?.forEach((element: AdminIntersection) => {
          element.rsus = (element.rsus as string[])?.join(', ')
        })
      })
      .addCase(updateTableData.rejected, (state) => {
        state.loading = false
      })
      .addCase(deleteIntersection.pending, (state) => {
        state.loading = true
      })
      .addCase(deleteIntersection.fulfilled, (state, action) => {
        state.loading = false
      })
      .addCase(deleteIntersection.rejected, (state) => {
        state.loading = false
      })
  },
})

export const { setEditIntersectionRowData } = adminIntersectionTabSlice.actions

export const selectLoading = (state: RootState) => state.adminIntersectionTab.loading
export const selectTableData = (state: RootState) => state.adminIntersectionTab.value.tableData
export const selectColumns = (state: RootState) => state.adminIntersectionTab.value.columns
export const selectEditIntersectionRowData = (state: RootState) =>
  state.adminIntersectionTab.value.editIntersectionRowData

export default adminIntersectionTabSlice.reducer

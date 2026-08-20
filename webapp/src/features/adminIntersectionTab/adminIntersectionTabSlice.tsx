import { createSlice } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminEditIntersectionFormType } from '../adminEditIntersection/AdminEditIntersection'

const initialState = {
  columns: [
    { title: 'Intersection ID', field: 'intersection_id', id: 0 },
    { title: 'Intersection Name', field: 'intersection_name', id: 1 },
    { title: 'Origin IP', field: 'origin_ip', id: 2 },
    { title: 'Linked RSUs', field: 'rsus', id: 3 },
  ],
  editIntersectionRowData: {} as AdminEditIntersectionFormType,
}

export const adminIntersectionTabSlice = createSlice({
  name: 'adminIntersectionTab',
  initialState: {
    value: initialState,
  },
  reducers: {
    setEditIntersectionRowData: (state, action) => {
      state.value.editIntersectionRowData = action.payload
    },
  },
})

export const { setEditIntersectionRowData } = adminIntersectionTabSlice.actions

export const selectColumns = (state: RootState) => state.adminIntersectionTab.value.columns
export const selectEditIntersectionRowData = (state: RootState) =>
  state.adminIntersectionTab.value.editIntersectionRowData

export default adminIntersectionTabSlice.reducer

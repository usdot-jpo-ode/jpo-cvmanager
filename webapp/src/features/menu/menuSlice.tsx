import { createSlice } from '@reduxjs/toolkit'
import { updateRowData } from '../../generalSlices/rsuSlice'
import { RootState } from '../../store'
const { DateTime } = require('luxon')

const initialState = {
  previousRequest: null,
  currentSort: null,
  sortedCountList: [],
  displayCounts: false,
  view: 'buttons',
}

export const sortCountList = (key, currentSort, countList) => (dispatch) => {
  let sortFn = () => {}
  // Support both descending and ascending sort
  // based on the current sort
  // Default is ascending
  if (key === currentSort) {
    dispatch(setCurrentSort(key + 'desc'))
    sortFn = function (a, b) {
      if (a[key] > b[key]) return -1
      if (a[key] < b[key]) return 1
      return 0
    }
  } else {
    dispatch(setCurrentSort(key))
    sortFn = function (a, b) {
      if (a[key] < b[key]) return -1
      if (a[key] > b[key]) return 1
      return 0
    }
  }

  let arrayCopy = [...countList]
  arrayCopy.sort(sortFn)
  dispatch(setSortedCountList(arrayCopy))
  return arrayCopy
}

export const changeDate = (e, type, requestOut, previousRequest) => (dispatch) => {
  let tmp = e
  let mst = DateTime.fromISO(tmp.toISOString())
  mst.setZone('America/Denver')
  let data
  if (type === 'start') {
    data = { start: mst.toString() }
  } else {
    data = { end: mst.toString() }
  }
  if (requestOut) {
    previousRequest.abort()
    dispatch(setPreviousRequest(null))
  }
  dispatch(updateRowData(data))
  return data
}

export const menuSlice = createSlice({
  name: 'menu',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setCurrentSort: (state, action) => {
      state.value.currentSort = action.payload
    },
    setSortedCountList: (state, action) => {
      state.value.sortedCountList = action.payload
    },
    setDisplay: (state, action) => {
      state.value.view = action.payload
      state.value.displayCounts = action.payload == 'tab'
    },
    setPreviousRequest: (state, action) => {
      state.value.previousRequest = action.payload
    },
  },
})

export const { setCurrentSort, setSortedCountList, setDisplay, setPreviousRequest } = menuSlice.actions

export const selectLoading = (state: RootState) => state.menu.loading
export const selectPreviousRequest = (state: RootState) => state.menu.value.previousRequest
export const selectDisplay = (state: RootState) => state.menu.value.display
export const selectCurrentSort = (state: RootState) => state.menu.value.currentSort
export const selectSortedCountList = (state: RootState) => state.menu.value.sortedCountList
export const selectDisplayCounts = (state: RootState) => state.menu.value.displayCounts
export const selectView = (state: RootState) => state.menu.value.view

export default menuSlice.reducer

import { AnyAction, ThunkDispatch, createSlice } from '@reduxjs/toolkit'
import { updateRowData } from '../../generalSlices/rsuSlice'
import { RootState } from '../../store'
const { DateTime } = require('luxon')

const initialState = {
  currentSort: null as null | string,
  sortedCountList: [] as CountsListElement[],
  displayCounts: false,
  view: 'buttons',
}

export const sortCountList =
  (key: string, currentSort: string, countList: CountsListElement[]) =>
  (dispatch: ThunkDispatch<RootState, any, AnyAction>) => {
    let sortFn = (
      a: { [key: string]: string | number | void },
      b: { [key: string]: string | number | void }
    ): number => {
      return 0
    }
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

export const changeDate =
  (e: Date, type: 'start' | 'end', requestOut: boolean) => (dispatch: ThunkDispatch<RootState, any, AnyAction>) => {
    let tmp = e
    let mst = DateTime.fromISO(tmp.toISOString())
    mst.setZone('America/Denver')
    let data
    if (type === 'start') {
      data = { start: mst.toString() }
    } else {
      data = { end: mst.toString() }
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
  },
})

export const { setCurrentSort, setSortedCountList, setDisplay } = menuSlice.actions

export const selectLoading = (state: RootState) => state.menu.loading
export const selectCurrentSort = (state: RootState) => state.menu.value.currentSort
export const selectSortedCountList = (state: RootState) => state.menu.value.sortedCountList
export const selectDisplayCounts = (state: RootState) => state.menu.value.displayCounts
export const selectView = (state: RootState) => state.menu.value.view

export default menuSlice.reducer

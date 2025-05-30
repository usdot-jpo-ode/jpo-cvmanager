import { AnyAction, PayloadAction, ThunkDispatch, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { updateRowData } from '../../generalSlices/rsuSlice'
import { RootState } from '../../store'
import { CountsListElement } from '../../models/Rsu'
import { toggleLayerActive } from '../../pages/mapSlice'
const { DateTime } = require('luxon')

const initialState = {
  currentSort: null as null | string,
  sortedCountList: [] as CountsListElement[],
  displayCounts: false,
  displayRsuErrors: false,
  menuSelection: [],
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
    let mst = DateTime.fromJSDate(e).setZone('America/Denver')
    let data
    if (type === 'start') {
      data = { start: mst.toString() }
    } else {
      data = { end: mst.toString() }
    }
    dispatch(updateRowData(data))
    return data
  }

export const toggleMapMenuSelection = createAsyncThunk(
  'menu/toggleMapMenuSelection',
  async (label: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    let menuSelection = [...selectMenuSelection(currentState)]
    if (menuSelection.includes(label)) {
      menuSelection = menuSelection.filter((item) => item !== label)
      switch (label) {
        case 'Display Message Counts':
          dispatch(setDisplay(null))
          break
        case 'Display RSU Status':
          dispatch(setDisplay(null))
      }
    } else {
      menuSelection = [...menuSelection, label]
      switch (label) {
        case 'Display Message Counts':
          if (menuSelection.includes('Display RSU Status')) {
            menuSelection = [...menuSelection.filter((item) => item !== 'Display RSU Status'), 'Display Message Counts']
          }
          dispatch(setDisplay('displayCounts'))
          break
        case 'Display RSU Status':
          if (menuSelection.includes('Display Message Counts')) {
            menuSelection = [...menuSelection.filter((item) => item !== 'Display Message Counts'), 'Display RSU Status']
          }
          dispatch(setDisplay('displayRsuErrors'))
      }
    }
    return menuSelection
  }
)

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
    setDisplay: (state, action: PayloadAction<String>) => {
      state.value.displayCounts = action.payload == 'displayCounts'
      state.value.displayRsuErrors = action.payload == 'displayRsuErrors'
    },
  },
  extraReducers: (builder) => {
    builder.addCase(toggleMapMenuSelection.fulfilled, (state, action) => {
      state.value.menuSelection = action.payload
    })
  },
})

export const { setCurrentSort, setSortedCountList, setDisplay } = menuSlice.actions

export const selectLoading = (state: RootState) => state.menu.loading
export const selectCurrentSort = (state: RootState) => state.menu.value.currentSort
export const selectSortedCountList = (state: RootState) => state.menu.value.sortedCountList
export const selectDisplayCounts = (state: RootState) => state.menu.value.displayCounts
export const selectDisplayRsuErrors = (state: RootState) => state.menu.value.displayRsuErrors
export const selectMenuSelection = (state: RootState) => state.menu.value.menuSelection

export default menuSlice.reducer

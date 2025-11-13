/* eslint-disable @typescript-eslint/no-unused-vars */
import { AnyAction, PayloadAction, ThunkDispatch, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { updateRowData } from '../../generalSlices/rsuSlice'
import { RootState } from '../../store'
import { CountsListElement } from '../../models/Rsu'
import { DateTime } from 'luxon'

const initialState = {
  sortedCountList: [] as CountsListElement[],
  displayCounts: false,
  displayRsuErrors: false,
  menuSelection: [],
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
    setDisplay: (state, action: PayloadAction<string>) => {
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
export const selectDisplayCounts = (state: RootState) => state.menu.value.displayCounts
export const selectDisplayRsuErrors = (state: RootState) => state.menu.value.displayRsuErrors
export const selectMenuSelection = (state: RootState) => state.menu.value.menuSelection

export default menuSlice.reducer

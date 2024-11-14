import reducer from './menuSlice'
import {
  // functions
  sortCountList,
  changeDate,

  // reducers
  setCurrentSort,
  setSortedCountList,
  setDisplay,

  // selectors
  selectLoading,
  selectCurrentSort,
  selectSortedCountList,
  selectDisplayCounts,
  selectView,
} from './menuSlice'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
const { DateTime } = require('luxon')

describe('menu reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        currentSort: null,
        sortedCountList: [],
        displayCounts: false,
        displayRsuErrors: false,
        view: 'buttons',
      },
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['menu'] = {
    loading: null,
    value: {
      currentSort: null,
      sortedCountList: null,
      displayCounts: false,
      displayRsuErrors: false,
      view: null,
    },
  }

  it('setCurrentSort reducer updates state correctly', async () => {
    const currentSort = 'currentSort'
    expect(reducer(initialState, setCurrentSort(currentSort))).toEqual({
      ...initialState,
      value: { ...initialState.value, currentSort },
    })
  })

  it('setSortedCountList reducer updates state correctly', async () => {
    const sortedCountList = 'sortedCountList'
    expect(reducer(initialState, setSortedCountList(sortedCountList))).toEqual({
      ...initialState,
      value: { ...initialState.value, sortedCountList },
    })
  })

  it('setDisplay reducer updates state correctly', async () => {
    let view = 'tab'
    expect(reducer(initialState, setDisplay({ view: 'tab', display: 'displayCounts' }))).toEqual({
      ...initialState,
      value: { ...initialState.value, view, displayCounts: true },
    })

    view = 'not tab'
    expect(reducer(initialState, setDisplay({ view: 'not tab', display: 'somethingElse' }))).toEqual({
      ...initialState,
      value: { ...initialState.value, view, displayCounts: false },
    })
  })
})

describe('functions', () => {
  it('sortCountList ascending', async () => {
    let dispatch = jest.fn()
    const key = 'key'
    const currentSort = 'keydesc'
    const countList = [{ key: 1 }, { key: 2 }] as any
    const resp = sortCountList(key, currentSort, countList)(dispatch)
    expect(resp).toEqual(countList)
    expect(dispatch).toHaveBeenCalledTimes(2)
  })

  it('sortCountList descending', async () => {
    let dispatch = jest.fn()
    const key = 'key'
    const currentSort = 'key'
    const countList = [{ key: 1 }, { key: 2 }] as any
    const resp = sortCountList(key, currentSort, countList)(dispatch)
    expect(resp).toEqual([{ key: 2 }, { key: 1 }])
    expect(dispatch).toHaveBeenCalledTimes(2)
  })

  it('changeDate start', async () => {
    let dispatch = jest.fn()
    const e = DateTime.fromISO('2021-01-01T07:00:00.000Z').toJSDate()
    const expected = { start: '2021-01-01T00:00:00.000-07:00' }
    const type = 'start'
    const requestOut = true
    apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, data: 'data' })
    const resp = changeDate(e, type, requestOut)(dispatch)
    expect(resp).toEqual(expected)
    expect(dispatch).toHaveBeenCalledTimes(1)
  })

  it('changeDate end', async () => {
    let dispatch = jest.fn()
    const e = DateTime.fromISO('2021-01-01T07:00:00.000Z').toJSDate()
    const expected = { end: '2021-01-01T00:00:00.000-07:00' }
    const type = 'end'
    const requestOut = true
    apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, data: 'data' })
    const resp = changeDate(e, type, requestOut)(dispatch)
    expect(resp).toEqual(expected)
    expect(dispatch).toHaveBeenCalledTimes(1)
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      display: 'display',
      currentSort: 'currentSort',
      sortedCountList: 'sortedCountList',
      displayCounts: 'displayCounts',
      view: 'view',
    },
  }
  const state = { menu: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectCurrentSort(state)).toEqual('currentSort')
    expect(selectSortedCountList(state)).toEqual('sortedCountList')
    expect(selectDisplayCounts(state)).toEqual('displayCounts')
    expect(selectView(state)).toEqual('view')
  })
})

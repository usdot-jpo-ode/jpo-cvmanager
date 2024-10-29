import reducer from './adminIntersectionTabSlice'
import {
  // async thunks
  updateTableData,
  deleteIntersection,
  deleteMultipleIntersections,

  // reducers
  setEditIntersectionRowData,

  // selectors
  selectLoading,
  selectTableData,
  selectColumns,
  selectEditIntersectionRowData,
} from './adminIntersectionTabSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin Intersection tab reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        tableData: [],
        title: 'Intersections',
        columns: [
          { title: 'Intersection ID', field: 'intersection_id', id: 0 },
          { title: 'Intersection Name', field: 'intersection_name', id: 1 },
          { title: 'Origin IP', field: 'origin_ip', id: 2 },
          { title: 'Linked RSUs', field: 'rsus', id: 3 },
        ],
        editIntersectionRowData: {},
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminIntersectionTab'] = {
    loading: null,
    value: {
      tableData: null,
      title: null,
      columns: null,
      editIntersectionRowData: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('updateTableData', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = updateTableData()

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('data')
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id: 'all' },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id: 'all' },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminIntersectionTab/updateTableData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let intersection_data = [{ rsus: ['1.1.1.1', '1.1.1.2'] }]
      let intersection_data_expected = [{ rsus: '1.1.1.1, 1.1.1.2' }]
      let state = reducer(initialState, {
        type: 'adminIntersectionTab/updateTableData/fulfilled',
        payload: { intersection_data },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, tableData: intersection_data_expected },
      })

      intersection_data = undefined
      state = reducer(initialState, {
        type: 'adminIntersectionTab/updateTableData/fulfilled',
        payload: { intersection_data },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, tableData: intersection_data },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminIntersectionTab/updateTableData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteIntersection', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const intersection_id = '1'
      let shouldUpdateTableData = true

      let action = deleteIntersection({ intersection_id, shouldUpdateTableData })

      apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      shouldUpdateTableData = false
      dispatch = jest.fn()
      action = deleteIntersection({ intersection_id, shouldUpdateTableData })

      apiHelper._deleteData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminIntersectionTab/deleteIntersection/pending',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let state = reducer(initialState, {
        type: 'adminIntersectionTab/deleteIntersection/fulfilled',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminIntersectionTab/deleteIntersection/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteMultipleIntersections', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rows = [{ intersection_id: '1' }, { intersection_id: '2' }, { intersection_id: '3' }] as any

      let action = deleteMultipleIntersections(rows)

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(rows.length + 1 + 2)
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminIntersectionTab'] = {
    loading: null,
    value: {
      tableData: null,
      title: null,
      columns: null,
      editIntersectionRowData: null,
    },
  }

  it('setEditIntersectionRowData reducer updates state correctly', async () => {
    const editIntersectionRowData = 'editIntersectionRowData'
    expect(reducer(initialState, setEditIntersectionRowData(editIntersectionRowData))).toEqual({
      ...initialState,
      value: { ...initialState.value, editIntersectionRowData },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      tableData: 'tableData',
      title: 'title',
      columns: 'columns',
      editIntersectionRowData: 'editIntersectionRowData',
    },
  }
  const state = { adminIntersectionTab: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectTableData(state)).toEqual('tableData')
    expect(selectColumns(state)).toEqual('columns')
    expect(selectEditIntersectionRowData(state)).toEqual('editIntersectionRowData')
  })
})

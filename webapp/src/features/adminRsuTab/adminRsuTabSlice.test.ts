import reducer from './adminRsuTabSlice'
import {
  // async thunks
  updateTableData,
  deleteRsu,
  deleteMultipleRsus,

  // reducers
  setEditRsuRowData,

  // selectors
  selectLoading,
  selectTableData,
  selectColumns,
  selectEditRsuRowData,
} from './adminRsuTabSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin RSU tab reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        activeDiv: 'rsu_table',
        tableData: [],
        title: 'RSUs',
        columns: [
          { title: 'Milepost', field: 'milepost', id: 0 },
          { title: 'IP Address', field: 'ip', id: 1 },
          { title: 'Primary Route', field: 'primary_route', id: 2 },
          { title: 'RSU Model', field: 'model', id: 3 },
          { title: 'Serial Number', field: 'serial_number', id: 4 },
        ],
        editRsuRowData: {},
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminRsuTab'] = {
    loading: null,
    value: {
      tableData: null,
      title: null,
      columns: null,
      editRsuRowData: null,
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
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip: 'all' },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip: 'all' },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminRsuTab/updateTableData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let rsu_data = 'rsu_data'
      let state = reducer(initialState, {
        type: 'adminRsuTab/updateTableData/fulfilled',
        payload: { rsu_data },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, tableData: rsu_data },
      })

      rsu_data = undefined
      state = reducer(initialState, {
        type: 'adminRsuTab/updateTableData/fulfilled',
        payload: { rsu_data },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, tableData: rsu_data },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminRsuTab/updateTableData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteRsu', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rsu_ip = '1.1.1.1'
      let shouldUpdateTableData = true

      let action = deleteRsu({ rsu_ip, shouldUpdateTableData })

      apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      shouldUpdateTableData = false
      dispatch = jest.fn()
      action = deleteRsu({ rsu_ip, shouldUpdateTableData })

      apiHelper._deleteData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminRsuTab/deleteRsu/pending',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let state = reducer(initialState, {
        type: 'adminRsuTab/deleteRsu/fulfilled',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminRsuTab/deleteRsu/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteMultipleRsus', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rows = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }, { ip: '1.1.1.3' }] as any

      let action = deleteMultipleRsus(rows)

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(rows.length + 1 + 2)
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminRsuTab'] = {
    loading: null,
    value: {
      tableData: null,
      title: null,
      columns: null,
      editRsuRowData: null,
    },
  }

  it('setEditRsuRowData reducer updates state correctly', async () => {
    const editRsuRowData = 'editRsuRowData'
    expect(reducer(initialState, setEditRsuRowData(editRsuRowData))).toEqual({
      ...initialState,
      value: { ...initialState.value, editRsuRowData },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      activeDiv: 'activeDiv',
      tableData: 'tableData',
      title: 'title',
      columns: 'columns',
      editRsuRowData: 'editRsuRowData',
    },
  }
  const state = { adminRsuTab: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectTableData(state)).toEqual('tableData')
    expect(selectColumns(state)).toEqual('columns')
    expect(selectEditRsuRowData(state)).toEqual('editRsuRowData')
  })
})

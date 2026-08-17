import reducer from './adminIntersectionTabSlice'
import {
  // reducers
  setEditIntersectionRowData,

  // selectors
  selectColumns,
  selectEditIntersectionRowData,
} from './adminIntersectionTabSlice'
import { RootState } from '../../store'

describe('admin Intersection tab reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      value: {
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

describe('reducers', () => {
  const initialState: RootState['adminIntersectionTab'] = {
    value: {
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
    value: {
      columns: 'columns',
      editIntersectionRowData: 'editIntersectionRowData',
    },
  }
  const state = { adminIntersectionTab: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectColumns(state)).toEqual('columns')
    expect(selectEditIntersectionRowData(state)).toEqual('editIntersectionRowData')
  })
})

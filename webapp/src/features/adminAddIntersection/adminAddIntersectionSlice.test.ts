import reducer from './adminAddIntersectionSlice'
import {
  // functions
  convertApiJsonToKeyedFormat,
  validateFormContents,
  mapFormToRequestJson,

  // reducers
  updateSelectedOrganizations,
  updateSelectedRsus,
  resetForm,

  // selectors
  selectSelectedOrganizations,
  selectSelectedRsus,
  selectLoading,
} from './adminAddIntersectionSlice'
import { RootState } from '../../store'

describe('admin add Intersection reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        selectedOrganizations: [],
        selectedRsus: [],
      },
    })
  })
})

describe('functions', () => {
  it('convertApiJsonToKeyedFormat', async () => {
    const apiJson = {
      organizations: ['org1', 'org2'],
      rsus: ['rsu1', 'rsu2'],
    }

    const expected = {
      organizations: [
        { id: 0, name: 'org1' },
        { id: 1, name: 'org2' },
      ],
      rsus: [
        { id: 0, name: 'rsu1' },
        { id: 1, name: 'rsu2' },
      ],
    }
    expect(convertApiJsonToKeyedFormat(apiJson)).toEqual(expected)
  })

  it('checkForm all invalid', async () => {
    expect(
      validateFormContents({
        value: {
          selectedOrganizations: [],
          selectedRsus: [],
        },
      } as any)
    ).toEqual(false)
  })

  it('checkForm all valid', async () => {
    expect(
      validateFormContents({
        value: {
          selectedOrganizations: ['org1'],
          selectedRsus: ['rsu1'],
        },
      } as any)
    ).toEqual(true)
  })

  it('updateJson', async () => {
    const data = {
      ref_pt: {
        latitude: '39.7392',
        longitude: '-104.9903',
      },
      intersection_name: 'a',
      intersection_id: '1',
    } as any

    const state = {
      value: {
        selectedOrganizations: [{ name: 'org1' }],
        selectedRsus: [{ name: 'rsu1' }],
      },
    } as any

    const expected = {
      ref_pt: {
        latitude: 39.7392,
        longitude: -104.9903,
      },
      intersection_id: 1,
      intersection_name: 'a',
      organizations: ['org1'],
      rsus: ['rsu1'],
    }

    expect(mapFormToRequestJson(data, state)).toEqual(expected)
  })
})

describe('reducers', () => {
  const initialState: RootState['adminAddIntersection'] = {
    loading: null,
    value: {
      selectedOrganizations: null,
      selectedRsus: null,
    },
  }

  it('updateSelectedOrganizations reducer updates state correctly', async () => {
    const selectedOrganizations = [{ id: 1, name: 'selectedOrganizations' }]
    expect(reducer(initialState, updateSelectedOrganizations(selectedOrganizations))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedOrganizations },
    })
  })

  it('updateSelectedRsus reducer updates state correctly', async () => {
    const selectedRsus = [{ id: 1, name: 'selectedRsus' }]
    expect(reducer(initialState, updateSelectedRsus(selectedRsus))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsus },
    })
  })

  it('resetForm reducer updates state correctly', async () => {
    const selectedOrganizations = [] as any
    const selectedRsus = [] as any
    expect(reducer(initialState, resetForm(selectedOrganizations))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        selectedOrganizations,
        selectedRsus,
      },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      selectedOrganizations: 'selectedOrganizations',
      selectedRsus: 'selectedRsus',
    },
  }
  const state = { adminAddIntersection: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectSelectedOrganizations(state)).toEqual('selectedOrganizations')
    expect(selectSelectedRsus(state)).toEqual('selectedRsus')
  })
})

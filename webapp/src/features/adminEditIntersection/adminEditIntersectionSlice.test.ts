import reducer from './adminEditIntersectionSlice'
import {
  // functions
  validateFormContents,
  mapFormToRequestJson,

  // reducers
  clear,
  setSelectedOrganizations,
  setSelectedRsus,
  setSubmitAttempt,
  updateStates,

  // selectors
  selectApiData,
  selectOrganizations,
  selectSelectedOrganizations,
  selectRsus,
  selectSelectedRsus,
  selectSubmitAttempt,
} from './adminEditIntersectionSlice'
import { RootState } from '../../store'

describe('admin edit Intersection reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      value: {
        apiData: undefined,
        organizations: [],
        selectedOrganizations: [],
        rsus: [],
        selectedRsus: [],
        submitAttempt: false,
      },
    })
  })
})

describe('functions', () => {
  it('checkForm selectedOrganizations', async () => {
    expect(
      validateFormContents({
        value: {
          selectedOrganizations: [],
          selectedRsus: [],
        },
      } as any)
    ).toEqual(false)
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
      intersection_name: 'a',
    } as any
    const state = {
      value: {
        apiData: {
          allowed_selections: {
            organizations: ['org1', 'org2', 'org4'],
            rsus: ['rsu1', 'rsu2', 'rsu4'],
          },
          intersection_data: {
            organizations: ['org2', 'org4'],
            rsus: ['rsu2', 'rsu4'],
          },
        },
        selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
        selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }, { name: 'rsu3' }],
      },
    } as any

    const expected = {
      intersection_name: 'a',
      organizations_to_add: ['org1'],
      organizations_to_remove: ['org4'],
      rsus_to_add: ['rsu1'],
      rsus_to_remove: ['rsu4'],
    }

    expect(mapFormToRequestJson(data, state)).toEqual(expected)
  })

  it('updateJson selectedRoute Other', async () => {
    const data = {
      intersection_name: 'a',
    } as any
    const state = {
      value: {
        apiData: {
          allowed_selections: {
            organizations: ['org1', 'org2', 'org4'],
            rsus: ['rsu1', 'rsu2', 'rsu4'],
          },
          intersection_data: {
            organizations: ['org2', 'org4'],
            rsus: ['rsu2', 'rsu4'],
          },
        },
        selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
        selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }, { name: 'rsu3' }],
      },
    } as any

    const expected = {
      intersection_name: 'a',
      organizations_to_add: ['org1'],
      organizations_to_remove: ['org4'],
      rsus_to_add: ['rsu1'],
      rsus_to_remove: ['rsu4'],
    }

    expect(mapFormToRequestJson(data, state)).toEqual(expected)
  })
})

describe('reducers', () => {
  const initialState: RootState['adminEditIntersection'] = {
    value: {
      apiData: undefined,
      organizations: [] as { name: string }[],
      selectedOrganizations: [] as { name: string }[],
      rsus: [] as { name: string }[],
      selectedRsus: [] as { name: string }[],
      submitAttempt: false,
    },
  }

  it('clear reducer updates state correctly', async () => {
    const selectedOrganizations = [{ name: 'selectedOrganizations' }]

    expect(reducer({ ...initialState, value: { ...initialState.value, selectedOrganizations } }, clear())).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
      },
    })
  })

  it('setSelectedOrganizations reducer updates state correctly', async () => {
    const selectedOrganizations = 'selectedOrganizations'
    expect(reducer(initialState, setSelectedOrganizations(selectedOrganizations))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedOrganizations },
    })
  })

  it('setSelectedRsus reducer updates state correctly', async () => {
    const selectedRsus = 'selectedRsus'
    expect(reducer(initialState, setSelectedRsus(selectedRsus))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsus },
    })
  })

  it('setSubmitAttempt reducer updates state correctly', async () => {
    expect(reducer(initialState, setSubmitAttempt(true))).toEqual({
      ...initialState,
      value: { ...initialState.value, submitAttempt: true },
    })
  })

  it('updateStates', async () => {
    const apiData = {
      allowed_selections: {
        organizations: ['org1', 'org2'],
        rsus: ['rsu1', 'rsu2'],
      },
      intersection_data: {
        organizations: ['org1', 'org2'],
        rsus: ['rsu1', 'rsu2'],
      },
    } as any

    const values = {
      organizations: [{ name: 'org1' }, { name: 'org2' }],
      rsus: [{ name: 'rsu1' }, { name: 'rsu2' }],
      selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }],
      selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }],
    }
    expect(reducer(initialState, updateStates(apiData))).toEqual({
      ...initialState,
      value: { ...initialState.value, ...values, apiData },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    value: {
      apiData: 'apiData',
      organizations: 'organizations',
      selectedOrganizations: 'selectedOrganizations',
      rsus: 'rsus',
      selectedRsus: 'selectedRsus',
      submitAttempt: 'submitAttempt',
    },
  }
  const state = { adminEditIntersection: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectApiData(state)).toEqual('apiData')
    expect(selectOrganizations(state)).toEqual('organizations')
    expect(selectSelectedOrganizations(state)).toEqual('selectedOrganizations')
    expect(selectRsus(state)).toEqual('rsus')
    expect(selectSelectedRsus(state)).toEqual('selectedRsus')
    expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
  })
})

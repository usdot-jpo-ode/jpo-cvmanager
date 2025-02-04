import reducer from './adminOrganizationTabSlice'
import {
  // async thunks
  getOrgData,
  deleteOrg,
  editOrg,

  // reducers
  updateTitle,
  setActiveDiv,
  setSelectedOrg,

  // selectors
  selectLoading,
  selectActiveDiv,
  selectTitle,
  selectOrgData,
  selectSelectedOrg,
  selectRsuTableData,
  selectUserTableData,
} from './adminOrganizationTabSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin organization tab reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        activeDiv: 'organization_table',
        title: 'Organizations',
        orgData: [],
        selectedOrg: {},
        rsuTableData: [],
        userTableData: [],
        intersectionTableData: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminOrganizationTab'] = {
    loading: null,
    value: {
      activeDiv: null,
      title: null,
      orgData: null,
      selectedOrg: null,
      rsuTableData: null,
      intersectionTableData: null,
      userTableData: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getOrgData', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const orgName = 'orgName'
      const specifiedOrg = 'specifiedOrg'
      let all = false
      const action = getOrgData({ orgName, all, specifiedOrg })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', all, specifiedOrg })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminOrg,
        token: 'token',
        query_params: { org_name: orgName },
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminOrg,
        token: 'token',
        query_params: { org_name: orgName },
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminOrganizationTab/getOrgData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled all', async () => {
      const loading = false
      let specifiedOrg = 'org2'
      const orgData = [{ id: 0, name: 'org1', rsu_count: 24, user_count: 12 }]
      let all = true
      const data = {
        org_data: [
          {
            id: 0,
            name: 'org1',
            user_count: 12,
            rsu_count: 24,
          },
        ],
      }
      let state = reducer(initialState, {
        type: 'adminOrganizationTab/getOrgData/fulfilled',
        payload: { data, all, specifiedOrg, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, orgData },
      })

      // test with no specifiedOrg
      specifiedOrg = null
      state = reducer(initialState, {
        type: 'adminOrganizationTab/getOrgData/fulfilled',
        payload: { data, all, specifiedOrg, success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, orgData, selectedOrg: orgData[0] },
      })
    })

    it('Updates the state correctly fulfilled not all', async () => {
      const loading = false
      let all = false
      const data = {
        org_data: {
          org_users: 'org_users',
          org_rsus: 'org_rsus',
          org_intersections: 'org_intersections',
        },
      }

      let state = reducer(initialState, {
        type: 'adminOrganizationTab/getOrgData/fulfilled',
        payload: { all, data, message: 'message', success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
          rsuTableData: data.org_data.org_rsus,
          userTableData: data.org_data.org_users,
          intersectionTableData: data.org_data.org_intersections,
        },
      })
    })

    it('Updates the state correctly fulfilled unsuccessful', async () => {
      const loading = false

      const state = reducer(initialState, {
        type: 'adminOrganizationTab/getOrgData/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminOrganizationTab/getOrgData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('deleteOrg', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const org_name = 'orgName'
      const action = deleteOrg(org_name)

      apiHelper._deleteData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
      let resp = await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminOrg,
        token: 'token',
        query_params: { org_name },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      dispatch = jest.fn()
      apiHelper._deleteData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(apiHelper._deleteData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminOrg,
        token: 'token',
        query_params: { org_name },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })
  })

  describe('editOrg', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { data: 'data', orig_name: 'orig_name' } as any
      const action = editOrg(json)

      apiHelper._patchData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '' })
      expect(apiHelper._patchData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminOrg,
        token: 'token',
        body: JSON.stringify({
          orig_name: 'orig_name',
          users_to_add: [],
          users_to_modify: [],
          users_to_remove: [],
          rsus_to_add: [],
          rsus_to_remove: [],
          intersections_to_add: [],
          intersections_to_remove: [],
          ...json,
        }),
      })

      apiHelper._patchData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._patchData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminOrg,
        token: 'token',
        body: JSON.stringify({
          orig_name: 'orig_name',
          users_to_add: [],
          users_to_modify: [],
          users_to_remove: [],
          rsus_to_add: [],
          rsus_to_remove: [],
          intersections_to_add: [],
          intersections_to_remove: [],
          ...json,
        }),
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminOrganizationTab/editOrg/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false

      let state = reducer(initialState, {
        type: 'adminOrganizationTab/editOrg/fulfilled',
        payload: { message: 'message', success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })

      // Error Case

      state = reducer(initialState, {
        type: 'adminOrganizationTab/editOrg/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminOrganizationTab/editOrg/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminOrganizationTab'] = {
    loading: null,
    value: {
      activeDiv: null,
      title: null,
      orgData: null,
      selectedOrg: null,
      rsuTableData: null,
      intersectionTableData: null,
      userTableData: null,
    },
  }

  it('updateTitle reducer updates state correctly', async () => {
    let activeDiv = 'organization_table'
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, activeDiv },
        },
        updateTitle()
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title: 'CV Manager Organizations' },
    })

    activeDiv = 'edit_organization'
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, activeDiv },
        },
        updateTitle()
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title: 'Edit Organization' },
    })

    activeDiv = 'add_organization'
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, activeDiv },
        },
        updateTitle()
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv, title: 'Add Organization' },
    })
  })

  it('setActiveDiv reducer updates state correctly', async () => {
    const activeDiv = 'activeDiv'
    expect(reducer(initialState, setActiveDiv(activeDiv))).toEqual({
      ...initialState,
      value: { ...initialState.value, activeDiv },
    })
  })

  it('setSelectedOrg reducer updates state correctly', async () => {
    const selectedOrg = 'selectedOrg'
    expect(reducer(initialState, setSelectedOrg(selectedOrg))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedOrg },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      activeDiv: 'activeDiv',
      title: 'title',
      orgData: 'orgData',
      selectedOrg: 'selectedOrg',
      rsuTableData: 'rsuTableData',
      userTableData: 'userTableData',
    },
  }
  const state = { adminOrganizationTab: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectActiveDiv(state)).toEqual('activeDiv')
    expect(selectTitle(state)).toEqual('title')
    expect(selectOrgData(state)).toEqual('orgData')
    expect(selectSelectedOrg(state)).toEqual('selectedOrg')
    expect(selectRsuTableData(state)).toEqual('rsuTableData')
    expect(selectUserTableData(state)).toEqual('userTableData')
  })
})

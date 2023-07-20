import reducer from './adminEditRsuSlice'
import {
  // async thunks
  getRsuInfo,
  editRsu,
  submitForm,

  // functions
  checkForm,
  updateJson,

  // reducers
  setSuccessMsg,
  updateSelectedRoute,
  setSelectedRoute,
  setSelectedModel,
  setSelectedSshGroup,
  setSelectedSnmpGroup,
  setSelectedSnmpVersion,
  setSelectedOrganizations,
  updateStates,

  // selectors
  selectLoading,
  selectSuccessMsg,
  selectApiData,
  selectErrorState,
  selectErrorMsg,
  selectPrimaryRoutes,
  selectSelectedRoute,
  selectOtherRouteDisabled,
  selectRsuModels,
  selectSelectedModel,
  selectSshCredentialGroups,
  selectSelectedSshGroup,
  selectSnmpCredentialGroups,
  selectSelectedSnmpGroup,
  selectSnmpVersions,
  selectSelectedSnmpVersion,
  selectOrganizations,
  selectSelectedOrganizations,
  selectSubmitAttempt,
} from './adminEditRsuSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'

describe('admin edit RSU reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        apiData: {},
        errorState: false,
        errorMsg: '',
        primaryRoutes: [],
        selectedRoute: '',
        otherRouteDisabled: true,
        rsuModels: [],
        selectedModel: '',
        sshCredentialGroups: [],
        selectedSshGroup: '',
        snmpCredentialGroups: [],
        selectedSnmpGroup: '',
        snmpVersions: [],
        selectedSnmpVersion: '',
        organizations: [],
        selectedOrganizations: [],
        submitAttempt: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    value: {
      successMsg: null,
      apiData: null,
      errorState: null,
      errorMsg: null,
      primaryRoutes: null,
      selectedRoute: null,
      otherRouteDisabled: null,
      rsuModels: null,
      selectedModel: null,
      sshCredentialGroups: null,
      selectedSshGroup: null,
      snmpCredentialGroups: null,
      selectedSnmpGroup: null,
      snmpVersions: null,
      selectedSnmpVersion: null,
      organizations: null,
      selectedOrganizations: null,
      submitAttempt: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getRsuInfo', () => {
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
      const action = getRsuInfo(rsu_ip)

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'body' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditRsu/getRsuInfo/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const apiData = 'apiData'
      let errorMsg = ''
      let errorState = false

      let state = reducer(
        { ...initialState, value: { ...initialState.value, apiData } },
        {
          type: 'adminEditRsu/getRsuInfo/fulfilled',
          payload: { message: 'message', success: true, data: apiData },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, errorMsg, errorState, apiData },
      })

      // Error Case
      errorMsg = 'message'
      errorState = true

      state = reducer(initialState, {
        type: 'adminEditRsu/getRsuInfo/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditRsu/getRsuInfo/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('editRsu', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { rsu_ip: '1.1.1.1' }
      const action = editRsu(json)

      global.setTimeout = jest.fn((cb) => cb())
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: true, message: 'Changes were successfully applied!' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminRsu,
          token: 'token',
          query_params: { rsu_ip: json.orig_ip },
          body: JSON.stringify(json),
        })
        expect(setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
      } catch (e) {
        global.setTimeout.mockClear()
        throw e
      }

      dispatch = jest.fn()
      global.setTimeout = jest.fn((cb) => cb())
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminRsu,
          token: 'token',
          query_params: { rsu_ip: json.orig_ip },
          body: JSON.stringify(json),
        })
        expect(setTimeout).not.toHaveBeenCalled()
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
      } catch (e) {
        global.setTimeout.mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditRsu/editRsu/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let successMsg = 'message'
      let errorMsg = ''
      let errorState = false

      let state = reducer(
        { ...initialState, value: { ...initialState.value } },
        {
          type: 'adminEditRsu/editRsu/fulfilled',
          payload: { message: 'message', success: true },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })

      // Error Case
      successMsg = ''
      errorMsg = 'message'
      errorState = true

      state = reducer(initialState, {
        type: 'adminEditRsu/editRsu/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditRsu/editRsu/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('submitForm', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      let getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditRsu: {
          value: {
            selectedRoute: 'I-25',
            selectedModel: 'model1',
            selectedSshGroup: 'group1',
            selectedSnmpGroup: 'group1snmp',
            selectedSnmpVersion: 'version_1',
            selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
            apiData: {
              allowed_selections: {
                organizations: ['org1', 'org2', 'org4'],
              },
              rsu_data: {
                organizations: ['org2', 'org4'],
              },
            },
          },
        },
      })
      const data = { data: 'data' }

      let action = submitForm(data)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(false)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      // invalid checkForm
      dispatch = jest.fn()
      getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditRsu: {
          value: {
            selectedRoute: '',
            selectedModel: '',
            selectedSshGroup: '',
            selectedSnmpGroup: '',
            selectedSnmpVersion: '',
            selectedOrganizations: [],
          },
        },
      })
      action = submitForm(data)
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(true)
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly fulfilled', async () => {
      const submitAttempt = 'submitAttempt'

      const state = reducer(initialState, {
        type: 'adminEditRsu/submitForm/fulfilled',
        payload: submitAttempt,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, submitAttempt },
      })
    })
  })
})

describe('functions', () => {
  it('checkForm selectedRoute', async () => {
    expect(
      checkForm({
        value: {
          selectedRoute: '',
        },
      })
    ).toEqual(false)
  })

  it('checkForm selectedModel', async () => {
    expect(
      checkForm({
        value: {
          selectedModel: '',
        },
      })
    ).toEqual(false)
  })

  it('checkForm selectedSshGroup', async () => {
    expect(
      checkForm({
        value: {
          selectedSshGroup: '',
        },
      })
    ).toEqual(false)
  })

  it('checkForm selectedSnmpGroup', async () => {
    expect(
      checkForm({
        value: {
          selectedSnmpGroup: '',
        },
      })
    ).toEqual(false)
  })

  it('checkForm selectedSnmpVersion', async () => {
    expect(
      checkForm({
        value: {
          selectedSnmpVersion: '',
        },
      })
    ).toEqual(false)
  })

  it('checkForm selectedOrganizations', async () => {
    expect(
      checkForm({
        value: {
          selectedOrganizations: [],
        },
      })
    ).toEqual(false)
  })

  it('checkForm all invalid', async () => {
    expect(
      checkForm({
        value: {
          selectedRoute: '',
          selectedModel: '',
          selectedSshGroup: '',
          selectedSnmpGroup: '',
          selectedSnmpVersion: '',
          selectedOrganizations: [],
        },
      })
    ).toEqual(false)
  })

  it('checkForm all valid', async () => {
    expect(
      checkForm({
        value: {
          selectedRoute: 'I-25',
          selectedModel: 'model1',
          selectedSshGroup: 'group1',
          selectedSnmpGroup: 'group1snmp',
          selectedSnmpVersion: 'version_1',
          selectedOrganizations: ['org1'],
        },
      })
    ).toEqual(true)
  })

  it('updateJson', async () => {
    const data = {
      milepost: 0.0,
    }
    const state = {
      value: {
        selectedRoute: 'selectedRoute',
        selectedModel: 'selectedModel',
        selectedSshGroup: 'selectedSshGroup',
        selectedSnmpGroup: 'selectedSnmpGroup',
        selectedSnmpVersion: 'selectedSnmpVersion',
        apiData: {
          allowed_selections: {
            organizations: ['org1', 'org2', 'org4'],
          },
          rsu_data: {
            organizations: ['org2', 'org4'],
          },
        },
        selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
      },
    }

    const expected = {
      milepost: 0.0,
      primary_route: 'selectedRoute',
      organizations_to_add: ['org1'],
      organizations_to_remove: ['org4'],
      model: 'selectedModel',
      ssh_credential_group: 'selectedSshGroup',
      snmp_credential_group: 'selectedSnmpGroup',
      snmp_version_group: 'selectedSnmpVersion',
    }

    expect(updateJson(data, state)).toEqual(expected)
  })

  it('updateJson selectedRoute Other', async () => {
    const data = {
      milepost: 0.0,
    }
    const state = {
      value: {
        selectedRoute: 'Other',
        selectedModel: 'selectedModel',
        selectedSshGroup: 'selectedSshGroup',
        selectedSnmpGroup: 'selectedSnmpGroup',
        selectedSnmpVersion: 'selectedSnmpVersion',
        apiData: {
          allowed_selections: {
            organizations: ['org1', 'org2', 'org4'],
          },
          rsu_data: {
            organizations: ['org2', 'org4'],
          },
        },
        selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
      },
    }

    const expected = {
      milepost: 0.0,
      organizations_to_add: ['org1'],
      organizations_to_remove: ['org4'],
      model: 'selectedModel',
      ssh_credential_group: 'selectedSshGroup',
      snmp_credential_group: 'selectedSnmpGroup',
      snmp_version_group: 'selectedSnmpVersion',
    }

    expect(updateJson(data, state)).toEqual(expected)
  })
})

describe('reducers', () => {
  const initialState = {
    loading: null,
    value: {
      selectedRsu: null,
    },
  }

  it('setSuccessMsg reducer updates state correctly', async () => {
    const successMsg = 'successMsg'
    expect(reducer(initialState, setSuccessMsg(successMsg))).toEqual({
      ...initialState,
      value: { ...initialState.value, successMsg },
    })
  })

  it('updateSelectedRoute reducer updates state correctly', async () => {
    let selectedRoute = 'selectedRoute'
    let otherRouteDisabled = false
    expect(reducer(initialState, updateSelectedRoute(selectedRoute))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRoute, otherRouteDisabled },
    })

    selectedRoute = 'Other'
    otherRouteDisabled = true
    expect(reducer(initialState, updateSelectedRoute(selectedRoute))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRoute, otherRouteDisabled },
    })
  })

  it('setSelectedRoute reducer updates state correctly', async () => {
    const selectedRoute = 'selectedRoute'
    expect(reducer(initialState, setSelectedRoute(selectedRoute))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRoute },
    })
  })

  it('setSelectedModel reducer updates state correctly', async () => {
    const selectedModel = 'selectedModel'
    expect(reducer(initialState, setSelectedModel(selectedModel))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedModel },
    })
  })

  it('setSelectedSshGroup reducer updates state correctly', async () => {
    const selectedSshGroup = 'selectedSshGroup'
    expect(reducer(initialState, setSelectedSshGroup(selectedSshGroup))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSshGroup },
    })
  })

  it('setSelectedSnmpGroup reducer updates state correctly', async () => {
    const selectedSnmpGroup = 'selectedSnmpGroup'
    expect(reducer(initialState, setSelectedSnmpGroup(selectedSnmpGroup))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSnmpGroup },
    })
  })

  it('setSelectedSnmpVersion reducer updates state correctly', async () => {
    const selectedSnmpVersion = 'selectedSnmpVersion'
    expect(reducer(initialState, setSelectedSnmpVersion(selectedSnmpVersion))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSnmpVersion },
    })
  })

  it('setSelectedOrganizations reducer updates state correctly', async () => {
    const selectedOrganizations = 'selectedOrganizations'
    expect(reducer(initialState, setSelectedOrganizations(selectedOrganizations))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedOrganizations },
    })
  })
  it('updateStates', async () => {
    // write test for updateApiJson
    const apiData = {
      allowed_selections: {
        primary_routes: ['I-25', 'I-70'],
        rsu_models: ['model1', 'model2'],
        ssh_credential_groups: ['group1', 'group2'],
        snmp_credential_groups: ['group1snmp', 'group2snmp'],
        snmp_version_groups: ['version_1', 'version_2'],
        organizations: ['org1', 'org2'],
      },
      rsu_data: {
        primary_route: 'I-25',
        model: 'model1',
        ssh_credential_group: 'group1',
        snmp_credential_group: 'group1snmp',
        snmp_version_group: 'version_1',
        organizations: ['org1', 'org2'],
      },
    }

    const values = {
      primaryRoutes: [{ name: 'I-25' }, { name: 'I-70' }],
      rsuModels: [{ name: 'model1' }, { name: 'model2' }],
      sshCredentialGroups: [{ name: 'group1' }, { name: 'group2' }],
      snmpCredentialGroups: [{ name: 'group1snmp' }, { name: 'group2snmp' }],
      snmpVersions: [{ name: 'version_1' }, { name: 'version_2' }],
      organizations: [{ name: 'org1' }, { name: 'org2' }],
      selectedRoute: 'I-25',
      selectedModel: 'model1',
      selectedSshGroup: 'group1',
      selectedSnmpGroup: 'group1snmp',
      selectedSnmpVersion: 'version_1',
      selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }],
    }
    expect(reducer(initialState, updateStates(apiData))).toEqual({
      ...initialState,
      value: { ...initialState.value, ...values, apiData },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      successMsg: 'successMsg',
      apiData: 'apiData',
      errorState: 'errorState',
      errorMsg: 'errorMsg',
      primaryRoutes: 'primaryRoutes',
      selectedRoute: 'selectedRoute',
      otherRouteDisabled: 'otherRouteDisabled',
      rsuModels: 'rsuModels',
      selectedModel: 'selectedModel',
      sshCredentialGroups: 'sshCredentialGroups',
      selectedSshGroup: 'selectedSshGroup',
      snmpCredentialGroups: 'snmpCredentialGroups',
      selectedSnmpGroup: 'selectedSnmpGroup',
      snmpVersions: 'snmpVersions',
      selectedSnmpVersion: 'selectedSnmpVersion',
      organizations: 'organizations',
      selectedOrganizations: 'selectedOrganizations',
      submitAttempt: 'submitAttempt',
    },
  }
  const state = { adminEditRsu: initialState }

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectSuccessMsg(state)).toEqual('successMsg')
    expect(selectApiData(state)).toEqual('apiData')
    expect(selectErrorState(state)).toEqual('errorState')
    expect(selectErrorMsg(state)).toEqual('errorMsg')
    expect(selectPrimaryRoutes(state)).toEqual('primaryRoutes')
    expect(selectSelectedRoute(state)).toEqual('selectedRoute')
    expect(selectOtherRouteDisabled(state)).toEqual('otherRouteDisabled')
    expect(selectRsuModels(state)).toEqual('rsuModels')
    expect(selectSelectedModel(state)).toEqual('selectedModel')
    expect(selectSshCredentialGroups(state)).toEqual('sshCredentialGroups')
    expect(selectSelectedSshGroup(state)).toEqual('selectedSshGroup')
    expect(selectSnmpCredentialGroups(state)).toEqual('snmpCredentialGroups')
    expect(selectSelectedSnmpGroup(state)).toEqual('selectedSnmpGroup')
    expect(selectSnmpVersions(state)).toEqual('snmpVersions')
    expect(selectSelectedSnmpVersion(state)).toEqual('selectedSnmpVersion')
    expect(selectOrganizations(state)).toEqual('organizations')
    expect(selectSelectedOrganizations(state)).toEqual('selectedOrganizations')
    expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
  })
})

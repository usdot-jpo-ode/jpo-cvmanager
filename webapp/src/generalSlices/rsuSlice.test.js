import reducer from './rsuSlice'
import {
  // async thunks
  getRsuData,
  getRsuInfoOnly,
  getRsuLastOnline,
  _getRsuInfo,
  _getRsuOnlineStatus,
  _getRsuCounts,
  _getRsuMapInfo,
  getSsmSrmData,
  getIssScmsStatus,
  updateRowData,
  updateBsmData,
  getMapData,

  // functions
  updateMessageType,

  // reducers
  selectRsu,
  toggleMapDisplay,
  clearBsm,
  toggleSsmSrmDisplay,
  setSelectedSrm,
  toggleBsmPointSelect,
  updateBsmPoints,
  updateBsmDate,
  triggerBsmDateError,
  changeMessageType,
  setBsmFilter,
  setBsmFilterStep,
  setBsmFilterOffset,
  setLoading,

  // selectors
  selectLoading,
  selectRequestOut,
  selectSelectedRsu,
  selectRsuManufacturer,
  selectRsuIpv4,
  selectRsuPrimaryRoute,
  selectRsuData,
  selectRsuOnlineStatus,
  selectRsuCounts,
  selectCountList,
  selectCurrentSort,
  selectStartDate,
  selectEndDate,
  selectMessageLoading,
  selectWarningMessage,
  selectMsgType,
  selectRsuMapData,
  selectMapList,
  selectMapDate,
  selectDisplayMap,
  selectBsmStart,
  selectBsmEnd,
  selectAddBsmPoint,
  selectBsmCoordinates,
  selectBsmData,
  selectBsmDateError,
  selectBsmFilter,
  selectBsmFilterStep,
  selectBsmFilterOffset,
  selectIssScmsStatusData,
  selectSsmDisplay,
  selectSrmSsmList,
  selectSelectedSrm,
  selectHeatMapData,
} from './rsuSlice'
import RsuApi from '../apis/rsu-api'

describe('rsu reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      requestOut: false,
      value: {
        selectedRsu: null,
        rsuData: [],
        rsuOnlineStatus: {},
        rsuCounts: {},
        countList: [],
        currentSort: '',
        startDate: '',
        endDate: '',
        heatMapData: {
          features: [],
          type: 'FeatureCollection',
        },
        messageLoading: false,
        warningMessage: false,
        msgType: 'BSM',
        rsuMapData: {},
        mapList: [],
        mapDate: '',
        displayMap: false,
        bsmStart: '',
        bsmEnd: '',
        addBsmPoint: false,
        bsmCoordinates: [],
        bsmData: [],
        bsmDateError: false,
        bsmFilter: false,
        bsmFilterStep: 60,
        bsmFilterOffset: 0,
        issScmsStatusData: {},
        ssmDisplay: false,
        srmSsmList: [],
        selectedSrm: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    requestOut: null,
    value: {
      selectedRsu: null,
      rsuData: null,
      rsuOnlineStatus: null,
      rsuCounts: null,
      countList: null,
      currentSort: null,
      startDate: null,
      endDate: null,
      heatMapData: {
        features: [],
        type: 'FeatureCollection',
      },
      messageLoading: null,
      warningMessage: null,
      msgType: null,
      rsuMapData: null,
      mapList: null,
      mapDate: null,
      displayMap: null,
      bsmStart: null,
      bsmEnd: null,
      addBsmPoint: null,
      bsmCoordinates: null,
      bsmData: null,
      bsmDateError: null,
      bsmFilter: null,
      bsmFilterStep: null,
      bsmFilterOffset: null,
      issScmsStatusData: null,
      ssmDisplay: null,
      srmSsmList: null,
      selectedSrm: null,
    },
  }

  beforeAll(() => {
    jest.mock('../apis/rsu-api.js')
  })

  afterAll(() => {
    jest.unmock('../apis/rsu-api.js')
  })

  describe('getRsuData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
        rsu: {
          value: {
            rsuOnlineStatus: {},
            startDate: '',
            endDate: '',
          },
        },
      })
      const action = getRsuData()

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(4 + 2) // 4 for the 4 dispatched actions, 2 for the pending and fulfilled actions
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let rsuData = []
      let rsuOnlineStatus = {}
      let rsuCounts = {}
      let countList = []
      const state = reducer(initialState, {
        type: 'rsu/getRsuData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
          rsuData,
          rsuOnlineStatus,
          rsuCounts,
          countList,
        },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let rsuCounts = { ipv4_address: { count: 4 } }
      let rsuData = [
        {
          properties: {
            ipv4_address: 'ipv4_address',
          },
          geometry: {
            coordinates: [-104.999824, 39.750392],
          },
        },
      ]
      const state = reducer(
        { ...initialState, value: { ...initialState.value, rsuData, rsuCounts } },
        {
          type: 'rsu/getRsuData/fulfilled',
        }
      )

      let heatMapData = {
        features: [
          {
            type: 'Feature',
            geometry: {
              type: 'Point',
              coordinates: [-104.999824, 39.750392],
            },
            properties: {
              ipv4_address: 'ipv4_address',
              count: 4,
            },
          },
        ],
        type: 'FeatureCollection',
      }
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, rsuData, rsuCounts, heatMapData },
      })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'rsu/getRsuData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('getRsuInfoOnly', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const action = getRsuInfoOnly()

      const rsuData = ['1.1.1.1']
      RsuApi.getRsuInfo = jest.fn().mockReturnValue({ rsuList: rsuData })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuData)
      expect(RsuApi.getRsuInfo).toHaveBeenCalledWith('token', 'name')
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      const state = reducer(initialState, {
        type: 'rsu/getRsuInfoOnly/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'rsu/getRsuInfoOnly/fulfilled',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'rsu/getRsuInfoOnly/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('getRsuLastOnline', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const rsu_ip = '1.1.1.1'
      const action = getRsuLastOnline(rsu_ip)

      RsuApi.getRsuOnline = jest.fn().mockReturnValue(rsu_ip)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsu_ip)
      expect(RsuApi.getRsuOnline).toHaveBeenCalledWith('token', 'name', '', { rsu_ip })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      const state = reducer(initialState, {
        type: 'rsu/getRsuLastOnline/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let rsuOnlineStatus = { '1.1.1.1': {} }
      const payload = { last_online: '2021-03-01T00:00:00.000000Z', ip: '1.1.1.1' }
      const state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, rsuOnlineStatus },
        },
        {
          type: 'rsu/getRsuLastOnline/fulfilled',
          payload: payload,
        }
      )

      rsuOnlineStatus = { '1.1.1.1': { last_online: '2021-03-01T00:00:00.000000Z' } }
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, rsuOnlineStatus },
      })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'rsu/getRsuLastOnline/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('_getRsuInfo', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const action = _getRsuInfo()

      const rsuList = ['1.1.1.1']
      RsuApi.getRsuInfo = jest.fn().mockReturnValue({ rsuList })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuList)
      expect(RsuApi.getRsuInfo).toHaveBeenCalledWith('token', 'name')
    })

    it('Updates the state correctly fulfilled', async () => {
      const rsuData = 'rsuData'
      const state = reducer(initialState, {
        type: 'rsu/_getRsuInfo/fulfilled',
        payload: rsuData,
      })
      expect(state).toEqual({ ...initialState, value: { ...initialState.value, rsuData } })
    })
  })

  describe('_getRsuOnlineStatus', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const action = _getRsuOnlineStatus({
        rsuOnlineStatusState: 'rsuOnlineStatusState',
      })

      const rsuOnlineStatus = 'rsuOnlineStatus'
      RsuApi.getRsuOnline = jest.fn().mockReturnValue(rsuOnlineStatus)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuOnlineStatus)
      expect(RsuApi.getRsuOnline).toHaveBeenCalledWith('token', 'name')
    })

    it('returns and calls the api correctly default value', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const action = _getRsuOnlineStatus('rsuOnlineStatusState')

      const rsuOnlineStatus = null
      RsuApi.getRsuOnline = jest.fn().mockReturnValue(rsuOnlineStatus)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('rsuOnlineStatusState')
      expect(RsuApi.getRsuOnline).toHaveBeenCalledWith('token', 'name')
    })

    it('Updates the state correctly fulfilled', async () => {
      const rsuOnlineStatus = 'rsuOnlineStatus'
      const state = reducer(initialState, {
        type: 'rsu/_getRsuOnlineStatus/fulfilled',
        payload: rsuOnlineStatus,
      })
      expect(state).toEqual({ ...initialState, value: { ...initialState.value, rsuOnlineStatus } })
    })
  })

  describe('_getRsuCounts', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
        rsu: {
          value: {
            msgType: 'BSM',
            startDate: '',
            endDate: '',
          },
        },
      })
      const action = _getRsuCounts()

      const rsuCounts = {
        '1.1.1.1': { road: 'road', count: 'count' },
      }
      const countList = [
        {
          key: '1.1.1.1',
          rsu: '1.1.1.1',
          road: 'road',
          count: 'count',
        },
      ]
      RsuApi.getRsuCounts = jest.fn().mockReturnValue(rsuCounts)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ rsuCounts, countList })
      expect(RsuApi.getRsuCounts).toHaveBeenCalledWith('token', 'name', '', {
        message: 'BSM',
        start: '',
        end: '',
      })
    })
    it('returns and calls the api correctly', async () => {
      const rsuCounts = {
        '1.1.1.1': { road: 'road', count: 'count' },
      }

      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
        rsu: {
          value: {
            msgType: 'BSM',
            startDate: '',
            endDate: '',
            rsuCounts,
          },
        },
      })

      const action = _getRsuCounts()
      const countList = [
        {
          key: '1.1.1.1',
          rsu: '1.1.1.1',
          road: 'road',
          count: 'count',
        },
      ]
      RsuApi.getRsuCounts = jest.fn().mockReturnValue(null)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ rsuCounts, countList })
      expect(RsuApi.getRsuCounts).toHaveBeenCalledWith('token', 'name', '', {
        message: 'BSM',
        start: '',
        end: '',
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let rsuCounts = 'rsuCounts'
      let countList = 'countList'
      const payload = { rsuCounts, countList }
      const state = reducer(initialState, {
        type: 'rsu/_getRsuCounts/fulfilled',
        payload: payload,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, rsuCounts, countList },
      })
    })
  })

  describe('_getRsuMapInfo', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const action = _getRsuMapInfo({
        startDate: 'startDate',
        endDate: 'endDate',
      })

      RsuApi.getRsuMapInfo = jest.fn().mockReturnValue('rsuMapData')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ endDate: 'endDate', rsuMapData: 'rsuMapData', startDate: 'startDate' })
      expect(RsuApi.getRsuMapInfo).toHaveBeenCalledWith('token', 'name', '', { ip_list: 'True' })
    })

    it('Updates the state correctly fulfilled', async () => {
      const startDate = 'startDate'
      const endDate = 'endDate'
      const mapList = 'mapList'
      const payload = { startDate, endDate, rsuMapData: mapList }
      const state = reducer(initialState, {
        type: 'rsu/_getRsuMapInfo/fulfilled',
        payload: payload,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, startDate, endDate, mapList },
      })
    })
  })

  describe('getSsmSrmData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = getSsmSrmData()

      RsuApi.getSsmSrmData = jest.fn().mockReturnValue('srmSsmList')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('srmSsmList')
      expect(RsuApi.getSsmSrmData).toHaveBeenCalledWith('token')
    })

    it('Updates the state correctly fulfilled', async () => {
      const srmSsmList = 'srmSsmList'
      const state = reducer(initialState, {
        type: 'rsu/getSsmSrmData/fulfilled',
        payload: srmSsmList,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, srmSsmList },
      })
    })
  })

  describe('getIssScmsStatus', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const action = getIssScmsStatus()

      RsuApi.getIssScmsStatus = jest.fn().mockReturnValue('issScmsStatus')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('issScmsStatus')
      expect(RsuApi.getIssScmsStatus).toHaveBeenCalledWith('token', 'name')
    })

    it('Updates the state correctly fulfilled', async () => {
      const issScmsStatusData = 'issScmsStatus'
      const state = reducer(initialState, {
        type: 'rsu/getIssScmsStatus/fulfilled',
        payload: issScmsStatusData,
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, issScmsStatusData },
      })
    })

    it('Updates the state correctly fulfilled default value', async () => {
      const issScmsStatusData = 'issScmsStatus'
      const state = reducer(
        { ...initialState, value: { ...initialState.value, issScmsStatusData } },
        {
          type: 'rsu/getIssScmsStatus/fulfilled',
          payload: null,
        }
      )

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, issScmsStatusData },
      })
    })
  })

  describe('updateRowData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
      })
      const data = {
        message: 'message',
        start: 1,
        end: 86400000,
      }
      const action = updateRowData(data)

      const rsuCounts = {
        '1.1.1.1': { road: 'road', count: 'count' },
      }
      const countList = [
        {
          key: '1.1.1.1',
          rsu: '1.1.1.1',
          road: 'road',
          count: 'count',
        },
      ]
      RsuApi.getRsuCounts = jest.fn().mockReturnValue(rsuCounts)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({
        msgType: 'message',
        startDate: 1,
        endDate: 86400000,
        warningMessage: false,
        rsuCounts,
        countList,
      })
      expect(RsuApi.getRsuCounts).toHaveBeenCalledWith('token', 'name', '', data)
    })

    it('returns and calls the api correctly default values', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
        rsu: {
          value: {
            msgType: 'message',
            startDate: 1,
            endDate: 86400002,
          },
        },
      })
      const data = {}
      const action = updateRowData(data)

      const rsuCounts = {
        '1.1.1.1': { road: 'road', count: 'count' },
      }
      const countList = [
        {
          key: '1.1.1.1',
          rsu: '1.1.1.1',
          road: 'road',
          count: 'count',
        },
      ]
      RsuApi.getRsuCounts = jest.fn().mockReturnValue(rsuCounts)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({
        msgType: 'message',
        startDate: 1,
        endDate: 86400002,
        warningMessage: true,
        rsuCounts,
        countList,
      })
      expect(RsuApi.getRsuCounts).toHaveBeenCalledWith('token', 'name', '', {
        message: 'message',
        start: 1,
        end: 86400002,
      })
    })

    it('Updates the state correctly pending', async () => {
      const requestOut = true
      const messageLoading = false
      const state = reducer(initialState, {
        type: 'rsu/updateRowData/pending',
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, requestOut, messageLoading },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const rsuCounts = { '1.1.1.1': { count: 5 } }
      const countList = 'countList'
      const heatMapData = {
        type: 'FeatureCollection',
        features: [
          {
            properties: {
              ipv4_address: '1.1.1.1',
            },
          },
          {
            properties: {
              ipv4_address: '1.1.1.2',
            },
          },
        ],
      }
      const warningMessage = 'warningMessage'
      const requestOut = false
      const messageLoading = false
      const msgType = 'msgType'
      const startDate = 'startDate'
      const endDate = 'endDate'
      const payload = {
        rsuCounts,
        countList,
        warningMessage,
        msgType,
        startDate,
        endDate,
      }
      const state = reducer(
        {
          ...initialState,
          value: {
            ...initialState.value,
            heatMapData,
          },
        },
        {
          type: 'rsu/updateRowData/fulfilled',
          payload: payload,
        }
      )

      heatMapData['features'][0]['properties']['count'] = 5
      heatMapData['features'][1]['properties']['count'] = 0

      expect(state).toEqual({
        ...initialState,
        value: {
          ...initialState.value,
          rsuCounts,
          countList,
          heatMapData,
          warningMessage,
          requestOut,
          messageLoading,
          msgType,
          startDate,
          endDate,
        },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const requestOut = false
      const messageLoading = false
      const state = reducer(initialState, {
        type: 'rsu/updateRowData/rejected',
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, requestOut, messageLoading },
      })
    })
  })

  describe('updateBsmData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        rsu: {
          value: {
            bsmStart: 'bsmStart',
            bsmEnd: 'bsmEnd',
            bsmCoordinates: [1, 2, 3],
          },
        },
      })
      const action = updateBsmData()

      RsuApi.postBsmData = jest.fn().mockReturnValue('bsmCounts')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('bsmCounts')
      expect(RsuApi.postBsmData).toHaveBeenCalledWith(
        'token',
        JSON.stringify({
          start: 'bsmStart',
          end: 'bsmEnd',
          geometry: [1, 2, 3],
        }),
        ''
      )
    })

    it('condition blocks execution', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        rsu: {
          value: {
            bsmStart: '',
            bsmEnd: '',
            bsmCoordinates: [1, 2],
          },
        },
      })
      const action = updateBsmData()

      RsuApi.postBsmData = jest.fn().mockReturnValue('bsmCounts')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(undefined)
      expect(RsuApi.postBsmData).not.toHaveBeenCalled()
    })

    it('Updates the state correctly pending', async () => {
      const addBsmPoint = false
      const loading = true
      const bsmStart = 1
      const bsmEnd = 86400000
      const bsmDateError = false
      const state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, bsmStart, bsmEnd },
        },
        {
          type: 'rsu/updateBsmData/pending',
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, addBsmPoint, bsmDateError, bsmStart, bsmEnd },
      })
    })

    it('Updates the state correctly pending date error', async () => {
      const addBsmPoint = false
      const loading = true
      const bsmStart = 1
      const bsmEnd = 86400002
      const bsmDateError = true
      const state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, bsmStart, bsmEnd },
        },
        {
          type: 'rsu/updateBsmData/pending',
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, addBsmPoint, bsmDateError, bsmStart, bsmEnd },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const bsmData = 'bsmData'
      const loading = false
      const bsmFilter = true
      const bsmFilterStep = 60
      const bsmFilterOffset = 0
      const state = reducer(initialState, {
        type: 'rsu/updateBsmData/fulfilled',
        payload: { body: bsmData },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
          bsmData,
          bsmFilter,
          bsmFilterStep,
          bsmFilterOffset,
        },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'rsu/updateBsmData/rejected',
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })
  })

  describe('getMapData', () => {
    it('condition blocks execution', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
        rsu: {
          value: {
            selectedRsu: { properties: { ipv4_address: '1.1.1.1' } },
          },
        },
      })
      const action = getMapData()

      RsuApi.getRsuMapInfo = jest.fn().mockReturnValue({ geojson: 'geojson', date: 'date' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({
        rsuMapData: 'geojson',
        mapDate: 'date',
      })
      expect(RsuApi.getRsuMapInfo).toHaveBeenCalledWith('token', 'name', '', { ip_address: '1.1.1.1' })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'rsu/getMapData/pending',
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const rsuMapData = 'rsuMapData'
      const mapDate = 'mapDate'
      const state = reducer(initialState, {
        type: 'rsu/getMapData/fulfilled',
        payload: { rsuMapData, mapDate },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, rsuMapData, mapDate },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'rsu/getMapData/rejected',
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })
  })
})

describe('functions', () => {
  it('updateMessageType', async () => {
    const dispatch = jest.fn()

    updateMessageType('messageType')(dispatch)
    expect(dispatch).toHaveBeenCalledTimes(2)
  })
})

describe('reducers', () => {
  const initialState = {
    loading: null,
    requestOut: null,
    value: {
      selectedRsu: null,
      rsuData: null,
      rsuOnlineStatus: null,
      rsuCounts: null,
      countList: null,
      currentSort: null,
      startDate: null,
      endDate: null,
      heatMapData: {
        features: [],
        type: 'FeatureCollection',
      },
      messageLoading: null,
      warningMessage: null,
      msgType: null,
      rsuMapData: null,
      mapList: null,
      mapDate: null,
      displayMap: null,
      bsmStart: null,
      bsmEnd: null,
      addBsmPoint: null,
      bsmCoordinates: null,
      bsmData: null,
      bsmDateError: null,
      bsmFilter: null,
      bsmFilterStep: null,
      bsmFilterOffset: null,
      issScmsStatusData: null,
      ssmDisplay: null,
      srmSsmList: null,
      selectedSrm: null,
    },
  }

  it('selectRsu reducer updates state correctly', async () => {
    const selectedRsu = 'selectedRsu'
    expect(reducer(initialState, selectRsu(selectedRsu))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsu },
    })
  })

  it('toggleMapDisplay reducer updates state correctly', async () => {
    expect(
      reducer({ ...initialState, value: { ...initialState.value, displayMap: true } }, toggleMapDisplay())
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, displayMap: false },
    })
  })

  it('clearBsm reducer updates state correctly', async () => {
    expect(reducer(initialState, clearBsm())).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmCoordinates: [], bsmData: [], bsmStart: '', bsmEnd: '', bsmDateError: false },
    })
  })

  it('toggleSsmSrmDisplay reducer updates state correctly', async () => {
    expect(
      reducer({ ...initialState, value: { ...initialState.value, ssmDisplay: true } }, toggleSsmSrmDisplay())
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, ssmDisplay: false },
    })
  })

  it('setSelectedSrm reducer updates state correctly', async () => {
    let selectedSrm = { selectedSrm: 1 }
    expect(reducer(initialState, setSelectedSrm(selectedSrm))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSrm: [selectedSrm] },
    })

    expect(reducer(initialState, setSelectedSrm({}))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSrm: [] },
    })
  })

  it('toggleBsmPointSelect reducer updates state correctly', async () => {
    expect(
      reducer({ ...initialState, value: { ...initialState.value, addBsmPoint: true } }, toggleBsmPointSelect())
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, addBsmPoint: false },
    })
  })

  it('updateBsmPoints reducer updates state correctly', async () => {
    const bsmCoordinates = 'bsmCoordinates'
    expect(reducer(initialState, updateBsmPoints(bsmCoordinates))).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmCoordinates },
    })
  })

  it('updateBsmDate reducer updates state correctly', async () => {
    let type = 'start'
    const date = 'date'
    expect(reducer(initialState, updateBsmDate({ type, date }))).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmStart: 'date' },
    })

    type = 'end'
    expect(reducer(initialState, updateBsmDate({ type, date }))).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmEnd: 'date' },
    })
  })

  it('triggerBsmDateError reducer updates state correctly', async () => {
    expect(reducer(initialState, triggerBsmDateError())).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmDateError: true },
    })
  })

  it('changeMessageType reducer updates state correctly', async () => {
    const msgType = 'msgType'
    expect(reducer(initialState, changeMessageType(msgType))).toEqual({
      ...initialState,
      value: { ...initialState.value, msgType },
    })
  })

  it('setBsmFilter reducer updates state correctly', async () => {
    const bsmFilter = 'bsmFilter'
    expect(reducer(initialState, setBsmFilter(bsmFilter))).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmFilter },
    })
  })

  it('setBsmFilterStep reducer updates state correctly', async () => {
    const bsmFilterStep = 'bsmFilterStep'
    expect(reducer(initialState, setBsmFilterStep(bsmFilterStep))).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmFilterStep },
    })
  })

  it('setBsmFilterOffset reducer updates state correctly', async () => {
    const bsmFilterOffset = 'bsmFilterOffset'
    expect(reducer(initialState, setBsmFilterOffset(bsmFilterOffset))).toEqual({
      ...initialState,
      value: { ...initialState.value, bsmFilterOffset },
    })
  })

  it('setLoading reducer updates state correctly', async () => {
    const loading = 'loading'
    expect(reducer(initialState, setLoading(loading))).toEqual({
      ...initialState,
      loading,
      value: { ...initialState.value },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    requestOut: 'requestOut',
    value: {
      selectedRsu: {
        properties: {
          manufacturer_name: 'manufacturer_name',
          ipv4_address: 'ipv4_address',
          primary_route: 'primary_route',
        },
      },
      rsuData: 'rsuData',
      rsuOnlineStatus: 'rsuOnlineStatus',
      rsuCounts: 'rsuCounts',
      countList: 'countList',
      currentSort: 'currentSort',
      startDate: 'startDate',
      endDate: 'endDate',
      heatMapData: 'heatMapData',
      messageLoading: 'messageLoading',
      warningMessage: 'warningMessage',
      msgType: 'msgType',
      rsuMapData: 'rsuMapData',
      mapList: 'mapList',
      mapDate: 'mapDate',
      displayMap: 'displayMap',
      bsmStart: 'bsmStart',
      bsmEnd: 'bsmEnd',
      addBsmPoint: 'addBsmPoint',
      bsmCoordinates: 'bsmCoordinates',
      bsmData: 'bsmData',
      bsmDateError: 'bsmDateError',
      bsmFilter: 'bsmFilter',
      bsmFilterStep: 'bsmFilterStep',
      bsmFilterOffset: 'bsmFilterOffset',
      issScmsStatusData: 'issScmsStatusData',
      ssmDisplay: 'ssmDisplay',
      srmSsmList: 'srmSsmList',
      selectedSrm: 'selectedSrm',
    },
  }
  const rsuState = { rsu: initialState }

  it('selectors return the correct value', async () => {
    expect(selectLoading(rsuState)).toEqual('loading')
    expect(selectRequestOut(rsuState)).toEqual('requestOut')

    expect(selectSelectedRsu(rsuState)).toEqual(initialState.value.selectedRsu)
    expect(selectRsuManufacturer(rsuState)).toEqual('manufacturer_name')
    expect(selectRsuIpv4(rsuState)).toEqual('ipv4_address')
    expect(selectRsuPrimaryRoute(rsuState)).toEqual('primary_route')
    expect(selectRsuData(rsuState)).toEqual('rsuData')
    expect(selectRsuOnlineStatus(rsuState)).toEqual('rsuOnlineStatus')
    expect(selectRsuCounts(rsuState)).toEqual('rsuCounts')
    expect(selectCountList(rsuState)).toEqual('countList')
    expect(selectCurrentSort(rsuState)).toEqual('currentSort')
    expect(selectStartDate(rsuState)).toEqual('startDate')
    expect(selectEndDate(rsuState)).toEqual('endDate')
    expect(selectMessageLoading(rsuState)).toEqual('messageLoading')
    expect(selectWarningMessage(rsuState)).toEqual('warningMessage')
    expect(selectMsgType(rsuState)).toEqual('msgType')
    expect(selectRsuMapData(rsuState)).toEqual('rsuMapData')
    expect(selectMapList(rsuState)).toEqual('mapList')
    expect(selectMapDate(rsuState)).toEqual('mapDate')
    expect(selectDisplayMap(rsuState)).toEqual('displayMap')
    expect(selectBsmStart(rsuState)).toEqual('bsmStart')
    expect(selectBsmEnd(rsuState)).toEqual('bsmEnd')
    expect(selectAddBsmPoint(rsuState)).toEqual('addBsmPoint')
    expect(selectBsmCoordinates(rsuState)).toEqual('bsmCoordinates')
    expect(selectBsmData(rsuState)).toEqual('bsmData')
    expect(selectBsmDateError(rsuState)).toEqual('bsmDateError')
    expect(selectBsmFilter(rsuState)).toEqual('bsmFilter')
    expect(selectBsmFilterStep(rsuState)).toEqual('bsmFilterStep')
    expect(selectBsmFilterOffset(rsuState)).toEqual('bsmFilterOffset')
    expect(selectIssScmsStatusData(rsuState)).toEqual('issScmsStatusData')
    expect(selectSsmDisplay(rsuState)).toEqual('ssmDisplay')
    expect(selectSrmSsmList(rsuState)).toEqual('srmSsmList')
    expect(selectSelectedSrm(rsuState)).toEqual('selectedSrm')
    expect(selectHeatMapData(rsuState)).toEqual('heatMapData')
  })
})

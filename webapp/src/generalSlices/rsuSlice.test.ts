import reducer from './rsuSlice'
import {
  // async thunks
  getRsuData,
  getRsuInfoOnly,
  getRsuLastOnline,
  _getRsuInfo,
  _getRsuOnlineStatus,
  _getRsuCounts,
  getSsmSrmData,
  getIssScmsStatus,
  updateRowData,
  updateGeoMsgData,

  // functions
  updateMessageType,

  // reducers
  selectRsu,
  toggleMapDisplay,
  clearGeoMsg,
  toggleSsmSrmDisplay,
  setSelectedSrm,
  toggleGeoMsgPointSelect,
  updateGeoMsgPoints,
  updateGeoMsgDate,
  triggerGeoMsgDateError,
  changeCountsMsgType,
  setGeoMsgFilter,
  setGeoMsgFilterStep,
  setGeoMsgFilterOffset,
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
  selectGeoMsgStart,
  selectGeoMsgEnd,
  selectAddGeoMsgPoint,
  selectGeoMsgCoordinates,
  selectGeoMsgData,
  selectGeoMsgDateError,
  selectGeoMsgFilter,
  selectGeoMsgFilterStep,
  selectGeoMsgFilterOffset,
  selectIssScmsStatusData,
  selectSsmDisplay,
  selectSrmSsmList,
  selectSelectedSrm,
  selectHeatMapData,
} from './rsuSlice'
import RsuApi from '../apis/rsu-api'
import { RootState } from '../store'

// Mock luxon to return a fixed date time to make the tests deterministic
jest.mock('luxon', () => {
  const actualLuxon = jest.requireActual('luxon')
  return {
    ...actualLuxon,
    DateTime: {
      ...actualLuxon.DateTime,
      local: () => actualLuxon.DateTime.fromISO('2024-04-01T00:00:00.000-06:00'),
    },
  }
})

const { DateTime } = require('luxon')
const currentDate = DateTime.local()

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
        startDate: currentDate.minus({ days: 1 }).toString(),
        endDate: currentDate.toString(),
        heatMapData: {
          features: [],
          type: 'FeatureCollection',
        },
        messageLoading: false,
        warningMessage: false,
        countsMsgType: 'BSM',
        rsuMapData: {},
        mapList: [],
        mapDate: '',
        displayMap: false,
        geoMsgType: 'BSM',
        geoMsgStart: currentDate.minus({ days: 1 }).toString(),
        geoMsgEnd: currentDate.toString(),
        addGeoMsgPoint: false,
        geoMsgCoordinates: [],
        geoMsgData: [],
        geoMsgDateError: false,
        geoMsgFilter: false,
        geoMsgFilterStep: 60,
        geoMsgFilterOffset: 0,
        issScmsStatusData: {},
        ssmDisplay: false,
        srmSsmList: [],
        selectedSrm: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['rsu'] = {
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
      countsMsgType: null,
      geoMsgType: null,
      rsuMapData: null,
      mapList: null,
      mapDate: null,
      displayMap: null,
      geoMsgStart: null,
      geoMsgEnd: null,
      addGeoMsgPoint: null,
      geoMsgCoordinates: null,
      geoMsgData: null,
      geoMsgDateError: null,
      geoMsgFilter: null,
      geoMsgFilterStep: null,
      geoMsgFilterOffset: null,
      issScmsStatusData: null,
      ssmDisplay: null,
      srmSsmList: null,
      selectedSrm: null,
    },
  }

  beforeAll(() => {
    jest.mock('../apis/rsu-api')
  })

  afterAll(() => {
    jest.unmock('../apis/rsu-api')
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
      let rsuData = [] as any
      let rsuOnlineStatus = {}
      let rsuCounts = {}
      let countList = [] as any
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
      let rsuCounts = { ipv4_address: { count: 4 } } as any
      let rsuData = [
        {
          properties: {
            ipv4_address: 'ipv4_address',
          },
          geometry: {
            coordinates: [-104.999824, 39.750392],
          },
        },
      ] as any
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
      let rsuOnlineStatus = { '1.1.1.1': {} as any }
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
      } as any)

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
      const action = _getRsuOnlineStatus('rsuOnlineStatusState' as any)

      const rsuOnlineStatus = null as any
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
            countsMsgType: 'BSM',
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
            countsMsgType: 'BSM',
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
      const issScmsStatusData = 'issScmsStatus' as any
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
      const action = updateRowData(data as any)

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
        countsMsgType: 'message',
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
            countsMsgType: 'message',
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
        countsMsgType: 'message',
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
        requestOut,
        value: { ...initialState.value, messageLoading },
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
      } as any
      const warningMessage = 'warningMessage'
      const requestOut = false
      const messageLoading = false
      const countsMsgType = 'countsMsgType'
      const startDate = 'startDate'
      const endDate = 'endDate'
      const payload = {
        rsuCounts,
        countList,
        warningMessage,
        countsMsgType,
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
        requestOut,
        value: {
          ...initialState.value,
          rsuCounts,
          countList,
          heatMapData,
          warningMessage,
          messageLoading,
          countsMsgType,
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
        requestOut,
        value: { ...initialState.value, messageLoading },
      })
    })
  })

  describe('updateGeoMsgData', () => {
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
            geoMsgType: 'geoMsgType',
            geoMsgStart: 'geoMsgStart',
            geoMsgEnd: 'geoMsgEnd',
            geoMsgCoordinates: [1, 2, 3],
          },
        },
      })
      const action = updateGeoMsgData()

      RsuApi.postGeoMsgData = jest.fn().mockReturnValue('msgCounts')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('msgCounts')
      expect(RsuApi.postGeoMsgData).toHaveBeenCalledWith(
        'token',
        JSON.stringify({
          msg_type: 'geoMsgType',
          start: 'geoMsgStart',
          end: 'geoMsgEnd',
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
            geoMsgStart: '',
            geoMsgEnd: '',
            geoMsgCoordinates: [1, 2],
          },
        },
      })
      const action = updateGeoMsgData()

      RsuApi.postGeoMsgData = jest.fn().mockReturnValue('msgCounts')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(undefined)
      expect(RsuApi.postGeoMsgData).not.toHaveBeenCalled()
    })

    it('Updates the state correctly pending', async () => {
      const addGeoMsgPoint = false
      const loading = true
      const geoMsgStart = 1 as any
      const geoMsgEnd = 86400000 as any
      const state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, geoMsgStart, geoMsgEnd },
        },
        {
          type: 'rsu/updateGeoMsgData/pending',
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, addGeoMsgPoint, geoMsgStart, geoMsgEnd },
      })
    })

    it('Updates the state correctly pending date error', async () => {
      const addGeoMsgPoint = false
      const loading = true
      const geoMsgStart = 1 as any
      const geoMsgEnd = 86400002 as any
      const state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, geoMsgStart, geoMsgEnd },
        },
        {
          type: 'rsu/updateGeoMsgData/pending',
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, addGeoMsgPoint, geoMsgStart, geoMsgEnd },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const geoMsgData = 'geoMsgData'
      const loading = false
      const geoMsgFilter = true
      const geoMsgFilterStep = 60
      const geoMsgFilterOffset = 0
      const state = reducer(initialState, {
        type: 'rsu/updateGeoMsgData/fulfilled',
        payload: { body: geoMsgData },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
          geoMsgData,
          geoMsgFilter,
          geoMsgFilterStep,
          geoMsgFilterOffset,
        },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'rsu/updateGeoMsgData/rejected',
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

    updateMessageType('messageType' as any)(dispatch)
    expect(dispatch).toHaveBeenCalledTimes(2)
  })
})

describe('reducers', () => {
  const initialState: RootState['rsu'] = {
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
      countsMsgType: null,
      geoMsgType: null,
      rsuMapData: null,
      mapList: null,
      mapDate: null,
      displayMap: null,
      geoMsgStart: null,
      geoMsgEnd: null,
      addGeoMsgPoint: null,
      geoMsgCoordinates: null,
      geoMsgData: null,
      geoMsgDateError: null,
      geoMsgFilter: null,
      geoMsgFilterStep: null,
      geoMsgFilterOffset: null,
      issScmsStatusData: null,
      ssmDisplay: null,
      srmSsmList: null,
      selectedSrm: null,
    },
  }

  it('selectRsu reducer updates state correctly', async () => {
    const selectedRsu = {
      id: 1,
      type: 'Feature' as 'Feature',
      geometry: {
        type: 'Point' as 'Point',
        coordinates: [],
      },
      properties: null,
    }
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

  it('clearGeoMsg reducer updates state correctly', async () => {
    expect(reducer(initialState, clearGeoMsg())).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        geoMsgCoordinates: [],
        geoMsgData: [],
        geoMsgDateError: false,
      },
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
    let selectedSrm = {
      time: 'a',
      requestedId: 'b',
      role: 'c',
      status: 'd',
      type: 'e',
      requestId: 'f',
      lat: 1,
      long: 2,
    }
    expect(reducer(initialState, setSelectedSrm(selectedSrm))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSrm: [selectedSrm] },
    })

    expect(reducer(initialState, setSelectedSrm(null))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedSrm: [] },
    })
  })

  it('toggleGeoMsgPointSelect reducer updates state correctly', async () => {
    expect(
      reducer({ ...initialState, value: { ...initialState.value, addGeoMsgPoint: true } }, toggleGeoMsgPointSelect())
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, addGeoMsgPoint: false },
    })
  })

  it('updateGeoMsgPoints reducer updates state correctly', async () => {
    const geoMsgCoordinates = [[]]
    expect(reducer(initialState, updateGeoMsgPoints(geoMsgCoordinates))).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgCoordinates },
    })
  })

  it('updateGeoMsgDate reducer updates state correctly', async () => {
    let type = 'start' as 'start' | 'end'
    const date = 'date'
    expect(reducer(initialState, updateGeoMsgDate({ type, date }))).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgStart: 'date' },
    })

    type = 'end'
    expect(reducer(initialState, updateGeoMsgDate({ type, date }))).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgEnd: 'date' },
    })
  })

  it('triggerGeoMsgDateError reducer updates state correctly', async () => {
    expect(reducer(initialState, triggerGeoMsgDateError())).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgDateError: true },
    })
  })

  it('changeCountsMsgType reducer updates state correctly', async () => {
    const countsMsgType = 'countsMsgType'
    expect(reducer(initialState, changeCountsMsgType(countsMsgType))).toEqual({
      ...initialState,
      value: { ...initialState.value, countsMsgType },
    })
  })

  it('setGeoMsgFilter reducer updates state correctly', async () => {
    const geoMsgFilter = true
    expect(reducer(initialState, setGeoMsgFilter(geoMsgFilter))).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgFilter },
    })
  })

  it('setGeoMsgFilterStep reducer updates state correctly', async () => {
    const geoMsgFilterStep = 1
    expect(reducer(initialState, setGeoMsgFilterStep(geoMsgFilterStep))).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgFilterStep },
    })
  })

  it('setGeoMsgFilterOffset reducer updates state correctly', async () => {
    const geoMsgFilterOffset = 1234
    expect(reducer(initialState, setGeoMsgFilterOffset(geoMsgFilterOffset))).toEqual({
      ...initialState,
      value: { ...initialState.value, geoMsgFilterOffset },
    })
  })

  it('setLoading reducer updates state correctly', async () => {
    const loading = true
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
      countsMsgType: 'countsMsgType',
      rsuMapData: 'rsuMapData',
      mapList: 'mapList',
      mapDate: 'mapDate',
      displayMap: 'displayMap',
      geoMsgStart: 'geoMsgStart',
      geoMsgEnd: 'geoMsgEnd',
      addGeoMsgPoint: 'addGeoMsgPoint',
      geoMsgCoordinates: 'geoMsgCoordinates',
      geoMsgData: 'geoMsgData',
      geoMsgDateError: 'geoMsgDateError',
      geoMsgFilter: 'geoMsgFilter',
      geoMsgFilterStep: 'geoMsgFilterStep',
      geoMsgFilterOffset: 'geoMsgFilterOffset',
      issScmsStatusData: 'issScmsStatusData',
      ssmDisplay: 'ssmDisplay',
      srmSsmList: 'srmSsmList',
      selectedSrm: 'selectedSrm',
    },
  }
  const rsuState = { rsu: initialState } as any

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
    expect(selectMsgType(rsuState)).toEqual('countsMsgType')
    expect(selectRsuMapData(rsuState)).toEqual('rsuMapData')
    expect(selectMapList(rsuState)).toEqual('mapList')
    expect(selectMapDate(rsuState)).toEqual('mapDate')
    expect(selectDisplayMap(rsuState)).toEqual('displayMap')
    expect(selectGeoMsgStart(rsuState)).toEqual('geoMsgStart')
    expect(selectGeoMsgEnd(rsuState)).toEqual('geoMsgEnd')
    expect(selectAddGeoMsgPoint(rsuState)).toEqual('addGeoMsgPoint')
    expect(selectGeoMsgCoordinates(rsuState)).toEqual('geoMsgCoordinates')
    expect(selectGeoMsgData(rsuState)).toEqual('geoMsgData')
    expect(selectGeoMsgDateError(rsuState)).toEqual('geoMsgDateError')
    expect(selectGeoMsgFilter(rsuState)).toEqual('geoMsgFilter')
    expect(selectGeoMsgFilterStep(rsuState)).toEqual('geoMsgFilterStep')
    expect(selectGeoMsgFilterOffset(rsuState)).toEqual('geoMsgFilterOffset')
    expect(selectIssScmsStatusData(rsuState)).toEqual('issScmsStatusData')
    expect(selectSsmDisplay(rsuState)).toEqual('ssmDisplay')
    expect(selectSrmSsmList(rsuState)).toEqual('srmSsmList')
    expect(selectSelectedSrm(rsuState)).toEqual('selectedSrm')
    expect(selectHeatMapData(rsuState)).toEqual('heatMapData')
  })
})

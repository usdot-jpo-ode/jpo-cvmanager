import reducer from './rsuSlice'
import {
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
} from './rsuSlice'
import CdotApi from '../apis/cdot-rsu-api'

describe('rsu reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      bsmLoading: false,
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
        addPoint: false,
        bsmCoordinates: [],
        bsmData: [],
        bsmDateError: false,
        bsmFilter: false,
        bsmFilterStep: 30,
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
    bsmLoading: null,
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
      addPoint: null,
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
    jest.mock('../apis/cdot-rsu-api.js')
  })

  afterAll(() => {
    jest.unmock('../apis/cdot-rsu-api.js')
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
      expect(dispatch).toHaveBeenCalledTimes(5 + 2) // 5 for the 5 dispatched actions, 2 for the pending and fulfilled actions
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
      CdotApi.getRsuInfo = jest.fn().mockReturnValue({ rsuList: rsuData })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuData)
      expect(CdotApi.getRsuInfo).toHaveBeenCalledWith('token', 'name')
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

      CdotApi.getRsuOnline = jest.fn().mockReturnValue(rsu_ip)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsu_ip)
      expect(CdotApi.getRsuOnline).toHaveBeenCalledWith('token', 'name', '', { rsu_ip })
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
      const getState = jest.fn().mockReturnValue()
      const action = _getRsuInfo({ token: 'token', organization: 'name' })

      const rsuList = ['1.1.1.1']
      CdotApi.getRsuInfo = jest.fn().mockReturnValue({ rsuList })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuList)
      expect(CdotApi.getRsuInfo).toHaveBeenCalledWith('token', 'name')
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
      const getState = jest.fn().mockReturnValue()
      const action = _getRsuOnlineStatus({
        token: 'token',
        organization: 'name',
        rsuOnlineStatusState: 'rsuOnlineStatusState',
      })

      const rsuOnlineStatus = 'rsuOnlineStatus'
      CdotApi.getRsuOnline = jest.fn().mockReturnValue(rsuOnlineStatus)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuOnlineStatus)
      expect(CdotApi.getRsuOnline).toHaveBeenCalledWith('token', 'name')
    })

    it('returns and calls the api correctly default value', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue()
      const action = _getRsuOnlineStatus({
        token: 'token',
        organization: 'name',
        rsuOnlineStatusState: 'rsuOnlineStatusState',
      })

      const rsuOnlineStatus = null
      CdotApi.getRsuOnline = jest.fn().mockReturnValue(rsuOnlineStatus)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('rsuOnlineStatusState')
      expect(CdotApi.getRsuOnline).toHaveBeenCalledWith('token', 'name')
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
      CdotApi.getRsuCounts = jest.fn().mockReturnValue(rsuCounts)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ rsuCounts, countList })
      expect(CdotApi.getRsuCounts).toHaveBeenCalledWith('token', 'name', '', {
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
      CdotApi.getRsuCounts = jest.fn().mockReturnValue(null)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ rsuCounts, countList })
      expect(CdotApi.getRsuCounts).toHaveBeenCalledWith('token', 'name', '', {
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
        token: 'token',
        organization: 'name',
        startDate: 'startDate',
        endDate: 'endDate',
      })

      CdotApi.getRsuMapInfo = jest.fn().mockReturnValue('rsuMapData')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ endDate: 'endDate', rsuMapData: 'rsuMapData', startDate: 'startDate' })
      expect(CdotApi.getRsuMapInfo).toHaveBeenCalledWith('token', 'name', '', { ip_list: 'True' })
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
      const getState = jest.fn().mockReturnValue()
      const action = getSsmSrmData({ token: 'token' })

      CdotApi.getSsmSrmData = jest.fn().mockReturnValue('srmSsmList')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('srmSsmList')
      expect(CdotApi.getSsmSrmData).toHaveBeenCalledWith('token')
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
      const action = getIssScmsStatus({ token: 'token' })

      CdotApi.getIssScmsStatus = jest.fn().mockReturnValue('issScmsStatus')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('issScmsStatus')
      expect(CdotApi.getIssScmsStatus).toHaveBeenCalledWith('token', 'name')
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
})

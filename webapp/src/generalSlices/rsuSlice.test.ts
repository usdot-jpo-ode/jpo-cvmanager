import reducer from './rsuSlice'
import {
  // async thunks
  getRsuData,
  getRsuLastOnline,
  _getRsuInfo,
  _getRsuOnlineStatus,
  updateGeoMsgData,

  // reducers
  selectRsu,
  toggleMapDisplay,
  clearGeoMsg,
  toggleGeoMsgPointSelect,
  updateGeoMsgPoints,
  updateGeoMsgDate,
  triggerGeoMsgDateError,
  setGeoMsgFilter,
  setGeoMsgFilterStep,
  setGeoMsgFilterOffset,
  setLoading,

  // selectors
  selectLoading,
  selectSelectedRsu,
  selectRsuManufacturer,
  selectRsuIpv4,
  selectRsuPrimaryRoute,
  selectRsuData,
  selectRsuOnlineStatus,
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

import { DateTime } from 'luxon'
const currentDate = DateTime.local()

describe('rsu reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        selectedRsu: null,
        rsuData: [],
        rsuOnlineStatus: {},
        rsuMapData: {},
        mapList: [],
        mapDate: '',
        displayMap: false,
        geoMsgType: 'BSM',
        geoMsgStart: currentDate.minus({ hours: 3 }).toString(),
        geoMsgEnd: currentDate.toString(),
        addGeoMsgPoint: false,
        geoMsgCoordinates: [],
        geoMsgData: [],
        geoMsgDateError: false,
        geoMsgFilter: false,
        geoMsgFilterStep: 60,
        geoMsgFilterOffset: 0,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['rsu'] = {
    loading: null,
    value: {
      selectedRsu: null,
      rsuData: null,
      rsuOnlineStatus: null,
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
            organization: { organization: 'name' },
          },
        },
        rsu: {
          value: {
            rsuOnlineStatus: {},
          },
        },
      })
      const action = getRsuData()

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(2 + 2) // 2 for the 2 dispatched actions, 2 for the pending and fulfilled actions
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const rsuData = [] as any
      const rsuOnlineStatus = {}
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
        },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const rsuData = [
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
        { ...initialState, value: { ...initialState.value, rsuData } },
        {
          type: 'rsu/getRsuData/fulfilled',
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, rsuData },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'rsu/getRsuData/rejected',
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
            organization: { organization: 'name' },
          },
        },
      })
      const rsu_ip = '1.1.1.1'
      const action = getRsuLastOnline(rsu_ip)

      RsuApi.getRsuOnline = jest.fn().mockReturnValue(rsu_ip)
      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsu_ip)
      expect(RsuApi.getRsuOnline).toHaveBeenCalledWith('token', 'name', '', { rsu_ip })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
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
      const loading = false
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
      const loading = false
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
            organization: { organization: 'name' },
          },
        },
      })
      const action = _getRsuInfo()

      const rsuList = ['1.1.1.1']
      RsuApi.getRsuInfo = jest.fn().mockReturnValue({ rsuList })
      const resp = await action(dispatch, getState, undefined)
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
            organization: { organization: 'name' },
          },
        },
      })
      const action = _getRsuOnlineStatus({
        rsuOnlineStatusState: 'rsuOnlineStatusState',
      } as any)

      const rsuOnlineStatus = 'rsuOnlineStatus'
      RsuApi.getRsuOnline = jest.fn().mockReturnValue(rsuOnlineStatus)
      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(rsuOnlineStatus)
      expect(RsuApi.getRsuOnline).toHaveBeenCalledWith('token', 'name')
    })

    it('returns and calls the api correctly default value', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { organization: 'name' },
          },
        },
      })
      const action = _getRsuOnlineStatus('rsuOnlineStatusState' as any)

      const rsuOnlineStatus = null as any
      RsuApi.getRsuOnline = jest.fn().mockReturnValue(rsuOnlineStatus)
      const resp = await action(dispatch, getState, undefined)
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
      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ body: [] })
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
      const resp = await action(dispatch, getState, undefined)
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
      const geoMsgData = ['geoMsgData']
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

describe('reducers', () => {
  const initialState: RootState['rsu'] = {
    loading: null,
    value: {
      selectedRsu: null,
      rsuData: null,
      rsuOnlineStatus: null,
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
    },
  }

  it('selectRsu reducer updates state correctly', async () => {
    const selectedRsu = {
      id: 1,
      type: 'Feature' as const,
      geometry: {
        type: 'Point' as const,
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
    },
  }
  const rsuState = { rsu: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(rsuState)).toEqual('loading')

    expect(selectSelectedRsu(rsuState)).toEqual(initialState.value.selectedRsu)
    expect(selectRsuManufacturer(rsuState)).toEqual('manufacturer_name')
    expect(selectRsuIpv4(rsuState)).toEqual('ipv4_address')
    expect(selectRsuPrimaryRoute(rsuState)).toEqual('primary_route')
    expect(selectRsuData(rsuState)).toEqual('rsuData')
    expect(selectRsuOnlineStatus(rsuState)).toEqual('rsuOnlineStatus')
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
  })
})

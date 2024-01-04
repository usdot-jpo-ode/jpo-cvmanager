import reducer, {
  refreshSnmpFwdConfig,
  submitSnmpSet,
  deleteSnmpSet,
  filterSnmp,
  rebootRsu,
  checkFirmwareUpgrade,
  startFirmwareUpgrade,

  // reducers
  setMsgFwdConfig,
  setDestIp,
  setMsgType,
  toggleConfigPointSelect,
} from './configSlice'
import RsuApi from '../apis/rsu-api'

describe('config reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        msgFwdConfig: {},
        errorState: '',
        changeSuccess: false,
        rebootChangeSuccess: false,
        firmwareUpgradeAvailable: false,
        firmwareUpgradeName: '',
        firmwareUpgradeMsg: '',
        firmwareUpgradeErr: false,
        destIp: '',
        snmpMsgType: 'bsm',
        snmpFilterMsg: '',
        snmpFilterErr: false,
        addConfigPoint: false,
        configCoordinates: [],
        configList: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    value: {
      msgFwdConfig: null,
      errorState: null,
      changeSuccess: false,
      rebootChangeSuccess: false,
      firmwareUpgradeAvailable: false,
      firmwareUpgradeName: '',
      firmwareUpgradeMsg: '',
      firmwareUpgradeErr: false,
      destIp: '',
      snmpMsgType: 'bsm',
      snmpFilterMsg: '',
      snmpFilterErr: false,
      addConfigPoint: false,
    },
  }

  beforeAll(() => {
    jest.mock('../apis/rsu-api.js')
  })

  afterAll(() => {
    jest.unmock('../apis/rsu-api.js')
  })

  describe('refreshSnmpFwdConfig', () => {
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
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200, body: { RsuFwdSnmpwalk: 'test' } })

      const arg = ['1.2.3.4', '2.3.4.5']

      const action = refreshSnmpFwdConfig(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'rsufwdsnmpwalk',
          rsu_ip: arg,
          args: {},
        },
        ''
      )
      expect(resp.payload).toEqual({ msgFwdConfig: 'test', errorState: '' })

      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 400, body: { RsuFwdSnmpwalk: 'test' } })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ msgFwdConfig: {}, errorState: 'test' })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let msgFwdConfig = {}
      let rebootChangeSuccess = false
      let errorState = ''
      const state = reducer(initialState, {
        type: 'config/refreshSnmpFwdConfig/pending',
      })
      expect(state).toEqual({
        loading,
        rebootChangeSuccess,
        value: { ...initialState.value, msgFwdConfig, errorState },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let msgFwdConfig = 'test'
      let errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/refreshSnmpFwdConfig/fulfilled',
        payload: { msgFwdConfig, errorState },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, msgFwdConfig, errorState } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'config/refreshSnmpFwdConfig/rejected',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value } })
    })
  })

  describe('submitSnmpSet', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { name: 'name' },
          },
        },
        config: {
          value: {
            destIp: '1.1.1.1',
            snmpMsgType: 'bsm',
          },
        },
      })
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200, body: { RsuFwdSnmpset: 'test' } })

      const arg = ['1.2.3.4', '2.3.4.5']

      const action = submitSnmpSet(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'rsufwdsnmpset',
          rsu_ip: arg,
          args: {
            dest_ip: '1.1.1.1',
            msg_type: 'bsm',
          },
        },
        ''
      )
      expect(resp.payload).toEqual({ changeSuccess: true, errorState: '' })

      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 400, body: { RsuFwdSnmpset: 'error' } })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ changeSuccess: false, errorState: 'error' })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let changeSuccess = false
      let errorState = ''
      const state = reducer(initialState, {
        type: 'config/submitSnmpSet/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess, errorState } })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let changeSuccess = false
      let errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/submitSnmpSet/fulfilled',
        payload: { changeSuccess, errorState },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess, errorState } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'config/submitSnmpSet/rejected',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value } })
    })
  })

  describe('deleteSnmpSet', () => {
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
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200, body: { RsuFwdSnmpset: 'test' } })
      const arg = {
        ipList: ['1.2.3.4', '2.3.4.5'],
        destIp: '1.1.1.1',
        snmpMsgType: 'bsm',
      }

      const action = deleteSnmpSet(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'rsufwdsnmpset-del',
          rsu_ip: arg.ipList,
          args: {
            msg_type: arg.snmpMsgType,
            dest_ip: arg.destIp,
          },
        },
        ''
      )
      expect(resp.payload).toEqual({ changeSuccess: true, errorState: '' })

      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 400, body: { RsuFwdSnmpset: 'error' } })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ changeSuccess: false, errorState: 'error' })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let changeSuccess = false
      let errorState = ''
      const state = reducer(initialState, {
        type: 'config/deleteSnmpSet/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess, errorState } })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let changeSuccess = false
      let errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/deleteSnmpSet/fulfilled',
        payload: { changeSuccess, errorState },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess, errorState } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'config/deleteSnmpSet/rejected',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value } })
    })
  })

  describe('filterSnmp', () => {
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
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4', '2.3.4.5']

      const action = filterSnmp(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'snmpFilter',
          rsu_ip: arg,
          args: {},
        },
        ''
      )
      expect(resp.payload).toEqual({ snmpFilterErr: false, snmpFilterMsg: 'Filter applied' })

      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 400 })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ snmpFilterErr: true, snmpFilterMsg: 'Filter failed to be applied' })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let snmpFilterErr = false
      let snmpFilterMsg = ''
      const state = reducer(initialState, {
        type: 'config/filterSnmp/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, snmpFilterErr, snmpFilterMsg } })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let snmpFilterErr = false
      let snmpFilterMsg = 'error'
      const state = reducer(initialState, {
        type: 'config/filterSnmp/fulfilled',
        payload: { snmpFilterErr, snmpFilterMsg },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, snmpFilterErr, snmpFilterMsg } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      const state = reducer(initialState, {
        type: 'config/filterSnmp/rejected',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value } })
    })
  })

  describe('rebootRsu', () => {
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
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4', '2.3.4.5']

      const action = rebootRsu(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'reboot',
          rsu_ip: arg,
          args: {},
        },
        ''
      )
      expect(resp.payload).toEqual(undefined)
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let rebootChangeSuccess = false
      const state = reducer(initialState, {
        type: 'config/rebootRsu/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, rebootChangeSuccess } })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let rebootChangeSuccess = true
      const state = reducer(initialState, {
        type: 'config/rebootRsu/fulfilled',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, rebootChangeSuccess } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      let rebootChangeSuccess = false
      const state = reducer(initialState, {
        type: 'config/rebootRsu/rejected',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, rebootChangeSuccess } })
    })
  })

  describe('checkFirmwareUpgrade', () => {
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
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4']

      const action = checkFirmwareUpgrade(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'upgrade-check',
          rsu_ip: arg,
          args: {},
        },
        ''
      )
      expect(resp.payload).toEqual({ firmwareUpgradeAvailable: undefined, firmwareUpgradeName: undefined })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let firmwareUpgradeAvailable = false
      let firmwareUpgradeName = ''
      let firmwareUpgradeErr = false
      const state = reducer(initialState, {
        type: 'config/checkFirmwareUpgrade/pending',
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, firmwareUpgradeAvailable, firmwareUpgradeName, firmwareUpgradeErr },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let firmwareUpgradeAvailable = false
      let firmwareUpgradeName = ''
      let firmwareUpgradeMsg = 'Firmware is up to date!'
      const state = reducer(initialState, {
        type: 'config/checkFirmwareUpgrade/fulfilled',
        payload: { firmwareUpgradeAvailable, firmwareUpgradeName },
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, firmwareUpgradeAvailable, firmwareUpgradeName, firmwareUpgradeMsg },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let snmpFilterErr = false
      let snmpFilterMsg = 'error'
      const state = reducer(initialState, {
        type: 'config/filterSnmp/fulfilled',
        payload: { snmpFilterErr, snmpFilterMsg },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, snmpFilterErr, snmpFilterMsg } })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      let firmwareUpgradeAvailable = false
      let firmwareUpgradeName = ''
      let firmwareUpgradeMsg = 'An error occurred while checking for an upgrade'
      let firmwareUpgradeErr = true
      const state = reducer(initialState, {
        type: 'config/checkFirmwareUpgrade/rejected',
      })
      expect(state).toEqual({
        loading,
        value: {
          ...initialState.value,
          firmwareUpgradeAvailable,
          firmwareUpgradeName,
          firmwareUpgradeMsg,
          firmwareUpgradeErr,
        },
      })
    })
  })

  describe('startFirmwareUpgrade', () => {
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
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4']

      const action = startFirmwareUpgrade(arg)

      let resp = await action(dispatch, getState, undefined)
      expect(RsuApi.postRsuData).toHaveBeenCalledWith(
        'token',
        'name',
        {
          command: 'upgrade-rsu',
          rsu_ip: arg,
          args: {},
        },
        ''
      )
      expect(resp.payload).toEqual({ firmwareUpgradeAvailable: undefined, firmwareUpgradeName: undefined })
    })

    it('Updates the state correctly pending', async () => {
      let loading = true
      let firmwareUpgradeErr = false
      const state = reducer(initialState, {
        type: 'config/startFirmwareUpgrade/pending',
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, firmwareUpgradeErr },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      let loading = false
      let firmwareUpgradeAvailable = false
      let firmwareUpgradeName = ''
      let firmwareUpgradeMsg = 'Firmware is up to date!'
      let firmwareUpgradeErr = false
      let statusCode = 201
      const state = reducer(initialState, {
        type: 'config/startFirmwareUpgrade/fulfilled',
        payload: { message: firmwareUpgradeMsg, statusCode },
      })
      expect(state).toEqual({
        loading,
        value: {
          ...initialState.value,
          firmwareUpgradeAvailable,
          firmwareUpgradeName,
          firmwareUpgradeMsg,
          firmwareUpgradeErr,
        },
      })
    })

    it('Updates the state correctly rejected', async () => {
      let loading = false
      let firmwareUpgradeAvailable = false
      let firmwareUpgradeName = ''
      let firmwareUpgradeMsg = 'An error occurred while starting the firmware upgrade'
      let firmwareUpgradeErr = true
      const state = reducer(initialState, {
        type: 'config/startFirmwareUpgrade/rejected',
      })
      expect(state).toEqual({
        loading,
        value: {
          ...initialState.value,
          firmwareUpgradeAvailable,
          firmwareUpgradeName,
          firmwareUpgradeMsg,
          firmwareUpgradeErr,
        },
      })
    })
  })
})

describe('reducers', () => {
  const initialState = {
    loading: null,
    value: {
      msgFwdConfig: null,
      errorState: null,
      changeSuccess: false,
      rebootChangeSuccess: false,
      firmwareUpgradeAvailable: false,
      firmwareUpgradeName: '',
      firmwareUpgradeMsg: '',
      firmwareUpgradeErr: false,
      destIp: '',
      snmpMsgType: 'bsm',
      snmpFilterMsg: '',
      snmpFilterErr: false,
      addConfigPoint: false,
    },
  }

  it('setMsgFwdConfig reducer updates state correctly', async () => {
    const msgFwdConfig = 'updated'
    expect(reducer(initialState, setMsgFwdConfig(msgFwdConfig))).toEqual({
      ...initialState,
      value: { ...initialState.value, msgFwdConfig },
    })
  })

  it('setDestIp reducer updates state correctly', async () => {
    const destIp = 'updated'
    expect(reducer(initialState, setDestIp(destIp))).toEqual({
      ...initialState,
      value: { ...initialState.value, destIp },
    })
  })

  it('setMsgType reducer updates state correctly', async () => {
    const snmpMsgType = 'updated'
    expect(reducer(initialState, setMsgType(snmpMsgType))).toEqual({
      ...initialState,
      value: { ...initialState.value, snmpMsgType },
    })
  })

  it('toggleConfigPointSelect reducer updates state correctly', async () => {
    const addConfigPoint = initialState.value.addConfigPoint
    expect(reducer(initialState, toggleConfigPointSelect(addConfigPoint))).toEqual({
      ...initialState,
      value: { ...initialState.value, addConfigPoint: !addConfigPoint },
    })
  })
})

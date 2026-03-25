import reducer, {
  getCachedSnmpFwdConfigsFromDatabase,
  getRsuMsgConfigsFromRsu,
  submitSnmpSet,
  deleteSnmpSet,
  rebootRsu,
  checkFirmwareUpgrade,
  startFirmwareUpgrade,

  // reducers
  setDestIp,
  setMsgType,
  toggleConfigPointSelect,
} from './configSlice'
import RsuApi from '../apis/rsu-api'
import { RootState } from '../store'

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
        includeSecurityHeader: false,
        addConfigPoint: false,
        configCoordinates: [],
        configList: [],
        msgFwdConfigType: 'database',
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: false,
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
      includeSecurityHeader: false,
      addConfigPoint: false,
      configCoordinates: null,
      configList: null,
    },
  } as RootState['config']

  beforeAll(() => {
    jest.mock('../apis/rsu-api')
  })

  afterAll(() => {
    jest.unmock('../apis/rsu-api')
  })

  describe('refreshSnmpFwdConfig', () => {
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
      RsuApi.getCachedRsuMsgFwdConfigsFromDatabase = jest.fn().mockReturnValue({ RsuFwdSnmpwalk: 'test' })

      const rsu_ip = '1.2.3.4'

      const action = getCachedSnmpFwdConfigsFromDatabase(rsu_ip)

      const resp = await action(dispatch, getState, undefined)
      expect(RsuApi.getCachedRsuMsgFwdConfigsFromDatabase).toHaveBeenCalledWith('token', 'name', '', { rsu_ip })
      expect(resp.payload).toEqual({ msgFwdConfig: 'test', errorState: '' })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const msgFwdConfig = {}
      const rebootChangeSuccess = false
      const changeSuccess = false
      const errorState = ''
      const destIp = ''
      const snmpMsgType = 'bsm'
      const state = reducer(initialState, {
        type: 'config/refreshSnmpFwdConfig/pending',
      })
      expect(state).toEqual({
        loading,
        value: {
          ...initialState.value,
          msgFwdConfig,
          errorState,
          rebootChangeSuccess,
          changeSuccess,
          destIp,
          snmpMsgType,
        },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const msgFwdConfig = 'test'
      const errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/refreshSnmpFwdConfig/fulfilled',
        payload: { msgFwdConfig, errorState },
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, msgFwdConfig, errorState, msgFwdConfigType: 'database' },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'config/refreshSnmpFwdConfig/rejected',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value } })
    })
  })

  describe('getRsuMsgFwdFetch', () => {
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
      RsuApi.getRsuMsgConfigsFromRsu = jest.fn().mockReturnValue({ RsuFwdSnmpwalk: 'test' })

      const rsu_ip = '1.2.3.4'

      const action = getRsuMsgConfigsFromRsu(rsu_ip)

      const resp = await action(dispatch, getState, undefined)
      expect(RsuApi.getRsuMsgConfigsFromRsu).toHaveBeenCalledWith('token', 'name', '', { rsu_ip })
      expect(resp.payload).toEqual({ msgFwdConfig: 'test', errorState: '' })
    })

    it('returns error when API fails', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.getRsuMsgConfigsFromRsu = jest.fn().mockReturnValue(null)

      const rsu_ip = '1.2.3.4'

      const action = getRsuMsgConfigsFromRsu(rsu_ip)

      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('Failed to fetch RSU message forwarding configuration')
    })

    it('returns error when API throws exception', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.getRsuMsgConfigsFromRsu = jest.fn().mockImplementation(() => {
        throw new Error('Test Exception')
      })

      const rsu_ip = '1.2.3.4'

      const action = getRsuMsgConfigsFromRsu(rsu_ip)

      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('Test Exception')
    })

    it('returns error when API throws a string', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.getRsuMsgConfigsFromRsu = jest.fn().mockImplementation(() => {
        throw 'String Exception'
      })

      const rsu_ip = '1.2.3.4'

      const action = getRsuMsgConfigsFromRsu(rsu_ip)

      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('String Exception')
    })

    it('returns error when API throws an unknown error', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.getRsuMsgConfigsFromRsu = jest.fn().mockImplementation(() => {
        throw 123
      })

      const rsu_ip = '1.2.3.4'

      const action = getRsuMsgConfigsFromRsu(rsu_ip)

      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual('An unknown error occurred while fetching RSU message forwarding configuration')
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const msgFwdConfig = {}
      const rebootChangeSuccess = false
      const changeSuccess = false
      const errorState = ''
      const destIp = ''
      const snmpMsgType = 'bsm'
      const state = reducer(initialState, {
        type: 'config/getRsuMsgFwdFetch/pending',
      })
      expect(state).toEqual({
        loading,
        value: {
          ...initialState.value,
          msgFwdConfig,
          errorState,
          rebootChangeSuccess,
          changeSuccess,
          destIp,
          snmpMsgType,
        },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const msgFwdConfig = 'test'
      const errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/getRsuMsgFwdFetch/fulfilled',
        payload: { msgFwdConfig, errorState },
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, msgFwdConfig, errorState, msgFwdConfigType: 'rsu' },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'config/getRsuMsgFwdFetch/rejected',
        payload: 'error',
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, errorState: 'error' },
      })
    })
  })

  describe('submitSnmpSet', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
            organization: { organization: 'name' },
          },
        },
        config: {
          value: {
            destIp: '1.1.1.1',
            snmpMsgType: 'bsm',
            security: 0,
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
            security: 0,
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
      const loading = true
      const changeSuccess = false
      const state = reducer(initialState, {
        type: 'config/submitSnmpSet/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess } })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const changeSuccess = false
      const errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/submitSnmpSet/fulfilled',
        payload: { changeSuccess, errorState },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess } })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
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
            organization: { organization: 'name' },
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
      const loading = true
      const changeSuccess = false
      const state = reducer(initialState, {
        type: 'config/deleteSnmpSet/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess } })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const changeSuccess = false
      const errorState = 'error'
      const state = reducer(initialState, {
        type: 'config/deleteSnmpSet/fulfilled',
        payload: { changeSuccess, errorState },
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, changeSuccess } })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'config/deleteSnmpSet/rejected',
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
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4', '2.3.4.5']

      const action = rebootRsu(arg)

      const resp = await action(dispatch, getState, undefined)
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
      const loading = true
      const rebootChangeSuccess = false
      const state = reducer(initialState, {
        type: 'config/rebootRsu/pending',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, rebootChangeSuccess } })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const rebootChangeSuccess = true
      const state = reducer(initialState, {
        type: 'config/rebootRsu/fulfilled',
      })
      expect(state).toEqual({ loading, value: { ...initialState.value, rebootChangeSuccess } })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const rebootChangeSuccess = false
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
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4']

      const action = checkFirmwareUpgrade(arg)

      const resp = await action(dispatch, getState, undefined)
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
      const loading = true
      const firmwareUpgradeAvailable = false
      const firmwareUpgradeName = ''
      const firmwareUpgradeErr = false
      const state = reducer(initialState, {
        type: 'config/checkFirmwareUpgrade/pending',
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, firmwareUpgradeAvailable, firmwareUpgradeName, firmwareUpgradeErr },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const firmwareUpgradeAvailable = false
      const firmwareUpgradeName = ''
      const firmwareUpgradeMsg = 'Firmware is up to date!'
      const state = reducer(initialState, {
        type: 'config/checkFirmwareUpgrade/fulfilled',
        payload: { firmwareUpgradeAvailable, firmwareUpgradeName },
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, firmwareUpgradeAvailable, firmwareUpgradeName, firmwareUpgradeMsg },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const firmwareUpgradeAvailable = false
      const firmwareUpgradeName = ''
      const firmwareUpgradeMsg = 'An error occurred while checking for an upgrade'
      const firmwareUpgradeErr = true
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
            organization: { organization: 'name' },
          },
        },
      })
      RsuApi.postRsuData = jest.fn().mockReturnValue({ status: 200 })

      const arg = ['1.2.3.4']

      const action = startFirmwareUpgrade(arg)

      const resp = await action(dispatch, getState, undefined)
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
      const loading = true
      const firmwareUpgradeErr = false
      const state = reducer(initialState, {
        type: 'config/startFirmwareUpgrade/pending',
      })
      expect(state).toEqual({
        loading,
        value: { ...initialState.value, firmwareUpgradeErr },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const firmwareUpgradeAvailable = false
      const firmwareUpgradeName = ''
      const firmwareUpgradeMsg = 'Firmware is up to date!'
      const firmwareUpgradeErr = false
      const statusCode = 201
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
      const loading = false
      const firmwareUpgradeAvailable = false
      const firmwareUpgradeName = ''
      const firmwareUpgradeMsg = 'An error occurred while starting the firmware upgrade'
      const firmwareUpgradeErr = true
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
      includeSecurityHeader: false,
      addConfigPoint: false,
      configCoordinates: null,
      configList: null,
    },
  } as RootState['config']

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
    expect(reducer(initialState, toggleConfigPointSelect())).toEqual({
      ...initialState,
      value: { ...initialState.value, addConfigPoint: !addConfigPoint },
    })
  })
})

import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import RsuApi from '../apis/rsu-api'
import { selectToken, selectOrganizationName } from './userSlice'
import { RootState } from '../store'
import { RsuCommandPostBody, RsuDsrcFwdConfigs, RsuRxTxMsgFwdConfigs, SnmpFwdWalkConfig } from '../models/RsuApi'

const initialState = {
  msgFwdConfig: {} as any,
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
  configCoordinates: [] as number[][],
  configList: [] as number[],
}

export const refreshSnmpFwdConfig = createAsyncThunk(
  'config/refreshSnmpFwdConfig',
  async (rsu_ip: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const response = await RsuApi.getRsuMsgFwdConfigs(token, organization, '', { rsu_ip })

    return {
      msgFwdConfig: response.RsuFwdSnmpwalk,
      errorState: '',
    }
  }
)

export const submitSnmpSet = createAsyncThunk('config/submitSnmpSet', async (ipList: string[], { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const destIp = selectDestIp(currentState)
  const snmpMsgType = selectSnmpMsgType(currentState)
  const securityHeader = selectIncludeSecurityHeader(currentState) ? 1 : 0

  const body: RsuCommandPostBody = {
    command: 'rsufwdsnmpset',
    rsu_ip: ipList,
    args: {
      dest_ip: destIp,
      msg_type: snmpMsgType,
      security: securityHeader,
    },
  }

  const response = await RsuApi.postRsuData(token, organization, body, '')

  return response.status === 200
    ? { changeSuccess: true, errorState: '' }
    : { changeSuccess: false, errorState: response.body.RsuFwdSnmpset }
})

export const deleteSnmpSet = createAsyncThunk(
  'config/deleteSnmpSet',
  async (
    data: {
      ipList: string[]
      destIp: string
      snmpMsgType: string
    },
    { getState }
  ) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const body: RsuCommandPostBody = {
      command: 'rsufwdsnmpset-del',
      rsu_ip: data?.ipList,
      args: {
        dest_ip: data?.destIp,
        msg_type: data?.snmpMsgType,
      },
    }

    const response = await RsuApi.postRsuData(token, organization, body, '')

    return response.status === 200
      ? { changeSuccess: true, errorState: '' }
      : { changeSuccess: false, errorState: response.body.RsuFwdSnmpset }
  }
)

export const rebootRsu = createAsyncThunk('config/rebootRsu', async (ipList: string[], { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)

  const body: RsuCommandPostBody = {
    command: 'reboot',
    rsu_ip: ipList,
    args: {},
  }

  RsuApi.postRsuData(token, organization, body, '')

  return
})

export const checkFirmwareUpgrade = createAsyncThunk(
  'config/checkFirmwareUpgrade',
  async (rsuIp: string[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const body: RsuCommandPostBody = {
      command: 'upgrade-check',
      rsu_ip: rsuIp,
      args: {},
    }

    const response = await RsuApi.postRsuData(token, organization, body, '')
    return {
      firmwareUpgradeAvailable: response.body?.upgrade_available,
      firmwareUpgradeName: response.body?.upgrade_name,
    }
  }
)

export const startFirmwareUpgrade = createAsyncThunk(
  'config/startFirmwareUpgrade',
  async (ipList: string[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const body: RsuCommandPostBody = {
      command: 'upgrade-rsu',
      rsu_ip: ipList,
      args: {},
    }

    const response = await RsuApi.postRsuData(token, organization, body, '')

    if (ipList.length === 1) {
      return {
        message: response.body?.[ipList[0]]?.data?.message,
        statusCode: response.body?.[ipList[0]]?.code,
      }
    }

    return response.status === 200
      ? { message: 'Firmware upgrades started successfully', statusCode: response.status }
      : { message: 'Firmware upgrades failed to be started', statusCode: response.status }
  }
)

export const geoRsuQuery = createAsyncThunk(
  'config/geoRsuQuery',
  async (vendor: string, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)
    const configCoordinates = selectConfigCoordinates(currentState)

    return await RsuApi.postRsuGeo(
      token,
      organization,
      JSON.stringify({
        geometry: configCoordinates,
        vendor: vendor,
      }),
      ''
    )
  },
  {
    // Will guard thunk from being executed
    condition: (_, { getState }) => {
      const currentState = getState() as RootState as RootState
      const configCoordinates = selectConfigCoordinates(currentState)

      const valid = configCoordinates.length > 2
      return valid
    },
  }
)

export const configSlice = createSlice({
  name: 'config',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setDestIp: (state, action) => {
      state.value.destIp = action.payload
    },
    setMsgType: (state, action) => {
      state.value.snmpMsgType = action.payload
    },
    setIncludeSecurityHeader: (state, action) => {
      state.value.includeSecurityHeader = action.payload
    },
    toggleConfigPointSelect: (state) => {
      state.value.addConfigPoint = !state.value.addConfigPoint
    },
    updateConfigPoints: (state, action) => {
      state.value.configCoordinates = action.payload
    },
    clearConfig: (state) => {
      state.value.configCoordinates = []
      state.value.configList = []
      state.loading = false
    },
    clearFirmware: (state) => {
      state.value.firmwareUpgradeAvailable = false
      state.value.firmwareUpgradeName = ''
      state.value.firmwareUpgradeMsg = ''
      state.value.firmwareUpgradeErr = true
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(refreshSnmpFwdConfig.pending, (state) => {
        state.loading = true
        state.value.msgFwdConfig = {} as RsuDsrcFwdConfigs | RsuRxTxMsgFwdConfigs
        state.value.errorState = ''
        state.value.destIp = ''
        state.value.snmpMsgType = 'bsm'
        state.value.changeSuccess = false
        state.value.rebootChangeSuccess = false
      })
      .addCase(refreshSnmpFwdConfig.fulfilled, (state, action) => {
        state.loading = false
        state.value.msgFwdConfig = action.payload.msgFwdConfig
        state.value.errorState = action.payload.errorState
      })
      .addCase(refreshSnmpFwdConfig.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitSnmpSet.pending, (state) => {
        state.loading = true
      })
      .addCase(submitSnmpSet.fulfilled, (state, action) => {
        state.loading = false
      })
      .addCase(submitSnmpSet.rejected, (state) => {
        state.loading = false
      })
      .addCase(deleteSnmpSet.pending, (state) => {
        state.loading = true
      })
      .addCase(deleteSnmpSet.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(deleteSnmpSet.rejected, (state) => {
        state.loading = false
      })
      .addCase(rebootRsu.pending, (state) => {
        state.loading = true
        state.value.rebootChangeSuccess = false
      })
      .addCase(rebootRsu.fulfilled, (state, action) => {
        state.loading = false
        state.value.rebootChangeSuccess = true
      })
      .addCase(rebootRsu.rejected, (state) => {
        state.loading = false
        state.value.rebootChangeSuccess = false
      })
      .addCase(checkFirmwareUpgrade.pending, (state) => {
        state.loading = true
        state.value.firmwareUpgradeAvailable = false
        state.value.firmwareUpgradeName = ''
        state.value.firmwareUpgradeErr = false
      })
      .addCase(checkFirmwareUpgrade.fulfilled, (state, action) => {
        state.loading = false
        state.value.firmwareUpgradeAvailable = action.payload.firmwareUpgradeAvailable
        state.value.firmwareUpgradeName = action.payload.firmwareUpgradeName
        if (!action.payload.firmwareUpgradeAvailable) state.value.firmwareUpgradeMsg = 'Firmware is up to date!'
      })
      .addCase(checkFirmwareUpgrade.rejected, (state) => {
        state.loading = false
        state.value.firmwareUpgradeAvailable = false
        state.value.firmwareUpgradeName = ''
        state.value.firmwareUpgradeMsg = 'An error occurred while checking for an upgrade'
        state.value.firmwareUpgradeErr = true
      })
      .addCase(startFirmwareUpgrade.pending, (state) => {
        state.loading = true
        state.value.firmwareUpgradeErr = false
      })
      .addCase(startFirmwareUpgrade.fulfilled, (state, action) => {
        state.loading = false
        state.value.firmwareUpgradeAvailable = false
        state.value.firmwareUpgradeName = ''
        state.value.firmwareUpgradeMsg = action.payload.message
        if (action.payload.statusCode !== 201 && action.payload.statusCode !== 200)
          state.value.firmwareUpgradeErr = true
      })
      .addCase(startFirmwareUpgrade.rejected, (state) => {
        state.loading = false
        state.value.firmwareUpgradeAvailable = false
        state.value.firmwareUpgradeName = ''
        state.value.firmwareUpgradeMsg = 'An error occurred while starting the firmware upgrade'
        state.value.firmwareUpgradeErr = true
      })
      .addCase(geoRsuQuery.pending, (state) => {
        state.loading = true
        state.value.errorState = ''
        state.value.destIp = ''
        state.value.snmpMsgType = 'bsm'
        state.value.changeSuccess = false
        state.value.rebootChangeSuccess = false
      })
      .addCase(geoRsuQuery.fulfilled, (state, action) => {
        state.value.configList = action.payload.body
        state.value.addConfigPoint = false
        state.loading = false
      })
      .addCase(geoRsuQuery.rejected, (state) => {
        state.loading = false
      })
  },
})

export const selectMsgFwdConfig = (state: RootState) => state.config.value.msgFwdConfig
export const selectChangeSuccess = (state: RootState) => state.config.value.changeSuccess
export const selectRebootChangeSuccess = (state: RootState) => state.config.value.rebootChangeSuccess
export const selectErrorState = (state: RootState) => state.config.value.errorState
export const selectFirmwareUpgradeAvailable = (state: RootState) => state.config.value.firmwareUpgradeAvailable
export const selectFirmwareUpgradeName = (state: RootState) => state.config.value.firmwareUpgradeName
export const selectFirmwareUpgradeMsg = (state: RootState) => state.config.value.firmwareUpgradeMsg
export const selectFirmwareUpgradeErr = (state: RootState) => state.config.value.firmwareUpgradeErr
export const selectDestIp = (state: RootState) => state.config.value.destIp
export const selectSnmpMsgType = (state: RootState) => state.config.value.snmpMsgType
export const selectIncludeSecurityHeader = (state: RootState) => state.config.value.includeSecurityHeader
export const selectLoading = (state: RootState) => state.config.loading
export const selectAddConfigPoint = (state: RootState) => state.config.value.addConfigPoint
export const selectConfigCoordinates = (state: RootState) => state.config.value.configCoordinates
export const selectConfigList = (state: RootState) => state.config.value.configList

export const {
  setDestIp,
  setMsgType,
  setIncludeSecurityHeader,
  toggleConfigPointSelect,
  updateConfigPoints,
  clearConfig,
  clearFirmware,
} = configSlice.actions

export default configSlice.reducer

import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import RsuApi from '../apis/rsu-api'
import { selectToken, selectOrganizationName } from './userSlice'
import { RootState } from '../store'

const initialState = {
  msgFwdConfig: {},
  errorState: '',
  changeSuccess: false,
  rebootChangeSuccess: false,
  destIp: '',
  snmpMsgType: 'bsm',
  snmpFilterMsg: '',
  snmpFilterErr: false,
  addConfigPoint: false,
  configCoordinates: [] as number[][],
  configList: [] as number[],
}

export const refreshSnmpFwdConfig = createAsyncThunk(
  'config/refreshSnmpFwdConfig',
  async (ipList, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const body = {
      command: 'rsufwdsnmpwalk',
      rsu_ip: ipList,
      args: {},
    }

    const response = await RsuApi.postRsuData(token, organization, body, '')

    return response.status === 200
      ? { msgFwdConfig: response.body.RsuFwdSnmpwalk, errorState: '' }
      : { msgFwdConfig: {}, errorState: response.body.RsuFwdSnmpwalk }
  }
)

export const submitSnmpSet = createAsyncThunk('config/submitSnmpSet', async (ipList, { getState, dispatch }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const destIp = selectDestIp(currentState)
  const snmpMsgType = selectSnmpMsgType(currentState)

  const body = {
    command: 'rsufwdsnmpset',
    rsu_ip: ipList,
    args: {
      dest_ip: destIp,
      msg_type: snmpMsgType,
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
    { getState, dispatch }
  ) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)
    let body = {}

    body = {
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

export const filterSnmp = createAsyncThunk('config/filterSnmp', async (ipList, { getState, dispatch }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)

  const body = {
    command: 'snmpFilter',
    rsu_ip: ipList,
    args: {},
  }

  const response = await RsuApi.postRsuData(token, organization, body, '')

  return response.status === 200
    ? { snmpFilterErr: false, snmpFilterMsg: 'Filter applied' }
    : {
        snmpFilterErr: true,
        snmpFilterMsg: 'Filter failed to be applied',
      }
})

export const rebootRsu = createAsyncThunk('config/rebootRsu', async (ipList, { getState, dispatch }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)

  const body = {
    command: 'reboot',
    rsu_ip: ipList,
    args: {},
  }

  RsuApi.postRsuData(token, organization, body, '')

  return
})

export const geoRsuQuery = createAsyncThunk(
  'config/geoRsuQuery',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)
    const configCoordinates = selectConfigCoordinates(currentState)
    console.debug(configCoordinates)

    return await RsuApi.postRsuGeo(
      token,
      organization,
      JSON.stringify({
        geometry: configCoordinates,
      }),
      ''
    )
  },
  {
    // Will guard thunk from being executed
    condition: (_, { getState, extra }) => {
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
    setMsgFwdConfig: (state, action) => {
      state.value.msgFwdConfig = action.payload
    },
    setDestIp: (state, action) => {
      state.value.destIp = action.payload
    },
    setMsgType: (state, action) => {
      state.value.snmpMsgType = action.payload
    },
    toggleConfigPointSelect: (state) => {
      console.debug('toggleConfigPointSelect')
      state.value.addConfigPoint = !state.value.addConfigPoint
    },
    updateConfigPoints: (state, action) => {
      state.value.configCoordinates = action.payload
      console.debug('updateConfigPoints', action.payload)
    },
    clearConfig: (state) => {
      state.value.configCoordinates = []
      state.value.configList = []
      state.loading = false
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(refreshSnmpFwdConfig.pending, (state) => {
        state.loading = true
        state.value.msgFwdConfig = {}
        state.value.errorState = ''
        state.value.snmpFilterMsg = ''
        state.value.destIp = ''
        state.value.snmpMsgType = 'bsm'
        state.value.changeSuccess = false
        state.value.snmpFilterErr = false
        state.value.rebootChangeSuccess = false
        console.debug('Pending refreshSnmpFwdConfig', state.loading)
      })
      .addCase(refreshSnmpFwdConfig.fulfilled, (state, action) => {
        state.loading = false
        state.value.msgFwdConfig = action.payload.msgFwdConfig
        state.value.errorState = action.payload.errorState
        console.debug('fulfilled refreshSnmpFwdConfig', state.loading)
      })
      .addCase(refreshSnmpFwdConfig.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitSnmpSet.pending, (state) => {
        state.loading = true
        state.value.errorState = ''
        state.value.changeSuccess = false
      })
      .addCase(submitSnmpSet.fulfilled, (state, action) => {
        state.loading = false
        state.value.changeSuccess = action.payload.changeSuccess
        state.value.errorState = action.payload.errorState
      })
      .addCase(submitSnmpSet.rejected, (state) => {
        state.loading = false
      })
      .addCase(deleteSnmpSet.pending, (state) => {
        state.loading = true
        state.value.errorState = ''
        state.value.changeSuccess = false
      })
      .addCase(deleteSnmpSet.fulfilled, (state, action) => {
        state.loading = false
        state.value.changeSuccess = action.payload.changeSuccess
        state.value.errorState = action.payload.errorState
      })
      .addCase(deleteSnmpSet.rejected, (state) => {
        state.loading = false
      })
      .addCase(filterSnmp.pending, (state) => {
        state.loading = true
        state.value.snmpFilterMsg = ''
        state.value.snmpFilterErr = false
      })
      .addCase(filterSnmp.fulfilled, (state, action) => {
        state.loading = false
        state.value.snmpFilterMsg = action.payload.snmpFilterMsg
        state.value.snmpFilterErr = action.payload.snmpFilterErr
      })
      .addCase(filterSnmp.rejected, (state) => {
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
      .addCase(geoRsuQuery.pending, (state) => {
        state.loading = true
        state.value.errorState = ''
        state.value.snmpFilterMsg = ''
        state.value.destIp = ''
        state.value.snmpMsgType = 'bsm'
        state.value.changeSuccess = false
        state.value.snmpFilterErr = false
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
export const selectDestIp = (state: RootState) => state.config.value.destIp
export const selectSnmpMsgType = (state: RootState) => state.config.value.snmpMsgType
export const selectSnmpFilterMsg = (state: RootState) => state.config.value.snmpFilterMsg
export const selectSnmpFilterErr = (state: RootState) => state.config.value.snmpFilterErr
export const selectLoading = (state: RootState) => state.config.loading
export const selectAddConfigPoint = (state: RootState) => state.config.value.addConfigPoint
export const selectConfigCoordinates = (state: RootState) => state.config.value.configCoordinates
export const selectConfigList = (state: RootState) => state.config.value.configList

export const { setMsgFwdConfig, setDestIp, setMsgType, toggleConfigPointSelect, updateConfigPoints, clearConfig } =
  configSlice.actions

export default configSlice.reducer

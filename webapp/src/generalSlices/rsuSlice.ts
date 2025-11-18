import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import RsuApi from '../apis/rsu-api'
import {
  IssScmsStatus,
  RsuInfo,
  RsuMapInfo,
  RsuMapInfoIpList,
  RsuOnlineStatusRespMultiple,
  RsuOnlineStatusRespSingle,
  SsmSrmData,
} from '../models/RsuApi'
import { RootState } from '../store'
import { selectToken, selectOrganizationName } from './userSlice'
import { SelectedSrm } from '../models/Srm'
import { MessageType } from '../models/MessageTypes'
import { toast } from 'react-hot-toast'
import { DateTime } from 'luxon'

const currentDate = DateTime.local()

const initialState = {
  selectedRsu: null as RsuInfo,
  rsuData: [] as RsuInfo[],
  rsuOnlineStatus: {} as RsuOnlineStatusRespMultiple,
  currentSort: '',
  startDate: currentDate.minus({ days: 1 }).toString(),
  endDate: currentDate.toString(),
  messageLoading: false,
  warningMessage: false,
  geoMsgType: 'BSM' as MessageType | undefined,
  rsuMapData: {} as RsuMapInfo['geojson'],
  mapList: [] as RsuMapInfoIpList,
  mapDate: '' as RsuMapInfo['date'],
  displayMap: false,
  // TODO: lowering the default start date to 3 hours ago to reduce the number of messages returned
  // this is a temporary fix until the Processed BSM messages in mongo   are stored without duplicates
  geoMsgStart: currentDate.minus({ hours: 3 }).toString(),
  geoMsgEnd: currentDate.toString(),
  addGeoMsgPoint: false,
  geoMsgCoordinates: [] as number[][],
  geoMsgData: [] as Array<GeoJSON.Feature<GeoJSON.Geometry>>,
  geoMsgDateError: false,
  geoMsgFilter: false,
  geoMsgFilterStep: 60,
  geoMsgFilterOffset: 0,
  issScmsStatusData: {} as IssScmsStatus,
  ssmDisplay: false,
  srmSsmList: [] as SsmSrmData,
  selectedSrm: [] as SelectedSrm[],
}

export const getRsuData = createAsyncThunk(
  'rsu/getRsuData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState

    await Promise.all([
      dispatch(resetCountsDates()),
      dispatch(_getRsuInfo()),
      dispatch(_getRsuOnlineStatus(currentState.rsu.value.rsuOnlineStatus)),
    ])
  },
  {
    condition: (_, { getState }) => selectToken(getState() as RootState) != undefined,
  }
)

export const getRsuInfoOnly = createAsyncThunk('rsu/getRsuInfoOnly', async (_, { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuInfo = await RsuApi.getRsuInfo(token, organization)
  const rsuData = rsuInfo.rsuList
  return rsuData
})

export const getRsuLastOnline = createAsyncThunk('rsu/getRsuLastOnline', async (rsu_ip: string, { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuLastOnline = await RsuApi.getRsuOnline(token, organization, '', { rsu_ip })
  return rsuLastOnline
})

export const _getRsuInfo = createAsyncThunk('rsu/_getRsuInfo', async (_, { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuInfo = await RsuApi.getRsuInfo(token, organization)
  const rsuData = rsuInfo.rsuList

  return rsuData
})

export const _getRsuOnlineStatus = createAsyncThunk(
  'rsu/_getRsuOnlineStatus',
  async (rsuOnlineStatusState: RsuOnlineStatusRespMultiple, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)
    const rsuOnlineStatus = (await RsuApi.getRsuOnline(token, organization)) ?? rsuOnlineStatusState

    return rsuOnlineStatus
  }
)

export const getSsmSrmData = createAsyncThunk('rsu/getSsmSrmData', async (_, { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  return await RsuApi.getSsmSrmData(token)
})

export const getIssScmsStatus = createAsyncThunk(
  'rsu/getIssScmsStatus',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    return await RsuApi.getIssScmsStatus(token, organization)
  },
  {
    condition: (_, { getState }) => selectToken(getState() as RootState) != undefined,
  }
)

export const updateRowData = createAsyncThunk(
  'rsu/updateRowData',
  async (
    data: {
      message?: MessageType
      start?: string
      end?: string
    },
    { getState }
  ) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const startDate = Object.prototype.hasOwnProperty.call(data, 'start')
      ? data['start']
      : currentState.rsu.value.startDate
    const endDate = Object.prototype.hasOwnProperty.call(data, 'end') ? data['end'] : currentState.rsu.value.endDate

    const warningMessage = new Date(endDate).getTime() - new Date(startDate).getTime() > 86400000

    return {
      startDate,
      endDate,
      warningMessage,
    }
  },
  {
    condition: (_, { getState }) => selectToken(getState() as RootState) != undefined,
  }
)

export const updateGeoMsgData = createAsyncThunk(
  'rsu/updateGeoMsgData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const requestBody = {
      msg_type: currentState.rsu.value.geoMsgType,
      start: currentState.rsu.value.geoMsgStart,
      end: currentState.rsu.value.geoMsgEnd,
      geometry: currentState.rsu.value.geoMsgCoordinates,
    }

    try {
      const geoMapDataPromise = RsuApi.postGeoMsgData(token, JSON.stringify(requestBody), '')
      toast.promise(geoMapDataPromise, {
        loading: `Retrieving ${requestBody.msg_type} Data`,
        success: (data) => `Retrieved ${data.body.length.toLocaleString()} messages`,
        error: (err) => `Query failed: ${err}`,
      })
      const geoMapData = await geoMapDataPromise

      // Check if response exists and has a body
      if (!geoMapData || !geoMapData.body) {
        toast.error('No data returned from API')
        return { body: [] }
      }

      // Check if body is empty
      if (geoMapData.body.length === 0) {
        toast.error('No messages found for the selected criteria')
        return { body: [] }
      }

      // Get unique IDs and assign color indices
      const uniqueIds = Array.from(new Set(geoMapData.body.map((item) => item.properties.id)))
      const idToColorIndex = Object.fromEntries(
        uniqueIds.map((id, index) => [id, index % 10]) // Using modulo 10 to cycle through 10 colors
      )

      // Assign color indices to each feature
      geoMapData.body = geoMapData.body.map((feature) => ({
        ...feature,
        properties: {
          ...feature.properties,
          colorIndex: idToColorIndex[feature.properties.id],
        },
      }))

      return geoMapData
    } catch (err) {
      const toastMessage = `Query failed: ${err}`
      toast.error(toastMessage)
      console.error(err)
      return { body: [] } // Return empty body on error
    }
  },
  {
    // Will guard thunk from being executed
    condition: (_, { getState }) => {
      const { rsu } = getState() as RootState
      const valid = rsu.value.geoMsgStart !== '' && rsu.value.geoMsgEnd !== '' && rsu.value.geoMsgCoordinates.length > 2
      return valid
    },
  }
)

export const rsuSlice = createSlice({
  name: 'rsu',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    selectRsu: (state, action: PayloadAction<RsuInfo>) => {
      state.value.selectedRsu = action.payload
    },
    toggleMapDisplay: (state) => {
      state.value.displayMap = !state.value.displayMap
    },
    clearGeoMsg: (state) => {
      state.value.geoMsgCoordinates = []
      state.value.geoMsgData = []
      state.value.geoMsgDateError = false
    },
    toggleSsmSrmDisplay: (state) => {
      state.value.ssmDisplay = !state.value.ssmDisplay
    },
    setSelectedSrm: (state, action: PayloadAction<SelectedSrm>) => {
      state.value.selectedSrm = Object.keys(action.payload ?? {}).length === 0 ? [] : [action.payload]
    },
    toggleGeoMsgPointSelect: (state) => {
      state.value.addGeoMsgPoint = !state.value.addGeoMsgPoint
    },
    updateGeoMsgPoints: (state, action: PayloadAction<number[][]>) => {
      state.value.geoMsgCoordinates = action.payload
    },
    updateGeoMsgDate: (state, action: PayloadAction<{ type: 'start' | 'end'; date: string }>) => {
      if (action.payload.type === 'start') state.value.geoMsgStart = action.payload.date
      else state.value.geoMsgEnd = action.payload.date
    },
    triggerGeoMsgDateError: (state) => {
      state.value.geoMsgDateError = true
    },
    changeGeoMsgType: (state, action: PayloadAction<MessageType | undefined>) => {
      state.value.geoMsgType = action.payload
    },
    setGeoMsgFilter: (state, action: PayloadAction<boolean>) => {
      state.value.geoMsgFilter = action.payload
    },
    setGeoMsgFilterStep: (state, action: PayloadAction<number>) => {
      state.value.geoMsgFilterStep = action.payload
    },
    setGeoMsgFilterOffset: (state, action: PayloadAction<number>) => {
      state.value.geoMsgFilterOffset = action.payload
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload
    },
    resetCountsDates: (state) => {
      const now = DateTime.local().setZone(DateTime.local().zoneName)
      state.value.startDate = now.minus({ days: 1 }).toString()
      state.value.endDate = now.toString()
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getRsuData.pending, (state) => {
        state.loading = true
        state.value.rsuData = []
        state.value.rsuOnlineStatus = {}
      })
      .addCase(getRsuData.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(getRsuData.rejected, (state) => {
        state.loading = false
      })
      .addCase(getRsuInfoOnly.pending, (state) => {
        state.loading = true
      })
      .addCase(getRsuInfoOnly.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(getRsuInfoOnly.rejected, (state) => {
        state.loading = false
      })
      .addCase(getRsuLastOnline.pending, (state) => {
        state.loading = true
      })
      .addCase(getRsuLastOnline.fulfilled, (state, action) => {
        state.loading = false
        const payload = action.payload as RsuOnlineStatusRespSingle
        if (Object.prototype.hasOwnProperty.call(state.value.rsuOnlineStatus, payload.ip)) {
          ;(state.value.rsuOnlineStatus as RsuOnlineStatusRespMultiple)[payload.ip]['last_online'] = payload.last_online
        }
      })
      .addCase(getRsuLastOnline.rejected, (state) => {
        state.loading = false
      })
      .addCase(_getRsuInfo.fulfilled, (state, action) => {
        state.value.rsuData = action.payload
      })
      .addCase(_getRsuOnlineStatus.fulfilled, (state, action) => {
        state.value.rsuOnlineStatus = action.payload as RsuOnlineStatusRespMultiple
      })
      .addCase(getSsmSrmData.pending, (state) => {
        state.loading = true
      })
      .addCase(getSsmSrmData.rejected, (state) => {
        state.loading = false
      })
      .addCase(getSsmSrmData.fulfilled, (state, action) => {
        state.value.srmSsmList = action.payload
      })
      .addCase(getIssScmsStatus.fulfilled, (state, action) => {
        state.value.issScmsStatusData = action.payload ?? state.value.issScmsStatusData
      })
      .addCase(updateRowData.pending, (state) => {
        state.value.messageLoading = false
      })
      .addCase(updateRowData.fulfilled, (state, action) => {
        if (action.payload === null) return
        state.value.warningMessage = action.payload.warningMessage
        state.value.messageLoading = false
        state.value.startDate = action.payload.startDate
        state.value.endDate = action.payload.endDate
      })
      .addCase(updateRowData.rejected, (state) => {
        state.value.messageLoading = false
      })
      .addCase(updateGeoMsgData.pending, (state) => {
        state.loading = true
        state.value.addGeoMsgPoint = false
      })
      .addCase(updateGeoMsgData.fulfilled, (state, action) => {
        state.value.geoMsgData = action.payload.body
        state.loading = false
        state.value.geoMsgFilter = true
        state.value.geoMsgFilterStep = 60
        state.value.geoMsgFilterOffset = 0
      })
      .addCase(updateGeoMsgData.rejected, (state) => {
        state.loading = false
      })
  },
})

export const selectLoading = (state: RootState) => state.rsu.loading

export const selectSelectedRsu = (state: RootState) => state.rsu.value.selectedRsu
export const selectRsuManufacturer = (state: RootState) => state.rsu.value.selectedRsu?.properties?.manufacturer_name
export const selectRsuIpv4 = (state: RootState) => state.rsu.value.selectedRsu?.properties?.ipv4_address
export const selectRsuPrimaryRoute = (state: RootState) => state.rsu.value.selectedRsu?.properties?.primary_route
export const selectRsuData = (state: RootState) => state.rsu.value.rsuData
export const selectRsuOnlineStatus = (state: RootState) => state.rsu.value.rsuOnlineStatus
export const selectCurrentSort = (state: RootState) => state.rsu.value.currentSort
export const selectStartDate = (state: RootState) => state.rsu.value.startDate
export const selectEndDate = (state: RootState) => state.rsu.value.endDate
export const selectMessageLoading = (state: RootState) => state.rsu.value.messageLoading
export const selectWarningMessage = (state: RootState) => state.rsu.value.warningMessage
export const selectGeoMsgType = (state: RootState) => state.rsu.value.geoMsgType
export const selectRsuMapData = (state: RootState) => state.rsu.value.rsuMapData
export const selectMapList = (state: RootState) => state.rsu.value.mapList
export const selectMapDate = (state: RootState) => state.rsu.value.mapDate
export const selectDisplayMap = (state: RootState) => state.rsu.value.displayMap
export const selectGeoMsgStart = (state: RootState) => state.rsu.value.geoMsgStart
export const selectGeoMsgEnd = (state: RootState) => state.rsu.value.geoMsgEnd
export const selectAddGeoMsgPoint = (state: RootState) => state.rsu.value.addGeoMsgPoint
export const selectGeoMsgCoordinates = (state: RootState) => state.rsu.value.geoMsgCoordinates
export const selectGeoMsgData = (state: RootState) => state.rsu.value.geoMsgData
export const selectGeoMsgDateError = (state: RootState) => state.rsu.value.geoMsgDateError
export const selectGeoMsgFilter = (state: RootState) => state.rsu.value.geoMsgFilter
export const selectGeoMsgFilterStep = (state: RootState) => state.rsu.value.geoMsgFilterStep
export const selectGeoMsgFilterOffset = (state: RootState) => state.rsu.value.geoMsgFilterOffset
export const selectIssScmsStatusData = (state: RootState) => state.rsu.value.issScmsStatusData
export const selectSsmDisplay = (state: RootState) => state.rsu.value.ssmDisplay
export const selectSrmSsmList = (state: RootState) => state.rsu.value.srmSsmList
export const selectSelectedSrm = (state: RootState) => state.rsu.value.selectedSrm

export const {
  selectRsu,
  toggleMapDisplay,
  clearGeoMsg,
  toggleSsmSrmDisplay,
  setSelectedSrm,
  toggleGeoMsgPointSelect,
  updateGeoMsgPoints,
  updateGeoMsgDate,
  triggerGeoMsgDateError,
  changeGeoMsgType,
  setGeoMsgFilter,
  setGeoMsgFilterStep,
  setGeoMsgFilterOffset,
  setLoading,
  resetCountsDates,
} = rsuSlice.actions

export default rsuSlice.reducer

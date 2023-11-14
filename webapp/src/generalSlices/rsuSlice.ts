import { AnyAction, createAsyncThunk, createSlice, ThunkDispatch } from '@reduxjs/toolkit'
import RsuApi from '../apis/rsu-api'
import {
  ApiMsgRespWithCodes,
  IssScmsStatus,
  RsuCounts,
  RsuInfo,
  RsuMapInfo,
  RsuMapInfoIpList,
  RsuOnlineStatusRespMultiple,
  RsuOnlineStatusRespSingle,
  RsuProperties,
  SsmSrmData,
} from '../apis/rsu-api-types'
import { MessageType } from '../constants'
import { RootState } from '../store'
import { selectToken, selectOrganizationName } from './userSlice'
import { SelectedSrm } from '../types/Srm'
const { DateTime } = require('luxon')

const initialState = {
  selectedRsu: null as RsuInfo['rsuList'][0],
  rsuData: [] as RsuInfo['rsuList'],
  rsuOnlineStatus: {} as RsuOnlineStatusRespMultiple,
  rsuCounts: {} as RsuCounts,
  countList: [] as CountsListElement[],
  currentSort: '',
  startDate: '',
  endDate: '',
  messageLoading: false,
  warningMessage: false,
  msgType: 'BSM',
  rsuMapData: {} as RsuMapInfo['geojson'],
  mapList: [] as RsuMapInfoIpList,
  mapDate: '' as RsuMapInfo['date'],
  displayMap: false,
  bsmStart: '',
  bsmEnd: '',
  addBsmPoint: false,
  bsmCoordinates: [] as number[][],
  bsmData: [] as Array<GeoJSON.Feature<GeoJSON.Geometry>>,
  bsmDateError: false,
  bsmFilter: false,
  bsmFilterStep: 60,
  bsmFilterOffset: 0,
  issScmsStatusData: {} as IssScmsStatus,
  ssmDisplay: false,
  srmSsmList: [] as SsmSrmData,
  selectedSrm: [] as SelectedSrm[],
  heatMapData: {
    type: 'FeatureCollection',
    features: [],
  } as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
}

export const updateMessageType =
  (messageType: MessageType) => async (dispatch: ThunkDispatch<RootState, void, AnyAction>) => {
    dispatch(changeMessageType(messageType))
    dispatch(updateRowData({ message: messageType }))
  }

export const getRsuData = createAsyncThunk(
  'rsu/getRsuData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    await Promise.all([
      dispatch(_getRsuInfo()),
      dispatch(_getRsuOnlineStatus(currentState.rsu.value.rsuOnlineStatus)),
      dispatch(_getRsuCounts()),
      dispatch(
        _getRsuMapInfo({
          startDate: currentState.rsu.value.startDate,
          endDate: currentState.rsu.value.endDate,
        })
      ),
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

export const _getRsuCounts = createAsyncThunk('rsu/_getRsuCounts', async (_, { getState }) => {
  const currentState = getState() as RootState
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)

  const query_params = {
    message: currentState.rsu.value.msgType,
    start: currentState.rsu.value.startDate,
    end: currentState.rsu.value.endDate,
  }
  const rsuCounts =
    (await RsuApi.getRsuCounts(token, organization, '', query_params)) ?? currentState.rsu.value.rsuCounts
  const countList = Object.entries(rsuCounts).map(([key, value]) => {
    return {
      key: key,
      rsu: key,
      road: value.road,
      count: value.count,
    }
  })

  return { rsuCounts, countList }
})

export const _getRsuMapInfo = createAsyncThunk(
  'rsu/_getRsuMapInfo',
  async ({ startDate, endDate }: { startDate: string; endDate: string }, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)
    let local_date = DateTime.local({ zone: 'America/Denver' })
    let localEndDate = endDate === '' ? local_date.toString() : endDate
    let localStartDate = startDate === '' ? local_date.minus({ days: 1 }).toString() : startDate

    const rsuMapData = (await RsuApi.getRsuMapInfo(token, organization, '', {
      ip_list: 'True',
    })) as RsuMapInfoIpList

    return {
      endDate: localEndDate,
      startDate: localStartDate,
      rsuMapData,
    }
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

    const msgType = data.hasOwnProperty('message') ? data['message'] : currentState.rsu.value.msgType
    const startDate = data.hasOwnProperty('start') ? data['start'] : currentState.rsu.value.startDate
    const endDate = data.hasOwnProperty('end') ? data['end'] : currentState.rsu.value.endDate

    const warningMessage = new Date(endDate).getTime() - new Date(startDate).getTime() > 86400000

    const rsuCountsData = await RsuApi.getRsuCounts(token, organization, '', {
      message: msgType,
      start: startDate,
      end: endDate,
    })

    var countList = Object.entries(rsuCountsData).map(([key, value]) => {
      return {
        key: key,
        rsu: key,
        road: value.road,
        count: value.count,
      }
    })

    return {
      msgType,
      startDate,
      endDate,
      warningMessage,
      rsuCounts: rsuCountsData,
      countList,
    }
  },
  {
    condition: (_, { getState }) => selectToken(getState() as RootState) != undefined,
  }
)

export const updateBsmData = createAsyncThunk(
  'rsu/updateBsmData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    try {
      const bsmMapData: ApiMsgRespWithCodes & { body: Array<GeoJSON.Feature<GeoJSON.Geometry>> } =
        await RsuApi.postBsmData(
          token,
          {
            start: currentState.rsu.value.bsmStart,
            end: currentState.rsu.value.bsmEnd,
            geometry: currentState.rsu.value.bsmCoordinates,
          },
          ''
        )
      return bsmMapData
    } catch (err) {
      console.error(err)
    }
  },
  {
    // Will guard thunk from being executed
    condition: (_, { getState }) => {
      const { rsu } = getState() as RootState
      const valid = rsu.value.bsmStart !== '' && rsu.value.bsmEnd !== '' && rsu.value.bsmCoordinates.length > 2
      return valid
    },
  }
)

export const getMapData = createAsyncThunk(
  'rsu/getMapData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)
    const selectedRsu = selectSelectedRsu(currentState)

    const rsuMapData = (await RsuApi.getRsuMapInfo(token, organization, '', {
      ip_address: selectedRsu.properties.ipv4_address,
    })) as RsuMapInfo
    return {
      rsuMapData: rsuMapData.geojson,
      mapDate: rsuMapData.date,
    }
  },
  {
    condition: (_, { getState }) => selectToken(getState() as RootState) != undefined,
  }
)

export const rsuSlice = createSlice({
  name: 'rsu',
  initialState: {
    loading: false,
    requestOut: false,
    value: initialState,
  },
  reducers: {
    selectRsu: (state, action) => {
      state.value.selectedRsu = action.payload
    },
    toggleMapDisplay: (state) => {
      state.value.displayMap = !state.value.displayMap
    },
    clearBsm: (state) => {
      state.value.bsmCoordinates = []
      state.value.bsmData = []
      state.value.bsmStart = ''
      state.value.bsmEnd = ''
      state.value.bsmDateError = false
    },
    toggleSsmSrmDisplay: (state) => {
      state.value.ssmDisplay = !state.value.ssmDisplay
    },
    setSelectedSrm: (state, action) => {
      state.value.selectedSrm = Object.keys(action.payload).length === 0 ? [] : [action.payload]
    },
    toggleBsmPointSelect: (state) => {
      state.value.addBsmPoint = !state.value.addBsmPoint
    },
    updateBsmPoints: (state, action) => {
      state.value.bsmCoordinates = action.payload
    },
    updateBsmDate: (state, action) => {
      if (action.payload.type === 'start') state.value.bsmStart = action.payload.date
      else state.value.bsmEnd = action.payload.date
    },
    triggerBsmDateError: (state) => {
      state.value.bsmDateError = true
    },
    changeMessageType: (state, action) => {
      state.value.msgType = action.payload
    },
    setBsmFilter: (state, action) => {
      state.value.bsmFilter = action.payload
    },
    setBsmFilterStep: (state, action) => {
      state.value.bsmFilterStep = action.payload
    },
    setBsmFilterOffset: (state, action) => {
      state.value.bsmFilterOffset = action.payload
    },
    setLoading: (state, action) => {
      state.loading = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getRsuData.pending, (state) => {
        state.loading = true
        state.value.rsuData = []
        state.value.rsuOnlineStatus = {}
        state.value.rsuCounts = {}
        state.value.countList = []
        state.value.heatMapData = {
          type: 'FeatureCollection',
          features: [],
        }
      })
      .addCase(getRsuData.fulfilled, (state) => {
        const heatMapFeatures: GeoJSON.Feature<GeoJSON.Geometry>[] = []
        state.value.rsuData.forEach((rsu) => {
          heatMapFeatures.push({
            type: 'Feature',
            geometry: {
              type: 'Point',
              coordinates: [rsu.geometry.coordinates[0], rsu.geometry.coordinates[1]],
            },
            properties: {
              ipv4_address: rsu.properties.ipv4_address,
              count:
                rsu.properties.ipv4_address in state.value.rsuCounts
                  ? state.value.rsuCounts[rsu.properties.ipv4_address].count
                  : 0,
            },
          })
        })
        state.value.heatMapData.features = heatMapFeatures
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
        if (state.value.rsuOnlineStatus.hasOwnProperty(payload.ip)) {
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
      .addCase(_getRsuCounts.fulfilled, (state, action) => {
        state.value.rsuCounts = action.payload.rsuCounts
        state.value.countList = action.payload.countList
      })
      .addCase(_getRsuMapInfo.fulfilled, (state, action) => {
        state.value.startDate = action.payload.startDate
        state.value.endDate = action.payload.endDate
        state.value.mapList = action.payload.rsuMapData
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
        state.requestOut = true
        state.value.messageLoading = false
      })
      .addCase(updateRowData.fulfilled, (state, action) => {
        if (action.payload === null) return
        state.value.rsuCounts = action.payload.rsuCounts
        state.value.countList = action.payload.countList
        state.value.heatMapData.features.forEach((feat, index) => {
          const ip = feat.properties.ipv4_address as string
          state.value.heatMapData.features[index].properties.count =
            ip in action.payload.rsuCounts ? action.payload.rsuCounts[ip].count : 0
        })
        state.value.warningMessage = action.payload.warningMessage
        state.requestOut = false
        state.value.messageLoading = false
        state.value.msgType = action.payload.msgType
        state.value.startDate = action.payload.startDate
        state.value.endDate = action.payload.endDate
      })
      .addCase(updateRowData.rejected, (state) => {
        state.requestOut = false
        state.value.messageLoading = false
      })
      .addCase(updateBsmData.pending, (state) => {
        state.loading = true
        state.value.addBsmPoint = false
        state.value.bsmDateError =
          new Date(state.value.bsmEnd).getTime() - new Date(state.value.bsmStart).getTime() > 86400000
      })
      .addCase(updateBsmData.fulfilled, (state, action) => {
        state.value.bsmData = action.payload.body
        state.loading = false
        state.value.bsmFilter = true
        state.value.bsmFilterStep = 60
        state.value.bsmFilterOffset = 0
      })
      .addCase(updateBsmData.rejected, (state) => {
        state.loading = false
      })
      .addCase(getMapData.pending, (state) => {
        state.loading = true
      })
      .addCase(getMapData.fulfilled, (state, action) => {
        state.loading = false
        state.value.rsuMapData = action.payload.rsuMapData
        state.value.mapDate = action.payload.mapDate
      })
      .addCase(getMapData.rejected, (state) => {
        state.loading = false
      })
  },
})

export const selectLoading = (state: RootState) => state.rsu.loading
export const selectRequestOut = (state: RootState) => state.rsu.requestOut

export const selectSelectedRsu = (state: RootState) => state.rsu.value.selectedRsu
export const selectRsuManufacturer = (state: RootState) => state.rsu.value.selectedRsu?.properties?.manufacturer_name
export const selectRsuIpv4 = (state: RootState) => state.rsu.value.selectedRsu?.properties?.ipv4_address
export const selectRsuPrimaryRoute = (state: RootState) => state.rsu.value.selectedRsu?.properties?.primary_route
export const selectRsuData = (state: RootState) => state.rsu.value.rsuData
export const selectRsuOnlineStatus = (state: RootState) => state.rsu.value.rsuOnlineStatus
export const selectRsuCounts = (state: RootState) => state.rsu.value.rsuCounts
export const selectCountList = (state: RootState) => state.rsu.value.countList
export const selectCurrentSort = (state: RootState) => state.rsu.value.currentSort
export const selectStartDate = (state: RootState) => state.rsu.value.startDate
export const selectEndDate = (state: RootState) => state.rsu.value.endDate
export const selectMessageLoading = (state: RootState) => state.rsu.value.messageLoading
export const selectWarningMessage = (state: RootState) => state.rsu.value.warningMessage
export const selectMsgType = (state: RootState) => state.rsu.value.msgType
export const selectRsuMapData = (state: RootState) => state.rsu.value.rsuMapData
export const selectMapList = (state: RootState) => state.rsu.value.mapList
export const selectMapDate = (state: RootState) => state.rsu.value.mapDate
export const selectDisplayMap = (state: RootState) => state.rsu.value.displayMap
export const selectBsmStart = (state: RootState) => state.rsu.value.bsmStart
export const selectBsmEnd = (state: RootState) => state.rsu.value.bsmEnd
export const selectAddBsmPoint = (state: RootState) => state.rsu.value.addBsmPoint
export const selectBsmCoordinates = (state: RootState) => state.rsu.value.bsmCoordinates
export const selectBsmData = (state: RootState) => state.rsu.value.bsmData
export const selectBsmDateError = (state: RootState) => state.rsu.value.bsmDateError
export const selectBsmFilter = (state: RootState) => state.rsu.value.bsmFilter
export const selectBsmFilterStep = (state: RootState) => state.rsu.value.bsmFilterStep
export const selectBsmFilterOffset = (state: RootState) => state.rsu.value.bsmFilterOffset
export const selectIssScmsStatusData = (state: RootState) => state.rsu.value.issScmsStatusData
export const selectSsmDisplay = (state: RootState) => state.rsu.value.ssmDisplay
export const selectSrmSsmList = (state: RootState) => state.rsu.value.srmSsmList
export const selectSelectedSrm = (state: RootState) => state.rsu.value.selectedSrm
export const selectHeatMapData = (state: RootState) => state.rsu.value.heatMapData

export const {
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
} = rsuSlice.actions

export default rsuSlice.reducer

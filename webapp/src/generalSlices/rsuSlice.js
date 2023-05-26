import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import CdotApi from '../apis/cdot-rsu-api'
import { selectToken, selectOrganizationName } from './userSlice'
const { DateTime } = require('luxon')

const initialState = {
  selectedRsu: null,
  rsuData: [],
  rsuOnlineStatus: {},
  rsuCounts: {},
  countList: [],
  currentSort: '',
  startDate: '',
  endDate: '',
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
  heatMapData: {
    type: 'FeatureCollection',
    features: [],
  },
}

export const updateMessageType = (messageType) => async (dispatch) => {
  dispatch(changeMessageType(messageType))
  dispatch(updateRowData({ message: messageType }))
}

export const getRsuData = createAsyncThunk(
  'rsu/getRsuData',
  async (_, { getState, dispatch }) => {
    const currentState = getState()
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    await Promise.all([
      dispatch(_getRsuInfo({ token, organization })),
      dispatch(
        _getRsuOnlineStatus({
          token,
          organization,
          rsuOnlineStatusState: currentState.rsu.value.rsuOnlineStatus,
        })
      ),
      dispatch(_getRsuCounts({ token, organization })),
      dispatch(
        _getRsuMapInfo({
          token,
          organization,
          startDate: currentState.rsu.value.startDate,
          endDate: currentState.rsu.value.endDate,
        })
      ),
    ])

    return
  },
  {
    condition: (_, { getState }) => selectToken(getState()),
  }
)

export const getRsuInfoOnly = createAsyncThunk('rsu/getRsuInfoOnly', async (_, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuInfo = await CdotApi.getRsuInfo(token, organization)
  const rsuData = rsuInfo.rsuList
  return rsuData
})

export const getRsuLastOnline = createAsyncThunk('rsu/getRsuLastOnline', async (rsu_ip, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuLastOnline = await CdotApi.getRsuOnline(token, organization, '', { rsu_ip: rsu_ip })
  return rsuLastOnline
})

const _getRsuInfo = createAsyncThunk('rsu/_getRsuInfo', async (_, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuInfo = await CdotApi.getRsuInfo(token, organization)
  const rsuData = rsuInfo.rsuList

  return rsuData
})

const _getRsuOnlineStatus = createAsyncThunk('rsu/_getRsuOnlineStatus', async (rsuOnlineStatusState, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const rsuOnlineStatus = (await CdotApi.getRsuOnline(token, organization)) ?? rsuOnlineStatusState

  return rsuOnlineStatus
})

export const _getRsuCounts = createAsyncThunk('rsu/_getRsuCounts', async (_, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)

  const query_params = {
    message: currentState.rsu.value.msgType,
    start: currentState.rsu.value.startDate,
    end: currentState.rsu.value.endDate,
  }
  const rsuCounts =
    (await CdotApi.getRsuCounts(token, organization, '', query_params)) ?? currentState.rsu.value.rsuCounts
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

const _getRsuMapInfo = createAsyncThunk('rsu/_getRsuMapInfo', async ({ startDate, endDate }, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  let local_date = DateTime.local({ zone: 'America/Denver' })
  let localEndDate = endDate === '' ? local_date.toString() : endDate
  let localStartDate = startDate === '' ? local_date.minus({ days: 1 }).toString() : startDate

  const rsuMapData = await CdotApi.getRsuMapInfo(token, organization, '', {
    ip_list: 'True',
  })

  return {
    endDate: localEndDate,
    startDate: localStartDate,
    rsuMapData,
  }
})

export const getSsmSrmData = createAsyncThunk('rsu/getSsmSrmData', async (_, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const srmSsmList = await CdotApi.getSsmSrmData(token)

  return srmSsmList
})

export const getIssScmsStatus = createAsyncThunk(
  'rsu/getIssScmsStatus',
  async (_, { getState }) => {
    try {
      const currentState = getState()
      const token = selectToken(currentState)
      const organization = selectOrganizationName(currentState)

      return await CdotApi.getIssScmsStatus(token, organization)
    } catch (err) {
      console.error(err)
    }
  },
  {
    condition: (_, { getState }) => selectToken(getState()),
  }
)

export const updateRowData = createAsyncThunk(
  'rsu/updateRowData',
  async (data, { getState }) => {
    const currentState = getState()
    const token = selectToken(currentState)
    const organization = selectOrganizationName(currentState)

    const msgType = data.hasOwnProperty('message') ? data['message'] : currentState.rsu.value.msgType
    const startDate = data.hasOwnProperty('start') ? data['start'] : currentState.rsu.value.startDate
    const endDate = data.hasOwnProperty('end') ? data['end'] : currentState.rsu.value.endDate

    const warningMessage = new Date(endDate).getTime() - new Date(startDate).getTime() > 86400000

    const rsuCountsData = await CdotApi.getRsuCounts(
      token,
      organization,
      '',
      {
        message: msgType,
        start: startDate,
        end: endDate,
      },
      (err) => {
        if (err.name === 'AbortError') {
          console.error('previous request aborted')
          return
        }
      }
    )

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
    condition: (_, { getState }) => selectToken(getState()),
  }
)

export const updateBsmData = createAsyncThunk(
  'rsu/updateBsmData',
  async (_, { getState }) => {
    const currentState = getState()
    const token = selectToken(currentState)

    try {
      const bsmMapData = await CdotApi.postBsmData(
        token,
        JSON.stringify({
          start: currentState.rsu.value.bsmStart,
          end: currentState.rsu.value.bsmEnd,
          geometry: currentState.rsu.value.bsmCoordinates,
        }),
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
      const { rsu } = getState()
      const valid = rsu.value.bsmStart !== '' && rsu.value.bsmEnd !== '' && rsu.value.bsmCoordinates.length > 2
      return valid
    },
  }
)

export const getMapData = createAsyncThunk('rsu/getMapData', async (_, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  const organization = selectOrganizationName(currentState)
  const selectedRsu = selectSelectedRsu(currentState)

  try {
    const rsuMapData = await CdotApi.getRsuMapInfo(token, organization, '', {
      ip_address: selectedRsu.properties.ipv4_address,
    })
    return {
      rsuMapData: rsuMapData.geojson,
      mapDate: rsuMapData.date,
    }
  } catch (err) {
    console.error(err)
  }
})

export const rsuSlice = createSlice({
  name: 'rsu',
  initialState: {
    loading: false,
    bsmLoading: false,
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
      state.value.selectedSrm = action.payload === {} ? [] : [action.payload]
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
    sortCountList: (state, action) => {
      let arrayCopy = [...state.value.countList]
      arrayCopy.sort(this.compareBy(action.payload))
      state.value.countList = arrayCopy
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
        const heatMapFeatures = []
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
        if (state.value.rsuOnlineStatus.hasOwnProperty(action.payload.ip)) {
          state.value.rsuOnlineStatus[action.payload.ip]['last_online'] = action.payload.last_online
        }
      })
      .addCase(getRsuLastOnline.rejected, (state) => {
        state.loading = false
      })
      .addCase(_getRsuInfo.fulfilled, (state, action) => {
        state.value.rsuData = action.payload
      })
      .addCase(_getRsuOnlineStatus.fulfilled, (state, action) => {
        state.value.rsuOnlineStatus = action.payload
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
        state.value.requestOut = true
        state.value.messageLoading = false
      })
      .addCase(updateRowData.fulfilled, (state, action) => {
        if (action.payload === null) return
        state.value.rsuCounts = action.payload.rsuCounts
        state.value.countList = action.payload.countList
        state.value.heatMapData.features.forEach((feat, index) => {
          state.value.heatMapData.features[index].properties.count =
            feat.properties.ipv4_address in action.payload.rsuCounts
              ? action.payload.rsuCounts[feat.properties.ipv4_address].count
              : 0
        })
        state.value.warningMessage = action.payload.warningMessage
        state.value.requestOut = false
        state.value.messageLoading = false
        state.value.msgType = action.payload.msgType
        state.value.startDate = action.payload.startDate
        state.value.endDate = action.payload.endDate
        state.value.warningMessage = action.payload.warningMessage
      })
      .addCase(updateRowData.rejected, (state) => {
        state.value.requestOut = false
        state.value.messageLoading = false
      })
      .addCase(updateBsmData.pending, (state) => {
        state.bsmLoading = true
        state.value.addBsmPoint = false
        state.value.bsmDateError =
          new Date(state.value.bsmEnd).getTime() - new Date(state.value.bsmStart).getTime() > 86400000
      })
      .addCase(updateBsmData.fulfilled, (state, action) => {
        state.value.bsmData = action.payload.body
        state.bsmLoading = false
        state.value.bsmFilter = true
        state.value.bsmFilterStep = 60
        state.value.bsmFilterOffset = 0
      })
      .addCase(updateBsmData.rejected, (state) => {
        state.bsmLoading = false
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

export const selectLoading = (state) => {
  return state.rsu.loading
}
export const selectBsmLoading = (state) => state.rsu.bsmLoading
export const selectRequestOut = (state) => state.rsu.requestOut

export const selectSelectedRsu = (state) => state.rsu.value.selectedRsu
export const selectRsuManufacturer = (state) => state.rsu.value.selectedRsu?.properties?.manufacturer_name
export const selectRsuIpv4 = (state) => state.rsu.value.selectedRsu?.properties?.ipv4_address
export const selectRsuPrimaryRoute = (state) => state.rsu.value.selectedRsu?.properties?.primary_route
export const selectRsuData = (state) => state.rsu.value.rsuData
export const selectRsuOnlineStatus = (state) => state.rsu.value.rsuOnlineStatus
export const selectRsuCounts = (state) => state.rsu.value.rsuCounts
export const selectCountList = (state) => state.rsu.value.countList
export const selectCurrentSort = (state) => state.rsu.value.currentSort
export const selectStartDate = (state) => state.rsu.value.startDate
export const selectEndDate = (state) => state.rsu.value.endDate
export const selectMessageLoading = (state) => state.rsu.value.messageLoading
export const selectWarningMessage = (state) => state.rsu.value.warningMessage
export const selectMsgType = (state) => state.rsu.value.msgType
export const selectRsuMapData = (state) => state.rsu.value.rsuMapData
export const selectMapList = (state) => state.rsu.value.mapList
export const selectMapDate = (state) => state.rsu.value.mapDate
export const selectDisplayMap = (state) => state.rsu.value.displayMap
export const selectBsmStart = (state) => state.rsu.value.bsmStart
export const selectBsmEnd = (state) => state.rsu.value.bsmEnd
export const selectAddBsmPoint = (state) => state.rsu.value.addBsmPoint
export const selectBsmCoordinates = (state) => state.rsu.value.bsmCoordinates
export const selectBsmData = (state) => state.rsu.value.bsmData
export const selectBsmDateError = (state) => state.rsu.value.bsmDateError
export const selectBsmFilter = (state) => state.rsu.value.bsmFilter
export const selectBsmFilterStep = (state) => state.rsu.value.bsmFilterStep
export const selectBsmFilterOffset = (state) => state.rsu.value.bsmFilterOffset
export const selectIssScmsStatusData = (state) => state.rsu.value.issScmsStatusData
export const selectSsmDisplay = (state) => state.rsu.value.ssmDisplay
export const selectSrmSsmList = (state) => state.rsu.value.srmSsmList
export const selectSelectedSrm = (state) => state.rsu.value.selectedSrm
export const selectHeatMapData = (state) => state.rsu.value.heatMapData

export const {
  selectRsu,
  toggleMapDisplay,
  updateRowDataSimple,
  clearBsm,
  toggleSsmSrmDisplay,
  setSelectedSrm,
  toggleBsmPointSelect,
  updateBsmPoints,
  updateBsmDate,
  triggerBsmDateError,
  sortCountList,
  changeMessageType,
  setBsmFilter,
  setBsmFilterStep,
  setBsmFilterOffset,
  setLoading,
} = rsuSlice.actions

export default rsuSlice.reducer

import { PayloadAction, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../../generalSlices/userSlice'
import { v4 as uuidv4 } from 'uuid'
import { RootState } from '../../../store'
import DecoderApi from '../../../apis/intersections/decoder-api'
import { getTimestamp } from '../map/map-component'
import {
  pullInitialData,
  resetMapView,
  selectIntersectionId,
  selectLoadOnNull,
  selectRoadRegulatorId,
  selectSourceDataType,
  setDecoderModeEnabled,
  setMapProps,
} from '../map/map-slice'

const initialState = {
  data: {} as { [id: string]: DecoderDataEntry },
  selectedMapMessage: undefined as undefined | { id: string; intersectionId: number; rsuIp: string },
  selectedBsms: [] as string[],
  currentBsms: [] as OdeBsmData[],
  dialogOpen: false,
}

const submitDecoderRequest = (token: string, data: string, type: DECODER_MESSAGE_TYPE) => {
  return DecoderApi.submitDecodeRequest({
    token,
    data,
    type,
  })
}

const getTimestampFromType = (type: DECODER_MESSAGE_TYPE, decodedResponse: DecoderApiResponseGeneric | undefined) => {
  switch (type) {
    case 'MAP':
      return getTimestamp(decodedResponse?.processedMap?.properties.odeReceivedAt)
    case 'SPAT':
      return getTimestamp(decodedResponse?.processedSpat?.utcTimeStamp)
    case 'BSM':
      return getTimestamp(decodedResponse?.bsm?.metadata.odeReceivedAt)
  }
}

const getIntersectionId = (decodedResponse: DecoderApiResponseGeneric | undefined): number | undefined => {
  if (!decodedResponse) {
    return undefined
  }

  switch (decodedResponse.type) {
    case 'MAP':
      const mapPayload = decodedResponse.processedMap
      return mapPayload?.properties?.intersectionId
    case 'SPAT':
      const spatPayload = decodedResponse.processedSpat
      return spatPayload?.intersectionId
    default:
      return undefined
  }
}

const updateRecordWithResponse = (
  record: DecoderDataEntry,
  id: string,
  response: DecoderApiResponseGeneric | undefined
): DecoderDataEntry => ({
  ...record,
  decodedResponse: response,
  timestamp: getTimestampFromType(record.type, response),
  status: record.text == '' ? 'NOT_STARTED' : response?.decodeErrors !== '' ? 'ERROR' : 'COMPLETED',
})

const isGreyedOut = (mapMessageIntersectionId: number, intersectionId: number | undefined) => {
  return mapMessageIntersectionId === undefined || intersectionId !== mapMessageIntersectionId
}

export const onTextChanged = createAsyncThunk(
  'asn1Decoder/onTextChanged',
  async (payload: { id: string; text: string; type: DECODER_MESSAGE_TYPE }, { getState, dispatch }) => {
    const { id, text, type } = payload

    const token = selectToken(getState() as RootState)
    const data = selectData(getState() as RootState)

    submitDecoderRequest(token, text, type)?.then((response) => {
      dispatch(textChangedResponse({ id, type, response }))
    })
    const prevData = {
      ...data,
      [id]: {
        id: id,
        type: type,
        status: 'IN_PROGRESS',
        selected: false,
        isGreyedOut: false,
        text: text,
        decodedResponse: undefined,
      },
    }
    let newEntry = {}
    if (
      prevData[id].text != undefined &&
      Object.values(prevData).find((v) => v.type == type && v.text == '') == undefined
    ) {
      let newId = uuidv4()
      newEntry[newId] = {
        id: newId,
        type: type,
        status: 'NOT_STARTED',
        text: '',
        selected: false,
        isGreyedOut: false,
        decodedResponse: undefined,
      }
    }
    return {
      ...newEntry,
      ...data,
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const onFileUploaded = createAsyncThunk(
  'asn1Decoder/onFileUploaded',
  async (payload: { contents: string[]; type: DECODER_MESSAGE_TYPE }, { getState, dispatch }) => {
    const { contents, type } = payload

    const token = selectToken(getState() as RootState)
    const data = selectData(getState() as RootState)

    const promises: Promise<DecoderApiResponseGeneric | undefined>[] = []

    const keyedContents: { id: string; text: string }[] = contents.map((text) => ({ id: uuidv4(), text: text }))

    let newEntries = {}
    keyedContents.forEach(({ id, text }) => {
      promises.push(submitDecoderRequest(token, text, type))
      newEntries[id] = {
        id,
        type: type,
        status: 'IN_PROGRESS',
        text: text,
        timestamp: undefined,
        selected: false,
        isGreyedOut: false,
        decodedResponse: undefined,
      }
    })

    Promise.all(promises).then((responses) => {
      dispatch(
        textChangedResponseMultiple(
          responses.map((response, index) => ({ id: keyedContents[index].id, type, response }))
        )
      )
    })

    return {
      ...data,
      ...newEntries,
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const updateAllDataOnMap = createAsyncThunk(
  'asn1Decoder/updateAllDataOnMap',
  async (_, { getState, dispatch }) => {
    const data = selectData(getState() as RootState)
    const selectedMapMessage = selectSelectedMapMessage(getState() as RootState)
    const currentBsms = selectCurrentBsms(getState() as RootState)
    const sourceDataType = selectSourceDataType(getState() as RootState)
    const intersectionId = selectIntersectionId(getState() as RootState)
    const roadRegulatorId = selectRoadRegulatorId(getState() as RootState)
    const loadOnNull = selectLoadOnNull(getState() as RootState)

    dispatch(
      setMapProps({
        sourceData: {
          map: Object.values(data)
            .filter((v) => v.type === 'MAP' && v.status == 'COMPLETED' && v.id == selectedMapMessage?.id)
            .map((v) => v.decodedResponse?.processedMap!),
          spat: Object.values(data)
            .filter(
              (v) =>
                v.type === 'SPAT' &&
                v.status == 'COMPLETED' &&
                !isGreyedOut(selectedMapMessage.intersectionId, getIntersectionId(v.decodedResponse))
            )
            .map((v) => v.decodedResponse?.processedSpat!),
          bsm: currentBsms,
        },
        sourceDataType,
        intersectionId,
        roadRegulatorId,
        loadOnNull,
      })
    )
    dispatch(pullInitialData())
  }
)
export const decoderModeToggled = createAsyncThunk(
  'asn1Decoder/decoderModeToggled',
  async (enabled: boolean, { getState, dispatch }) => {
    const sourceDataType = selectSourceDataType(getState() as RootState)
    const intersectionId = selectIntersectionId(getState() as RootState)
    const roadRegulatorId = selectRoadRegulatorId(getState() as RootState)
    const loadOnNull = selectLoadOnNull(getState() as RootState)

    if (enabled) {
      dispatch(resetMapView())
      dispatch(setDecoderModeEnabled(true))
    } else {
      dispatch(resetMapView())
      dispatch(setDecoderModeEnabled(false))
      dispatch(
        setMapProps({
          sourceData: {
            map: [],
            spat: [],
            bsm: [],
          },
          sourceDataType,
          intersectionId,
          roadRegulatorId,
          loadOnNull,
        })
      )
      dispatch(pullInitialData())
    }
  }
)

export const asn1DecoderSlice = createSlice({
  name: 'asn1Decoder',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    addSelectedBsm: (state, action: PayloadAction<string>) => {
      state.value.selectedBsms = [...state.value.selectedBsms, action.payload]
    },
    removeSelectedBsm: (state, action: PayloadAction<string>) => {
      state.value.selectedBsms = state.value.selectedBsms.filter((bsmId) => bsmId !== action.payload)
    },
    toggleBsmSelection: (state) => {
      if (state.value.selectedBsms.length == Object.values(state.value.data).filter((v) => v.type === 'BSM').length) {
        state.value.selectedBsms = []
      } else {
        state.value.selectedBsms = Object.values(state.value.data)
          .filter((v) => v.type === 'BSM')
          .map((v) => v.id)
      }
    },
    onItemDeleted: (state, action: PayloadAction<string>) => {
      const id = action.payload
      if (state.value.data[id]?.text != '') {
        delete state.value.data[id]
      }
    },
    textChangedResponse: (
      state,
      action: PayloadAction<{ id: string; type: DECODER_MESSAGE_TYPE; response: DecoderApiResponseGeneric }>
    ) => {
      const { id, type, response } = action.payload
      if (type == 'BSM') {
        state.value.selectedBsms = [...state.value.selectedBsms, id]
      }
      state.value.data[id] = updateRecordWithResponse(state.value.data[id], id, response)
    },
    textChangedResponseMultiple: (
      state,
      action: PayloadAction<{ id: string; type: DECODER_MESSAGE_TYPE; response: DecoderApiResponseGeneric }[]>
    ) => {
      const responses = action.payload
      const selectedBsms: string[] = responses
        .map(({ id, type }) => (type == 'BSM' ? id : null))
        .filter((id) => id != null)
      const data: { [id: string]: DecoderDataEntry } = {}
      responses.forEach(({ id, type, response }) => {
        data[id] = updateRecordWithResponse(state.value.data[id], id, response)
      })
      state.value.selectedBsms = [...state.value.selectedBsms, ...selectedBsms]
      state.value.data = { ...state.value.data, ...data }
    },
    onItemSelected: (state, action: PayloadAction<string>) => {
      const id = action.payload
      const type = state.value.data[id].type
      switch (type) {
        case 'MAP':
          const intersectionId = state.value.data[id]?.decodedResponse?.processedMap?.properties?.intersectionId
          const rsuIp = state.value.data[id]?.decodedResponse?.processedMap?.properties?.originIp
          if (intersectionId) {
            state.value.selectedMapMessage = { id, intersectionId, rsuIp: rsuIp! }
          }
          return
        case 'BSM':
          if (state.value.selectedBsms.includes(id)) {
            state.value.selectedBsms = state.value.selectedBsms.filter((bsmId) => bsmId !== id)
          } else {
            state.value.selectedBsms = [...state.value.selectedBsms, id]
          }
      }
    },
    initializeData: (state) => {
      const freshData = [] as DecoderDataEntry[]
      for (let i = 0; i < 3; i++) {
        const id = uuidv4()
        if (i % 3 == 2) {
          state.value.selectedBsms = [...state.value.selectedBsms, id]
        }
        freshData.push({
          id: id,
          type: i % 3 == 0 ? 'MAP' : i % 3 == 1 ? 'SPAT' : 'BSM',
          status: 'NOT_STARTED',
          text: '',
          selected: false,
          isGreyedOut: false,
          decodedResponse: undefined,
        })
      }
      state.value.data = freshData.reduce((acc, entry) => ({ ...acc, [entry.id]: entry }), {})
    },
    updateCurrentBsms: (state, action: PayloadAction<DecoderDataEntry[]>) => {
      const newBsmData = action.payload
        .filter((v) => v.type === 'BSM' && v.status === 'COMPLETED' && state.value.selectedBsms.includes(v.id))
        .map((v) => v.decodedResponse?.bsm)
      state.value.currentBsms = newBsmData.filter((v) => v !== undefined)
    },
    setAsn1DecoderDialogOpen: (state, action: PayloadAction<boolean>) => {
      state.value.dialogOpen = action.payload
    },
  },
  extraReducers: (builder) => {
    builder.addCase(onTextChanged.fulfilled, (state, action) => {
      state.value.data = action.payload
    })
    builder.addCase(onFileUploaded.fulfilled, (state, action) => {
      state.value.data = action.payload
    })
  },
})

export const {
  addSelectedBsm,
  removeSelectedBsm,
  toggleBsmSelection,
  onItemDeleted,
  textChangedResponse,
  textChangedResponseMultiple,
  onItemSelected,
  initializeData,
  updateCurrentBsms,
  setAsn1DecoderDialogOpen,
} = asn1DecoderSlice.actions

export const selectData = (state: RootState) => state.asn1Decoder.value.data
export const selectSelectedMapMessage = (state: RootState) => state.asn1Decoder.value.selectedMapMessage
export const selectSelectedBsms = (state: RootState) => state.asn1Decoder.value.selectedBsms
export const selectCurrentBsms = (state: RootState) => state.asn1Decoder.value.currentBsms
export const selectDialogOpen = (state: RootState) => state.asn1Decoder.value.dialogOpen

export default asn1DecoderSlice.reducer

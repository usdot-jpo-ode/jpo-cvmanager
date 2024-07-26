import { PayloadAction, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import dayjs from 'dayjs'

interface Item {
  label: string
  value: string
}

interface DataSelectorFormType {
  type: string
  startDate: Date
  timeRange: number
  timeUnit: dayjs.ManipulateType
  intersectionId: number | undefined
  roadRegulatorId: number
  submit: boolean | null

  // type specific filters
  bsmVehicleId: number | null
  eventTypes: Item[]
  assessmentTypes: Item[]
}

const initialState = {
  type: '',
  events: [] as MessageMonitor.Event[],
  assessments: [] as Assessment[],
  graphData: [] as GraphArrayDataType[],
  openMapDialog: false,
  roadRegulatorIntersectionIds: {} as { [roadRegulatorId: number]: number[] },
  dataSelectorForm: {
    type: 'events',
    startDate: new Date(),
    timeRange: 0,
    timeUnit: 'minutes' as dayjs.ManipulateType,
    intersectionId: undefined as number | undefined,
    roadRegulatorId: -1,
    submit: null as boolean | null,

    // type specific filters
    bsmVehicleId: null,
    eventTypes: [] as Item[],
    assessmentTypes: [] as Item[],
  } as DataSelectorFormType,
}

export const dataSelectorSlice = createSlice({
  name: 'dataSelector',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setType: (state, action: PayloadAction<string>) => {
      state.value.type = action.payload
    },
    setEvents: (state, action: PayloadAction<MessageMonitor.Event[]>) => {
      state.value.events = action.payload
    },
    setAssessments: (state, action: PayloadAction<Assessment[]>) => {
      state.value.assessments = action.payload
    },
    setGraphData: (state, action: PayloadAction<GraphArrayDataType[]>) => {
      state.value.graphData = action.payload
    },
    setOpenMapDialog: (state, action: PayloadAction<boolean>) => {
      state.value.openMapDialog = action.payload
    },
    setRoadRegulatorIntersectionIds: (state, action: PayloadAction<{ [roadRegulatorId: number]: number[] }>) => {
      state.value.roadRegulatorIntersectionIds = action.payload
    },
    setDataSelectorForm: (state, action: PayloadAction<DataSelectorFormType>) => {
      state.value.dataSelectorForm = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
  },
})

export const {
  setType,
  setEvents,
  setAssessments,
  setGraphData,
  setOpenMapDialog,
  setRoadRegulatorIntersectionIds,
  setDataSelectorForm,
} = dataSelectorSlice.actions

export const selectLoading = (state: RootState) => state.dataSelector.loading
export const selectType = (state: RootState) => state.dataSelector.value.type
export const selectEvents = (state: RootState) => state.dataSelector.value.events
export const selectAssessments = (state: RootState) => state.dataSelector.value.assessments
export const selectGraphData = (state: RootState) => state.dataSelector.value.graphData
export const selectOpenMapDialog = (state: RootState) => state.dataSelector.value.openMapDialog
export const selectRoadRegulatorIntersectionIds = (state: RootState) =>
  state.dataSelector.value.roadRegulatorIntersectionIds
export const selectDataSelectorForm = (state: RootState) => state.dataSelector.value.dataSelectorForm

export default dataSelectorSlice.reducer

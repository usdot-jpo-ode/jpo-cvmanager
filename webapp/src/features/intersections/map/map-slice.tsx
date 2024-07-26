import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { selectToken } from '../../../generalSlices/userSlice'
import { CompatClient, IMessage, Stomp } from '@stomp/stompjs'
import MessageMonitorApi from '../../../apis/intersections/mm-api'
import EventsApi from '../../../apis/intersections/events-api'
import NotificationApi from '../../../apis/intersections/notification-api'
import toast from 'react-hot-toast'
import {
  generateSignalStateFeatureCollection,
  parseBsmToGeojson,
  parseMapSignalGroups,
  parseSpatSignalGroups,
} from './utilities/message-utils'
import { generateColorDictionary, generateMapboxStyleExpression } from './utilities/colors'
import { setBsmCircleColor, setBsmLegendColors } from './map-layer-style-slice'
import { getTimeRange } from './utilities/map-utils'
import { ViewState } from 'react-map-gl'
import { selectRsuMapData } from '../../../generalSlices/rsuSlice'
import { SsmSrmData } from '../../../apis/rsu-api-types'
import EnvironmentVars from '../../../EnvironmentVars'
import { downloadAllData } from './utilities/file-utilities'

export type MAP_LAYERS =
  | 'map-message'
  | 'map-message-labels'
  | 'connecting-lanes'
  | 'connecting-lanes-labels'
  | 'invalid-lane-collection'
  | 'bsm'
  | 'signal-states'

export type MAP_QUERY_PARAMS = {
  startDate: Date
  endDate: Date
  eventDate: Date
  vehicleId?: string
  intersectionId?: number
  roadRegulatorId?: number
  default?: boolean
}

export type IMPORTED_MAP_MESSAGE_DATA = {
  mapData: ProcessedMap[]
  bsmData: OdeBsmData[]
  spatData: ProcessedSpat[]
  notificationData: any
}

type timestamp = {
  timestamp: number
}

export type MAP_PROPS = {
  sourceData:
    | MessageMonitor.Notification
    | MessageMonitor.Event
    | Assessment
    | timestamp
    | {
        map: ProcessedMap[]
        spat: ProcessedSpat[]
        bsm: OdeBsmData[]
      }
    | undefined
  sourceDataType: 'notification' | 'event' | 'assessment' | 'timestamp' | 'exact' | undefined
  intersectionId: number | undefined
  roadRegulatorId: number | undefined
  loadOnNull?: boolean
}

export type RAW_MESSAGE_DATA_EXPORT = {
  map?: ProcessedMap[]
  spat?: ProcessedSpat[]
  bsm?: BsmFeatureCollection
  notification?: MessageMonitor.Notification
  event?: MessageMonitor.Event
  assessment?: Assessment
}

export type BSM_COUNTS_CHART_DATA = MessageMonitor.MinuteCount & {
  minutesAfterMidnight: number
  timestamp: string
}

interface MinimalClient {
  connect: (headers: unknown, connectCallback: () => void, errorCallback?: (error: string) => void) => void
  subscribe: (destination: string, callback: (message: IMessage) => void) => void
  disconnect: (disconnectCallback: () => void) => void
}

const getTimestamp = (dt: any): number => {
  try {
    const dtFromString = Date.parse(dt as any as string)
    if (isNaN(dtFromString)) {
      if (dt > 1000000000000) {
        return dt // already in milliseconds
      } else {
        return dt * 1000
      }
    } else {
      return dtFromString
    }
  } catch (e) {
    console.error('Failed to parse timestamp from value: ' + dt, e)
    return 0
  }
}

const initialState = {
  layersVisible: {
    'map-message': false,
    'map-message-labels': false,
    'connecting-lanes': false,
    'connecting-lanes-labels': false,
    'invalid-lane-collection': false,
    bsm: false,
    'signal-states': false,
  } as Record<MAP_LAYERS, boolean>,
  allInteractiveLayerIds: ['map-message', 'connecting-lanes', 'signal-states', 'bsm'] as MAP_LAYERS[],
  queryParams: {
    startDate: new Date(Date.now() - 1000 * 60 * 1),
    endDate: new Date(Date.now() + 1000 * 60 * 1),
    eventDate: new Date(Date.now()),
    vehicleId: undefined,
    intersectionId: undefined,
    roadRegulatorId: undefined,
  } as MAP_QUERY_PARAMS,
  sourceData: undefined as MAP_PROPS['sourceData'] | undefined,
  sourceDataType: undefined as MAP_PROPS['sourceDataType'] | undefined,
  intersectionId: undefined as MAP_PROPS['intersectionId'] | undefined,
  roadRegulatorId: undefined as MAP_PROPS['roadRegulatorId'] | undefined,
  loadOnNull: true as MAP_PROPS['loadOnNull'] | undefined,
  mapData: undefined as ProcessedMap | undefined,
  mapSignalGroups: undefined as SignalStateFeatureCollection | undefined,
  signalStateData: undefined as SignalStateFeatureCollection | undefined,
  spatSignalGroups: undefined as SpatSignalGroups | undefined,
  currentSignalGroups: undefined as SpatSignalGroup[] | undefined,
  currentBsms: {
    type: 'FeatureCollection' as 'FeatureCollection',
    features: [],
  } as BsmFeatureCollection,
  connectingLanes: undefined as ConnectingLanesFeatureCollection | undefined,
  bsmData: {
    type: 'FeatureCollection' as 'FeatureCollection',
    features: [],
  } as BsmFeatureCollection,
  surroundingEvents: [] as MessageMonitor.Event[],
  filteredSurroundingEvents: [] as MessageMonitor.Event[],
  surroundingNotifications: [] as MessageMonitor.Notification[],
  filteredSurroundingNotifications: [] as MessageMonitor.Notification[],
  bsmEventsByMinute: [] as BSM_COUNTS_CHART_DATA[],
  playbackModeActive: false,
  viewState: {
    latitude: 39.587905,
    longitude: -105.0907089,
    zoom: 19,
  } as Partial<ViewState>,
  timeWindowSeconds: 60,
  sliderValue: 0,
  sliderTimeValue: {
    start: new Date(),
    end: new Date(),
  },
  lastSliderUpdate: undefined as number | undefined,
  renderTimeInterval: [0, 0],
  hoveredFeature: undefined as any,
  selectedFeature: undefined as any,
  rawData: {} as RAW_MESSAGE_DATA_EXPORT,
  mapSpatTimes: { mapTime: 0, spatTime: 0 },
  sigGroupLabelsVisible: false,
  laneLabelsVisible: false,
  showPopupOnHover: false,
  importedMessageData: undefined as IMPORTED_MAP_MESSAGE_DATA | undefined,
  cursor: 'default',
  loadInitialDataTimeoutId: undefined as NodeJS.Timeout | undefined,
  wsClient: undefined as MinimalClient | undefined,
  liveDataActive: false,
  currentMapData: [] as ProcessedMap[],
  currentSpatData: [] as ProcessedSpat[],
  currentBsmData: {
    type: 'FeatureCollection',
    features: [],
  } as BsmFeatureCollection,
  bsmTrailLength: 20,
  liveDataRestart: -1,
  liveDataRestartTimeoutId: undefined as NodeJS.Timeout | undefined,
  srmCount: 0,
  srmSsmCount: 0,
  srmMsgList: [],
}

const getNewSliderTimeValue = (startDate: Date, sliderValue: number, timeWindowSeconds: number) => {
  return {
    start: new Date((startDate.getTime() / 1000 + sliderValue - timeWindowSeconds) * 1000),
    end: new Date((startDate.getTime() / 1000 + sliderValue) * 1000),
  }
}

export const generateQueryParams = (source: MAP_PROPS['sourceData'], sourceDataType: MAP_PROPS['sourceDataType']) => {
  const startOffset = 1000 * 60 * 1
  const endOffset = 1000 * 60 * 1

  switch (sourceDataType) {
    case 'notification':
      const notification = source as MessageMonitor.Notification
      return {
        startDate: new Date(notification.notificationGeneratedAt - startOffset),
        endDate: new Date(notification.notificationGeneratedAt + endOffset),
        eventDate: new Date(notification.notificationGeneratedAt),
        vehicleId: undefined,
      }
    case 'event':
      const event = source as MessageMonitor.Event
      return {
        startDate: new Date(event.eventGeneratedAt - startOffset),
        endDate: new Date(event.eventGeneratedAt + endOffset),
        eventDate: new Date(event.eventGeneratedAt),
        vehicleId: undefined,
      }
    case 'assessment':
      const assessment = source as Assessment
      return {
        startDate: new Date(assessment.assessmentGeneratedAt - startOffset),
        endDate: new Date(assessment.assessmentGeneratedAt + endOffset),
        eventDate: new Date(assessment.assessmentGeneratedAt),
        vehicleId: undefined,
      }
    case 'timestamp':
      const ts = (source as timestamp).timestamp
      return {
        startDate: new Date(ts - startOffset),
        endDate: new Date(ts + endOffset),
        eventDate: new Date(ts),
        vehicleId: undefined,
      }
    case 'exact':
      let startDate = undefined as number | undefined
      let endDate = undefined as number | undefined

      for (const spat of (source as { spat: ProcessedSpat[] }).spat) {
        if (!startDate || getTimestamp(spat.utcTimeStamp) < startDate) {
          startDate = getTimestamp(spat.utcTimeStamp)
        }
        if (!endDate || getTimestamp(spat.utcTimeStamp) > endDate) {
          endDate = getTimestamp(spat.utcTimeStamp)
        }
      }
      return {
        startDate: new Date(startDate ?? Date.now()),
        endDate: new Date(endDate ?? Date.now() + 1),
        eventDate: new Date((startDate ?? Date.now()) / 2 + (endDate ?? Date.now() + 1) / 2),
        vehicleId: undefined,
      }
    default:
      return {
        startDate: new Date(Date.now() - startOffset),
        endDate: new Date(Date.now() + endOffset),
        eventDate: new Date(Date.now()),
        vehicleId: undefined,
      }
  }
}

export const pullInitialData = createAsyncThunk(
  'intersectionMap/pullInitialData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const importedMessageData = selectImportedMessageData(currentState)
    const queryParams = selectQueryParams(currentState)
    const sourceData = selectSourceData(currentState)
    const sourceDataType = selectSourceDataType(currentState)

    console.debug('Pulling Initial Data')
    let rawMap: ProcessedMap[] = []
    let rawSpat: ProcessedSpat[] = []
    let rawBsm: OdeBsmData[] = []
    if (sourceDataType == 'exact') {
      rawMap = (sourceData as { map: ProcessedMap[] }).map.map((map) => ({
        ...map,
        properties: {
          ...map.properties,
          odeReceivedAt: getTimestamp(map.properties.odeReceivedAt),
        },
      }))
      rawSpat = (sourceData as { spat: ProcessedSpat[] }).spat.map((spat) => ({
        ...spat,
        utcTimeStamp: getTimestamp(spat.utcTimeStamp),
      }))
      rawBsm = (sourceData as { bsm: OdeBsmData[] }).bsm.map((bsm) => ({
        ...bsm,
        metadata: {
          ...bsm.metadata,
          odeReceivedAt: getTimestamp(bsm.metadata.odeReceivedAt),
        },
      }))
    } else if (queryParams.default == true) {
      const latestSpats = await MessageMonitorApi.getSpatMessages({
        token: authToken,
        intersectionId: queryParams.intersectionId,
        roadRegulatorId: queryParams.roadRegulatorId,
        latest: true,
      })
      if (latestSpats && latestSpats.length > 0) {
        _updateQueryParams({
          state: currentState.intersectionMap,
          ...generateQueryParams({ timestamp: getTimestamp(latestSpats.at(-1)?.utcTimeStamp) }, 'timestamp'),
          intersectionId: queryParams.intersectionId,
          roadRegulatorId: queryParams.roadRegulatorId,
        })
      }
    } else if (importedMessageData == undefined) {
      // ######################### Retrieve MAP Data #########################
      const rawMapPromise = MessageMonitorApi.getMapMessages({
        token: authToken,
        intersectionId: queryParams.intersectionId!,
        roadRegulatorId: queryParams.roadRegulatorId!,
        //startTime: new Date(queryParams.startDate.getTime() - 1000 * 60 * 60 * 1),
        endTime: queryParams.endDate,
        latest: true,
      })
      toast.promise(rawMapPromise, {
        loading: `Loading MAP Data`,
        success: `Successfully got MAP Data`,
        error: `Failed to get MAP data. Please see console`,
      })
      rawMap = await rawMapPromise

      // ######################### Retrieve SPAT Data #########################
      const rawSpatPromise = MessageMonitorApi.getSpatMessages({
        token: authToken,
        intersectionId: queryParams.intersectionId!,
        roadRegulatorId: queryParams.roadRegulatorId!,
        startTime: queryParams.startDate,
        endTime: queryParams.endDate,
      })
      toast.promise(rawSpatPromise, {
        loading: `Loading SPAT Data`,
        success: `Successfully got SPAT Data`,
        error: `Failed to get SPAT data. Please see console`,
      })
      rawSpat = (await rawSpatPromise)
        .sort((a, b) => a.utcTimeStamp - b.utcTimeStamp)
        .map((spat) => ({
          ...spat,
          utcTimeStamp: getTimestamp(spat.utcTimeStamp),
        }))

      dispatch(getBsmDailyCounts())
      dispatch(getSurroundingEvents())
      dispatch(getSurroundingNotifications())
    } else {
      rawMap = importedMessageData.mapData
      rawSpat = [...importedMessageData.spatData].sort((a, b) => a.utcTimeStamp - b.utcTimeStamp)
      rawBsm = importedMessageData.bsmData
    }
    if (!rawMap || rawMap.length == 0) {
      console.info('NO MAP MESSAGES WITHIN TIME')
      return
    }

    const latestMapMessage: ProcessedMap = rawMap.at(-1)!
    if (latestMapMessage != null) {
      dispatch(
        setViewState({
          latitude: latestMapMessage?.properties.refPoint.latitude,
          longitude: latestMapMessage?.properties.refPoint.longitude,
          zoom: 19,
        })
      )
    }
    const mapCoordinates: OdePosition3D = latestMapMessage?.properties.refPoint

    // ######################### SPAT Signal Groups #########################
    const mapSignalGroupsLocal = parseMapSignalGroups(latestMapMessage)
    const spatSignalGroupsLocal = parseSpatSignalGroups(rawSpat)

    // ######################### BSMs #########################
    if (!importedMessageData && sourceDataType != 'exact') {
      const rawBsmPromise = MessageMonitorApi.getBsmMessages({
        token: authToken,
        vehicleId: queryParams.vehicleId,
        startTime: queryParams.startDate,
        endTime: queryParams.endDate,
        long: mapCoordinates.longitude,
        lat: mapCoordinates.latitude,
        distance: 500,
      })
      toast.promise(rawBsmPromise, {
        loading: `Loading BSM Data`,
        success: `Successfully got BSM Data`,
        error: `Failed to get BSM data. Please see console`,
      })
      rawBsm = await rawBsmPromise
    }
    let bsmGeojson = parseBsmToGeojson(rawBsm)
    bsmGeojson = {
      ...bsmGeojson,
      features: [...[...bsmGeojson.features].sort((a, b) => b.properties.odeReceivedAt - a.properties.odeReceivedAt)],
    }
    dispatch(renderEntireMap({ currentMapData: rawMap, currentSpatData: rawSpat, currentBsmData: bsmGeojson }))
    return {
      mapData: latestMapMessage,
      connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
      spatSignalGroups: spatSignalGroupsLocal,
      mapSignalGroups: mapSignalGroupsLocal,
      mapTime: latestMapMessage.properties.odeReceivedAt as unknown as number,
    }
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined &&
      (selectSourceData(getState() as RootState) != undefined || selectLoadOnNull(getState() as RootState) == true),
  }
)

export const renderEntireMap = createAsyncThunk(
  'intersectionMap/renderEntireMap',
  async (
    args: { currentMapData: ProcessedMap[]; currentSpatData: ProcessedSpat[]; currentBsmData: BsmFeatureCollection },
    { getState, dispatch }
  ) => {
    const { currentMapData, currentSpatData, currentBsmData } = args
    const currentState = getState() as RootState

    const queryParams = selectQueryParams(currentState)
    const sourceData = selectSourceData(currentState)
    const sourceDataType = selectSourceDataType(currentState)

    // ######################### MAP Data #########################
    const latestMapMessage: ProcessedMap = currentMapData.at(-1)
    if (latestMapMessage != null) {
      setViewState({
        latitude: latestMapMessage?.properties.refPoint.latitude,
        longitude: latestMapMessage?.properties.refPoint.longitude,
        zoom: 19,
      })
    }

    // ######################### SPAT Signal Groups #########################
    const mapSignalGroupsLocal = parseMapSignalGroups(latestMapMessage)

    const spatSignalGroupsLocal = parseSpatSignalGroups(currentSpatData)

    const uniqueIds = new Set(currentBsmData.features.map((bsm) => bsm.properties?.id))
    // generate equally spaced unique colors for each uniqueId
    const colors = generateColorDictionary(uniqueIds)
    dispatch(setBsmLegendColors(colors))
    // add color to each feature
    const bsmLayerStyle = generateMapboxStyleExpression(colors)
    dispatch(setBsmCircleColor(bsmLayerStyle))

    // ######################### Message Data #########################
    const rawData = {}
    rawData['map'] = currentMapData
    rawData['spat'] = currentSpatData
    rawData['bsm'] = currentBsmData
    if (sourceDataType == 'notification') {
      rawData['notification'] = sourceData as MessageMonitor.Notification
    } else if (sourceDataType == 'event') {
      rawData['event'] = sourceData as MessageMonitor.Event
    } else if (sourceDataType == 'assessment') {
      rawData['assessment'] = sourceData as Assessment
    }
    return {
      connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
      mapData: latestMapMessage,
      mapTime: latestMapMessage.properties.odeReceivedAt as unknown as number,
      mapSignalGroups: mapSignalGroupsLocal,
      spatSignalGroups: spatSignalGroupsLocal,
      bsmData: currentBsmData,
      rawData: rawData,
      sliderValue: Math.min(
        getTimeRange(queryParams.startDate, queryParams.eventDate ?? new Date()),
        getTimeRange(queryParams.startDate, queryParams.endDate)
      ),
    }
  },
  {
    condition: (
      args: { currentMapData: ProcessedMap[]; currentSpatData: ProcessedSpat[]; currentBsmData: BsmFeatureCollection },
      { getState }
    ) => args.currentMapData.length != 0,
  }
)

export const renderIterative_Map = createAsyncThunk(
  'intersectionMap/renderIterative_Map',
  async (newMapData: ProcessedMap[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const queryParams = selectQueryParams(currentState)
    const currentMapData: ProcessedMap[] = selectCurrentMapData(currentState)

    const start = Date.now()
    const OLDEST_DATA_TO_KEEP = queryParams.eventDate.getTime() - queryParams.startDate.getTime() // milliseconds

    const currTimestamp = getTimestamp(newMapData.at(-1)!.properties.odeReceivedAt) / 1000
    let oldIndex = 0
    for (let i = 0; i < currentMapData.length; i++) {
      if ((currentMapData[i].properties.odeReceivedAt as unknown as number) < currTimestamp - OLDEST_DATA_TO_KEEP) {
        oldIndex = i
      } else {
        break
      }
    }
    const currentMapDataLocal = currentMapData.slice(oldIndex, currentMapData.length).concat(newMapData)

    // ######################### MAP Data #########################
    const latestMapMessage: ProcessedMap = currentMapDataLocal.at(-1)!
    if (latestMapMessage != null) {
      setViewState({
        latitude: latestMapMessage?.properties.refPoint.latitude,
        longitude: latestMapMessage?.properties.refPoint.longitude,
        zoom: 19,
      })
    }

    // ######################### SPAT Signal Groups #########################
    const mapSignalGroupsLocal = parseMapSignalGroups(latestMapMessage)

    console.debug('MAP RENDER TIME:', Date.now() - start, 'ms')
    const previousMapMessage: ProcessedMap | undefined = currentMapData.at(-1)
    if (
      latestMapMessage != null &&
      (latestMapMessage.properties.refPoint.latitude != previousMapMessage?.properties.refPoint.latitude ||
        latestMapMessage.properties.refPoint.longitude != previousMapMessage?.properties.refPoint.longitude)
    ) {
      setViewState({
        latitude: latestMapMessage?.properties.refPoint.latitude,
        longitude: latestMapMessage?.properties.refPoint.longitude,
        zoom: 19,
      })
    }
    dispatch(setRawData({ map: currentMapDataLocal }))
    return {
      currentMapData: currentMapDataLocal,
      connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
      mapData: latestMapMessage,
      mapTime: currTimestamp,
      mapSignalGroups: mapSignalGroupsLocal,
    }
  },
  {
    condition: (newMapData: ProcessedMap[], { getState }) => newMapData.length != 0,
  }
)

export const renderIterative_Spat = createAsyncThunk(
  'intersectionMap/renderIterative_Spat',
  async (newSpatData: ProcessedSpat[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const queryParams = selectQueryParams(currentState)
    const currentSpatSignalGroups: SpatSignalGroups = selectSpatSignalGroups(currentState) ?? {}
    const currentProcessedSpatData: ProcessedSpat[] = selectCurrentSpatData(currentState) ?? []

    const start = Date.now()
    const OLDEST_DATA_TO_KEEP = queryParams.eventDate.getTime() - queryParams.startDate.getTime() // milliseconds
    if (newSpatData.length == 0) {
      console.warn('Did not attempt to render map (iterative SPAT), no new SPAT messages available:', newSpatData)
      return { signalGroups: currentSpatSignalGroups, raw: currentProcessedSpatData }
    }
    // Inject and filter spat data
    // 2024-01-09T00:24:28.354Z
    newSpatData = newSpatData.map((spat) => ({
      ...spat,
      utcTimeStamp: getTimestamp(spat.utcTimeStamp),
    }))
    const currTimestamp = getTimestamp(newSpatData.at(-1)!.utcTimeStamp)

    let oldIndex = 0
    const currentSpatSignalGroupsArr = Object.keys(currentSpatSignalGroups).map((key) => ({
      key,
      sigGroup: currentSpatSignalGroups[key],
    }))
    for (let i = 0; i < currentSpatSignalGroupsArr.length; i++) {
      if (Number(currentSpatSignalGroupsArr[i].key) < currTimestamp - OLDEST_DATA_TO_KEEP) {
        oldIndex = i
      } else {
        break
      }
    }
    const newSpatSignalGroups = parseSpatSignalGroups(newSpatData)
    const newSpatSignalGroupsArr = Object.keys(newSpatSignalGroups).map((key) => ({
      key,
      sigGroup: newSpatSignalGroups[key],
    }))
    const filteredSpatSignalGroupsArr = currentSpatSignalGroupsArr
      .slice(oldIndex, currentSpatSignalGroupsArr.length)
      .concat(newSpatSignalGroupsArr)
    const currentSpatSignalGroupsLocal = filteredSpatSignalGroupsArr.reduce((acc, curr) => {
      acc[curr.key] = curr.sigGroup
      return acc
    }, {} as SpatSignalGroups)

    // Update current processed spat data
    oldIndex = 0
    for (let i = 0; i < currentProcessedSpatData.length; i++) {
      if (currentProcessedSpatData[i].utcTimeStamp < currTimestamp - OLDEST_DATA_TO_KEEP) {
        oldIndex = i
      } else {
        break
      }
    }
    const currentProcessedSpatDataLocal = currentProcessedSpatData
      .slice(oldIndex, currentProcessedSpatData.length)
      .concat(newSpatData)
    console.debug('SPAT RENDER TIME:', Date.now() - start, 'ms')
    return { signalGroups: currentSpatSignalGroupsLocal, raw: currentProcessedSpatDataLocal }
  },
  {
    condition: (newSpatData: ProcessedSpat[], { getState }) => newSpatData.length != 0,
  }
)

export const renderIterative_Bsm = createAsyncThunk(
  'intersectionMap/renderIterative_Bsm',
  async (newBsmData: OdeBsmData[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const queryParams = selectQueryParams(currentState)
    const currentBsmData: BsmFeatureCollection = selectCurrentBsmData(currentState)

    const OLDEST_DATA_TO_KEEP = queryParams.eventDate.getTime() - queryParams.startDate.getTime() // milliseconds
    // Inject and filter spat data
    const currTimestamp = new Date(newBsmData.at(-1)!.metadata.odeReceivedAt as string).getTime() / 1000
    let oldIndex = 0
    for (let i = 0; i < currentBsmData.features.length; i++) {
      if (Number(currentBsmData.features[i].properties.odeReceivedAt) < currTimestamp - OLDEST_DATA_TO_KEEP) {
        oldIndex = i
      } else {
        break
      }
    }
    const newBsmGeojson = parseBsmToGeojson(newBsmData)
    const currentBsmGeojson = {
      ...currentBsmData,
      features: currentBsmData.features.slice(oldIndex, currentBsmData.features.length).concat(newBsmGeojson.features),
    }

    const uniqueIds = new Set(currentBsmGeojson.features.map((bsm) => bsm.properties?.id))
    // generate equally spaced unique colors for each uniqueId
    const colors = generateColorDictionary(uniqueIds)
    dispatch(setBsmLegendColors(colors))
    // add color to each feature
    const bsmLayerStyle = generateMapboxStyleExpression(colors)
    dispatch(setBsmCircleColor(bsmLayerStyle))
    dispatch(setRawData({ bsm: currentBsmGeojson }))
    return currentBsmGeojson
  },
  {
    condition: (newBsmData: OdeBsmData[], { getState }) => newBsmData.length != 0,
  }
)

export const getBsmDailyCounts = createAsyncThunk(
  'intersectionMap/getBsmDailyCounts',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)

    const dayStart = new Date(queryParams.startDate)
    dayStart.setHours(0, 0, 0, 0)
    const dayEnd = new Date(queryParams.startDate)
    dayEnd.setHours(23, 59, 59, 0)

    const bsmEventsByMinutePromise = EventsApi.getBsmByMinuteEvents(
      authToken,
      queryParams.intersectionId!,
      dayStart,
      dayEnd,
      { test: false }
    )
    toast.promise(bsmEventsByMinutePromise, {
      loading: `Loading BSM Event Counts`,
      success: `Successfully got BSM event counts`,
      error: `Failed to get BSM event counts. Please see console`,
    })
    return bsmEventsByMinutePromise
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined,
  }
)

export const getSurroundingEvents = createAsyncThunk(
  'intersectionMap/getSurroundingEvents',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)

    const surroundingEventsPromise = EventsApi.getAllEvents(
      authToken,
      queryParams.intersectionId!,
      queryParams.roadRegulatorId!,
      queryParams.startDate,
      queryParams.endDate
    )
    toast.promise(surroundingEventsPromise, {
      loading: `Loading Event Data`,
      success: `Successfully got Event Data`,
      error: `Failed to get Event data. Please see console`,
    })
    return surroundingEventsPromise
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined,
  }
)

export const getSurroundingNotifications = createAsyncThunk(
  'intersectionMap/getSurroundingNotifications',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)

    const surroundingNotificationsPromise = NotificationApi.getAllNotifications({
      token: authToken,
      intersectionId: queryParams.intersectionId!,
      roadRegulatorId: queryParams.roadRegulatorId!,
      startTime: queryParams.startDate,
      endTime: queryParams.endDate,
    })
    toast.promise(surroundingNotificationsPromise, {
      loading: `Loading Notification Data`,
      success: `Successfully got Notification Data`,
      error: `Failed to get Notification data. Please see console`,
    })
    return surroundingNotificationsPromise
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined,
  }
)

export const initializeLiveStreaming = createAsyncThunk(
  'intersectionMap/initializeLiveStreaming',
  async (
    args: { token: string; roadRegulatorId: number; intersectionId: number; numRestarts?: number },
    { getState, dispatch }
  ) => {
    const { token, roadRegulatorId, intersectionId, numRestarts = 0 } = args
    // Connect to WebSocket when component mounts
    const liveDataActive = selectLiveDataActive(getState() as RootState)
    const wsClient = selectWsClient(getState() as RootState)

    dispatch(onTimeQueryChanged({ eventTime: new Date(), timeBefore: 10, timeAfter: 0, timeWindowSeconds: 2 }))
    dispatch(resetMapView())

    if (!liveDataActive) {
      console.debug('Not initializing live streaming because liveDataActive is false')
      return
    }

    let protocols = ['v10.stomp', 'v11.stomp']
    protocols.push(token)
    const url = `${EnvironmentVars.CVIZ_API_WS_URL}/stomp`
    console.debug('Connecting to STOMP endpoint: ' + url + ' with token: ' + token)

    // Stomp Client Documentation: https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html
    let client = Stomp.client(url, protocols)
    client.debug = (e) => {
      console.debug('STOMP Debug: ' + e)
    }

    // Topics are in the format /live/{roadRegulatorID}/{intersectionID}/{spat,map,bsm}
    let spatTopic = `/live/${roadRegulatorId}/${intersectionId}/spat`
    let mapTopic = `/live/${roadRegulatorId}/${intersectionId}/map`
    let bsmTopic = `/live/${roadRegulatorId}/${intersectionId}/bsm` // TODO: Filter by road regulator ID
    let spatTime = Date.now()
    let mapTime = Date.now()
    let bsmTime = Date.now()
    let connectionStartTime = Date.now()
    client.connect(
      {
        // "username": "test",
        // "password": "test",
        // Token: token,
      },
      () => {
        client.subscribe(spatTopic, function (mes: IMessage) {
          const spatMessage: ProcessedSpat = JSON.parse(mes.body)
          console.debug('Received SPaT message ' + (Date.now() - spatTime) + ' ms')
          spatTime = Date.now()
          dispatch(renderIterative_Spat([spatMessage]))
          dispatch(maybeUpdateSliderValue())
        })

        client.subscribe(mapTopic, function (mes: IMessage) {
          const mapMessage: ProcessedMap = JSON.parse(mes.body)
          console.debug('Received MAP message ' + (Date.now() - mapTime) + ' ms')
          mapTime = Date.now()
          dispatch(renderIterative_Map([mapMessage]))
          dispatch(maybeUpdateSliderValue())
        })

        client.subscribe(bsmTopic, function (mes: IMessage) {
          const bsmData: OdeBsmData = JSON.parse(mes.body)
          console.debug('Received BSM message ' + (Date.now() - bsmTime) + ' ms')
          bsmTime = Date.now()
          dispatch(renderIterative_Bsm([bsmData]))
          dispatch(maybeUpdateSliderValue())
        })
      },
      (error) => {
        console.error('Live Streaming ERROR connecting to live data Websocket: ' + error)
      }
    )

    client.onDisconnect = (frame) => {
      console.debug(
        'Live Streaming Disconnected from STOMP endpoint: ' +
          frame +
          ' (numRestarts: ' +
          numRestarts +
          ', wsClient: ' +
          wsClient +
          ')'
      )
      if (numRestarts < 5 && liveDataActive) {
        let numRestartsLocal = numRestarts
        if (Date.now() - connectionStartTime > 10000) {
          numRestartsLocal = 0
        }
        console.debug('Attempting to reconnect to STOMP endpoint (numRestarts: ' + numRestartsLocal + ')')
        dispatch(
          setLiveDataRestartTimeoutId(
            setTimeout(() => {
              dispatch(setLiveDataRestart(numRestartsLocal + 1))
            }, numRestartsLocal * 2000)
          )
        )
      } else {
        cleanUpLiveStreaming()
      }
    }

    client.onStompError = (frame) => {
      console.error('Live Streaming STOMP ERROR', frame)
    }

    client.onWebSocketClose = (frame) => {
      console.error(
        'Live Streaming STOMP WebSocket Close: ' +
          frame +
          ' (numRestarts: ' +
          numRestarts +
          ', wsClient: ' +
          wsClient +
          ')'
      )
      if (numRestarts < 5 && liveDataActive) {
        let numRestartsLocal = numRestarts
        if (Date.now() - connectionStartTime > 10000) {
          numRestartsLocal = 0
        }
        console.debug('Attempting to reconnect to STOMP endpoint (numRestarts: ' + numRestartsLocal + ')')
        dispatch(
          setLiveDataRestartTimeoutId(
            setTimeout(() => {
              dispatch(setLiveDataRestart(numRestartsLocal + 1))
            }, numRestartsLocal * 2000)
          )
        )
      } else {
        dispatch(cleanUpLiveStreaming())
      }
    }

    client.onWebSocketError = (frame) => {
      // TODO: Consider restarting connection on error
      console.error('Live Streaming STOMP WebSocket Error', frame)
    }

    return client
  }
)

export const updateRenderedMapState = createAsyncThunk(
  'intersectionMap/updateRenderedMapState',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)
    const spatSignalGroups = selectSpatSignalGroups(currentState)
    const mapSignalGroups = selectMapSignalGroups(currentState)
    const renderTimeInterval = selectRenderTimeInterval(currentState)
    const bsmData = selectBsmData(currentState)
    const bsmTrailLength = selectBsmTrailLength(currentState)
    const surroundingEvents = selectSurroundingEvents(currentState)
    const surroundingNotifications = selectSurroundingNotifications(currentState)

    // ASSUMPTION: mapSignalGroups && spatSignalGroups

    let currentSignalGroups: SpatSignalGroup[] | undefined
    let signalStateData: SignalStateFeatureCollection | undefined
    let spatTime: number | undefined

    // retrieve filtered SPATs
    let closestSignalGroup: { spat: SpatSignalGroup[]; datetime: number } | null = null
    for (const datetime in spatSignalGroups) {
      const datetimeNum = Number(datetime) / 1000 // milliseconds to seconds
      if (datetimeNum >= renderTimeInterval[0] && datetimeNum <= renderTimeInterval[1]) {
        if (
          closestSignalGroup === null ||
          Math.abs(datetimeNum - renderTimeInterval[1]) < Math.abs(closestSignalGroup.datetime - renderTimeInterval[1])
        ) {
          closestSignalGroup = { datetime: datetimeNum, spat: spatSignalGroups[datetime] }
        }
      }
    }
    if (closestSignalGroup !== null) {
      currentSignalGroups = closestSignalGroup.spat
      signalStateData = generateSignalStateFeatureCollection(mapSignalGroups!, closestSignalGroup.spat)
      spatTime = closestSignalGroup.datetime
    }

    // retrieve filtered BSMs
    const filteredBsms: BsmFeature[] = bsmData?.features?.filter(
      (feature) =>
        feature.properties?.odeReceivedAt >= renderTimeInterval[0] &&
        feature.properties?.odeReceivedAt <= renderTimeInterval[1]
    )
    const sortedBsms = filteredBsms.sort((a, b) => b.properties.odeReceivedAt - a.properties.odeReceivedAt)

    // Update BSM legend colors
    const uniqueIds = new Set(filteredBsms.map((bsm) => bsm.properties?.id))
    const colors = generateColorDictionary(uniqueIds)

    dispatch(setBsmLegendColors(colors))
    const bsmLayerStyle = generateMapboxStyleExpression(colors)
    dispatch(setBsmCircleColor(bsmLayerStyle))

    const lastBsms: BsmFeature[] = []
    const bsmCounts: { [id: string]: number } = {}
    for (let i = 0; i < sortedBsms.length; i++) {
      const id = sortedBsms[i].properties?.id
      if (bsmCounts[id] == undefined) {
        bsmCounts[id] = 0
      }
      if (bsmCounts[id] < bsmTrailLength) {
        lastBsms.push(sortedBsms[i])
        bsmCounts[id]++
      }
    }

    const filteredEvents: MessageMonitor.Event[] = surroundingEvents.filter(
      (event) =>
        event.eventGeneratedAt / 1000 >= renderTimeInterval[0] && event.eventGeneratedAt / 1000 <= renderTimeInterval[1]
    )

    const filteredNotifications: MessageMonitor.Notification[] = surroundingNotifications.filter(
      (notification) =>
        notification.notificationGeneratedAt / 1000 >= renderTimeInterval[0] &&
        notification.notificationGeneratedAt / 1000 <= renderTimeInterval[1]
    )

    return {
      currentSignalGroups: closestSignalGroup?.spat,
      signalStateData: closestSignalGroup
        ? generateSignalStateFeatureCollection(mapSignalGroups!, closestSignalGroup?.spat)
        : undefined,
      spatTime: closestSignalGroup?.datetime,
      currentBsms: { type: 'FeatureCollection' as 'FeatureCollection', features: lastBsms },
      filteredSurroundingEvents: filteredEvents,
      filteredSurroundingNotifications: filteredNotifications,
    }
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined,
  }
)

const compareQueryParams = (oldParams: MAP_QUERY_PARAMS, newParams: MAP_QUERY_PARAMS) => {
  return (
    oldParams.startDate.getTime() != newParams.startDate.getTime() ||
    oldParams.endDate.getTime() != newParams.endDate.getTime() ||
    oldParams.eventDate.getTime() != newParams.eventDate.getTime() ||
    oldParams.vehicleId != newParams.vehicleId ||
    oldParams.intersectionId != newParams.intersectionId ||
    oldParams.roadRegulatorId != newParams.roadRegulatorId
  )
}

const generateRenderTimeInterval = (startDate: Date, sliderValue: number, timeWindowSeconds: number) => {
  const startTime = startDate.getTime() / 1000

  const filteredStartTime = startTime + sliderValue / 10 - timeWindowSeconds
  const filteredEndTime = startTime + sliderValue / 10

  return [filteredStartTime, filteredEndTime]
}

const _updateQueryParams = ({
  state,
  startDate,
  endDate,
  eventDate,
  vehicleId,
  intersectionId,
  roadRegulatorId,
  resetTimeWindow,
  updateSlider,
}: {
  state: RootState['intersectionMap']
  startDate?: Date
  endDate?: Date
  eventDate?: Date
  vehicleId?: string
  intersectionId?: number
  roadRegulatorId?: number
  resetTimeWindow?: boolean
  updateSlider?: boolean
}) => {
  const newQueryParams = {
    startDate: startDate ?? state.value.queryParams.startDate,
    endDate: endDate ?? state.value.queryParams.endDate,
    eventDate: eventDate ?? state.value.queryParams.eventDate,
    vehicleId: vehicleId ?? state.value.queryParams.vehicleId,
    intersectionId: intersectionId ?? state.value.queryParams.intersectionId,
    roadRegulatorId: roadRegulatorId ?? state.value.queryParams.roadRegulatorId,
  }
  if (compareQueryParams(state.value.queryParams, newQueryParams)) {
    state.value.queryParams = newQueryParams
    state.value.sliderTimeValue = getNewSliderTimeValue(
      state.value.queryParams.startDate,
      state.value.sliderValue,
      state.value.timeWindowSeconds
    )
    if (resetTimeWindow) state.value.timeWindowSeconds = 60
    if (updateSlider) state.value.sliderValue = getTimeRange(newQueryParams.startDate, newQueryParams.endDate)
  }
}

export const downloadMapData = createAsyncThunk(
  'intersectionMap/downloadMapData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const rawData = selectRawData(currentState)!
    const queryParams = selectQueryParams(currentState)

    return downloadAllData(rawData, queryParams)
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined,
  }
)

export const renderRsuData = createAsyncThunk(
  'intersectionMap/renderRsuData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const rsuMapData = selectRsuMapData(currentState)
    console.log('rsuMapData', rsuMapData)

    dispatch(resetMapView())

    dispatch(
      renderEntireMap({
        currentMapData: [rsuMapData as unknown as ProcessedMap],
        currentSpatData: [],
        currentBsmData: { type: 'FeatureCollection', features: [] },
      })
    )

    return
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
      selectQueryParams(getState() as RootState).roadRegulatorId != undefined,
  }
)

export const intersectionMapSlice = createSlice({
  name: 'intersectionMap',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateSsmSrmCounts: (state, action: PayloadAction<{ srmSsmList: SsmSrmData; rsuIpv4: string }>) => {
      let localSrmCount = 0
      let localSsmCount = 0
      let localMsgList = []
      // console.error('srmSsmList', state.value.srmSsmList)
      for (const elem of action.payload.srmSsmList) {
        if (elem.ip === action.payload.rsuIpv4) {
          localMsgList.push(elem)
          if (elem.type === 'srmTx') {
            localSrmCount += 1
          } else {
            localSsmCount += 1
          }
        }
      }
      state.value.srmCount = localSrmCount
      state.value.srmSsmCount = localSsmCount
      state.value.srmMsgList = localMsgList
    },
    setSurroundingEvents: (state, action: PayloadAction<MessageMonitor.Event[]>) => {
      state.value.surroundingEvents = action.payload
    },
    maybeUpdateSliderValue: (state) => {
      if (
        state.value.liveDataActive &&
        (!state.value.lastSliderUpdate || Date.now() - state.value.lastSliderUpdate > 1 * 1000)
      ) {
        _updateQueryParams({
          state,
          startDate: new Date(
            Date.now() - (state.value.queryParams.endDate.getTime() - state.value.queryParams.startDate.getTime())
          ),
          endDate: new Date(Date.now()),
          eventDate: new Date(Date.now()),
          vehicleId: undefined,
          intersectionId: state.value.queryParams.intersectionId,
          roadRegulatorId: state.value.queryParams.roadRegulatorId,
          updateSlider: true,
        })
      }
    },
    updateLiveSliderValue: (state) => {
      const newQueryParams = {
        startDate: new Date(
          Date.now() - (state.value.queryParams.endDate.getTime() - state.value.queryParams.startDate.getTime())
        ),
        endDate: new Date(Date.now()),
        eventDate: new Date(Date.now()),
        vehicleId: undefined,
        intersectionId: state.value.queryParams.intersectionId,
        roadRegulatorId: state.value.queryParams.roadRegulatorId,
      }
      state.value.queryParams = newQueryParams
      state.value.sliderValue = getTimeRange(newQueryParams.startDate, newQueryParams.endDate)
      state.value.renderTimeInterval = [
        newQueryParams.startDate.getTime() / 1000,
        newQueryParams.endDate.getTime() / 1000,
      ]
      state.value.sliderTimeValue = getNewSliderTimeValue(
        state.value.queryParams.startDate,
        state.value.sliderValue,
        state.value.timeWindowSeconds
      )
    },
    setViewState: (state, action: PayloadAction<Partial<ViewState>>) => {
      state.value.viewState = action.payload
    },
    handleImportedMapMessageData: (
      state,
      action: PayloadAction<{
        mapData: ProcessedMap[]
        bsmData: OdeBsmData[]
        spatData: ProcessedSpat[]
        notificationData: any
      }>
    ) => {
      const { mapData, bsmData, spatData, notificationData } = action.payload
      const sortedSpatData = spatData.sort((x, y) => x.utcTimeStamp - y.utcTimeStamp)
      const startTime = new Date(sortedSpatData[0].utcTimeStamp)
      const endTime = new Date(sortedSpatData[sortedSpatData.length - 1].utcTimeStamp)
      state.value.importedMessageData = { mapData, bsmData, spatData, notificationData }
      state.value.queryParams = {
        startDate: startTime,
        endDate: endTime,
        eventDate: startTime,
        intersectionId: mapData[0].properties.intersectionId,
        roadRegulatorId: -1,
      }
      state.value.sliderTimeValue = getNewSliderTimeValue(
        state.value.queryParams.startDate,
        state.value.sliderValue,
        state.value.timeWindowSeconds
      )
      state.value.timeWindowSeconds = 60
    },
    updateQueryParams: (
      state,
      action: PayloadAction<{
        startDate?: Date
        endDate?: Date
        eventDate?: Date
        vehicleId?: string
        intersectionId?: number
        roadRegulatorId?: number
        resetTimeWindow?: boolean
        updateSlider?: boolean
      }>
    ) => {
      _updateQueryParams({ state, ...action.payload })
    },
    onTimeQueryChanged: (
      state,
      action: PayloadAction<{
        eventTime?: Date
        timeBefore?: number
        timeAfter?: number
        timeWindowSeconds?: number
      }>
    ) => {
      let { eventTime, timeBefore, timeAfter, timeWindowSeconds } = action.payload
      eventTime ??= new Date()
      const updatedQueryParams = {
        startDate: new Date(eventTime.getTime() - (timeBefore ?? 0) * 1000),
        endDate: new Date(eventTime.getTime() + (timeAfter ?? 0) * 1000),
        eventDate: eventTime,
        intersectionId: state.value.queryParams.intersectionId,
        roadRegulatorId: state.value.queryParams.roadRegulatorId,
      }
      if (compareQueryParams(state.value.queryParams, updatedQueryParams)) {
        // Detected change in query params
        state.value.queryParams = updatedQueryParams
        state.value.sliderTimeValue = getNewSliderTimeValue(
          state.value.queryParams.startDate,
          state.value.sliderValue,
          state.value.timeWindowSeconds
        )
      } else {
        // No change in query params
      }
      state.value.timeWindowSeconds = timeWindowSeconds ?? state.value.timeWindowSeconds
    },
    setSliderValue: (state, action: PayloadAction<number | number[]>) => {
      state.value.sliderValue = action.payload as number
      state.value.liveDataActive = false
    },
    incrementSliderValue: (state, action: PayloadAction<number | undefined>) => {
      const maxSliderValue = getTimeRange(state.value.queryParams.startDate, state.value.queryParams.endDate)
      if (state.value.sliderValue == maxSliderValue) {
        state.value.playbackModeActive = false
      } else {
        state.value.sliderValue += action.payload ?? 1
      }
    },
    updateRenderTimeInterval: (state) => {
      state.value.renderTimeInterval = generateRenderTimeInterval(
        state.value.queryParams.startDate,
        state.value.sliderValue,
        state.value.timeWindowSeconds
      )
    },
    onMapClick: (
      state,
      action: PayloadAction<{
        event: { point: mapboxgl.Point; lngLat: mapboxgl.LngLat }
        mapRef: React.MutableRefObject<any>
      }>
    ) => {
      const features = action.payload.mapRef.current.queryRenderedFeatures(action.payload.event.point, {
        // layers: state.value.allInteractiveLayerIds,
      })
      const feature = features?.[0]
      if (feature && state.value.allInteractiveLayerIds.includes(feature.layer.id)) {
        state.value.selectedFeature = { clickedLocation: action.payload.event.lngLat, feature }
      } else {
        state.value.selectedFeature = undefined
      }
    },
    onMapMouseMove: (
      state,
      action: PayloadAction<{ features: mapboxgl.MapboxGeoJSONFeature[] | undefined; lngLat: mapboxgl.LngLat }>
    ) => {
      const feature = action.payload.features?.[0]
      if (feature && state.value.allInteractiveLayerIds.includes(feature.layer.id as MAP_LAYERS)) {
        state.value.hoveredFeature = { clickedLocation: action.payload.lngLat, feature }
      }
    },
    onMapMouseEnter: (
      state,
      action: PayloadAction<{ features: mapboxgl.MapboxGeoJSONFeature[] | undefined; lngLat: mapboxgl.LngLat }>
    ) => {
      state.value.cursor = 'pointer'
      const feature = action.payload.features?.[0]
      if (feature && state.value.allInteractiveLayerIds.includes(feature.layer.id as MAP_LAYERS)) {
        state.value.hoveredFeature = { clickedLocation: action.payload.lngLat, feature }
      } else {
        state.value.hoveredFeature = undefined
      }
    },
    onMapMouseLeave: (state) => {
      state.value.cursor = ''
      state.value.hoveredFeature = undefined
    },
    cleanUpLiveStreaming: (state) => {
      if (state.value.wsClient) {
        state.value.wsClient.disconnect(() => {
          console.debug('Disconnected from STOMP endpoint')
        })
        state.value.timeWindowSeconds = 60
      }
      if (state.value.liveDataRestartTimeoutId) {
        clearTimeout(state.value.liveDataRestartTimeoutId)
        state.value.liveDataRestartTimeoutId = undefined
      }
      state.value.liveDataActive = false
      state.value.liveDataRestart = -1
      state.value.wsClient = undefined
    },
    setLoadInitialdataTimeoutId: (state, action: PayloadAction<NodeJS.Timeout>) => {
      state.value.loadInitialDataTimeoutId = action.payload
    },
    clearSelectedFeature: (state) => {
      state.value.selectedFeature = undefined
    },
    clearHoveredFeature: (state) => {
      state.value.hoveredFeature = undefined
    },
    setLaneLabelsVisible: (state, action: PayloadAction<boolean>) => {
      state.value.laneLabelsVisible = action.payload
    },
    setSigGroupLabelsVisible: (state, action: PayloadAction<boolean>) => {
      state.value.sigGroupLabelsVisible = action.payload
    },
    setShowPopupOnHover: (state, action: PayloadAction<boolean>) => {
      state.value.showPopupOnHover = action.payload
    },
    toggleLiveDataActive: (state) => {
      state.value.liveDataActive = !state.value.liveDataActive
    },
    setBsmTrailLength: (state, action: PayloadAction<number>) => {
      state.value.bsmTrailLength = action.payload
    },
    setTimeWindowSeconds: (state, action: PayloadAction<number>) => {
      state.value.bsmTrailLength = action.payload
    },
    setRawData: (state, action: PayloadAction<RAW_MESSAGE_DATA_EXPORT>) => {
      state.value.rawData.map = action.payload.map ?? state.value.rawData.map
      state.value.rawData.spat = action.payload.spat ?? state.value.rawData.spat
      state.value.rawData.bsm = action.payload.bsm ?? state.value.rawData.bsm
      state.value.rawData.notification = action.payload.notification ?? state.value.rawData.notification
      state.value.rawData.event = action.payload.event ?? state.value.rawData.event
      state.value.rawData.assessment = action.payload.assessment ?? state.value.rawData.assessment
    },
    setMapProps: (state, action: PayloadAction<MAP_PROPS>) => {
      state.value.sourceData = action.payload.sourceData
      state.value.sourceDataType = action.payload.sourceDataType
      state.value.intersectionId = action.payload.intersectionId
      state.value.roadRegulatorId = action.payload.roadRegulatorId
      state.value.loadOnNull = action.payload.loadOnNull
    },
    setCurrentSpatData: (state, action: PayloadAction<ProcessedSpat[]>) => {
      state.value.currentSpatData = action.payload
    },
    togglePlaybackModeActive: (state) => {
      state.value.playbackModeActive = !state.value.playbackModeActive
    },
    resetMapView: (state) => {
      state.value.mapSignalGroups = undefined
      state.value.signalStateData = undefined
      state.value.spatSignalGroups = undefined
      state.value.currentSignalGroups = undefined
      state.value.connectingLanes = undefined
      state.value.surroundingEvents = []
      state.value.filteredSurroundingEvents = []
      state.value.surroundingNotifications = []
      state.value.filteredSurroundingNotifications = []
      state.value.bsmData = { type: 'FeatureCollection', features: [] }
      state.value.currentBsmData = { type: 'FeatureCollection', features: [] }
      state.value.mapSpatTimes = { mapTime: 0, spatTime: 0 }
      state.value.rawData = {}
      state.value.sliderValue = 0
      state.value.playbackModeActive = false
      state.value.currentSpatData = []
      // state.value.currentProcessedSpatData = [];
      state.value.currentBsmData = { type: 'FeatureCollection', features: [] }
    },
    setLiveDataRestartTimeoutId: (state, action) => {
      state.value.liveDataRestartTimeoutId = action.payload
    },
    setLiveDataRestart: (state, action) => {
      state.value.liveDataRestart = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getSurroundingEvents.fulfilled, (state, action: PayloadAction<MessageMonitor.Event[]>) => {
        state.value.surroundingEvents = action.payload
      })
      .addCase(getSurroundingNotifications.fulfilled, (state, action: PayloadAction<MessageMonitor.Notification[]>) => {
        state.value.surroundingNotifications = action.payload
      })
      .addCase(
        pullInitialData.fulfilled,
        (
          state,
          action: PayloadAction<
            | {
                mapData: ProcessedMap
                connectingLanes: ConnectingLanesFeatureCollection
                spatSignalGroups: SpatSignalGroups
                mapSignalGroups: SignalStateFeatureCollection
                mapTime: number
              }
            | undefined
          >
        ) => {
          if (!action.payload) return
          state.value.mapData = action.payload.mapData
          if (action.payload.mapData != null)
            state.value.viewState = {
              latitude: action.payload.mapData.properties.refPoint.latitude,
              longitude: action.payload.mapData.properties.refPoint.longitude,
              zoom: 19,
            }
          state.value.connectingLanes = action.payload.connectingLanes
          state.value.spatSignalGroups = action.payload.spatSignalGroups
          state.value.mapSignalGroups = action.payload.mapSignalGroups
          state.value.mapSpatTimes = { ...state.value.mapSpatTimes, mapTime: action.payload.mapTime }
        }
      )
      .addCase(
        renderEntireMap.fulfilled,
        (
          state,
          action: PayloadAction<{
            mapData: ProcessedMap
            connectingLanes: ConnectingLanesFeatureCollection
            spatSignalGroups: SpatSignalGroups
            mapSignalGroups: SignalStateFeatureCollection
            mapTime: number
            bsmData: BsmFeatureCollection
            rawData: any
            sliderValue: number
          }>
        ) => {
          state.value.mapData = action.payload.mapData
          if (action.payload.mapData != null)
            state.value.viewState = {
              latitude: action.payload.mapData.properties.refPoint.latitude,
              longitude: action.payload.mapData.properties.refPoint.longitude,
              zoom: 19,
            }
          state.value.connectingLanes = action.payload.connectingLanes
          state.value.spatSignalGroups = action.payload.spatSignalGroups
          state.value.mapSignalGroups = action.payload.mapSignalGroups
          state.value.mapSpatTimes = { ...state.value.mapSpatTimes, mapTime: action.payload.mapTime }
          state.value.bsmData = action.payload.bsmData
          state.value.rawData = action.payload.rawData
          state.value.sliderValue = action.payload.sliderValue
          state.value.sliderTimeValue = getNewSliderTimeValue(
            state.value.queryParams.startDate,
            state.value.sliderValue,
            state.value.timeWindowSeconds
          )
        }
      )
      .addCase(
        renderIterative_Map.fulfilled,
        (
          state,
          action: PayloadAction<{
            currentMapData: ProcessedMap[]
            connectingLanes: ConnectingLanesFeatureCollection
            mapData: ProcessedMap
            mapTime: number
            mapSignalGroups: SignalStateFeatureCollection
          }>
        ) => {
          state.value.currentMapData = action.payload.currentMapData
          const previousMapMessage: ProcessedMap | undefined = action.payload.currentMapData.at(-1)
          if (
            state.value.mapData != null &&
            (state.value.mapData.properties.refPoint.latitude != previousMapMessage?.properties.refPoint.latitude ||
              state.value.mapData.properties.refPoint.longitude != previousMapMessage?.properties.refPoint.longitude)
          )
            state.value.viewState = {
              latitude: action.payload.mapData.properties.refPoint.latitude,
              longitude: action.payload.mapData.properties.refPoint.longitude,
              zoom: 19,
            }
          state.value.connectingLanes = action.payload.connectingLanes
          state.value.mapData = action.payload.mapData
          state.value.mapSignalGroups = action.payload.mapSignalGroups
          state.value.mapSpatTimes = { ...state.value.mapSpatTimes, mapTime: action.payload.mapTime }
          state.value.rawData = { ...state.value.rawData, map: action.payload.currentMapData }
        }
      )
      .addCase(
        renderIterative_Spat.fulfilled,
        (state, action: PayloadAction<{ signalGroups: SpatSignalGroups; raw: ProcessedSpat[] }>) => {
          state.value.spatSignalGroups = action.payload.signalGroups
          state.value.currentSpatData = action.payload.raw
          state.value.rawData = { ...state.value.rawData, spat: action.payload.raw }
        }
      )
      .addCase(renderIterative_Bsm.fulfilled, (state, action: PayloadAction<BsmFeatureCollection>) => {
        state.value.currentBsmData = action.payload
        state.value.bsmData = action.payload
        state.value.rawData = { ...state.value.rawData, bsm: action.payload }
      })
      .addCase(
        updateRenderedMapState.fulfilled,
        (
          state,
          action: PayloadAction<{
            currentSignalGroups: SpatSignalGroup[] | undefined
            signalStateData: SignalStateFeatureCollection | undefined
            spatTime: number | undefined
            currentBsms: BsmFeatureCollection
            filteredSurroundingEvents: MessageMonitor.Event[]
            filteredSurroundingNotifications: MessageMonitor.Notification[]
          }>
        ) => {
          state.value.currentSignalGroups = action.payload.currentSignalGroups ?? state.value.currentSignalGroups
          state.value.signalStateData = action.payload.signalStateData ?? state.value.signalStateData
          state.value.mapSpatTimes = {
            ...state.value.mapSpatTimes,
            spatTime: action.payload.spatTime ?? state.value.mapSpatTimes.spatTime,
          }
          state.value.currentBsms = action.payload.currentBsms
          state.value.filteredSurroundingEvents = action.payload.filteredSurroundingEvents
          state.value.filteredSurroundingNotifications = action.payload.filteredSurroundingNotifications
        }
      )
      .addCase(initializeLiveStreaming.fulfilled, (state, action: PayloadAction<CompatClient | undefined>) => {
        state.value.wsClient = action.payload
      })
      .addCase(getBsmDailyCounts.fulfilled, (state, action: PayloadAction<MessageMonitor.MinuteCount[]>) => {
        state.value.bsmEventsByMinute = (action.payload ?? []).map((item) => {
          const date = new Date(item.minute)
          const minutesAfterMidnight = date.getHours() * 60 + date.getMinutes()
          return {
            ...item,
            minutesAfterMidnight,
            timestamp: `${date.getHours().toString().padStart(2, '0')}:${date
              .getMinutes()
              .toString()
              .padStart(2, '0')}`,
          }
        })
      })
  },
})

export const selectLoading = (state: RootState) => state.intersectionMap.loading

export const selectLayersVisible = (state: RootState) => state.intersectionMap.value.layersVisible
export const selectAllInteractiveLayerIds = (state: RootState) => state.intersectionMap.value.allInteractiveLayerIds
export const selectQueryParams = (state: RootState) => state.intersectionMap.value.queryParams
export const selectSourceData = (state: RootState) => state.intersectionMap.value.sourceData
export const selectSourceDataType = (state: RootState) => state.intersectionMap.value.sourceDataType
export const selectIntersectionId = (state: RootState) => state.intersectionMap.value.intersectionId
export const selectRoadRegulatorId = (state: RootState) => state.intersectionMap.value.roadRegulatorId
export const selectLoadOnNull = (state: RootState) => state.intersectionMap.value.loadOnNull
export const selectMapData = (state: RootState) => state.intersectionMap.value.mapData
export const selectBsmData = (state: RootState) => state.intersectionMap.value.bsmData
export const selectMapSignalGroups = (state: RootState) => state.intersectionMap.value.mapSignalGroups
export const selectSignalStateData = (state: RootState) => state.intersectionMap.value.signalStateData
export const selectSpatSignalGroups = (state: RootState) => state.intersectionMap.value.spatSignalGroups
export const selectCurrentSignalGroups = (state: RootState) => state.intersectionMap.value.currentSignalGroups
export const selectCurrentBsms = (state: RootState) => state.intersectionMap.value.currentBsms
export const selectConnectingLanes = (state: RootState) => state.intersectionMap.value.connectingLanes
export const selectSurroundingEvents = (state: RootState) => state.intersectionMap.value.surroundingEvents
export const selectFilteredSurroundingEvents = (state: RootState) =>
  state.intersectionMap.value.filteredSurroundingEvents
export const selectSurroundingNotifications = (state: RootState) => state.intersectionMap.value.surroundingNotifications
export const selectFilteredSurroundingNotifications = (state: RootState) =>
  state.intersectionMap.value.filteredSurroundingNotifications
export const selectBsmEventsByMinute = (state: RootState) => state.intersectionMap.value.bsmEventsByMinute
export const selectPlaybackModeActive = (state: RootState) => state.intersectionMap.value.playbackModeActive
export const selectViewState = (state: RootState) => state.intersectionMap.value.viewState
export const selectTimeWindowSeconds = (state: RootState) => state.intersectionMap.value.timeWindowSeconds
export const selectSliderValue = (state: RootState) => state.intersectionMap.value.sliderValue
export const selectRenderTimeInterval = (state: RootState) => state.intersectionMap.value.renderTimeInterval
export const selectHoveredFeature = (state: RootState) => state.intersectionMap.value.hoveredFeature
export const selectSelectedFeature = (state: RootState) => state.intersectionMap.value.selectedFeature
export const selectRawData = (state: RootState) => state.intersectionMap.value.rawData
export const selectMapSpatTimes = (state: RootState) => state.intersectionMap.value.mapSpatTimes
export const selectSigGroupLabelsVisible = (state: RootState) => state.intersectionMap.value.sigGroupLabelsVisible
export const selectLaneLabelsVisible = (state: RootState) => state.intersectionMap.value.laneLabelsVisible
export const selectShowPopupOnHover = (state: RootState) => state.intersectionMap.value.showPopupOnHover
export const selectImportedMessageData = (state: RootState) => state.intersectionMap.value.importedMessageData
export const selectCursor = (state: RootState) => state.intersectionMap.value.cursor
export const selectLoadInitialDataTimeoutId = (state: RootState) => state.intersectionMap.value.loadInitialDataTimeoutId
export const selectWsClient = (state: RootState) => state.intersectionMap.value.wsClient
export const selectLiveDataActive = (state: RootState) => state.intersectionMap.value.liveDataActive
export const selectCurrentMapData = (state: RootState) => state.intersectionMap.value.currentMapData
export const selectCurrentSpatData = (state: RootState) => state.intersectionMap.value.currentSpatData
export const selectCurrentBsmData = (state: RootState) => state.intersectionMap.value.currentBsmData
export const selectSliderTimeValue = (state: RootState) => state.intersectionMap.value.sliderTimeValue
export const selectBsmTrailLength = (state: RootState) => state.intersectionMap.value.bsmTrailLength
export const selectLiveDataRestartTimeoutId = (state: RootState) => state.intersectionMap.value.liveDataRestartTimeoutId
export const selectLiveDataRestart = (state: RootState) => state.intersectionMap.value.liveDataRestart
export const selectSrmCount = (state: RootState) => state.intersectionMap.value.srmCount
export const selectSrmSsmCount = (state: RootState) => state.intersectionMap.value.srmSsmCount
export const selectSrmMsgList = (state: RootState) => state.intersectionMap.value.srmMsgList

export const {
  setSurroundingEvents,
  maybeUpdateSliderValue,
  setViewState,
  handleImportedMapMessageData,
  updateQueryParams,
  onTimeQueryChanged,
  setSliderValue,
  incrementSliderValue,
  updateRenderTimeInterval,
  onMapClick,
  onMapMouseMove,
  onMapMouseEnter,
  onMapMouseLeave,
  cleanUpLiveStreaming,
  setLoadInitialdataTimeoutId,
  clearSelectedFeature,
  clearHoveredFeature,
  setLaneLabelsVisible,
  setSigGroupLabelsVisible,
  setShowPopupOnHover,
  toggleLiveDataActive,
  setBsmTrailLength,
  setRawData,
  setMapProps,
  togglePlaybackModeActive,
  resetMapView,
  setLiveDataRestartTimeoutId,
  setLiveDataRestart,
} = intersectionMapSlice.actions

export default intersectionMapSlice.reducer

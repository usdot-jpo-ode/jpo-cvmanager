import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { selectToken } from '../../../generalSlices/userSlice'
import { CompatClient, IMessage, Stomp } from '@stomp/stompjs'
import MessageMonitorApi from '../../../apis/intersections/mm-api'
import EventsApi from '../../../apis/intersections/events-api'
import NotificationApi from '../../../apis/intersections/notification-api'
import toast from 'react-hot-toast'
import {
  addBsmTimestamps,
  generateSignalStateFeatureCollection,
  isValidDate,
  parseMapSignalGroups,
  parseSpatSignalGroups,
} from './utilities/message-utils'
import { generateColorDictionary, generateMapboxStyleExpression } from './utilities/colors'
import { setBsmCircleColor, setBsmLegendColors } from './map-layer-style-slice'
import { getTimeRangeDeciseconds } from './utilities/map-utils'
import { MapRef, ViewState } from 'react-map-gl'
import { selectRsuMapData } from '../../../generalSlices/rsuSlice'
import EnvironmentVars from '../../../EnvironmentVars'
import { downloadAllData } from './utilities/file-utilities'
import React from 'react'
import { SsmSrmData } from '../../../models/RsuApi'
import { getTimestamp } from './map-component'
import { getAccurateTimeMillis, selectTimeOffsetMillis } from '../../../generalSlices/timeSyncSlice'
import { combineUrlPaths } from '../../../apis/intersections/api-helper-cviz'

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
  isDefault?: boolean
}

export type IMPORTED_MAP_MESSAGE_DATA = {
  mapData: ProcessedMap[]
  bsmData: BsmFeatureCollection
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
        bsm: BsmFeatureCollection
      }
    | undefined
  sourceDataType: 'notification' | 'event' | 'assessment' | 'timestamp' | undefined
  intersectionId: number | undefined
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

const initialState = {
  mapRef: React.createRef() as React.MutableRefObject<MapRef>,
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
  } as MAP_QUERY_PARAMS,
  sourceData: undefined as MAP_PROPS['sourceData'] | undefined,
  initialSourceDataType: undefined as MAP_PROPS['sourceDataType'] | undefined,
  sourceDataType: undefined as MAP_PROPS['sourceDataType'] | undefined,
  intersectionId: undefined as MAP_PROPS['intersectionId'] | undefined,
  loadOnNull: true as MAP_PROPS['loadOnNull'] | undefined,
  mapData: undefined as ProcessedMap | undefined,
  mapSignalGroups: undefined as SignalStateFeatureCollection | undefined,
  signalStateData: undefined as SignalStateFeatureCollection | undefined,
  spatSignalGroups: undefined as SpatSignalGroups | undefined,
  currentSignalGroups: undefined as SpatSignalGroup[] | undefined,
  currentBsms: {
    type: 'FeatureCollection' as const,
    features: [],
  } as BsmFeatureCollection,
  connectingLanes: undefined as ConnectingLanesFeatureCollection | undefined,
  bsmData: {
    type: 'FeatureCollection' as const,
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
  sliderValueDeciseconds: 0,
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
  pullInitialDataAbortControllers: [] as AbortController[],
  abortAllFutureRequests: false,
  srmCount: 0,
  srmSsmCount: 0,
  srmMsgList: [],
  decoderModeEnabled: false,
}

const getNewSliderTimeValue = (startDate: Date, sliderValueDeciseconds: number, timeWindowSeconds: number) => {
  return {
    start: new Date((startDate.getTime() / 1000 + sliderValueDeciseconds / 10 - timeWindowSeconds) * 1000),
    end: new Date((startDate.getTime() / 1000 + sliderValueDeciseconds / 10) * 1000),
  }
}

export const generateQueryParams = (
  source: MAP_PROPS['sourceData'],
  sourceDataType: MAP_PROPS['sourceDataType'],
  decoderModeEnabled: boolean
) => {
  const startOffset = 1000 * 60 * 1
  const endOffset = 1000 * 60 * 1

  switch (sourceDataType) {
    case 'notification': {
      const notification = source as MessageMonitor.Notification
      return {
        startDate: new Date(notification.notificationGeneratedAt - startOffset),
        endDate: new Date(notification.notificationGeneratedAt + endOffset),
        eventDate: new Date(notification.notificationGeneratedAt),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    case 'event': {
      const event = source as MessageMonitor.Event
      return {
        startDate: new Date(event.eventGeneratedAt - startOffset),
        endDate: new Date(event.eventGeneratedAt + endOffset),
        eventDate: new Date(event.eventGeneratedAt),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    case 'assessment': {
      const assessment = source as Assessment
      return {
        startDate: new Date(assessment.assessmentGeneratedAt - startOffset),
        endDate: new Date(assessment.assessmentGeneratedAt + endOffset),
        eventDate: new Date(assessment.assessmentGeneratedAt),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    case 'timestamp': {
      const ts = (source as timestamp).timestamp
      return {
        startDate: new Date(ts - startOffset),
        endDate: new Date(ts + endOffset),
        eventDate: new Date(ts),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    default:
      if (decoderModeEnabled) {
        let startDate = undefined as number | undefined
        let endDate = undefined as number | undefined

        for (const spat of (source as { spat: ProcessedSpat[] })?.spat ?? []) {
          if (!startDate || spat.utcTimeStamp < startDate) {
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
          isDefault: false,
        }
      }
      return {
        startDate: new Date(Date.now() - startOffset),
        endDate: new Date(Date.now() + endOffset),
        eventDate: new Date(Date.now()),
        vehicleId: undefined,
        isDefault: true,
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
    const decoderModeEnabled = selectDecoderModeEnabled(currentState)

    if (
      queryParams.intersectionId === -1 &&
      (!decoderModeEnabled || (sourceData as { map: ProcessedMap[] })?.map?.length === 0)
    ) {
      dispatch(resetMapView())
      if (!decoderModeEnabled) {
        console.debug('Intersection ID is -1. Not attempting to pull initial map data.')
        return
      }
    }
    dispatch(resetInitialDataAbortControllers())
    dispatch(setAbortAllFutureRequests(false))
    let rawMap: ProcessedMap[] = []
    let rawSpat: ProcessedSpat[] = []
    let rawBsmGeojson: BsmFeatureCollection = { type: 'FeatureCollection', features: [] }
    let abortController = new AbortController()
    if (decoderModeEnabled) {
      rawMap = (sourceData as { map: ProcessedMap[] }).map.map((map) => ({
        ...map,
        properties: {
          ...map.properties,
          timeStamp: getTimestamp(map.properties.timeStamp),
          odeReceivedAt: getTimestamp(map.properties.odeReceivedAt),
        },
      }))
      rawSpat = (sourceData as { spat: ProcessedSpat[] }).spat.map((spat) => ({
        ...spat,
        utcTimeStamp: getTimestamp(spat.utcTimeStamp),
      }))
      rawBsmGeojson = addBsmTimestamps((sourceData as { bsm: BsmFeatureCollection }).bsm)
      if (rawSpat && rawSpat.length != 0 && rawMap && rawMap.length != 0) {
        const sortedSpatData = rawSpat.sort((x, y) => x.utcTimeStamp - y.utcTimeStamp)
        const startTime = new Date(sortedSpatData[0].utcTimeStamp)
        const endTime = new Date(sortedSpatData[sortedSpatData.length - 1].utcTimeStamp)
        if (
          (queryParams.startDate.getTime() !== startTime.getTime() ||
            queryParams.endDate.getTime() !== endTime.getTime()) &&
          isValidDate(startTime) &&
          isValidDate(endTime)
        ) {
          dispatch(
            updateQueryParams({
              ...generateQueryParams(
                {
                  map: [],
                  spat: rawSpat,
                  bsm: {
                    type: 'FeatureCollection',
                    features: [],
                  },
                },
                null,
                decoderModeEnabled
              ),
              intersectionId: rawMap[0].properties.intersectionId,
            })
          )
        }
      }
    } else if (queryParams.isDefault == true) {
      abortController = new AbortController()
      dispatch(addInitialDataAbortController(abortController))
      if (selectAbortAllFutureRequests(getState() as RootState)) {
        return
      }
      const latestSpats = await MessageMonitorApi.getSpatMessages({
        token: authToken,
        intersectionId: queryParams.intersectionId,
        latest: true,
        abortController,
      })
      if (latestSpats && latestSpats.length > 0) {
        dispatch(
          updateQueryParams({
            state: currentState.intersectionMap,
            ...generateQueryParams(
              { timestamp: getTimestamp(latestSpats.at(-1)?.utcTimeStamp) },
              'timestamp',
              decoderModeEnabled
            ),
            intersectionId: queryParams.intersectionId,
          })
        )
        return
      } else {
        dispatch(
          updateQueryParams({
            state: currentState.intersectionMap,
            ...generateQueryParams({ timestamp: Date.now() }, 'timestamp', decoderModeEnabled),
            intersectionId: queryParams.intersectionId,
          })
        )
        return
      }
    } else if (importedMessageData == undefined) {
      if (selectAbortAllFutureRequests(getState() as RootState)) {
        return
      }
      // ######################### Retrieve MAP Data #########################
      abortController = new AbortController()
      dispatch(addInitialDataAbortController(abortController))
      const rawMapPromise = MessageMonitorApi.getMapMessages({
        token: authToken,
        intersectionId: queryParams.intersectionId!,
        endTime: queryParams.endDate,
        latest: true,
        abortController,
      })
      toast.promise(rawMapPromise, {
        loading: `Loading MAP Data`,
        success: `Successfully got MAP Data`,
        error: `Failed to get MAP data. Please see console`,
      })
      rawMap = (await rawMapPromise).map((map) => ({
        ...map,
        properties: {
          ...map.properties,
          timeStamp: getTimestamp(map.properties.timeStamp),
          odeReceivedAt: getTimestamp(map.properties.odeReceivedAt),
        },
      }))
    } else {
      rawMap = [...importedMessageData.mapData]
      rawSpat = [...importedMessageData.spatData].sort((a, b) => a.utcTimeStamp - b.utcTimeStamp)
      rawBsmGeojson = importedMessageData.bsmData
    }

    if (decoderModeEnabled) {
      let bsmGeojson = rawBsmGeojson
      bsmGeojson = {
        ...rawBsmGeojson,
        features: [
          ...[...rawBsmGeojson.features].sort(
            (a, b) => b.properties.odeReceivedAtEpochSeconds - a.properties.odeReceivedAtEpochSeconds
          ),
        ],
      }
      dispatch(renderEntireMap({ currentMapData: [], currentSpatData: [], currentBsmData: bsmGeojson }))
    }
    if (!rawMap || rawMap.length == 0) {
      console.info('No map messages found - exiting pullInitialData')
      return
    }

    const latestMapMessage: ProcessedMap = rawMap.at(-1)!
    const mapCoordinates: OdePosition3D = latestMapMessage?.properties.refPoint
    const mapSignalGroupsLocal = parseMapSignalGroups(latestMapMessage)
    dispatch(
      handleNewMapMessageData({
        mapData: latestMapMessage,
        connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
        mapSignalGroups: mapSignalGroupsLocal,
        mapTime: latestMapMessage.properties.odeReceivedAt as unknown as number,
      })
    )
    if (importedMessageData == undefined && !decoderModeEnabled) {
      if (selectAbortAllFutureRequests(getState() as RootState)) {
        return
      }
      // ######################### Retrieve SPAT Data #########################
      abortController = new AbortController()
      dispatch(addInitialDataAbortController(abortController))
      const rawSpatPromise = MessageMonitorApi.getSpatMessagesWithLatest({
        token: authToken,
        intersectionId: queryParams.intersectionId!,
        startTime: queryParams.startDate,
        endTime: queryParams.endDate,
        abortController,
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

      if (selectAbortAllFutureRequests(getState() as RootState)) {
        return
      }
      dispatch(getBsmDailyCounts())
      dispatch(getSurroundingEvents())
      dispatch(getSurroundingNotifications())
    }

    // ######################### SPAT Signal Groups #########################
    const spatSignalGroupsLocal = parseSpatSignalGroups(rawSpat)
    dispatch(setSpatSignalGroups(spatSignalGroupsLocal))

    // ######################### BSMs #########################
    if (selectAbortAllFutureRequests(getState() as RootState)) {
      return
    }

    if (!importedMessageData && !decoderModeEnabled) {
      abortController = new AbortController()
      dispatch(addInitialDataAbortController(abortController))
      const rawBsmPromise = MessageMonitorApi.getBsmMessages({
        token: authToken,
        vehicleId: queryParams.vehicleId,
        startTime: queryParams.startDate,
        endTime: queryParams.endDate,
        long: mapCoordinates.longitude,
        lat: mapCoordinates.latitude,
        distance: 500,
        abortController,
      })
      toast.promise(rawBsmPromise, {
        loading: `Loading BSM Data`,
        success: `Successfully got BSM Data`,
        error: `Failed to get BSM data. Please see console`,
      })
      rawBsmGeojson = addBsmTimestamps({ type: 'FeatureCollection', features: await rawBsmPromise })
    }
    const bsmGeojson = {
      ...rawBsmGeojson,
      features: [
        ...[...rawBsmGeojson.features].sort(
          (a, b) => b.properties.odeReceivedAtEpochSeconds - a.properties.odeReceivedAtEpochSeconds
        ),
      ],
    }
    if (!selectAbortAllFutureRequests(getState() as RootState)) {
      dispatch(renderIterative_Bsm(bsmGeojson.features))
    }
    return
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined &&
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
    const decoderModeEnabled = selectDecoderModeEnabled(currentState)

    // Still render BSMs if decoderModeEnabled is true, even if there are no map messages.
    // The condition guard eliminates sourceDataType != exact && currentMapData.length == 0
    if (decoderModeEnabled && currentMapData.length == 0) {
      const uniqueIds = new Set(currentBsmData.features.map((bsm) => bsm.properties?.id))
      // generate equally spaced unique colors for each uniqueId
      const colors = generateColorDictionary(uniqueIds)
      dispatch(setBsmLegendColors(colors))
      // add color to each feature
      const bsmLayerStyle = generateMapboxStyleExpression(colors)
      dispatch(setBsmCircleColor(bsmLayerStyle))

      return {
        bsmData: currentBsmData,
        rawData: { bsm: currentBsmData },
        sliderValueDeciseconds: Math.min(
          getTimeRangeDeciseconds(queryParams.startDate, queryParams.eventDate ?? new Date()),
          getTimeRangeDeciseconds(queryParams.startDate, queryParams.endDate)
        ),
      }
    }

    // ######################### MAP Data #########################
    const latestMapMessage: ProcessedMap = currentMapData.at(-1)
    const mapSignalGroupsLocal = parseMapSignalGroups(latestMapMessage)
    dispatch(
      handleNewMapMessageData({
        mapData: latestMapMessage,
        connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
        mapSignalGroups: mapSignalGroupsLocal,
        mapTime: latestMapMessage.properties.odeReceivedAt as unknown as number,
      })
    )

    // ######################### SPAT Signal Groups #########################
    const spatSignalGroupsLocal = parseSpatSignalGroups(currentSpatData)
    dispatch(setSpatSignalGroups(spatSignalGroupsLocal))

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
      bsmData: currentBsmData,
      rawData: rawData,
      sliderValueDeciseconds: Math.min(
        getTimeRangeDeciseconds(queryParams.startDate, queryParams.eventDate ?? new Date()),
        getTimeRangeDeciseconds(queryParams.startDate, queryParams.endDate)
      ),
    }
  },
  {
    condition: (
      args: { currentMapData: ProcessedMap[]; currentSpatData: ProcessedSpat[]; currentBsmData: BsmFeatureCollection },
      { getState }
    ) => args.currentMapData.length != 0 || selectDecoderModeEnabled(getState() as RootState),
  }
)

export const updateBsmData = createAsyncThunk(
  'intersectionMap/updateBsmData',
  async (bsmFC: BsmFeatureCollection, { dispatch }) => {
    const uniqueIds = new Set(bsmFC.features.map((bsm) => bsm.properties?.id))
    // generate equally spaced unique colors for each uniqueId
    const colors = generateColorDictionary(uniqueIds)
    dispatch(setBsmLegendColors(colors))
    // add color to each feature
    const bsmLayerStyle = generateMapboxStyleExpression(colors)
    dispatch(setBsmCircleColor(bsmLayerStyle))
    return bsmFC
  }
)

export const updateTrailedBsmData = createAsyncThunk(
  'intersectionMap/updateTrailedBsmData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const bsmData = selectBsmData(currentState)
    const renderTimeInterval = selectRenderTimeInterval(currentState)
    const bsmTrailLength = selectBsmTrailLength(currentState)

    const filteredBsms: ProcessedBsmFeature[] = bsmData?.features?.filter(
      (feature) =>
        feature.properties?.odeReceivedAtEpochSeconds >= renderTimeInterval[0] &&
        feature.properties?.odeReceivedAtEpochSeconds <= renderTimeInterval[1]
    )
    const sortedBsms = filteredBsms.sort(
      (a, b) => b.properties.odeReceivedAtEpochSeconds - a.properties.odeReceivedAtEpochSeconds
    )

    const uniqueIds = new Set(filteredBsms.map((bsm) => bsm.properties?.id).sort())
    // generate equally spaced unique colors for each uniqueId
    const colors = generateColorDictionary(uniqueIds)
    dispatch(setBsmLegendColors(colors))
    // add color to each feature
    const bsmLayerStyle = generateMapboxStyleExpression(colors)
    dispatch(setBsmCircleColor(bsmLayerStyle))

    const lastBsms: ProcessedBsmFeature[] = []
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
    return { ...bsmData, features: lastBsms }
  }
)

export const renderIterative_Map = createAsyncThunk(
  'intersectionMap/renderIterative_Map',
  async (newMapData: ProcessedMap[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const queryParams = selectQueryParams(currentState)
    const currentMapData: ProcessedMap[] = selectCurrentMapData(currentState)

    newMapData = newMapData.map((map) => ({
      ...map,
      properties: {
        ...map.properties,
        timeStamp: getTimestamp(map.properties.timeStamp),
        odeReceivedAt: getTimestamp(map.properties.odeReceivedAt),
      },
    }))

    const OLDEST_DATA_TO_KEEP = queryParams.eventDate.getTime() - queryParams.startDate.getTime() // milliseconds

    // find latest timestamp from currentMapData
    let latestTimestamp = 0
    for (let i = 0; i < currentMapData.length; i++) {
      const timestamp = currentMapData[i].properties.odeReceivedAt
      if (timestamp > latestTimestamp) {
        latestTimestamp = timestamp
      }
    }
    console.log(
      'renderIterative_MAP ts ',
      newMapData.at(-1)!.properties,
      newMapData.at(-1)!.properties.odeReceivedAt,
      latestTimestamp,
      Math.max(newMapData.at(-1)!.properties.odeReceivedAt, latestTimestamp)
    )
    const currTimestamp = Math.max(newMapData.at(-1)!.properties.odeReceivedAt, latestTimestamp)

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
    condition: (newMapData: ProcessedMap[]) => newMapData && newMapData.length != 0,
  }
)

export const renderIterative_Spat = createAsyncThunk(
  'intersectionMap/renderIterative_Spat',
  async (newSpatData: ProcessedSpat[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const queryParams = selectQueryParams(currentState)
    const currentSpatSignalGroups: SpatSignalGroups = selectSpatSignalGroups(currentState) ?? {}
    const currentProcessedSpatData: ProcessedSpat[] = selectCurrentSpatData(currentState) ?? []

    const OLDEST_DATA_TO_KEEP = queryParams.eventDate.getTime() - queryParams.startDate.getTime() // milliseconds
    // Inject and filter spat data
    // 2024-01-09T00:24:28.354Z
    newSpatData = newSpatData.map((spat) => ({
      ...spat,
      utcTimeStamp: getTimestamp(spat.utcTimeStamp),
    }))

    // Collect currentSpatSignalGroups dictionary (keyed by timestamp in milliseconds since epoch) into an array for simple iteration
    const currentSpatSignalGroupsArr = Object.keys(currentSpatSignalGroups).map((key) => ({
      timestamp: Number(key), // convert string key (timestamp) into timestamp number
      sigGroup: currentSpatSignalGroups[key],
    }))

    // find latest timestamp from currentSpatSignalGroupsArr
    let latestTimestamp = 0
    for (let i = 0; i < currentSpatSignalGroupsArr.length; i++) {
      const timestamp = currentSpatSignalGroupsArr[i].timestamp
      if (timestamp > latestTimestamp) {
        latestTimestamp = timestamp
      }
    }
    const currTimestamp = getTimestamp(Math.max(newSpatData.at(-1)!.utcTimeStamp, latestTimestamp))
    dispatch(maybeUpdateSliderValue(currTimestamp))

    let oldIndex = 0
    for (let i = 0; i < currentSpatSignalGroupsArr.length; i++) {
      if (currentSpatSignalGroupsArr[i].timestamp < currTimestamp - OLDEST_DATA_TO_KEEP) {
        oldIndex = i
      } else {
        break
      }
    }
    const newSpatSignalGroups = parseSpatSignalGroups(newSpatData)
    const newSpatSignalGroupsArr = Object.keys(newSpatSignalGroups).map((key) => ({
      timestamp: Number(key),
      sigGroup: newSpatSignalGroups[key],
    }))
    const filteredSpatSignalGroupsArr = currentSpatSignalGroupsArr
      .slice(oldIndex, currentSpatSignalGroupsArr.length)
      .concat(newSpatSignalGroupsArr)
    const currentSpatSignalGroupsLocal = filteredSpatSignalGroupsArr.reduce((acc, curr) => {
      acc[curr.timestamp] = curr.sigGroup
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
    return { signalGroups: currentSpatSignalGroupsLocal, raw: currentProcessedSpatDataLocal }
  },
  {
    condition: (newSpatData: ProcessedSpat[]) => newSpatData && newSpatData.length != 0,
  }
)

export const renderIterative_Bsm = createAsyncThunk(
  'intersectionMap/renderIterative_Bsm',
  async (newBsmData: ProcessedBsmFeature[], { getState, dispatch }) => {
    const currentState = getState() as RootState
    const queryParams = selectQueryParams(currentState)
    const currentBsmData: BsmFeatureCollection = selectCurrentBsmData(currentState)

    const newBsmFeatureCollection = addBsmTimestamps({ type: 'FeatureCollection', features: newBsmData })

    const OLDEST_DATA_TO_KEEP = queryParams.eventDate.getTime() - queryParams.startDate.getTime() // milliseconds

    // find latest timestamp from currentBsmData and newBsmData
    let latestTimestamp = 0
    for (let i = 0; i < currentBsmData.features.length; i++) {
      const timestamp = Number(currentBsmData.features[i].properties.odeReceivedAt)
      if (timestamp > latestTimestamp) {
        latestTimestamp = timestamp
      }
    }
    const currTimestamp = getTimestamp(
      Math.max(
        new Date(newBsmFeatureCollection.features.at(-1)!.properties.odeReceivedAt as unknown as string).getTime() /
          1000,
        latestTimestamp
      )
    )

    // Inject and filter BSM data
    let oldIndex = 0
    for (let i = 0; i < currentBsmData.features.length; i++) {
      if (Number(currentBsmData.features[i].properties.odeReceivedAt) < currTimestamp - OLDEST_DATA_TO_KEEP) {
        oldIndex = i
      } else {
        break
      }
    }
    const currentBsmGeojson = {
      ...currentBsmData,
      features: currentBsmData.features
        .slice(oldIndex, currentBsmData.features.length)
        .concat(newBsmFeatureCollection.features),
    }

    dispatch(updateBsmData(currentBsmGeojson))
    dispatch(setRawData({ bsm: currentBsmGeojson }))
    return currentBsmGeojson
  },
  {
    condition: (newBsmData: ProcessedBsmFeature[]) => newBsmData && newBsmData.length != 0,
  }
)

export const getBsmDailyCounts = createAsyncThunk(
  'intersectionMap/getBsmDailyCounts',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)

    const dayStart = new Date(queryParams.startDate)
    dayStart.setHours(0, 0, 0, 0)
    const dayEnd = new Date(queryParams.startDate)
    dayEnd.setHours(23, 59, 59, 0)

    if (selectAbortAllFutureRequests(getState() as RootState)) {
      return
    }
    const abortController = new AbortController()
    dispatch(addInitialDataAbortController(abortController))
    const bsmEventsByMinutePromise = EventsApi.getBsmByMinuteEvents({
      token: authToken,
      intersectionId: queryParams.intersectionId!,
      startTime: dayStart,
      endTime: dayEnd,
      test: false,
      abortController,
    })
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
      selectQueryParams(getState() as RootState).intersectionId != undefined,
  }
)

export const getSurroundingEvents = createAsyncThunk(
  'intersectionMap/getSurroundingEvents',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)

    if (selectAbortAllFutureRequests(getState() as RootState)) {
      return
    }
    const abortController = new AbortController()
    dispatch(addInitialDataAbortController(abortController))
    const surroundingEventsPromise = EventsApi.getAllEvents(
      authToken,
      queryParams.intersectionId!,
      queryParams.startDate,
      queryParams.endDate,
      abortController
    )
    return surroundingEventsPromise
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined,
  }
)

export const getSurroundingNotifications = createAsyncThunk(
  'intersectionMap/getSurroundingNotifications',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)

    if (selectAbortAllFutureRequests(getState() as RootState)) {
      return
    }
    const abortController = new AbortController()
    dispatch(addInitialDataAbortController(abortController))
    const surroundingNotificationsPromise = NotificationApi.getAllNotifications({
      token: authToken,
      intersectionId: queryParams.intersectionId!,
      startTime: queryParams.startDate,
      endTime: queryParams.endDate,
      abortController,
    })
    return surroundingNotificationsPromise
  },
  {
    condition: (_, { getState }) =>
      selectToken(getState() as RootState) != undefined &&
      selectQueryParams(getState() as RootState).intersectionId != undefined,
  }
)

export const initializeLiveStreaming = createAsyncThunk(
  'intersectionMap/initializeLiveStreaming',
  async (
    args: { token: string; intersectionId: number; numRestarts?: number; shouldResetMapView?: boolean },
    { getState, dispatch }
  ) => {
    const { token, intersectionId, numRestarts = 0, shouldResetMapView = true } = args
    // Connect to WebSocket when component mounts
    const liveDataActive = selectLiveDataActive(getState() as RootState)
    const wsClient = selectWsClient(getState() as RootState)

    dispatch(onTimeQueryChanged({ eventTime: new Date(), timeBefore: 10, timeAfter: 0, timeWindowSeconds: 2 }))
    if (shouldResetMapView) dispatch(resetMapView())

    if (!liveDataActive) {
      console.debug('Not initializing live streaming because liveDataActive is false')
      return
    }
    console.info('Live streaming data from Intersection API STOMP WebSocket endpoint')

    const protocols = ['v10.stomp', 'v11.stomp']
    protocols.push(token)
    const url = combineUrlPaths(EnvironmentVars.CVIZ_API_WS_URL, 'stomp')

    // Stomp Client Documentation: https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html
    const client = Stomp.client(url, protocols)

    // Get Current MAP Message
    const currentState = getState() as RootState
    const authToken = selectToken(currentState)!
    const queryParams = selectQueryParams(currentState)
    const rawMapPromise = MessageMonitorApi.getMapMessages({
      token: authToken,
      intersectionId: queryParams.intersectionId,
      latest: true,
    })
    toast.promise(rawMapPromise, {
      loading: `Loading MAP Data`,
      success: `Successfully got MAP Data`,
      error: `Failed to get MAP data. Please see console`,
    })
    dispatch(renderIterative_Map(await rawMapPromise))

    // Topics are in the format /live/{intersectionID}/{spat,map,bsm}
    const spatTopic = `/live/${intersectionId}/processed-spat`
    const mapTopic = `/live/${intersectionId}/processed-map`
    const bsmTopic = `/live/${intersectionId}/processed-bsm`

    const connectionStartTime = Date.now()
    client.connect(
      {},
      () => {
        client.subscribe(spatTopic, function (mes: IMessage) {
          const spatMessage: ProcessedSpat = JSON.parse(mes.body)
          const messageTime = getTimestamp(spatMessage.utcTimeStamp)
          console.debug(
            'Received SPaT message with age of ' +
              (getAccurateTimeMillis(selectTimeOffsetMillis(getState() as RootState)) - messageTime) +
              'ms'
          )
          dispatch(renderIterative_Spat([spatMessage]))
          // dispatch(maybeUpdateSliderValue())
        })

        client.subscribe(mapTopic, function (mes: IMessage) {
          const mapMessage: ProcessedMap = JSON.parse(mes.body)
          const messageTime = getTimestamp(mapMessage.properties.odeReceivedAt)
          console.debug(
            'Received MAP message with age of ' +
              (getAccurateTimeMillis(selectTimeOffsetMillis(getState() as RootState)) - messageTime) +
              'ms'
          )
          dispatch(renderIterative_Map([mapMessage]))
          // dispatch(maybeUpdateSliderValue())
        })

        client.subscribe(bsmTopic, function (mes: IMessage) {
          const bsmData: ProcessedBsmFeature = JSON.parse(mes.body)
          const messageTime = getTimestamp(bsmData.properties.odeReceivedAt)
          console.debug(
            'Received BSM message with age of ' +
              (getAccurateTimeMillis(selectTimeOffsetMillis(getState() as RootState)) - messageTime) +
              'ms'
          )
          dispatch(renderIterative_Bsm([bsmData]))
          // dispatch(maybeUpdateSliderValue())
        })
      },
      (error) => {
        console.error('Live Streaming ERROR connecting to live data Websocket: ' + error)
      }
    )

    function onDisconnect() {
      if (numRestarts < 5 && liveDataActive) {
        let numRestartsLocal = numRestarts
        if (Date.now() - connectionStartTime > 10000) {
          numRestartsLocal = 0
        }
        console.debug('Attempting to reconnect to STOMP endpoint (numRestarts: ' + numRestartsLocal + ')')

        if (numRestartsLocal == 0) {
          dispatch(
            initializeLiveStreaming({
              token,
              intersectionId,
              numRestarts: 0,
              shouldResetMapView: false,
            })
          )
        } else {
          dispatch(
            setLiveDataRestartTimeoutId(
              setTimeout(() => {
                dispatch(setLiveDataRestart(numRestartsLocal + 1))
              }, numRestartsLocal * 2000)
            )
          )
        }
      } else {
        if (numRestarts >= 5) {
          console.info('Disconnected from STOMP endpoint - number of retries exceeded')
        } else {
          console.info('Disconnected from STOMP endpoint - liveDataActive is no longer active')
        }
        cleanUpLiveStreaming()
      }
    }

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
      onDisconnect()
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
      onDisconnect()
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
    const timeFilterBsms = selectTimeFilterBsms(currentState)
    const spatSignalGroups = selectSpatSignalGroups(currentState)
    const mapSignalGroups = selectMapSignalGroups(currentState)
    const renderTimeInterval = selectRenderTimeInterval(currentState)
    const bsmData = selectBsmData(currentState)
    const surroundingEvents = selectSurroundingEvents(currentState)
    const surroundingNotifications = selectSurroundingNotifications(currentState)

    if (timeFilterBsms == false) {
      dispatch(setCurrentBsms(bsmData))
    }
    if (!mapSignalGroups || !spatSignalGroups) {
      let message = 'No map or spat data available'
      if (mapSignalGroups) message = 'No spat data available'
      else if (spatSignalGroups) message = 'No map data available'
      console.debug(`Not rendering BSM data: ${message}`)
      return
    }

    // retrieve filtered SPATs
    let closestSignalGroup: { spat: SpatSignalGroup[]; datetime: number } | null = null
    for (const datetime in spatSignalGroups) {
      const datetimeNum = Number(datetime) / 1000 // milliseconds to seconds
      if (datetimeNum <= renderTimeInterval[1]) {
        if (
          closestSignalGroup === null ||
          Math.abs(datetimeNum - renderTimeInterval[1]) < Math.abs(closestSignalGroup.datetime - renderTimeInterval[1])
        ) {
          closestSignalGroup = { datetime: datetimeNum, spat: spatSignalGroups[datetime] }
        }
      }
    }

    // retrieve filtered BSMs
    if (timeFilterBsms !== false) {
      dispatch(updateTrailedBsmData())
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
      spatTime: closestSignalGroup?.datetime * 1000,
      filteredSurroundingEvents: filteredEvents,
      filteredSurroundingNotifications: filteredNotifications,
    }
  },
  {
    condition: (_, { getState }) =>
      Boolean(
        (selectMapSignalGroups(getState() as RootState)?.features.length != 0 &&
          selectSpatSignalGroups(getState() as RootState)) ||
          selectBsmData(getState() as RootState)?.features.length != 0
      ),
  }
)

const compareQueryParams = (oldParams: MAP_QUERY_PARAMS, newParams: MAP_QUERY_PARAMS) => {
  return (
    oldParams.startDate.getTime() != newParams.startDate.getTime() ||
    oldParams.endDate.getTime() != newParams.endDate.getTime() ||
    oldParams.eventDate.getTime() != newParams.eventDate.getTime() ||
    oldParams.vehicleId != newParams.vehicleId ||
    oldParams.intersectionId != newParams.intersectionId ||
    oldParams.isDefault != newParams.isDefault
  )
}

const generateRenderTimeInterval = (startDate: Date, sliderValueDeciseconds: number, timeWindowSeconds: number) => {
  const startTime = startDate.getTime() / 1000

  const filteredStartTime = startTime + sliderValueDeciseconds / 10 - timeWindowSeconds
  const filteredEndTime = startTime + sliderValueDeciseconds / 10

  return [filteredStartTime, filteredEndTime]
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
      selectQueryParams(getState() as RootState).intersectionId != undefined,
  }
)

export const renderRsuData = createAsyncThunk(
  'intersectionMap/renderRsuData',
  async (_, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const rsuMapData = selectRsuMapData(currentState)

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
      selectQueryParams(getState() as RootState).intersectionId != undefined,
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
      const localMsgList = []
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
    maybeUpdateSliderValue: (state, action: PayloadAction<number | undefined>) => {
      if (
        state.value.liveDataActive &&
        (!state.value.lastSliderUpdate || Date.now() - state.value.lastSliderUpdate > 1 * 1000 || action.payload)
      ) {
        let sliderEndDate = new Date(state.value.queryParams.endDate.getTime() + state.value.lastSliderUpdate * 1000) // move slider forward by the elapsed time
        if (action.payload) {
          sliderEndDate = new Date(action.payload) // Time specified, move slider to specified end date
        }
        const newQueryParams = {
          startDate: new Date(
            sliderEndDate.getTime() -
              (state.value.queryParams.endDate.getTime() - state.value.queryParams.startDate.getTime())
          ),
          endDate: sliderEndDate,
          eventDate: sliderEndDate,
          vehicleId: undefined,
          intersectionId: state.value.queryParams.intersectionId,
        }
        state.value.queryParams = newQueryParams
        state.value.renderTimeInterval = [
          newQueryParams.endDate.getTime() / 1000 - state.value.timeWindowSeconds,
          newQueryParams.endDate.getTime() / 1000,
        ]
        state.value.sliderTimeValue = {
          start: new Date(newQueryParams.endDate.getTime() - state.value.timeWindowSeconds * 1000),
          end: newQueryParams.endDate,
        }
        state.value.sliderValueDeciseconds =
          (newQueryParams.endDate.getTime() - state.value.timeWindowSeconds * 1000 - newQueryParams.endDate.getTime()) /
          100
      }
    },
    setViewState: (state, action: PayloadAction<Partial<ViewState>>) => {
      state.value.viewState = action.payload
    },
    handleImportedMapMessageData: (
      state,
      action: PayloadAction<{
        mapData: ProcessedMap[]
        bsmData: BsmFeatureCollection
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
      }
      state.value.sliderTimeValue = getNewSliderTimeValue(
        state.value.queryParams.startDate,
        state.value.sliderValueDeciseconds,
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
        isDefault?: boolean
        resetTimeWindow?: boolean
        updateSlider?: boolean
      }>
    ) => {
      const newQueryParams = {
        startDate: action.payload.startDate ?? state.value.queryParams.startDate,
        endDate: action.payload.endDate ?? state.value.queryParams.endDate,
        eventDate: action.payload.eventDate ?? state.value.queryParams.eventDate,
        vehicleId: action.payload.vehicleId ?? state.value.queryParams.vehicleId,
        intersectionId: action.payload.intersectionId ?? state.value.queryParams.intersectionId,
        isDefault: action.payload.isDefault ?? state.value.queryParams.isDefault,
      }
      if (compareQueryParams(state.value.queryParams, newQueryParams)) {
        state.value.queryParams = newQueryParams
        state.value.sliderTimeValue = getNewSliderTimeValue(
          state.value.queryParams.startDate,
          state.value.sliderValueDeciseconds,
          state.value.timeWindowSeconds
        )
        if (action.payload.resetTimeWindow) state.value.timeWindowSeconds = 60
        if (action.payload.updateSlider)
          state.value.sliderValueDeciseconds = getTimeRangeDeciseconds(newQueryParams.startDate, newQueryParams.endDate)
      }
      // _updateQueryParams({ state: state.value, ...action.payload })
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
      const { eventTime, timeBefore, timeAfter, timeWindowSeconds } = action.payload
      const actualEventTime = eventTime ?? new Date()
      const updatedQueryParams = {
        startDate: new Date(actualEventTime.getTime() - (timeBefore ?? 0) * 1000),
        endDate: new Date(actualEventTime.getTime() + (timeAfter ?? 0) * 1000),
        eventDate: actualEventTime,
        intersectionId: state.value.queryParams.intersectionId,
        isDefault: state.value.queryParams.isDefault,
      }
      if (compareQueryParams(state.value.queryParams, updatedQueryParams)) {
        // Detected change in query params
        state.value.queryParams = updatedQueryParams
        state.value.sliderTimeValue = getNewSliderTimeValue(
          state.value.queryParams.startDate,
          state.value.sliderValueDeciseconds,
          state.value.timeWindowSeconds
        )
      } else {
        // No change in query params
      }
      state.value.timeWindowSeconds = timeWindowSeconds ?? state.value.timeWindowSeconds
    },
    setSliderValueDeciseconds: (state, action: PayloadAction<number | number[]>) => {
      state.value.sliderValueDeciseconds = action.payload as number
      state.value.liveDataActive = false
    },
    incrementSliderValue: (state, action: PayloadAction<number | undefined>) => {
      // action.payload in deciseconds
      const maxSliderValue = getTimeRangeDeciseconds(state.value.queryParams.startDate, state.value.queryParams.endDate)
      if (state.value.sliderValueDeciseconds == maxSliderValue) {
        state.value.playbackModeActive = false
      } else {
        state.value.sliderValueDeciseconds += action.payload ?? 1
      }
    },
    updateRenderTimeInterval: (state) => {
      state.value.renderTimeInterval = generateRenderTimeInterval(
        state.value.queryParams.startDate,
        state.value.sliderValueDeciseconds,
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
          console.debug('Successfully disconnected from STOMP endpoint')
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
    setLoadInitialDataTimeoutId: (state, action: PayloadAction<NodeJS.Timeout>) => {
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
    setLiveDataActive: (state, action: PayloadAction<boolean>) => {
      state.value.liveDataActive = action.payload
    },
    setBsmTrailLength: (state, action: PayloadAction<number>) => {
      state.value.bsmTrailLength = action.payload
    },
    setTimeWindowSeconds: (state, action: PayloadAction<number>) => {
      state.value.timeWindowSeconds = action.payload
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
      state.value.initialSourceDataType =
        state.value.initialSourceDataType == undefined
          ? action.payload.sourceDataType
          : state.value.initialSourceDataType
      state.value.sourceDataType = action.payload.sourceDataType
      state.value.intersectionId = action.payload.intersectionId
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
      state.value.currentBsms = { type: 'FeatureCollection', features: [] }
      state.value.currentBsmData = { type: 'FeatureCollection', features: [] }
      state.value.mapData = undefined
      state.value.mapSpatTimes = { mapTime: 0, spatTime: 0 }
      state.value.rawData = {}
      state.value.sourceData = { map: [], spat: [], bsm: { type: 'FeatureCollection', features: [] } }
      state.value.sliderValueDeciseconds = 0
      state.value.playbackModeActive = false
      state.value.currentSpatData = []
      // state.value.currentProcessedSpatData = [];
    },
    setLiveDataRestartTimeoutId: (state, action) => {
      state.value.liveDataRestartTimeoutId = action.payload
    },
    setLiveDataRestart: (state, action) => {
      state.value.liveDataRestart = action.payload
    },
    centerMapOnPoint: (
      state,
      action: PayloadAction<{
        latitude: number
        longitude: number
        zoom?: number
        heading?: number
        animationDurationMs?: number
      }>
    ) => {
      const { latitude, longitude, zoom, heading, animationDurationMs } = action.payload
      if (state.value.mapRef?.current) {
        state.value.mapRef.current.flyTo({
          center: [longitude, latitude],
          zoom: zoom ?? 19,
          bearing: heading ?? 0,
          duration: animationDurationMs ?? 1000,
        })
      } else {
        console.error('Error centering map - map ref not set')
      }
    },
    handleNewMapMessageData: (
      state,
      action: PayloadAction<{
        mapData: ProcessedMap
        connectingLanes: ConnectingLanesFeatureCollection
        mapSignalGroups: SignalStateFeatureCollection
        mapTime: number
      }>
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
      state.value.mapSignalGroups = action.payload.mapSignalGroups
      state.value.mapSpatTimes = { ...state.value.mapSpatTimes, mapTime: action.payload.mapTime }
    },
    addInitialDataAbortController: (state, action: PayloadAction<AbortController>) => {
      state.value.pullInitialDataAbortControllers = [...state.value.pullInitialDataAbortControllers, action.payload]
    },
    addInitialDataAbortPromise: (state, action: PayloadAction<{ abort: () => void }>) => {
      state.value.pullInitialDataAbortControllers = [
        ...state.value.pullInitialDataAbortControllers,
        new ThunkAbortController(action.payload),
      ]
    },
    resetInitialDataAbortControllers: (state) => {
      const controllers = state.value.pullInitialDataAbortControllers
      state.value.pullInitialDataAbortControllers = []
      state.value.abortAllFutureRequests = true
      controllers.forEach((abortController) => abortController.abort())
    },
    setSpatSignalGroups: (state, action: PayloadAction<SpatSignalGroups>) => {
      state.value.spatSignalGroups = action.payload
    },
    setCurrentBsms: (state, action: PayloadAction<BsmFeatureCollection>) => {
      state.value.currentBsms = action.payload
    },
    setMapRef: (state, action: PayloadAction<React.MutableRefObject<MapRef>>) => {
      state.value.mapRef.current = action.payload.current
    },
    setDecoderModeEnabled: (state, action: PayloadAction<boolean>) => {
      state.value.decoderModeEnabled = action.payload
    },
    setAbortAllFutureRequests: (state, action: PayloadAction<boolean>) => {
      state.value.abortAllFutureRequests = action.payload
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
        renderEntireMap.fulfilled,
        (
          state,
          action: PayloadAction<{
            bsmData: BsmFeatureCollection
            rawData: any
            sliderValueDeciseconds: number
          }>
        ) => {
          state.value.bsmData = action.payload.bsmData
          state.value.rawData = action.payload.rawData
          state.value.sliderValueDeciseconds = action.payload.sliderValueDeciseconds
          state.value.sliderTimeValue = getNewSliderTimeValue(
            state.value.queryParams.startDate,
            state.value.sliderValueDeciseconds,
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
          action: PayloadAction<
            | {
                currentSignalGroups: SpatSignalGroup[] | undefined
                signalStateData: SignalStateFeatureCollection | undefined
                spatTime: number | undefined
                filteredSurroundingEvents: MessageMonitor.Event[]
                filteredSurroundingNotifications: MessageMonitor.Notification[]
              }
            | undefined
          >
        ) => {
          if (!action.payload) return
          state.value.currentSignalGroups = action.payload.currentSignalGroups ?? state.value.currentSignalGroups
          state.value.signalStateData = action.payload.signalStateData ?? state.value.signalStateData
          state.value.mapSpatTimes = {
            ...state.value.mapSpatTimes,
            spatTime: action.payload.spatTime ?? state.value.mapSpatTimes.spatTime,
          }
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
      .addCase(updateBsmData.fulfilled, (state, action: PayloadAction<BsmFeatureCollection>) => {
        state.value.bsmData = action.payload
      })
      .addCase(updateTrailedBsmData.fulfilled, (state, action: PayloadAction<BsmFeatureCollection>) => {
        state.value.currentBsms = action.payload
      })
  },
})

export const selectLoading = (state: RootState) => state.intersectionMap.loading

export const selectLayersVisible = (state: RootState) => state.intersectionMap.value.layersVisible
export const selectAllInteractiveLayerIds = (state: RootState) => state.intersectionMap.value.allInteractiveLayerIds
export const selectQueryParams = (state: RootState) => state.intersectionMap.value.queryParams
export const selectSourceData = (state: RootState) => state.intersectionMap.value.sourceData
export const selectSourceDataType = (state: RootState) => state.intersectionMap.value.sourceDataType
export const selectInitialSourceDataType = (state: RootState) => state.intersectionMap.value.initialSourceDataType
export const selectIntersectionId = (state: RootState) => state.intersectionMap.value.intersectionId
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
export const selectSliderValueDeciseconds = (state: RootState) => state.intersectionMap.value.sliderValueDeciseconds
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
export const selectPullInitialDataAbortControllers = (state: RootState) =>
  state.intersectionMap.value.pullInitialDataAbortControllers
export const selectSrmCount = (state: RootState) => state.intersectionMap.value.srmCount
export const selectSrmSsmCount = (state: RootState) => state.intersectionMap.value.srmSsmCount
export const selectSrmMsgList = (state: RootState) => state.intersectionMap.value.srmMsgList
export const selectDecoderModeEnabled = (state: RootState) => state.intersectionMap.value.decoderModeEnabled
export const selectTimeFilterBsms = (state: RootState) => !state.intersectionMap.value.decoderModeEnabled
export const selectAbortAllFutureRequests = (state: RootState) => state.intersectionMap.value.abortAllFutureRequests

export const {
  setSurroundingEvents,
  maybeUpdateSliderValue,
  setViewState,
  handleImportedMapMessageData,
  updateQueryParams,
  onTimeQueryChanged,
  setSliderValueDeciseconds,
  incrementSliderValue,
  updateRenderTimeInterval,
  onMapClick,
  onMapMouseMove,
  onMapMouseEnter,
  onMapMouseLeave,
  cleanUpLiveStreaming,
  setLoadInitialDataTimeoutId,
  clearSelectedFeature,
  clearHoveredFeature,
  setLaneLabelsVisible,
  setSigGroupLabelsVisible,
  setShowPopupOnHover,
  setLiveDataActive,
  setBsmTrailLength,
  setTimeWindowSeconds,
  setRawData,
  setMapProps,
  togglePlaybackModeActive,
  resetMapView,
  setLiveDataRestartTimeoutId,
  setLiveDataRestart,
  centerMapOnPoint,
  handleNewMapMessageData,
  addInitialDataAbortController,
  addInitialDataAbortPromise,
  resetInitialDataAbortControllers,
  setSpatSignalGroups,
  setCurrentBsms,
  setMapRef,
  setDecoderModeEnabled,
  setAbortAllFutureRequests,
} = intersectionMapSlice.actions

export default intersectionMapSlice.reducer

import React from 'react'
import { createAction, createAsyncThunk, createSelector, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { selectToken } from '../../../generalSlices/userSlice'
import { IMessage, Client } from '@stomp/stompjs'
import MessageMonitorApi from '../../../apis/intersections/mm-api'
import EventsApi from '../../../apis/intersections/events-api'
import NotificationApi from '../../../apis/intersections/notification-api'
import toast from 'react-hot-toast'
import {
  addBsmTimestampsAndSortAscending,
  addMapTimestampsAndSortAscending,
  addSpatTimestampsAndSortAscending,
  generateSignalStateFeatureCollection,
  isValidDate,
  parseMapSignalGroups,
  parseSpatSignalGroups,
} from './utilities/message-utils'
import { generateColorDictionary, generateMapboxStyleExpression } from './utilities/colors'
import { setBsmCircleColor, setBsmLegendColors } from './map-layer-style-slice'
import { getTimeRangeDeciseconds } from './utilities/map-utils'
import { MapRef } from 'react-map-gl'
import { selectRsuMapData } from '../../../generalSlices/rsuSlice'
import EnvironmentVars from '../../../EnvironmentVars'
import { downloadAllData } from './utilities/file-utilities'
import { getTimestamp } from './map-component'
import { getAccurateTimeMillis, selectTimeOffsetMillis } from '../../../generalSlices/timeSyncSlice'
import { combineUrlPaths } from '../../../apis/intersections/api-helper-cviz'
import { ThunkAbortController } from './utilities/thunk-abort-contoller'
import {
  fetchSrmWithinTimeWindow,
  fetchSsmWithinTimeWindow,
  filterSrms,
  filterSsms,
  getTimeWindowFromRenderInterval,
} from '../../api/intersectionMapApiSlice'

export type MAP_LAYERS =
  | 'map-message'
  | 'map-message-labels'
  | 'connecting-lanes'
  | 'connecting-lanes-labels'
  | 'invalid-lane-collection'
  | 'bsm'
  | 'signal-states'
  | 'srm'
  | 'srm-requested-lanes'
  | 'ssm-connection-status'
  | 'ssm-connection-highlight'

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
  srmData: ProcessedSrmFeature[]
  ssmData: ProcessedSsm[]
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
        srm: ProcessedSrmFeature[]
        ssm: ProcessedSsm[]
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
  srm?: ProcessedSrmFeature[]
  ssm?: ProcessedSsm[]
  notification?: MessageMonitor.Notification
  event?: MessageMonitor.Event
  assessment?: Assessment
}

export type BSM_COUNTS_CHART_DATA = MessageMonitor.MinuteCount & {
  minutesAfterMidnight: number
  timestamp: string
}

const initialState = {
  mapRef: React.createRef() as React.MutableRefObject<MapRef>,
  layersVisible: {
    'map-message': true,
    'map-message-labels': false,
    'connecting-lanes': true,
    'connecting-lanes-labels': false,
    'invalid-lane-collection': true,
    'signal-states': true,
    bsm: true,
    srm: true,
    'srm-requested-lanes': true,
    'ssm-connection-status': true,
    'ssm-connection-highlight': true,
  } as Record<MAP_LAYERS, boolean>,
  allInteractiveLayerIds: [
    'map-message',
    'connecting-lanes',
    'signal-states',
    'bsm',
    'srm',
    'ssm-connection-status',
    'srm-requested-lanes',
    'ssm-connection-highlight',
  ] as MAP_LAYERS[],
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
  currentSsmData: [] as ProcessedSsm[],
  currentSrmData: [] as ProcessedSrmFeature[],
  surroundingEvents: [] as MessageMonitor.Event[],
  filteredSurroundingEvents: [] as MessageMonitor.Event[],
  surroundingNotifications: [] as MessageMonitor.Notification[],
  filteredSurroundingNotifications: [] as MessageMonitor.Notification[],
  bsmEventsByMinute: [] as BSM_COUNTS_CHART_DATA[],
  playbackModeActive: false,
  timeWindowSeconds: 10,
  sliderValueDeciseconds: 0,
  sliderTimeValue: {
    start: new Date(),
    end: new Date(),
  },
  lastSliderUpdate: undefined as number | undefined,
  renderTimeInterval: [0, 0],
  mapSpatTimes: { mapTime: 0, spatTime: 0 },
  sigGroupLabelsVisible: false,
  laneLabelsVisible: false,
  showPopupOnHover: false,
  importedMessageData: undefined as IMPORTED_MAP_MESSAGE_DATA | undefined,
  loadInitialDataTimeoutId: undefined as NodeJS.Timeout | undefined,
  wsClient: undefined as Client | undefined,
  wsSessionId: undefined as number | undefined,
  liveDataActive: false,
  currentMapData: [] as ProcessedMap[],
  currentSpatData: [] as ProcessedSpat[],
  bsmTrailLength: 20,
  liveDataRestart: -1,
  liveDataRestartTimeoutId: undefined as NodeJS.Timeout | undefined,
  pullInitialDataAbortControllers: [] as AbortController[],
  abortAllFutureRequests: false,
  decoderModeEnabled: false,
  liveSpatLatestLatencyMs: undefined as number | undefined,
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
  const offset = 1000 * 60 * 1

  switch (sourceDataType) {
    case 'notification': {
      const notification = source as MessageMonitor.Notification
      return {
        startDate: new Date(notification.notificationGeneratedAt - offset),
        endDate: new Date(notification.notificationGeneratedAt + offset),
        eventDate: new Date(notification.notificationGeneratedAt),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    case 'event': {
      const event = source as MessageMonitor.Event
      return {
        startDate: new Date(event.eventGeneratedAt - offset),
        endDate: new Date(event.eventGeneratedAt + offset),
        eventDate: new Date(event.eventGeneratedAt),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    case 'assessment': {
      const assessment = source as Assessment
      return {
        startDate: new Date(assessment.assessmentGeneratedAt - offset),
        endDate: new Date(assessment.assessmentGeneratedAt + offset),
        eventDate: new Date(assessment.assessmentGeneratedAt),
        vehicleId: undefined,
        isDefault: false,
      }
    }
    case 'timestamp': {
      const ts = (source as timestamp).timestamp
      return {
        startDate: new Date(ts - offset),
        endDate: new Date(ts + offset),
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
        startDate: new Date(Date.now() - offset),
        endDate: new Date(Date.now() + offset),
        eventDate: new Date(Date.now()),
        vehicleId: undefined,
        isDefault: true,
      }
  }
}

export const updateQueryParamsActionFromTimestamp = createAction(
  'intersectionMap/updateQueryParamsActionFromTimestamp',
  (args: { intersectionId: number; tsMillis: number }) => {
    const { intersectionId, tsMillis } = args
    const offset = 1000 * 60 * 1
    return {
      payload: {
        startDate: new Date(tsMillis - offset),
        endDate: new Date(tsMillis + offset),
        eventDate: new Date(tsMillis),
        vehicleId: undefined,
        intersectionId: intersectionId,
        isDefault: false,
      } as MAP_QUERY_PARAMS,
    }
  }
)

export const updateQueryParamsActionFromSpats = createAction(
  'intersectionMap/updateQueryParamsActionFromSpats',
  (args: { intersectionId: number; spats: ProcessedSpat[] }) => {
    const { intersectionId, spats } = args
    let startDate = undefined as number | undefined
    let endDate = undefined as number | undefined

    for (const spat of spats) {
      if (!startDate || spat.utcTimeStamp < startDate) {
        startDate = getTimestamp(spat.utcTimeStamp)
      }
      if (!endDate || getTimestamp(spat.utcTimeStamp) > endDate) {
        endDate = getTimestamp(spat.utcTimeStamp)
      }
    }
    return {
      payload: {
        startDate: new Date(startDate ?? Date.now()),
        endDate: new Date(endDate ?? Date.now() + 1),
        eventDate: new Date((startDate ?? Date.now()) / 2 + (endDate ?? Date.now() + 1) / 2),
        vehicleId: undefined,
        intersectionId: intersectionId,
        isDefault: false,
      } as MAP_QUERY_PARAMS,
    }
  }
)

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
      // Use source data loaded from decoder module
      const localSourceData = sourceData as {
        map: ProcessedMap[]
        spat: ProcessedSpat[]
        bsm: BsmFeatureCollection
        srm: ProcessedSrmFeature[]
        ssm: ProcessedSsm[]
      }
      rawMap = addMapTimestampsAndSortAscending(localSourceData.map)
      rawSpat = addSpatTimestampsAndSortAscending(localSourceData.spat)
      rawBsmGeojson = addBsmTimestampsAndSortAscending(localSourceData.bsm)
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
            updateQueryParamsActionFromSpats({ intersectionId: rawMap[0].properties.intersectionId, spats: rawSpat })
          )
        }
      }
    } else if (queryParams.isDefault == true) {
      // Retrieve latest SPAT to set time range, which will re-trigger pull initial data with new time range
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
          updateQueryParamsActionFromTimestamp({
            intersectionId: queryParams.intersectionId,
            tsMillis: getTimestamp(latestSpats.at(-1)?.utcTimeStamp),
          })
        )
        return
      } else {
        dispatch(
          updateQueryParamsActionFromTimestamp({
            intersectionId: queryParams.intersectionId,
            tsMillis: Date.now(),
          })
        )
        return
      }
    } else if (importedMessageData == undefined) {
      // Retrieve fresh data from Intersection API
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
      // Use imported message data
      rawMap = [...importedMessageData.mapData]
      rawSpat = [...importedMessageData.spatData].sort((a, b) => a.utcTimeStamp - b.utcTimeStamp)
      rawBsmGeojson = importedMessageData.bsmData
    }

    if (decoderModeEnabled) {
      // In decoder mode only, allow display of BSMs without MAP or SPaT data
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
        allMapData: rawMap,
        connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
        mapSignalGroups: mapSignalGroupsLocal,
        mapTime: latestMapMessage.properties.odeReceivedAt as unknown as number,
      })
    )

    if (importedMessageData == undefined && !decoderModeEnabled) {
      // Pull remaining data feeds from Intersection API
      if (selectAbortAllFutureRequests(getState() as RootState)) {
        return
      }

      dispatch(getBsmDailyCounts())
      dispatch(getSurroundingEvents())
      dispatch(getSurroundingNotifications())
      await fetchSsmWithinTimeWindow(queryParams, dispatch)
      await fetchSrmWithinTimeWindow(queryParams, dispatch)

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
    }

    // ######################### SPAT Signal Groups #########################
    const spatSignalGroupsLocal = parseSpatSignalGroups(rawSpat)
    dispatch(setSpatSignalGroups({ signalGroups: spatSignalGroupsLocal, spat: rawSpat }))

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
      rawBsmGeojson = addBsmTimestampsAndSortAscending({ type: 'FeatureCollection', features: await rawBsmPromise })
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
    args: {
      currentMapData: ProcessedMap[]
      currentSpatData: ProcessedSpat[]
      currentBsmData: BsmFeatureCollection
    },
    { getState, dispatch }
  ) => {
    const { currentMapData, currentSpatData, currentBsmData } = args
    const currentState = getState() as RootState

    const queryParams = selectQueryParams(currentState)
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
        allMapData: currentMapData,
        connectingLanes: latestMapMessage.connectingLanesFeatureCollection,
        mapSignalGroups: mapSignalGroupsLocal,
        mapTime: latestMapMessage.properties.odeReceivedAt as unknown as number,
      })
    )

    // ######################### SPAT Signal Groups #########################
    const spatSignalGroupsLocal = parseSpatSignalGroups(currentSpatData)
    dispatch(setSpatSignalGroups({ signalGroups: spatSignalGroupsLocal, spat: currentSpatData }))

    return {
      bsmData: currentBsmData,
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
    const mapRef = selectMapRef(currentState)

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
      mapRef.current.flyTo({
        center: [latestMapMessage?.properties.refPoint.longitude, latestMapMessage?.properties.refPoint.latitude],
        zoom: 19,
        duration: 100, //ms
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
      mapRef.current.flyTo({
        center: [latestMapMessage?.properties.refPoint.longitude, latestMapMessage?.properties.refPoint.latitude],
        zoom: 19,
        duration: 100, //ms
      })
    }
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
    const currentBsmData: BsmFeatureCollection = selectBsmData(currentState)

    const newBsmFeatureCollection = addBsmTimestampsAndSortAscending({
      type: 'FeatureCollection',
      features: newBsmData,
    })

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
    const { token, intersectionId, shouldResetMapView = true } = args
    // Connect to WebSocket when component mounts
    const currentState = getState() as RootState
    const liveDataActive = selectLiveDataActive(currentState)
    const wsClient = selectWsClient(currentState)
    const authToken = selectToken(currentState)
    const queryParams = selectQueryParams(currentState)
    let localWsClient = wsClient as Client | undefined

    dispatch(resetInitialDataAbortControllers())
    dispatch(setAbortAllFutureRequests(false))
    dispatch(onTimeQueryChanged({ eventTime: new Date(), timeBefore: 10, timeAfter: 0, timeWindowSeconds: 2 }))
    if (shouldResetMapView) dispatch(resetMapView())

    if (!liveDataActive) {
      console.debug('Not initializing live streaming because liveDataActive is false')
      return
    }
    if (wsClient != null) {
      wsClient.deactivate()
    }
    console.info('Live streaming data from Intersection API STOMP WebSocket endpoint')

    // Request initial SPaT and MAP data to default the view
    let abortController = new AbortController()
    dispatch(addInitialDataAbortController(abortController))
    MessageMonitorApi.getMapMessages({
      token: authToken,
      intersectionId: queryParams.intersectionId,
      latest: true,
      abortController,
    }).then((maps) => dispatch(renderIterative_Map(maps)))

    // Request latest SPaT data to handle deduplicated feed.
    // SPaT messages are sorted, so getting an older message
    // here after a newer message is received on the websocket won't cause any issues
    abortController = new AbortController()
    dispatch(addInitialDataAbortController(abortController))
    MessageMonitorApi.getSpatMessages({
      token: authToken,
      intersectionId: queryParams.intersectionId,
      latest: true,
      abortController,
    }).then((spats) => dispatch(renderIterative_Spat(spats)))

    const spatTopic = `/live/${intersectionId}/processed-spat`
    const mapTopic = `/live/${intersectionId}/processed-map`
    const bsmTopic = `/live/${intersectionId}/processed-bsm`

    const url = combineUrlPaths(EnvironmentVars.CVIZ_API_WS_URL, 'stomp')
    const sessionId = Math.floor(Math.random() * 1000000)

    localWsClient = new Client({
      webSocketFactory: () => {
        return new WebSocket(url, ['v10.stomp', 'v11.stomp', token])
      },
      onConnect: () => {
        console.log('Successfully connected to STOMP websocket', 'session ID:', sessionId)
        if (
          !selectLiveDataActive(getState() as RootState) ||
          selectWsSessionId(getState() as RootState) !== sessionId
        ) {
          console.debug('Cancelling re-connect because live data is no longer active or active session ID mismatch')
          localWsClient?.deactivate()
          return
        }
        try {
          localWsClient.subscribe(spatTopic, function (mes: IMessage) {
            const spatMessage: ProcessedSpat = JSON.parse(mes.body)
            const messageTime = getTimestamp(spatMessage.utcTimeStamp)
            const currentTimeMillis = getAccurateTimeMillis(selectTimeOffsetMillis(getState() as RootState))
            const messageLatencyMs = currentTimeMillis - messageTime
            console.debug(
              'Received SPaT message with age of ' +
                messageLatencyMs +
                'ms, clock offset: ' +
                selectTimeOffsetMillis(getState() as RootState) +
                'ms'
            )
            dispatch(renderIterative_Spat([spatMessage]))
            dispatch(setLiveSpatLatestLatencyMs(messageLatencyMs))
            dispatch(maybeUpdateSliderValue(currentTimeMillis))
          })
          localWsClient.subscribe(mapTopic, function (mes: IMessage) {
            const mapMessage: ProcessedMap = JSON.parse(mes.body)
            const messageTime = getTimestamp(mapMessage.properties.odeReceivedAt)
            const currentTimeMillis = getAccurateTimeMillis(selectTimeOffsetMillis(getState() as RootState))
            const messageLatencyMs = currentTimeMillis - messageTime
            console.debug(
              'Received MAP message with age of ' +
                messageLatencyMs +
                'ms, clock offset: ' +
                selectTimeOffsetMillis(getState() as RootState) +
                'ms'
            )
            dispatch(renderIterative_Map([mapMessage]))
            dispatch(maybeUpdateSliderValue(currentTimeMillis))
          })
          localWsClient.subscribe(bsmTopic, function (mes: IMessage) {
            const bsmData: ProcessedBsmFeature = JSON.parse(mes.body)
            const messageTime = getTimestamp(bsmData.properties.odeReceivedAt)
            const currentTimeMillis = getAccurateTimeMillis(selectTimeOffsetMillis(getState() as RootState))
            const messageLatencyMs = currentTimeMillis - messageTime
            console.debug(
              'Received BSM message with age of ' +
                messageLatencyMs +
                'ms, clock offset: ' +
                selectTimeOffsetMillis(getState() as RootState) +
                'ms'
            )
            dispatch(renderIterative_Bsm([bsmData]))
            dispatch(maybeUpdateSliderValue(currentTimeMillis))
          })
        } catch (error) {
          console.error('Error during subscription:', error, 'session ID:', sessionId)
        }

        // Request current SPaT and MAP data to ensure no messages were missed
        let abortController = new AbortController()
        dispatch(addInitialDataAbortController(abortController))
        MessageMonitorApi.getMapMessages({
          token: authToken,
          intersectionId: queryParams.intersectionId,
          latest: true,
          abortController,
        }).then((maps) => dispatch(renderIterative_Map(maps)))

        // Request latest SPaT data to handle deduplicated feed.
        // SPaT messages are sorted, so getting an older message
        // here after a newer message is received on the websocket won't cause any issues
        abortController = new AbortController()
        dispatch(addInitialDataAbortController(abortController))
        MessageMonitorApi.getSpatMessages({
          token: authToken,
          intersectionId: queryParams.intersectionId,
          latest: true,
          abortController,
        }).then((spats) => dispatch(renderIterative_Spat(spats)))
      },
    })

    localWsClient.debug = (msg) => console.debug('Live STOMP Websocket Debug Message:', msg)

    localWsClient.activate()

    async function forceReconnect() {
      console.info(`Forcing live data reconnect`, 'session ID:', sessionId)
      if (!selectLiveDataActive(getState() as RootState) || selectWsSessionId(getState() as RootState) !== sessionId) {
        dispatch(cleanUpLiveStreaming(false))
        localWsClient?.deactivate()
      } else {
        // TODO: revisit this
        // This is an imperfect solution to force a reconnect, but the stompjs library does not provide a better way
        // to do so this at the moment. Simply deactivating and reactivating the client did not work
        // The problem that occurs is that after ~6 minutes and around the 10th reconnect attempt,
        // the client is no longer able to reconnect unless the liveDataActive is toggled.
        localWsClient?.deactivate()
        dispatch(setLiveDataActive(false))
        setTimeout(() => dispatch(setLiveDataActive(true)), 3000)
      }
    }

    localWsClient.onStompError = (frame) => {
      console.error('Live Streaming STOMP ERROR', frame, 'session ID:', sessionId)
    }

    localWsClient.onWebSocketClose = (frame) => {
      console.error('Live Streaming STOMP WebSocket Close', frame, 'session ID:', sessionId)
    }

    localWsClient.onWebSocketError = (frame) => {
      // TODO: Consider restarting connection on error
      console.error('Live Streaming STOMP WebSocket Error', frame, 'session ID:', sessionId)
      forceReconnect()
    }

    return { wsClient: localWsClient, sessionId: sessionId }
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

    // Pre-process filtered SPAT keys
    const spatSignalGroupKeys = Object.keys(spatSignalGroups).map((key) => ({
      key: key,
      dtSeconds: Number(key) / 1000,
    }))

    // find closest SPAT signal group to the end of the render time interval and set that as the default signal group to render
    const lastSpatSignalGroupKeys = spatSignalGroupKeys.reduce((a, b) => (a.dtSeconds > b.dtSeconds ? a : b), {
      key: '',
      dtSeconds: 0,
    })
    let closestSignalGroup: { spat: SpatSignalGroup[]; dtSeconds: number } = {
      dtSeconds: lastSpatSignalGroupKeys.dtSeconds,
      spat: spatSignalGroups[lastSpatSignalGroupKeys.key],
    }

    // Iterate through SPAT signal groups to find closest prior to end of render interval
    for (const { key, dtSeconds } of spatSignalGroupKeys) {
      if (dtSeconds <= renderTimeInterval[1]) {
        if (
          Math.abs(dtSeconds - renderTimeInterval[1]) < Math.abs(closestSignalGroup.dtSeconds - renderTimeInterval[1])
        ) {
          closestSignalGroup = { dtSeconds: dtSeconds, spat: spatSignalGroups[key] }
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
      currentSignalGroups: closestSignalGroup.spat,
      signalStateData: generateSignalStateFeatureCollection(mapSignalGroups!, closestSignalGroup.spat),
      spatTime: closestSignalGroup.dtSeconds * 1000,
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
    const queryParams = selectQueryParams(currentState)
    const sourceData = selectSourceData(currentState)
    const sourceDataType = selectSourceDataType(currentState)

    const spatData = selectCurrentSpatData(currentState)
    const mapData = selectCurrentMapData(currentState)
    const bsmData = selectBsmData(currentState)
    const ssmData = selectCurrentSsmData(currentState)
    const srmData = selectCurrentSrmData(currentState)

    const rawData: any = {}
    rawData['spat'] = spatData
    rawData['map'] = mapData
    rawData['bsm'] = bsmData
    rawData['ssm'] = ssmData
    rawData['srm'] = srmData

    if (sourceDataType == 'notification') {
      rawData['notification'] = sourceData as MessageMonitor.Notification
    } else if (sourceDataType == 'event') {
      rawData['event'] = sourceData as MessageMonitor.Event
    } else if (sourceDataType == 'assessment') {
      rawData['assessment'] = sourceData as Assessment
    }

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
    handleImportedMapMessageData: (
      state,
      action: PayloadAction<{
        mapData: ProcessedMap[]
        bsmData: BsmFeatureCollection
        spatData: ProcessedSpat[]
        srmData: ProcessedSrmFeature[]
        ssmData: ProcessedSsm[]
        notificationData: any
      }>
    ) => {
      const { mapData, bsmData, spatData, srmData, ssmData, notificationData } = action.payload
      const sortedSpatData = spatData.sort((x, y) => x.utcTimeStamp - y.utcTimeStamp)
      const startTime = new Date(sortedSpatData[0].utcTimeStamp)
      const endTime = new Date(sortedSpatData[sortedSpatData.length - 1].utcTimeStamp)
      state.value.importedMessageData = { mapData, bsmData, spatData, srmData, ssmData, notificationData }
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
      state.value.timeWindowSeconds = 10
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
        if (action.payload.resetTimeWindow) state.value.timeWindowSeconds = 10
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
    cleanUpLiveStreaming: (state, action: PayloadAction<boolean>) => {
      const isRestart = action.payload ?? false
      if (state.value.wsClient) {
        state.value.wsClient.deactivate()
        state.value.wsClient = undefined
        state.value.wsSessionId = undefined
        console.debug('Successfully disconnected from STOMP endpoint')
      }
      if (!isRestart) {
        if (action.payload)
          if (state.value.liveDataRestartTimeoutId) {
            clearTimeout(state.value.liveDataRestartTimeoutId)
            state.value.liveDataRestartTimeoutId = undefined
          }
        state.value.timeWindowSeconds = 10
        state.value.liveDataActive = false
        state.value.liveDataRestart = -1
        state.value.liveSpatLatestLatencyMs = undefined
      }
    },
    setLoadInitialDataTimeoutId: (state, action: PayloadAction<NodeJS.Timeout>) => {
      state.value.loadInitialDataTimeoutId = action.payload
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
      state.value.currentSsmData = []
      state.value.currentSrmData = []
      state.value.mapData = undefined
      state.value.mapSpatTimes = { mapTime: 0, spatTime: 0 }
      state.value.sourceData = { map: [], spat: [], bsm: { type: 'FeatureCollection', features: [] }, srm: [], ssm: [] }
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
        allMapData: ProcessedMap[]
        connectingLanes: ConnectingLanesFeatureCollection
        mapSignalGroups: SignalStateFeatureCollection
        mapTime: number
      }>
    ) => {
      if (!action.payload) return
      state.value.mapData = action.payload.mapData
      state.value.currentMapData = action.payload.allMapData
      if (action.payload.mapData != null)
        state.value.mapRef.current.flyTo({
          center: [
            action.payload.mapData.properties.refPoint.longitude,
            action.payload.mapData.properties.refPoint.latitude,
          ],
          zoom: 19,
          duration: 100, //ms
        })
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
    setSpatSignalGroups: (state, action: PayloadAction<{ signalGroups: SpatSignalGroups; spat: ProcessedSpat[] }>) => {
      state.value.spatSignalGroups = action.payload.signalGroups
      state.value.currentSpatData = action.payload.spat
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
    setLayerVisibility: (state, action: PayloadAction<{ key: MAP_LAYERS; visible: boolean }>) => {
      state.value.layersVisible = {
        ...state.value.layersVisible,
        [action.payload.key]: action.payload.visible,
      }
    },
    setLayersVisible: (state, action: PayloadAction<Record<MAP_LAYERS, boolean>>) => {
      state.value.layersVisible = action.payload
    },
    setLiveSpatLatestLatencyMs: (state, action: PayloadAction<number | undefined>) => {
      state.value.liveSpatLatestLatencyMs = action.payload
    },
    setCurrentSsmData: (state, action: PayloadAction<ProcessedSsm[]>) => {
      state.value.currentSsmData = action.payload
    },
    setCurrentSrmData: (state, action: PayloadAction<ProcessedSrmFeature[]>) => {
      state.value.currentSrmData = action.payload
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
            sliderValueDeciseconds: number
          }>
        ) => {
          state.value.bsmData = action.payload.bsmData
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
            state.value.mapRef.current.flyTo({
              center: [
                action.payload.mapData.properties.refPoint.longitude,
                action.payload.mapData.properties.refPoint.latitude,
              ],
              zoom: 19,
              duration: 100, //ms
            })
          state.value.connectingLanes = action.payload.connectingLanes
          state.value.mapData = action.payload.mapData
          state.value.mapSignalGroups = action.payload.mapSignalGroups
          state.value.mapSpatTimes = { ...state.value.mapSpatTimes, mapTime: action.payload.mapTime }
        }
      )
      .addCase(
        renderIterative_Spat.fulfilled,
        (state, action: PayloadAction<{ signalGroups: SpatSignalGroups; raw: ProcessedSpat[] }>) => {
          state.value.spatSignalGroups = action.payload.signalGroups
          state.value.currentSpatData = action.payload.raw
        }
      )
      .addCase(renderIterative_Bsm.fulfilled, (state, action: PayloadAction<BsmFeatureCollection>) => {
        state.value.bsmData = action.payload
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
      .addCase(
        initializeLiveStreaming.fulfilled,
        (state, action: PayloadAction<{ wsClient: Client | undefined; sessionId: number | undefined }>) => {
          state.value.wsClient = action.payload.wsClient
          state.value.wsSessionId = action.payload.sessionId
        }
      )
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
      .addCase(updateQueryParamsActionFromTimestamp, (state, action: PayloadAction<MAP_QUERY_PARAMS>) => {
        state.value.queryParams = action.payload
      })
      .addCase(updateQueryParamsActionFromSpats, (state, action: PayloadAction<MAP_QUERY_PARAMS>) => {
        state.value.queryParams = action.payload
      })
  },
})

export const selectLoading = (state: RootState) => state.intersectionMap.loading

export const selectMapRef = (state: RootState) => state.intersectionMap.value.mapRef
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
export const selectCurrentSsmData = (state: RootState) => state.intersectionMap.value.currentSsmData
export const selectCurrentSrmData = (state: RootState) => state.intersectionMap.value.currentSrmData
export const selectSurroundingEvents = (state: RootState) => state.intersectionMap.value.surroundingEvents
export const selectFilteredSurroundingEvents = (state: RootState) =>
  state.intersectionMap.value.filteredSurroundingEvents
export const selectSurroundingNotifications = (state: RootState) => state.intersectionMap.value.surroundingNotifications
export const selectFilteredSurroundingNotifications = (state: RootState) =>
  state.intersectionMap.value.filteredSurroundingNotifications
export const selectBsmEventsByMinute = (state: RootState) => state.intersectionMap.value.bsmEventsByMinute
export const selectPlaybackModeActive = (state: RootState) => state.intersectionMap.value.playbackModeActive
export const selectTimeWindowSeconds = (state: RootState) => state.intersectionMap.value.timeWindowSeconds
export const selectSliderValueDeciseconds = (state: RootState) => state.intersectionMap.value.sliderValueDeciseconds
export const selectRenderTimeInterval = (state: RootState) => state.intersectionMap.value.renderTimeInterval
export const selectMapSpatTimes = (state: RootState) => state.intersectionMap.value.mapSpatTimes
export const selectSigGroupLabelsVisible = (state: RootState) => state.intersectionMap.value.sigGroupLabelsVisible
export const selectLaneLabelsVisible = (state: RootState) => state.intersectionMap.value.laneLabelsVisible
export const selectShowPopupOnHover = (state: RootState) => state.intersectionMap.value.showPopupOnHover
export const selectImportedMessageData = (state: RootState) => state.intersectionMap.value.importedMessageData
export const selectLoadInitialDataTimeoutId = (state: RootState) => state.intersectionMap.value.loadInitialDataTimeoutId
export const selectWsClient = (state: RootState) => state.intersectionMap.value.wsClient
export const selectWsSessionId = (state: RootState) => state.intersectionMap.value.wsSessionId
export const selectLiveDataActive = (state: RootState) => state.intersectionMap.value.liveDataActive
export const selectCurrentMapData = (state: RootState) => state.intersectionMap.value.currentMapData
export const selectCurrentSpatData = (state: RootState) => state.intersectionMap.value.currentSpatData
export const selectSliderTimeValue = (state: RootState) => state.intersectionMap.value.sliderTimeValue
export const selectBsmTrailLength = (state: RootState) => state.intersectionMap.value.bsmTrailLength
export const selectLiveDataRestartTimeoutId = (state: RootState) => state.intersectionMap.value.liveDataRestartTimeoutId
export const selectLiveDataRestart = (state: RootState) => state.intersectionMap.value.liveDataRestart
export const selectPullInitialDataAbortControllers = (state: RootState) =>
  state.intersectionMap.value.pullInitialDataAbortControllers
export const selectDecoderModeEnabled = (state: RootState) => state.intersectionMap.value.decoderModeEnabled
export const selectTimeFilterBsms = (state: RootState) => !state.intersectionMap.value.decoderModeEnabled
export const selectAbortAllFutureRequests = (state: RootState) => state.intersectionMap.value.abortAllFutureRequests
export const selectLiveSpatLatestLatencyMs = (state: RootState) => state.intersectionMap.value.liveSpatLatestLatencyMs

export const selectActiveSsmData = createSelector(
  [
    (state: RootState) => state.intersectionMap.value.currentSsmData,
    (state: RootState) => state.intersectionMap.value.renderTimeInterval,
  ],
  (currentSsmData, renderTimeInterval) => {
    const timeWindow = getTimeWindowFromRenderInterval(renderTimeInterval)
    return filterSsms(currentSsmData, timeWindow)
  }
)

export const selectActiveSrmData = createSelector(
  [
    (state: RootState) => state.intersectionMap.value.currentSrmData,
    (state: RootState) => state.intersectionMap.value.renderTimeInterval,
  ],
  (currentSrmData, renderTimeInterval) => {
    const timeWindow = getTimeWindowFromRenderInterval(renderTimeInterval)
    return filterSrms(currentSrmData, timeWindow)
  }
)

export const {
  setSurroundingEvents,
  maybeUpdateSliderValue,
  handleImportedMapMessageData,
  updateQueryParams,
  onTimeQueryChanged,
  setSliderValueDeciseconds,
  incrementSliderValue,
  updateRenderTimeInterval,
  cleanUpLiveStreaming,
  setLoadInitialDataTimeoutId,
  setLaneLabelsVisible,
  setSigGroupLabelsVisible,
  setShowPopupOnHover,
  setLiveDataActive,
  setBsmTrailLength,
  setTimeWindowSeconds,
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
  setLayerVisibility,
  setLayersVisible,
  setLiveSpatLatestLatencyMs,
  setCurrentSsmData,
  setCurrentSrmData,
} = intersectionMapSlice.actions

export default intersectionMapSlice.reducer

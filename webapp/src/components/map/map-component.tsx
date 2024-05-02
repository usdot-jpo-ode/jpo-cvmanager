import React, { useState, useEffect } from 'react'
import Map, { Source, Layer } from 'react-map-gl'

import { Container, Col } from 'reactstrap'

import { Paper, Box } from '@mui/material'

import ControlPanel from './control-panel'
import { SidePanel } from './side-panel'
import { CustomPopup } from './popup'
import { useDispatch, useSelector } from 'react-redux'
import { selectToken } from '../../generalSlices/userSlice'
import {
  selectBsmLayerStyle,
  selectConnectingLanesLabelsLayerStyle,
  selectConnectingLanesLayerStyle,
  selectMapMessageLabelsLayerStyle,
  selectMapMessageLayerStyle,
  selectMarkerLayerStyle,
  selectSignalStateLayerStyle,
} from './map-layer-style-slice'
import {
  MAP_PROPS,
  cleanUpLiveStreaming,
  clearHoveredFeature,
  clearSelectedFeature,
  incrementSliderValue,
  initializeLiveStreaming,
  onMapClick,
  onMapMouseEnter,
  onMapMouseLeave,
  onMapMouseMove,
  pullInitialData,
  selectAllInteractiveLayerIds,
  selectBsmData,
  selectConnectingLanes,
  selectCurrentBsms,
  selectCurrentSignalGroups,
  selectCursor,
  selectFilteredSurroundingEvents,
  selectFilteredSurroundingNotifications,
  selectHoveredFeature,
  selectLaneLabelsVisible,
  selectLiveDataActive,
  selectLoadInitialDataTimeoutId,
  selectMapData,
  selectMapSignalGroups,
  selectPlaybackModeActive,
  selectQueryParams,
  selectRenderTimeInterval,
  selectSelectedFeature,
  selectShowPopupOnHover,
  selectSigGroupLabelsVisible,
  selectSignalStateData,
  selectSliderValue,
  selectSpatSignalGroups,
  selectTimeWindowSeconds,
  selectViewState,
  setLoadInitialdataTimeoutId,
  setMapProps,
  setRawData,
  setViewState,
  updateQueryParams,
  updateRenderTimeInterval,
  updateRenderedMapState,
} from './map-slice'
import EnvironmentVars from '../../EnvironmentVars'
import { addConnections, createMarkerForNotification } from './utilities/message-utils'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { MapLegend } from './map-legend'
import { RsuInfo } from '../../apis/rsu-api-types'

const generateQueryParams = (source: MAP_PROPS['sourceData'], sourceDataType: MAP_PROPS['sourceDataType']) => {
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
    case 'rsu_ip':
      const rsu_info = source as RsuInfo['rsuList'][0]
      return {
        startDate: new Date(Date.now() - startOffset),
        endDate: new Date(Date.now() + endOffset),
        eventDate: new Date(Date.now()),
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

type timestamp = {
  timestamp: number
}

const MapTab = (props: MAP_PROPS) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  // userSlice
  const authToken = useSelector(selectToken)

  const mapMessageLayerStyle = useSelector(selectMapMessageLayerStyle)
  const mapMessageLabelsLayerStyle = useSelector(selectMapMessageLabelsLayerStyle)
  const connectingLanesLayerStyle = useSelector(selectConnectingLanesLayerStyle)
  const connectingLanesLabelsLayerStyle = useSelector(selectConnectingLanesLabelsLayerStyle)
  const markerLayerStyle = useSelector(selectMarkerLayerStyle)
  const bsmLayerStyle = useSelector(selectBsmLayerStyle)
  const signalStateLayerStyle = useSelector(selectSignalStateLayerStyle)

  const allInteractiveLayerIds = useSelector(selectAllInteractiveLayerIds)
  const queryParams = useSelector(selectQueryParams)
  const mapData = useSelector(selectMapData)
  const bsmData = useSelector(selectBsmData)
  const mapSignalGroups = useSelector(selectMapSignalGroups)
  const signalStateData = useSelector(selectSignalStateData)
  const spatSignalGroups = useSelector(selectSpatSignalGroups)
  const currentSignalGroups = useSelector(selectCurrentSignalGroups)
  const currentBsms = useSelector(selectCurrentBsms)
  const connectingLanes = useSelector(selectConnectingLanes)
  const filteredSurroundingEvents = useSelector(selectFilteredSurroundingEvents)
  const filteredSurroundingNotifications = useSelector(selectFilteredSurroundingNotifications)
  const viewState = useSelector(selectViewState)
  const timeWindowSeconds = useSelector(selectTimeWindowSeconds)
  const sliderValue = useSelector(selectSliderValue)
  const renderTimeInterval = useSelector(selectRenderTimeInterval)
  const hoveredFeature = useSelector(selectHoveredFeature)
  const selectedFeature = useSelector(selectSelectedFeature)
  const sigGroupLabelsVisible = useSelector(selectSigGroupLabelsVisible)
  const laneLabelsVisible = useSelector(selectLaneLabelsVisible)
  const showPopupOnHover = useSelector(selectShowPopupOnHover)
  const cursor = useSelector(selectCursor)
  const loadInitialDataTimeoutId = useSelector(selectLoadInitialDataTimeoutId)
  const liveDataActive = useSelector(selectLiveDataActive)
  const playbackModeActive = useSelector(selectPlaybackModeActive)

  const mapRef = React.useRef<any>(null)
  const [bsmTrailLength, setBsmTrailLength] = useState<number>(5)

  useEffect(() => {
    console.debug('SELECTED FEATURE', selectedFeature)
  }, [selectedFeature])

  useEffect(() => {
    dispatch(setMapProps(props))
  }, [props])

  // Increment sliderValue by 1 every second when playbackModeActive is true
  useEffect(() => {
    if (playbackModeActive) {
      const playbackPeriod = 100 //ms
      const playbackIncrement = Math.ceil(playbackPeriod / 100)
      const interval = setInterval(() => {
        dispatch(incrementSliderValue(playbackIncrement))
      }, 100)
      // Clear interval on component unmount
      return () => {
        clearInterval(interval)
      }
    }
    return () => {}
  }, [playbackModeActive])

  useEffect(() => {
    if (props.intersectionId != queryParams.intersectionId || props.roadRegulatorId != queryParams.roadRegulatorId) {
      dispatch(
        updateQueryParams({
          intersectionId: props.intersectionId,
          roadRegulatorId: props.roadRegulatorId,
        })
      )
      if (liveDataActive && authToken && props.roadRegulatorId && props.intersectionId) {
        cleanUpLiveStreaming()
        dispatch(
          initializeLiveStreaming({
            token: authToken,
            roadRegulatorId: props.roadRegulatorId,
            intersectionId: props.intersectionId,
          })
        )
      }
    }
  }, [props.intersectionId, props.roadRegulatorId])

  useEffect(() => {
    dispatch(
      updateQueryParams({
        ...generateQueryParams(props.sourceData, props.sourceDataType),
        intersectionId: props.intersectionId,
        roadRegulatorId: props.roadRegulatorId,
        resetTimeWindow: true,
      })
    )
  }, [props.sourceData])

  useEffect(() => {
    if (liveDataActive) {
      return
    }
    if (loadInitialDataTimeoutId) {
      console.log("Clearing 'Load Initial Data' timeout")
      clearTimeout(loadInitialDataTimeoutId)
    }
    const timeoutId = setTimeout(() => {
      console.log('Loading Initial Data')
      dispatch(pullInitialData())
    }, 500)
    dispatch(setLoadInitialdataTimeoutId(timeoutId))
  }, [queryParams])

  useEffect(() => {
    if (!mapSignalGroups || !spatSignalGroups) {
      console.debug('BSM Loading: No map or SPAT data', mapSignalGroups, spatSignalGroups)
      return
    }

    dispatch(updateRenderedMapState())
  }, [bsmData, mapSignalGroups, renderTimeInterval, spatSignalGroups])

  useEffect(() => {
    dispatch(updateRenderTimeInterval())
  }, [sliderValue, queryParams, timeWindowSeconds])

  useEffect(() => {
    if (liveDataActive) {
      if (authToken && props.roadRegulatorId && props.intersectionId) {
        dispatch(
          initializeLiveStreaming({
            token: authToken,
            roadRegulatorId: props.roadRegulatorId,
            intersectionId: props.intersectionId,
          })
        )
        if (bsmTrailLength > 15) setBsmTrailLength(5)
      } else {
        console.error(
          'Did not attempt to update notifications. Access token:',
          authToken,
          'Intersection ID:',
          props.intersectionId,
          'Road Regulator ID:',
          props.roadRegulatorId
        )
      }
    } else {
      if (bsmTrailLength < 15) setBsmTrailLength(20)
      dispatch(cleanUpLiveStreaming())
    }
  }, [liveDataActive])

  return (
    <Container style={{ width: '100%', height: '100%', display: 'flex' }}>
      <Col className="mapContainer" style={{ overflow: 'hidden', width: '100%', height: '100%', position: 'relative' }}>
        <div
          style={{
            padding: '0px 0px 6px 12px',
            marginTop: '6px',
            marginLeft: '35px',
            position: 'absolute',
            zIndex: 10,
            top: 0,
            left: 0,
            width: 1200,
            // width: 'calc(100% - 500px)',
            borderRadius: '4px',
            fontSize: '16px',
            maxHeight: 'calc(100vh - 120px)',
            overflow: 'auto',
            scrollBehavior: 'auto',
          }}
        >
          <Box style={{ position: 'relative' }}>
            <Paper sx={{ pt: 1, pb: 1, opacity: 0.85 }}>
              <ControlPanel />
            </Paper>
          </Box>
        </div>
        <div
          style={{
            padding: '0px 0px 6px 12px',
            position: 'absolute',
            zIndex: 9,
            bottom: 0,
            left: 0,
            fontSize: '16px',
            overflow: 'auto',
            scrollBehavior: 'auto',
            width: '100%',
          }}
        >
          <Box style={{ position: 'relative' }}>
            <MapLegend />
          </Box>
        </div>

        <Map
          {...viewState}
          ref={mapRef}
          onLoad={() => {}}
          mapStyle={EnvironmentVars.CVIZ_MAPBOX_STYLE_URL}
          mapboxAccessToken={EnvironmentVars.CVIZ_MAPBOX_TOKEN}
          attributionControl={true}
          customAttribution={['<a href="https://www.cotrip.com/" target="_blank">© CDOT</a>']}
          styleDiffing
          style={{ width: '100%', height: '100%' }}
          onMove={(evt) => dispatch(setViewState(evt.viewState))}
          onClick={(e) => dispatch(onMapClick({ event: { point: e.point, lngLat: e.lngLat }, mapRef }))}
          interactiveLayerIds={allInteractiveLayerIds}
          cursor={cursor}
          onMouseMove={(e) => dispatch(onMapMouseMove({ features: e.features, lngLat: e.lngLat }))}
          onMouseEnter={(e) => dispatch(onMapMouseEnter({ features: e.features, lngLat: e.lngLat }))}
          onMouseLeave={(e) => dispatch(onMapMouseLeave())}
        >
          <Source
            type="geojson"
            data={
              mapData?.mapFeatureCollection ?? {
                type: 'FeatureCollection' as 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...mapMessageLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (laneLabelsVisible ? mapData?.mapFeatureCollection : undefined) ?? {
                type: 'FeatureCollection' as 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...mapMessageLabelsLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (connectingLanes &&
                currentSignalGroups &&
                mapData?.mapFeatureCollection &&
                addConnections(connectingLanes, currentSignalGroups, mapData.mapFeatureCollection)) ?? {
                type: 'FeatureCollection' as 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...connectingLanesLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (connectingLanes && currentSignalGroups && sigGroupLabelsVisible && mapData?.mapFeatureCollection
                ? addConnections(connectingLanes, currentSignalGroups, mapData.mapFeatureCollection)
                : undefined) ?? {
                type: 'FeatureCollection' as 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...connectingLanesLabelsLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (mapData && props.sourceData && props.sourceDataType == 'notification'
                ? createMarkerForNotification(
                    [0, 0],
                    props.sourceData as MessageMonitor.Notification,
                    mapData.mapFeatureCollection
                  )
                : undefined) ?? {
                type: 'FeatureCollection' as 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...markerLayerStyle} />
            <Source
              type="geojson"
              data={
                currentBsms ?? {
                  type: 'FeatureCollection' as 'FeatureCollection',
                  features: [],
                }
              }
            >
              <Layer {...bsmLayerStyle} />
            </Source>
          </Source>
          <Source
            type="geojson"
            data={
              (connectingLanes && currentSignalGroups ? signalStateData : undefined) ?? {
                type: 'FeatureCollection' as 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...signalStateLayerStyle} />
          </Source>
          {/* <Source type="geojson" data={srmData}>
            <Layer {...srmLayerStyle} />
          </Source> */}
          {selectedFeature && (
            <CustomPopup selectedFeature={selectedFeature} onClose={() => dispatch(clearSelectedFeature())} />
          )}
          {showPopupOnHover && hoveredFeature && !selectedFeature && (
            <CustomPopup selectedFeature={hoveredFeature} onClose={() => dispatch(clearHoveredFeature())} />
          )}
        </Map>
        <SidePanel
          laneInfo={connectingLanes}
          signalGroups={currentSignalGroups}
          bsms={currentBsms}
          events={filteredSurroundingEvents}
          notifications={filteredSurroundingNotifications}
          sourceData={props.sourceData}
          sourceDataType={props.sourceDataType}
        />
      </Col>
    </Container>
  )
}

export default MapTab

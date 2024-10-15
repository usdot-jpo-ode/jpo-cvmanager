import React, { useState, useEffect } from 'react'
import Map, { Source, Layer, MapRef } from 'react-map-gl'

import { Container, Col } from 'reactstrap'

import { Paper, Box } from '@mui/material'

import ControlPanel from './control-panel'
import { SidePanel } from './side-panel'
import { CustomPopup } from './popup'
import { selectToken } from '../../../generalSlices/userSlice'
import {
  selectBsmLayerStyle,
  selectConnectingLanesLabelsLayerStyle,
  selectConnectingLanesLayerStyle,
  selectMapMessageLabelsLayerStyle,
  selectMapMessageLayerStyle,
  selectMarkerLayerStyle,
  selectSignalStateLayerStyle,
  selectSrmLayerStyle,
} from './map-layer-style-slice'
import {
  MAP_PROPS,
  cleanUpLiveStreaming,
  clearHoveredFeature,
  clearSelectedFeature,
  generateQueryParams,
  incrementSliderValue,
  initializeLiveStreaming,
  onMapClick,
  onMapMouseEnter,
  onMapMouseLeave,
  onMapMouseMove,
  pullInitialData,
  renderRsuData,
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
  selectLiveDataRestart,
  selectLiveDataRestartTimeoutId,
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
import EnvironmentVars from '../../../EnvironmentVars'
import { addConnections, createMarkerForNotification } from './utilities/message-utils'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { MapLegend } from './map-legend'
import { selectSelectedSrm } from '../../../generalSlices/rsuSlice'
import mbStyle from '../../../styles/intersectionMapStyle.json'
import { useAppDispatch, useAppSelector } from '../../../hooks'

export const getTimestamp = (dt: any): number => {
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

type timestamp = {
  timestamp: number
}

const IntersectionMap = (props: MAP_PROPS) => {
  const dispatch = useAppDispatch()

  // userSlice
  const authToken = useAppSelector(selectToken)

  const mapMessageLayerStyle = useAppSelector(selectMapMessageLayerStyle)
  const mapMessageLabelsLayerStyle = useAppSelector(selectMapMessageLabelsLayerStyle)
  const connectingLanesLayerStyle = useAppSelector(selectConnectingLanesLayerStyle)
  const connectingLanesLabelsLayerStyle = useAppSelector(selectConnectingLanesLabelsLayerStyle)
  const markerLayerStyle = useAppSelector(selectMarkerLayerStyle)
  const srmLayerStyle = useAppSelector(selectSrmLayerStyle)
  const bsmLayerStyle = useAppSelector(selectBsmLayerStyle)
  const signalStateLayerStyle = useAppSelector(selectSignalStateLayerStyle)

  const selectedSrm = useAppSelector(selectSelectedSrm)

  const allInteractiveLayerIds = useAppSelector(selectAllInteractiveLayerIds)
  const queryParams = useAppSelector(selectQueryParams)
  const mapData = useAppSelector(selectMapData)
  const bsmData = useAppSelector(selectBsmData)
  const mapSignalGroups = useAppSelector(selectMapSignalGroups)
  const signalStateData = useAppSelector(selectSignalStateData)
  const spatSignalGroups = useAppSelector(selectSpatSignalGroups)
  const currentSignalGroups = useAppSelector(selectCurrentSignalGroups)
  const currentBsms = useAppSelector(selectCurrentBsms)
  const connectingLanes = useAppSelector(selectConnectingLanes)
  const filteredSurroundingEvents = useAppSelector(selectFilteredSurroundingEvents)
  const filteredSurroundingNotifications = useAppSelector(selectFilteredSurroundingNotifications)
  const viewState = useAppSelector(selectViewState)
  const timeWindowSeconds = useAppSelector(selectTimeWindowSeconds)
  const sliderValue = useAppSelector(selectSliderValue)
  const renderTimeInterval = useAppSelector(selectRenderTimeInterval)
  const hoveredFeature = useAppSelector(selectHoveredFeature)
  const selectedFeature = useAppSelector(selectSelectedFeature)
  const sigGroupLabelsVisible = useAppSelector(selectSigGroupLabelsVisible)
  const laneLabelsVisible = useAppSelector(selectLaneLabelsVisible)
  const showPopupOnHover = useAppSelector(selectShowPopupOnHover)
  const cursor = useAppSelector(selectCursor)
  const loadInitialDataTimeoutId = useAppSelector(selectLoadInitialDataTimeoutId)
  const liveDataActive = useAppSelector(selectLiveDataActive)
  const playbackModeActive = useAppSelector(selectPlaybackModeActive)
  const liveDataRestartTimeoutId = useAppSelector(selectLiveDataRestartTimeoutId)
  const liveDataRestart = useAppSelector(selectLiveDataRestart)

  const mapRef = React.useRef<MapRef>(null)
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
      clearTimeout(loadInitialDataTimeoutId)
    }
    const timeoutId = setTimeout(() => dispatch(pullInitialData()), 500)
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
        setRawData({})
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

  useEffect(() => {
    if (liveDataRestart != -1 && liveDataRestart < 5 && liveDataActive) {
      if (authToken && props.roadRegulatorId && props.intersectionId) {
        dispatch(
          initializeLiveStreaming({
            token: authToken,
            roadRegulatorId: props.roadRegulatorId,
            intersectionId: props.intersectionId,
            numRestarts: liveDataRestart,
          })
        )
      }
    } else {
      dispatch(cleanUpLiveStreaming())
    }
  }, [liveDataRestart])

  useEffect(() => {
    const map = mapRef.current?.getMap()
    if (!map) return
    const images = [
      'traffic-light-icon-unknown',
      'traffic-light-icon-red-flashing',
      'traffic-light-icon-red-1',
      'traffic-light-icon-yellow-red-1',
      'traffic-light-icon-green-1',
      'traffic-light-icon-yellow-1',
    ]
    for (const image_name of images) {
      map.loadImage(`/icons/${image_name}.png`, (error, image) => {
        if (error) throw error
        if (!map.hasImage(image_name)) map.addImage(image_name, image, { sdf: true })
      })
    }
  }, [mapRef])

  return (
    <Container style={{ width: '100%', height: '100%', display: 'flex', padding: 0 }}>
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
          mapStyle={mbStyle as mapboxgl.Style}
          mapboxAccessToken={EnvironmentVars.MAPBOX_TOKEN}
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
          onLoad={(e: mapboxgl.MapboxEvent<undefined>) => {
            const map = e.target
            if (!map) return
            const images = [
              'traffic-light-icon-unknown',
              'traffic-light-icon-red-flashing',
              'traffic-light-icon-red-1',
              'traffic-light-icon-yellow-red-1',
              'traffic-light-icon-green-1',
              'traffic-light-icon-yellow-1',
            ]
            for (const image_name of images) {
              map.loadImage(`/icons/${image_name}.png`, (error, image) => {
                if (error) throw error
                if (!map.hasImage(image_name)) map.addImage(image_name, image)
              })
            }
          }}
        >
          <Source type="geojson" data={mapData?.mapFeatureCollection ?? { type: 'FeatureCollection', features: [] }}>
            <Layer {...mapMessageLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (connectingLanes &&
                currentSignalGroups &&
                mapData?.mapFeatureCollection &&
                addConnections(connectingLanes, currentSignalGroups, mapData.mapFeatureCollection)) ?? {
                type: 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...connectingLanesLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              {
                type: 'FeatureCollection',
                features: selectedSrm?.map((srm) => {
                  return {
                    type: 'Feature',
                    geometry: {
                      type: 'Point',
                      coordinates: [srm.long, srm.lat],
                    },
                    properties: {
                      requestId: srm.requestId,
                      requestedId: srm.requestedId,
                      status: srm.status,
                      time: srm.time,
                      role: srm.role,
                    },
                  }
                }),
              } as GeoJSON.FeatureCollection<GeoJSON.Point>
            }
          >
            <Layer {...srmLayerStyle} />
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
                : undefined) ?? { type: 'FeatureCollection', features: [] }
            }
          >
            <Layer {...markerLayerStyle} />
          </Source>
          <Source type="geojson" data={currentBsms ?? { type: 'FeatureCollection', features: [] }}>
            <Layer {...bsmLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (connectingLanes && currentSignalGroups ? signalStateData : undefined) ?? {
                type: 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...signalStateLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (laneLabelsVisible ? mapData?.mapFeatureCollection : undefined) ?? {
                type: 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...mapMessageLabelsLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              (connectingLanes && currentSignalGroups && sigGroupLabelsVisible && mapData?.mapFeatureCollection
                ? addConnections(connectingLanes, currentSignalGroups, mapData.mapFeatureCollection)
                : undefined) ?? { type: 'FeatureCollection', features: [] }
            }
          >
            <Layer {...connectingLanesLabelsLayerStyle} />
          </Source>
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

export default IntersectionMap

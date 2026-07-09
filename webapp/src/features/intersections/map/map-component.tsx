import React, { useState, useEffect, useMemo } from 'react'
import Map, { Source, Layer, MapRef } from 'react-map-gl'

import { Container, Col } from 'reactstrap'

import { Paper, Box, Fab, useTheme, FormControl, InputLabel, Select, MenuItem } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'

import ControlPanel from './control-panel'
import { SidePanel } from './map-info'
import { CustomPopup } from './popup'
import { selectToken } from '../../../generalSlices/userSlice'
import {
  selectBsmLayerStyle,
  selectConnectingLanesHighlightLayerStyle,
  selectConnectingLanesLabelsLayerStyle,
  selectConnectingLanesLayerStyle,
  selectConnectingLanesSsmStatusLayerStyle,
  selectMapMessageHighlightLayerStyle,
  selectMapMessageLabelsLayerStyle,
  selectMapMessageLayerStyle,
  selectMarkerLayerStyle,
  selectSignalStateLayerStyle,
  selectSrmLayerStyle,
  setBsmLegendColors,
  setSrmCircleColor,
  setSrmLegendColors,
} from './map-layer-style-slice'
import {
  MAP_PROPS,
  addInitialDataAbortPromise,
  cleanUpLiveStreaming,
  generateQueryParams,
  incrementSliderValue,
  initializeLiveStreaming,
  pullInitialData,
  resetInitialDataAbortControllers,
  selectActiveSrmData,
  selectActiveSsmData,
  selectAllInteractiveLayerIds,
  selectBsmData,
  selectConnectingLanes,
  selectCurrentBsms,
  selectCurrentSignalGroups,
  selectDecoderModeEnabled,
  selectFilteredSurroundingEvents,
  selectFilteredSurroundingNotifications,
  selectLaneLabelsVisible,
  selectLiveDataActive,
  selectLiveDataRestart,
  selectLoadInitialDataTimeoutId,
  selectMapData,
  selectMapSignalGroups,
  selectPlaybackModeActive,
  selectQueryParams,
  selectRenderTimeInterval,
  selectShowPopupOnHover,
  selectSigGroupLabelsVisible,
  selectSignalStateData,
  selectSliderValueDeciseconds,
  selectSpatSignalGroups,
  selectTimeWindowSeconds,
  setLoadInitialDataTimeoutId,
  setMapProps,
  setMapRef,
  updateQueryParams,
  updateRenderTimeInterval,
  updateRenderedMapState,
  MAP_LAYERS,
  selectLayersVisible,
} from './map-slice'
import EnvironmentVars from '../../../EnvironmentVars'
import {
  addConnections,
  createMarkerForNotification,
  addSsmStatus,
  addSsmSrmToConnections,
  addSsmSrmToMapFeatures,
} from './utilities/message-utils'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { MapLegend } from './map-legend'
import mbStyle from '../../../styles/intersectionMapStyle.json'
import DecoderEntryDialog from '../decoder/decoder-entry-dialog'
import { useLocation } from 'react-router-dom'
import { Remove } from '@mui/icons-material'
import VisualSettings from './visual-settings'
import { useDispatch, useSelector } from 'react-redux'
import LayerMenu from './layer-menu'
import { generateColorDictionary, generateMapboxStyleExpression } from './utilities/colors'

/**
 *  Converts a date string or timestamp to a timestamp in milliseconds since epoch.
 * @param dt - Date or timestamp to be converted - can be a string, seconds since epoch, or milliseconds since epoch
 * @returns timestamp in milliseconds since epoch
 */
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

const EMPTY_FEATURE_COLLECTION = { type: 'FeatureCollection' as const, features: [] }

const IntersectionMap = (props: MAP_PROPS) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const location = useLocation()
  const theme = useTheme()

  // userSlice
  const authToken = useSelector(selectToken)

  const layersVisible = useSelector(selectLayersVisible)
  const mapMessageLayerStyle = useSelector(selectMapMessageLayerStyle)
  const mapMessageHighlightLayerStyle = useSelector(selectMapMessageHighlightLayerStyle)
  const mapMessageLabelsLayerStyle = useSelector(selectMapMessageLabelsLayerStyle)
  const connectingLanesLayerStyle = useSelector(selectConnectingLanesLayerStyle)
  const connectingLanesSsmStatusLayerStyle = useSelector(selectConnectingLanesSsmStatusLayerStyle)
  const connectingLanesHighlightLayerStyle = useSelector(selectConnectingLanesHighlightLayerStyle)
  const connectingLanesLabelsLayerStyle = useSelector(selectConnectingLanesLabelsLayerStyle)
  const markerLayerStyle = useSelector(selectMarkerLayerStyle)
  const srmLayerStyle = useSelector(selectSrmLayerStyle)
  const bsmLayerStyle = useSelector(selectBsmLayerStyle)
  const signalStateLayerStyle = useSelector(selectSignalStateLayerStyle)

  const activeSsmData = useSelector(selectActiveSsmData)
  const activeSrmData = useSelector(selectActiveSrmData)

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
  const timeWindowSeconds = useSelector(selectTimeWindowSeconds)
  const sliderValueDeciseconds = useSelector(selectSliderValueDeciseconds)
  const renderTimeInterval = useSelector(selectRenderTimeInterval)
  const showPopupOnHover = useSelector(selectShowPopupOnHover)
  const loadInitialDataTimeoutId = useSelector(selectLoadInitialDataTimeoutId)
  const liveDataActive = useSelector(selectLiveDataActive)
  const playbackModeActive = useSelector(selectPlaybackModeActive)
  const liveDataRestart = useSelector(selectLiveDataRestart)
  const decoderModeEnabled = useSelector(selectDecoderModeEnabled)

  const mapRef = React.useRef<MapRef>(null)
  const [viewState, setViewState] = useState({
    latitude: 39.587905,
    longitude: -105.0907089,
    zoom: 11,
  })
  const [bsmTrailLength, setBsmTrailLength] = useState<number>(5)
  const [openPanel, setOpenPanel] = useState<string>('')
  const [cursor, setCursor] = useState<string>('default')
  const [hoveredFeature, setHoveredFeature] = useState<any>(undefined)
  const [selectedFeature, setSelectedFeature] = useState<any>(undefined)

  useEffect(() => {
    return () => {
      dispatch(resetInitialDataAbortControllers())
    }
  }, [location.pathname, dispatch])

  useEffect(() => {
    dispatch(setMapProps(props))
  }, [props])

  // Increment selectSliderValueDeciseconds by 1 every second when playbackModeActive is true
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
    if (props.intersectionId != queryParams.intersectionId) {
      dispatch(
        updateQueryParams({
          intersectionId: props.intersectionId,
        })
      )
      if (liveDataActive && authToken && props.intersectionId) {
        dispatch(cleanUpLiveStreaming())
        dispatch(
          initializeLiveStreaming({
            token: authToken,
            intersectionId: props.intersectionId,
          })
        )
      }
    }
  }, [props.intersectionId])

  useEffect(() => {
    dispatch(
      updateQueryParams({
        ...generateQueryParams(props.sourceData, props.sourceDataType, decoderModeEnabled),
        intersectionId: props.intersectionId,
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
    const timeoutId = setTimeout(() => dispatch(addInitialDataAbortPromise(dispatch(pullInitialData()))), 500)
    dispatch(setLoadInitialDataTimeoutId(timeoutId))
  }, [queryParams])

  useEffect(() => {
    dispatch(updateRenderedMapState())
  }, [bsmData, mapSignalGroups, renderTimeInterval, spatSignalGroups])

  useEffect(() => {
    if (!liveDataActive) {
      dispatch(updateRenderTimeInterval())
    }
  }, [sliderValueDeciseconds, queryParams, timeWindowSeconds])

  useEffect(() => {
    if (liveDataActive) {
      if (authToken && props.intersectionId) {
        dispatch(
          initializeLiveStreaming({
            token: authToken,
            intersectionId: props.intersectionId,
          })
        )
        if (bsmTrailLength > 15) setBsmTrailLength(5)
      } else {
        console.error(
          'Did not attempt to update notifications. Access token missing:',
          authToken == null || authToken == undefined,
          'Intersection ID:',
          props.intersectionId
        )
      }
    } else {
      if (bsmTrailLength < 15) setBsmTrailLength(20)
      dispatch(cleanUpLiveStreaming())
    }
  }, [liveDataActive])

  useEffect(() => {
    if (liveDataRestart != -1 && liveDataRestart < 5 && liveDataActive) {
      if (authToken && props.intersectionId) {
        dispatch(
          initializeLiveStreaming({
            token: authToken,
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
    if (mapRef.current) dispatch(setMapRef(mapRef))
  }, [mapRef])

  const activeSrmFeatureCollection = useMemo(() => {
    const srmFeatures = addSsmStatus(activeSrmData, activeSsmData)

    // Generate and set SRM colors and layer style
    const uniqueIds = new Set(srmFeatures.map((bsm) => bsm.properties?.vehicleID))
    // generate equally spaced unique colors for each uniqueId
    const colors = generateColorDictionary(uniqueIds)
    dispatch(setSrmLegendColors(colors))
    // add color to each feature
    const srmLayerStyle = generateMapboxStyleExpression(colors, 'vehicleID')
    dispatch(setSrmCircleColor(srmLayerStyle))
    return {
      type: 'FeatureCollection' as const,
      features: srmFeatures,
    }
  }, [activeSrmData, activeSsmData])

  const [connectingLanesFeatureCollection, connectingLanesOnlyWithSsmSrmFeatureCollection] = useMemo(() => {
    let connections: ConnectingLanesFeatureCollectionWithSignalState = EMPTY_FEATURE_COLLECTION
    if (connectingLanes && currentSignalGroups && mapData?.mapFeatureCollection) {
      connections = addConnections(connectingLanes, currentSignalGroups, mapData.mapFeatureCollection)
    }
    let srmSsmConnections = addSsmSrmToConnections(connections, activeSsmData, activeSrmData)
    const srmSsmOnlyConnections = {
      ...srmSsmConnections,
      features: srmSsmConnections.features
        .filter((feature) => feature.properties.signalStatuses.length > 0)
        .map((feature) => {
          const ssmInfo = feature.properties.signalStatuses[0] // already sorted with most relevant first
          return {
            ...feature,
            properties: {
              ...feature.properties,
              ssmStatus: ssmInfo ? ssmInfo.status : 'UNKNOWN',
            },
          }
        }),
    }
    return [srmSsmConnections, srmSsmOnlyConnections]
  }, [connectingLanes, currentSignalGroups, mapData, activeSsmData, activeSrmData])

  const [mapLanesFeatureCollection, mapLanesFeatureCollectionOnlyWithSsmSrm] = useMemo(() => {
    let mapFeatures: MapFeatureCollectionWithSsmSrm = EMPTY_FEATURE_COLLECTION
    if (mapData?.mapFeatureCollection) {
      // Does not require SRM data, that is optional
      mapFeatures = addSsmSrmToMapFeatures(mapData?.mapFeatureCollection, activeSsmData, activeSrmData)
    }
    const srmSsmOnlyMapFeatures = {
      ...mapFeatures,
      features: mapFeatures.features.filter((feature) => feature.properties.signalRequests.length > 0),
    }
    return [mapFeatures, srmSsmOnlyMapFeatures]
  }, [mapData, activeSsmData, activeSrmData])

  const notificationMarkerFeatureCollection = useMemo(() => {
    let notificationFeatures = EMPTY_FEATURE_COLLECTION
    if (mapData && props.sourceData && props.sourceDataType == 'notification') {
      notificationFeatures = createMarkerForNotification(
        [0, 0],
        props.sourceData as MessageMonitor.Notification,
        mapData.mapFeatureCollection
      )
    }
    return notificationFeatures
  }, [mapData, props.sourceData, props.sourceDataType])

  const spatSignalHeadFeatures = useMemo(() => {
    let signalHeadFeatures = EMPTY_FEATURE_COLLECTION
    if (connectingLanes && currentSignalGroups) {
      signalHeadFeatures = signalStateData
    }
    return signalHeadFeatures
  }, [connectingLanes, currentSignalGroups, signalStateData])

  const onMapClick = (point: mapboxgl.Point, lngLat: mapboxgl.LngLat) => {
    const features = mapRef.current.queryRenderedFeatures(point, {
      // layers: state.value.allInteractiveLayerIds,
    })
    const feature = features?.[0]
    if (feature && allInteractiveLayerIds.includes(feature.layer.id as MAP_LAYERS)) {
      setSelectedFeature({ clickedLocation: lngLat, feature })
    } else {
      setSelectedFeature(undefined)
    }
  }

  const onMapMouseMove = (features: mapboxgl.MapboxGeoJSONFeature[] | undefined, lngLat: mapboxgl.LngLat) => {
    const feature = features?.[0]
    if (feature && allInteractiveLayerIds.includes(feature.layer.id as MAP_LAYERS)) {
      setHoveredFeature({ clickedLocation: lngLat, feature })
    }
  }

  const onMapMouseEnter = (features: mapboxgl.MapboxGeoJSONFeature[] | undefined, lngLat: mapboxgl.LngLat) => {
    setCursor('pointer')
    const feature = features?.[0]
    if (feature && allInteractiveLayerIds.includes(feature.layer.id as MAP_LAYERS)) {
      setHoveredFeature({ clickedLocation: lngLat, feature })
    } else {
      setHoveredFeature(undefined)
    }
  }

  const onMapMouseLeave = () => {
    setCursor('default')
    setHoveredFeature(undefined)
  }

  useEffect(() => {
    const map = mapRef.current?.getMap()
    if (!map?.isStyleLoaded()) return

    Object.entries(layersVisible).forEach(([layerKey, isVisible]) => {
      if (map.getLayer(layerKey)) {
        map.setLayoutProperty(layerKey, 'visibility', isVisible ? 'visible' : 'none')
      }
    })
  }, [layersVisible])

  return (
    <Container style={{ width: '100%', height: '100%', display: 'flex', padding: 0 }}>
      <Col className="mapContainer" style={{ overflow: 'hidden', width: '100%', height: '100%', position: 'relative' }}>
        <div
          style={{
            position: 'absolute',
            zIndex: 10,
            top: theme.spacing(3),
            left: theme.spacing(3),
            width: '600px',
            maxHeight: 'calc(100vh - 240px)',
            overflow: 'auto',
            scrollBehavior: 'auto',
          }}
        >
          <Box style={{ position: 'relative' }}>
            <Paper sx={{ py: 1, backgroundColor: 'transparent' }}>
              <ControlPanel />
            </Paper>
          </Box>
        </div>
        <Fab
          color="primary"
          id="plus-button"
          sx={{
            position: 'absolute',
            zIndex: 10,
            top: theme.spacing(10),
            right: theme.spacing(3),
            '&:hover': {
              backgroundColor: theme.palette.custom.intersectionMapButtonHover,
            },
          }}
          size="small"
          onClick={() => {
            if (mapRef.current) {
              const map = mapRef.current.getMap()
              map.zoomIn()
            }
          }}
        >
          <AddIcon />
        </Fab>
        <Fab
          color="primary"
          id="minus-button"
          sx={{
            position: 'absolute',
            zIndex: 10,
            top: theme.spacing(17),
            right: theme.spacing(3),
            '&:hover': {
              backgroundColor: theme.palette.custom.intersectionMapButtonHover,
            },
          }}
          size="small"
          onClick={() => {
            if (mapRef.current) {
              const map = mapRef.current.getMap()
              map.zoomOut()
            }
          }}
        >
          <Remove />
        </Fab>
        <Map
          {...viewState}
          ref={mapRef}
          mapStyle={mbStyle as mapboxgl.Style}
          mapboxAccessToken={EnvironmentVars.MAPBOX_TOKEN}
          attributionControl={true}
          customAttribution={['<a href="https://www.cotrip.com/" target="_blank">© CDOT</a>']}
          styleDiffing
          style={{ width: '100%', height: '100%' }}
          onMove={(evt) => setViewState(evt.viewState)}
          onClick={(e) => onMapClick(e.point, e.lngLat)}
          interactiveLayerIds={allInteractiveLayerIds}
          cursor={cursor}
          onMouseMove={(e) => onMapMouseMove(e.features, e.lngLat)}
          onMouseEnter={(e) => onMapMouseEnter(e.features, e.lngLat)}
          onMouseLeave={() => onMapMouseLeave()}
          onLoad={(e: mapboxgl.MapboxEvent<undefined>) => {
            const map = e.target
            if (!map) return
            const images = [
              { name: 'traffic-light-icon-unknown', sdf: false },
              { name: 'traffic-light-icon-red-flashing', sdf: false },
              { name: 'traffic-light-icon-red-1', sdf: false },
              { name: 'traffic-light-icon-yellow-red-1', sdf: false },
              { name: 'traffic-light-icon-green-1', sdf: false },
              { name: 'traffic-light-icon-yellow-1', sdf: false },
              { name: 'srm_square', sdf: true },
              { name: 'close', sdf: true },
              { name: 'check', sdf: true },
              { name: 'circular-arrow', sdf: true },
              { name: 'gear', sdf: true },
              { name: 'lock', sdf: true },
              { name: 'question-mark', sdf: true },
              { name: 'sent', sdf: true },
              { name: 'timer', sdf: true },
              { name: 'warning', sdf: true },
            ]
            for (const img of images) {
              map.loadImage(`/icons/${img.name}.png`, (error, image) => {
                if (error) throw error
                if (!map.hasImage(img.name)) map.addImage(img.name, image, { sdf: img.sdf })
              })
            }

            // Set initial layer visibility after map loads
            map.once('idle', () => {
              Object.entries(layersVisible).forEach(([layerKey, isVisible]) => {
                if (map.getLayer(layerKey)) {
                  map.setLayoutProperty(layerKey, 'visibility', isVisible ? 'visible' : 'none')
                }
              })
            })

            if (mapRef.current) dispatch(setMapRef(mapRef))
          }}
        >
          {/* 
            LAYER RENDERING ORDER
            Layers render from bottom to top (first = bottom, last = top).
            To change layer stacking order, modify LAYER_RENDER_ORDER in map-layer-style-slice.ts
            and reorder the <Source>/<Layer> components below to match.
          */}
          <Source type="geojson" data={mapLanesFeatureCollectionOnlyWithSsmSrm}>
            <Layer {...mapMessageHighlightLayerStyle} />
          </Source>
          <Source type="geojson" data={connectingLanesOnlyWithSsmSrmFeatureCollection}>
            <Layer {...connectingLanesHighlightLayerStyle} />
          </Source>
          <Source type="geojson" data={mapLanesFeatureCollection}>
            <Layer {...mapMessageLayerStyle} />
          </Source>
          <Source type="geojson" data={connectingLanesFeatureCollection}>
            <Layer {...connectingLanesLayerStyle} />
          </Source>
          <Source type="geojson" data={notificationMarkerFeatureCollection}>
            <Layer {...markerLayerStyle} />
          </Source>
          <Source type="geojson" data={spatSignalHeadFeatures}>
            <Layer {...signalStateLayerStyle} />
          </Source>
          <Source type="geojson" data={connectingLanesOnlyWithSsmSrmFeatureCollection}>
            <Layer {...connectingLanesSsmStatusLayerStyle} />
          </Source>
          <Source type="geojson" data={currentBsms ?? { type: 'FeatureCollection', features: [] }}>
            <Layer {...bsmLayerStyle} />
          </Source>
          <Source type="geojson" data={activeSrmFeatureCollection}>
            <Layer {...srmLayerStyle} />
          </Source>
          <Source
            type="geojson"
            data={
              mapData?.mapFeatureCollection ?? {
                type: 'FeatureCollection',
                features: [],
              }
            }
          >
            <Layer {...mapMessageLabelsLayerStyle} />
          </Source>
          <Source type="geojson" data={connectingLanesFeatureCollection}>
            <Layer {...connectingLanesLabelsLayerStyle} />
          </Source>
          {selectedFeature && (
            <CustomPopup selectedFeature={selectedFeature} onClose={() => setSelectedFeature(undefined)} />
          )}
          {showPopupOnHover && hoveredFeature && !selectedFeature && (
            <CustomPopup selectedFeature={hoveredFeature} onClose={() => setHoveredFeature(undefined)} />
          )}
        </Map>
        <SidePanel
          laneInfo={connectingLanes}
          signalGroups={currentSignalGroups}
          bsms={currentBsms}
          ssmData={activeSsmData}
          srmData={activeSrmData}
          events={filteredSurroundingEvents}
          notifications={filteredSurroundingNotifications}
          sourceData={props.sourceData}
          sourceDataType={props.sourceDataType}
          openPanel={openPanel}
          setOpenPanel={(panel) => setOpenPanel(panel)}
        />
        <MapLegend openPanel={openPanel} setOpenPanel={(panel) => setOpenPanel(panel)} />
        <VisualSettings openPanel={openPanel} setOpenPanel={(panel) => setOpenPanel(panel)} />
        <LayerMenu openPanel={openPanel} setOpenPanel={(panel) => setOpenPanel(panel)} />
      </Col>
      <DecoderEntryDialog />
    </Container>
  )
}

export default IntersectionMap

import React, { useEffect, useState, useMemo } from 'react'
import cdotDark from '../styles/mapbox-styles/cdot-dark.json'
import mainLight from '../styles/mapbox-styles/main-light.json'
import mainDark from '../styles/mapbox-styles/main-dark.json'
import intersectionStyle from '../styles/intersectionMapStyle.json'

const mapStyles: { [key: string]: any } = {
  'mapbox-styles/cdot-dark.json': cdotDark,
  'mapbox-styles/main-light.json': mainLight,
  'mapbox-styles/main-dark.json': mainDark,
  'intersectionMapStyle.json': intersectionStyle,
}
import { CircleLayer, FillLayer, LineLayer } from 'mapbox-gl' // This is a dependency of react-map-gl even if you didn't explicitly install it
import Map, { Popup, Source, Layer, MapRef } from 'react-map-gl'
import { Container } from 'reactstrap'
import EnvironmentVars from '../EnvironmentVars'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import Slider from '@mui/material/Slider'
import {
  selectRsuData,
  selectSelectedRsu,
  selectRsuIpv4,
  selectAddGeoMsgPoint,
  selectGeoMsgStart,
  selectGeoMsgEnd,
  selectGeoMsgData,
  selectGeoMsgCoordinates,
  selectGeoMsgFilter,
  selectGeoMsgFilterStep,
  selectGeoMsgFilterOffset,

  // actions
  selectRsu,
  getRsuData,
  toggleGeoMsgPointSelect,
  clearGeoMsg,
  updateGeoMsgPoints,
  updateGeoMsgDate,
  setGeoMsgDataResult,
  setGeoMsgFilter,
  setGeoMsgFilterStep,
  setGeoMsgFilterOffset,
  changeGeoMsgType,
  selectGeoMsgType,
} from '../generalSlices/rsuSlice'
import { selectWzdxData, getWzdxData } from '../generalSlices/wzdxSlice'
import { selectIsAdminOrAbove, selectOrganizationName } from '../generalSlices/userSlice'
import {
  selectConfigCoordinates,
  toggleConfigPointSelect,
  selectAddConfigPoint,
  updateConfigPoints,
  geoRsuQuery,
  clearConfig,
  clearFirmware,
} from '../generalSlices/configSlice'
import ClearIcon from '@mui/icons-material/Clear'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import {
  Button,
  FormGroup,
  IconButton,
  Switch,
  Tooltip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Typography,
  FormControlLabel,
  Checkbox,
  useTheme,
  Paper,
  Select,
  MenuItem,
  alpha,
  FormControl,
  RadioGroup,
  Radio,
  InputLabel,
  Box,
  Divider,
  Grid2,
} from '@mui/material'
import './css/MsgMap.css'
import './css/Map.css'
import { WZDxFeature } from '../models/wzdx/WzdxWorkZoneFeed42'
import { selectIntersections, selectSelectedIntersection, setSelectedIntersectionId } from '../generalSlices/intersectionSlice'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { headerTabHeight } from '../styles/index'
import {
  getClusterColorStops,
  getClusterLabelSizeStops,
  getClusterRadiusStops,
  getHeatmapCountsStops,
  selectActiveLayers,
  selectViewState,
  setMapViewState,
  toggleLayerActive,
} from './mapSlice'
import {
  selectCountsEndDate,
  selectCountsMsgType,
  selectCountsStartDate,
  selectMenuSelection,
  toggleMapMenuSelection,
} from '../features/menu/menuSlice'
import { MapLayer } from '../models/MapLayer'
import { toast } from 'react-hot-toast'
import RoomOutlined from '@mui/icons-material/RoomOutlined'
import { selectHaasLocationData } from '../generalSlices/haasAlertSlice'
import { HaasLocationProperties } from '../models/haas/HaasWebsocketLocation'
import { HaasAlertVisualization } from '../components/HaasAlertVisualization'
import { Feature, Point } from 'geojson'
import { PrimaryButton } from '../styles/components/PrimaryButton'
import { ConditionalRenderRsu, ConditionalRenderRsuStatusMonitor, evaluateFeatureFlags } from '../feature-flags'
import { DateTime } from 'luxon'
import RsuStatusDialog from '../features/adminRsuTab/RsuStatusDialog'
import { selectToken } from '../generalSlices/userSlice'

import { MessageType } from '../models/MessageTypes'
import { useGetRsuCountsQuery } from '../features/api/rsuCountsApiSlice'
import { formatScmsExpiration, useGetScmsStatusQuery } from '../features/api/scmsApiSlice'
import { RSU_POINT_LAYER_ID, RsuMapLayer } from '../components/map-layers/RsuMapLayer'
import {
  INTERSECTION_ICON_ID,
  INTERSECTION_POINT_LAYER_ID,
  IntersectionMapLayer,
} from '../components/map-layers/IntersectionMapLayer'
import {
  WZDX_ICON_ID,
  WZDX_LINE_LAYER_ID,
  WZDX_POINT_LAYER_ID,
  WzdxMapLayer,
} from '../components/map-layers/WzdxMapLayer'
import { useGetRsuLastOnlineQuery, useGetRsuOnlineStatusesQuery } from '../features/api/rsuOnlineStatusApiSlice'
import { useGetGeoMsgDataMutation } from '../features/api/geoMsgApiSlice'

const MILLISECONDS_PER_MINUTE = 60000
const EMPTY_WZDX_FEATURES: WZDxFeature[] = []
const EMPTY_WZDX_DATA = { type: 'FeatureCollection' as const, features: EMPTY_WZDX_FEATURES }
const MAP_CLICK_LAYER_IDS = [
  'geoMsgPointLayer',
  RSU_POINT_LAYER_ID,
  INTERSECTION_POINT_LAYER_ID,
  WZDX_POINT_LAYER_ID,
]

const serializeTimestampAsUtc = (value: string): string => {
  const dateTime = DateTime.fromISO(value)
  if (!dateTime.isValid) throw new Error(`Invalid timestamp: ${value}`)
  return dateTime.toUTC().toISO({ suppressMilliseconds: true }) ?? dateTime.toUTC().toISO()!
}

const getGeoMsgApiErrorMessage = (error: unknown): string => {
  if (typeof error === 'string') return error
  if (error instanceof Error) return error.message
  if (error && typeof error === 'object') {
    const apiError = error as { data?: { detail?: unknown; title?: unknown }; error?: unknown; status?: unknown }
    if (typeof apiError.data?.detail === 'string') return apiError.data.detail
    if (typeof apiError.data?.title === 'string') return apiError.data.title
    if (typeof apiError.error === 'string') return apiError.error
    if (typeof apiError.status === 'number' || typeof apiError.status === 'string') {
      return String(apiError.status)
    }
  }
  return 'Unknown error'
}

const breakLine = (value: string) => {
  const lines = []
  let remainingData = ''
  const maxLineLength = 40
  for (let i = 0; i < value.length; i += maxLineLength) {
    let data = remainingData + value.substring(i, i + maxLineLength)
    const index = data.lastIndexOf(' ')
    if (data[0] === ' ') {
      data = data.substring(1, data.length)
      remainingData = data.substring(index, data.length)
    } else if (data?.[i + maxLineLength + 1] === ' ') {
      remainingData = data.substring(index + 1, data.length)
    } else if (data[index] === ' ') {
      remainingData = data.substring(index + 1, data.length)
    }
    lines.push(data.substring(0, index))
  }
  return lines.join('\n')
}

const WzdxPopupTable = ({ feature }: { feature: WZDxFeature }) => {
  const data = [
    ['road_name', feature.properties.core_details.road_names[0]],
    ['direction', feature.properties.core_details.direction],
    ['vehicle_impact', feature.properties.vehicle_impact],
    ['workers_present', feature.properties.worker_presence?.are_workers_present?.toString()],
    ['description', breakLine(feature.properties.core_details.description)],
    ['start_date', feature.properties.start_date],
    ['end_date', feature.properties.end_date],
  ]

  return (
    <div className="container">
      <table id="simple-board">
        <tbody>
          {data.map((row, rowIndex) => (
            <tr key={row[0]} id={`row${rowIndex}`}>
              {row.map((cell, cellIndex) => {
                const cellId = `cell${rowIndex}-${cellIndex}`
                return rowIndex === 0 ? (
                  <th key={cellId} id={cellId} style={{ minWidth: '120px' }}>
                    {cell}
                  </th>
                ) : (
                  <td key={cellId} id={cellId} style={{ minWidth: '120px' }}>
                    <pre>{cell}</pre>
                  </td>
                )
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

const getRsuDisplayColor = (displayType: string, onlineStatus: string, scmsStatus: boolean | null) => {
  if (displayType === 'online') {
    if (onlineStatus === 'online') return '#A1D363'
    if (onlineStatus === 'unstable') return '#D1A711'
    if (onlineStatus === 'offline') return '#E94F37'
  } else if (displayType === 'scms') {
    if (scmsStatus === true) return '#A1D363'
    if (scmsStatus === false) return '#E94F37'
  }

  return '#B0B0B0'
}

const calculateTimeWindow = (baseDate: string | Date, offset: number, step: number) => {
  const start = new Date(new Date(baseDate).getTime() + MILLISECONDS_PER_MINUTE * offset * step)
  const end = new Date(start.getTime() + MILLISECONDS_PER_MINUTE * step)
  return { start, end }
}

type LegendProps = {
  layers: MapLayer[]
  activeLayers: string[]
  onToggleLayer: (id: string) => void
}

const Legend = React.memo(({ layers, activeLayers, onToggleLayer }: LegendProps) => (
  <FormGroup>
    {layers
      .filter((layer) => evaluateFeatureFlags(layer.tag))
      .map((layer) => (
        <div key={layer.id}>
          <div style={{ fontSize: 'small', display: 'flex', alignItems: 'center' }}>
            <FormControlLabel
              label={<Typography>{layer.label}</Typography>}
              control={<Checkbox checked={activeLayers.includes(layer.id)} onChange={() => onToggleLayer(layer.id)} />}
            />
          </div>
        </div>
      ))}
  </FormGroup>
))

Legend.displayName = 'Legend'

function MapPage() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const [getGeoMsgData, { isLoading: isGeoMsgLoading }] = useGetGeoMsgDataMutation()

  const theme = useTheme()

  const mapRef = React.useRef<MapRef>(null)
  const intersectionIconLoadingRef = React.useRef(false)
  const wzdxIconLoadingRef = React.useRef(false)
  const organization = useSelector(selectOrganizationName)
  const rsuData = useSelector(selectRsuData)
  const selectedRsu = useSelector(selectSelectedRsu)
  const { data: issScmsStatusData = {} } = useGetScmsStatusQuery(organization, { skip: !organization })
  const { data: rsuOnlineStatus = {} } = useGetRsuOnlineStatusesQuery(organization, { skip: !organization })
  const rsuIpv4 = useSelector(selectRsuIpv4)
  const { data: selectedRsuLastOnline } = useGetRsuLastOnlineQuery(rsuIpv4 ?? '', {
    skip: !rsuIpv4,
  })
  const addConfigPoint = useSelector(selectAddConfigPoint)
  const configCoordinates = useSelector(selectConfigCoordinates)
  const geoMsgType = useSelector(selectGeoMsgType)

  const countsMsgType = useSelector(selectCountsMsgType)
  const countsStartDate = useSelector(selectCountsStartDate)
  const countsEndDate = useSelector(selectCountsEndDate)

  const geoMsgData = useSelector(selectGeoMsgData)
  const geoMsgCoordinates = useSelector(selectGeoMsgCoordinates)
  const addGeoMsgPoint = useSelector(selectAddGeoMsgPoint)
  const startGeoMsgDate = useSelector(selectGeoMsgStart)
  const endGeoMsgDate = useSelector(selectGeoMsgEnd)

  const filter = useSelector(selectGeoMsgFilter)
  const filterStep = useSelector(selectGeoMsgFilterStep)
  const filterOffset = useSelector(selectGeoMsgFilterOffset)

  const wzdxData = useSelector(selectWzdxData)
  const wzdxFeatures = wzdxData?.features ?? EMPTY_WZDX_FEATURES
  const safeWzdxData = wzdxData?.features ? wzdxData : EMPTY_WZDX_DATA

  const haasLocationData = useSelector(selectHaasLocationData)
  const [selectedHaasIncident, setSelectedHaasIncident] = useState<Feature<Point, HaasLocationProperties> | null>(null)

  const intersectionsList = useSelector(selectIntersections)
  const selectedIntersection = useSelector(selectSelectedIntersection)

  // Mapbox local state variables
  const viewState = useSelector(selectViewState)
  const [lastClickTime, setLastClickTime] = useState<number>(0)
  const menuSelection = useSelector(selectMenuSelection)
  const activeLayers = useSelector(selectActiveLayers)

  // RSU layer local state variables
  const [displayType, setDisplayType] = useState('online')

  const syncMapToViewState = React.useCallback(() => {
    const map = mapRef.current
    if (!map) return

    const center = map.getCenter()
    const cameraChanged =
      Math.abs(center.lat - viewState.latitude) > 1e-7 ||
      Math.abs(center.lng - viewState.longitude) > 1e-7 ||
      Math.abs(map.getZoom() - viewState.zoom) > 1e-7

    if (cameraChanged) {
      map.jumpTo({ center: [viewState.longitude, viewState.latitude], zoom: viewState.zoom })
    }
  }, [viewState])

  const ensureIntersectionIcon = React.useCallback(() => {
    const map = mapRef.current
    if (!map || map.hasImage(INTERSECTION_ICON_ID) || intersectionIconLoadingRef.current) return

    intersectionIconLoadingRef.current = true
    map.loadImage('/icons/intersection_icon.png', (error, image) => {
      intersectionIconLoadingRef.current = false
      if (error) {
        console.error('Unable to load intersection map icon:', error)
        return
      }
      if (image && !map.hasImage(INTERSECTION_ICON_ID)) {
        map.addImage(INTERSECTION_ICON_ID, image)
      }
    })
  }, [])

  const ensureWzdxIcon = React.useCallback(() => {
    const map = mapRef.current
    if (!map || map.hasImage(WZDX_ICON_ID) || wzdxIconLoadingRef.current) return

    wzdxIconLoadingRef.current = true
    map.loadImage('/workzone_icon.png', (error, image) => {
      wzdxIconLoadingRef.current = false
      if (error) {
        console.error('Unable to load WZDx map icon:', error)
        return
      }
      if (image && !map.hasImage(WZDX_ICON_ID)) {
        map.addImage(WZDX_ICON_ID, image)
      }
    })
  }, [])

  const ensureMapIcons = React.useCallback(() => {
    ensureIntersectionIcon()
    ensureWzdxIcon()
  }, [ensureIntersectionIcon, ensureWzdxIcon])

  // Camera movement is uncontrolled while the user interacts with the map. Redux updates still drive external
  // camera changes
  useEffect(() => {
    syncMapToViewState()
  }, [syncMapToViewState])

  const maxDurationMs = EnvironmentVars.MAX_QUERY_DURATION_DAYS * 24 * 60 * 60 * 1000
  const queryDurationExceeded = countsEndDate.getTime() - countsStartDate.getTime() > maxDurationMs

  const { data: rsuCounts } = useGetRsuCountsQuery(
    {
      organization,
      startDate: countsStartDate,
      endDate: countsEndDate,
      message: countsMsgType,
    },
    { skip: !organization || queryDurationExceeded }
  )

  // Add these new state variables near the other source states
  const [previewPoint, setPreviewPoint] = useState<GeoJSON.Feature<GeoJSON.Point> | null>(null)

  useEffect(() => {
    if (!addGeoMsgPoint && !addConfigPoint) {
      setPreviewPoint(null)
    }
  }, [addGeoMsgPoint, addConfigPoint])

  const [geoMsgPointSource, setGeoMsgPointSource] = useState<GeoJSON.FeatureCollection<GeoJSON.Geometry>>({
    type: 'FeatureCollection',
    features: [],
  })

  // baseDate is only used to set the startDate from a Date object
  const [baseDate] = useState(new Date(startGeoMsgDate))

  const [msgViewerSliderStartDate, setMsgViewerSliderStartDate] = useState(
    new Date(baseDate.getTime() + MILLISECONDS_PER_MINUTE * filterOffset * filterStep)
  )
  const [msgViewerSliderEndDate, setMsgViewerSliderEndDate] = useState(
    new Date(msgViewerSliderStartDate.getTime() + MILLISECONDS_PER_MINUTE * filterStep)
  )

  // stepOptions is used to set the step options for the message viewer
  const stepOptions = [
    { value: 1, label: '1 minute' },
    { value: 5, label: '5 minutes' },
    { value: 15, label: '15 minutes' },
    { value: 30, label: '30 minutes' },
    { value: 60, label: '60 minutes' },
  ]

  function stepValueToOption(val: number) {
    for (let i = 0; i < stepOptions.length; i++) {
      if (stepOptions[i].value === val) {
        return stepOptions[i]
      }
    }
  }

  // WZDx layer local state variables
  const [selectedWZDxMarker, setSelectedWZDxMarker] = useState<{
    feature: WZDxFeature
    coordinates: [number, number]
  } | null>(null)
  const [pageOpen] = useState(true)
  const isAdminOrAbove = useSelector(selectIsAdminOrAbove)

  // Vendor filter local state variable
  const [selectedVendor, setSelectedVendor] = useState('Select Vendor')
  const vendorArray: string[] = ['Select Vendor', 'Commsignia', 'Yunex', 'Kapsch']
  const setVendor = (newVal) => {
    setSelectedVendor(newVal)
  }

  const mbStyle = mapStyles[theme.palette.custom.mapStyleFilePath] || mapStyles['mapbox-styles/cdot-dark.json']

  // useEffects for Mapbox
  useEffect(() => {
    const listener = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        dispatch(selectRsu(null))
        dispatch(clearFirmware())
        setSelectedWZDxMarker(null)
      }
    }
    window.addEventListener('keydown', listener)

    return () => {
      window.removeEventListener('keydown', listener)
    }
  }, [selectedRsu, dispatch])

  // useEffects for RSU layer
  useEffect(() => {
    dispatch(getRsuData())
    dispatch(selectRsu(null))
    dispatch(clearFirmware())
  }, [organization, dispatch])

  // useEffects for BSM layer
  useEffect(() => {
    const { start: localStartDate, end: localEndDate } = calculateTimeWindow(startGeoMsgDate, filterOffset, filterStep)

    setMsgViewerSliderStartDate(localStartDate)
    setMsgViewerSliderEndDate(localEndDate)
  }, [startGeoMsgDate, filterOffset, filterStep])

  useEffect(() => {
    if (!startGeoMsgDate) {
      dateChanged(new Date(), 'start')
    }
    if (!endGeoMsgDate) {
      dateChanged(new Date(), 'end')
    }
    if (wzdxFeatures.length === 0) {
      dispatch(getWzdxData())
    }
  }, [dispatch])

  const createPointFeature = (point: number[]): GeoJSON.Feature<GeoJSON.Geometry> => {
    return {
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [...point],
      },
      properties: {},
    }
  }

  const isDateInRange = (date: Date, startDate: Date, endDate: Date): boolean => {
    return date >= startDate && date <= endDate
  }

  // Effect for handling polygon updates msg-viewer-layer

  const geoMsgPolygonPointSource = useMemo(() => {
    if (!activeLayers.includes('msg-viewer-layer')) return null

    return {
      type: 'FeatureCollection',
      features: geoMsgCoordinates.map((point) => createPointFeature(point)),
    } as GeoJSON.FeatureCollection<GeoJSON.Geometry>
  }, [geoMsgCoordinates, activeLayers])

  const geoMsgPolygonSource = useMemo(() => {
    if (!activeLayers.includes('msg-viewer-layer')) return null

    // Get coordinates including preview point if it exists
    let polygonCoords = [...geoMsgCoordinates]
    if (previewPoint && addGeoMsgPoint) {
      const previewCoords = previewPoint.geometry.coordinates

      if (polygonCoords.length >= 3 && polygonCoords[0] === polygonCoords[polygonCoords.length - 1]) {
        // For completed polygon: Remove closing point, add preview, then close
        polygonCoords = polygonCoords.slice(0, -1)
        polygonCoords.push(previewCoords)
        polygonCoords.push(polygonCoords[0])
      } else if (polygonCoords.length === 2) {
        // For two points: Draw triangle with preview point
        polygonCoords.push(previewCoords)
        polygonCoords.push(polygonCoords[0])
      } else if (polygonCoords.length === 1) {
        // For one point: Draw line to preview point
        polygonCoords = [[...polygonCoords[0]], [...previewCoords]] // Create a fresh array with both points
      }
    } else if (polygonCoords.length >= 3) {
      // Close the polygon if we have 3+ points and no preview
      polygonCoords.push(polygonCoords[0])
    }

    const polygonSource = {
      type: 'Feature',
      geometry: {
        type: polygonCoords.length === 2 ? 'LineString' : 'Polygon', // Use LineString for 2 points
        coordinates: polygonCoords.length === 2 ? polygonCoords : [polygonCoords],
      },
      properties: {},
    } as GeoJSON.Feature<GeoJSON.Geometry>

    return polygonSource
  }, [geoMsgCoordinates, activeLayers, addGeoMsgPoint, previewPoint])

  const configPolygonPointSource = useMemo(
    () =>
      ({
        type: 'FeatureCollection',
        features: configCoordinates.map(createPointFeature),
      }) as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
    [configCoordinates]
  )

  const configPolygonSource = useMemo(() => {
    // Get coordinates including preview point if it exists
    let polygonCoords = [...configCoordinates]
    if (previewPoint && addConfigPoint) {
      const previewCoords = previewPoint.geometry.coordinates

      if (polygonCoords.length >= 3 && polygonCoords[0] === polygonCoords[polygonCoords.length - 1]) {
        // For completed polygon: Remove closing point, add preview, then close
        polygonCoords = polygonCoords.slice(0, -1)
        polygonCoords.push(previewCoords)
        polygonCoords.push(polygonCoords[0])
      } else if (polygonCoords.length === 2) {
        // For two points: Draw triangle with preview point
        polygonCoords.push(previewCoords)
        polygonCoords.push(polygonCoords[0])
      } else if (polygonCoords.length === 1) {
        // For one point: Draw line to preview point
        polygonCoords = [[...polygonCoords[0]], [...previewCoords]] // Create a fresh array with both points
      }
    } else if (polygonCoords.length >= 3) {
      // Close the polygon if we have 3+ points and no preview
      polygonCoords.push(polygonCoords[0])
    }

    return {
      type: 'Feature',
      properties: {},
      geometry: {
        type: polygonCoords.length === 2 ? 'LineString' : 'Polygon', // Use LineString for 2 points
        coordinates: polygonCoords.length === 2 ? polygonCoords : [polygonCoords],
      },
    } as GeoJSON.Feature<GeoJSON.Geometry>
  }, [configCoordinates, addConfigPoint, previewPoint])

  // Effect for handling point source updates msg-viewer-layer
  useEffect(() => {
    // if the msg-viewer-layer is not active, exit the effect
    if (!activeLayers.includes(MAP_LAYERS.MSG_VIEWER.id)) return

    const pointSourceFeatures: Array<GeoJSON.Feature<GeoJSON.Geometry>> = []

    // Handle case when we have message data
    if ((geoMsgData?.length ?? 0) > 0) {
      // Filter messages within the selected time range and preserve properties
      geoMsgData.forEach((message) => {
        const messageDate = new Date(message['properties']['timeStamp'])
        if (isDateInRange(messageDate, msgViewerSliderStartDate, msgViewerSliderEndDate)) {
          // Create a new feature with all original properties
          const feature: GeoJSON.Feature<GeoJSON.Geometry> = {
            type: 'Feature',
            geometry: message.geometry,
            properties: {
              ...message.properties,
            },
          }
          pointSourceFeatures.push(feature)
        }
      })
    }

    setGeoMsgPointSource((prevPointSource) => ({
      ...prevPointSource,
      features: pointSourceFeatures,
    }))
  }, [geoMsgData, msgViewerSliderStartDate, msgViewerSliderEndDate, activeLayers, filter])

  // Helper function to calculate the maximum offset based on the start and end dates and the step
  const calculateMaxOffset = (start: string | Date, end: string | Date, step: number) => {
    return Math.floor((new Date(end).getTime() - new Date(start).getTime()) / (step * MILLISECONDS_PER_MINUTE))
  }

  const geoMsgFilterMaxOffset = useMemo(() => {
    return calculateMaxOffset(startGeoMsgDate, endGeoMsgDate, filterStep)
  }, [startGeoMsgDate, endGeoMsgDate, filterStep])

  // Helper function to calculate data availability for each time window...
  // in the v2x message viewer slider.
  const calculateDataAvailability = useMemo(() => {
    if (!geoMsgData || geoMsgData.length === 0) return []

    const availability: { offset: number; count: number }[] = []

    // Calculate data for each possible offset
    for (let offset = 0; offset <= geoMsgFilterMaxOffset; offset++) {
      const { start: windowStart, end: windowEnd } = calculateTimeWindow(startGeoMsgDate, offset, filterStep)

      // Count messages in this time window
      const messageCount = geoMsgData.filter((message) => {
        const messageDate = new Date(message.properties.timeStamp)
        return isDateInRange(messageDate, windowStart, windowEnd)
      }).length

      if (messageCount > 0) {
        availability.push({ offset, count: messageCount })
      }
    }

    return availability
  }, [geoMsgData, startGeoMsgDate, filterStep, geoMsgFilterMaxOffset])

  const heatMapData = useMemo(() => {
    const countsByIp: Record<string, number> = Object.fromEntries(
      (rsuCounts ?? []).map((c) => [c.rsu_ip, c.ode_input_count ?? 0])
    )
    return {
      type: 'FeatureCollection' as const,
      features:
        rsuData
          ?.map(
            (rsu) =>
              ({
                type: 'Feature',
                geometry: {
                  type: 'Point',
                  coordinates: [rsu.geometry.coordinates[0], rsu.geometry.coordinates[1]],
                },
                properties: {
                  ipv4_address: rsu.properties.ipv4_address,
                  count: countsByIp[rsu.properties.ipv4_address] ?? 0,
                },
              }) as GeoJSON.Feature<GeoJSON.Geometry>
          )
          ?.filter((feature) => feature.properties.count > 0) ?? [],
    }
  }, [rsuData, rsuCounts])

  const rsuPointData = useMemo(
    () =>
      ({
        type: 'FeatureCollection',
        features: (rsuData ?? [])
          .filter((rsu) => selectedVendor === 'Select Vendor' || rsu.properties.manufacturer_name === selectedVendor)
          .map((rsu) => {
            const ip = rsu.properties.ipv4_address
            const onlineStatus = Object.prototype.hasOwnProperty.call(rsuOnlineStatus, ip)
              ? rsuOnlineStatus[ip].current_status
              : 'offline'
            const scmsStatus =
              Object.prototype.hasOwnProperty.call(issScmsStatusData, ip) && issScmsStatusData[ip]
                ? issScmsStatusData[ip].health
                : null

            return {
              type: 'Feature' as const,
              id: rsu.id,
              geometry: rsu.geometry,
              properties: {
                ipv4_address: ip,
                display_color: getRsuDisplayColor(displayType, onlineStatus, scmsStatus),
              },
            }
          }),
      }) as GeoJSON.FeatureCollection<GeoJSON.Point>,
    [displayType, issScmsStatusData, rsuData, rsuOnlineStatus, selectedVendor]
  )

  const intersectionPointData = useMemo<GeoJSON.FeatureCollection<GeoJSON.Point>>(
    () => ({
      type: 'FeatureCollection',
      features: intersectionsList
        .filter((intersection) => intersection.latitude !== 0)
        .map((intersection) => ({
          type: 'Feature',
          id: intersection.intersectionID,
          properties: {
            intersectionId: intersection.intersectionID,
            intersectionName: intersection.intersectionID,
          },
          geometry: {
            type: 'Point',
            coordinates: [intersection.longitude, intersection.latitude],
          },
        })),
    }),
    [intersectionsList]
  )

  function dateChanged(e: Date, type: 'start' | 'end') {
    try {
      const date = DateTime.fromISO(e.toISOString())
      date.setZone(DateTime.local().zoneName)
      dispatch(updateGeoMsgDate({ type, date: date.toString() }))
    } catch (err) {
      console.error('Encountered issue updating date: ', err.message)
    }
  }

  const addGeoMsgPointToCoordinates = (point: { lat: number; lng: number }) => {
    const pointArray = [point.lng, point.lat]
    if (geoMsgCoordinates.length > 1) {
      if (geoMsgCoordinates[0] === geoMsgCoordinates.slice(-1)[0]) {
        const tmp = [...geoMsgCoordinates]
        tmp.pop()
        dispatch(updateGeoMsgPoints([...tmp, pointArray, geoMsgCoordinates[0]]))
      } else {
        dispatch(updateGeoMsgPoints([...geoMsgCoordinates, pointArray, geoMsgCoordinates[0]]))
      }
    } else {
      dispatch(updateGeoMsgPoints([...geoMsgCoordinates, pointArray]))
    }
  }

  const addConfigPointToCoordinates = (point: { lat: number; lng: number }) => {
    const pointArray = [point.lng, point.lat]
    if (configCoordinates?.length > 1) {
      if (configCoordinates[0] === configCoordinates.slice(-1)[0]) {
        const tmp = [...configCoordinates]
        tmp.pop()
        dispatch(updateConfigPoints([...tmp, pointArray, configCoordinates[0]]))
      } else {
        dispatch(updateConfigPoints([...configCoordinates, pointArray, configCoordinates[0]]))
      }
    } else {
      dispatch(updateConfigPoints([...configCoordinates, pointArray]))
    }
  }

  const heatmapStops = useMemo(() => {
    return getHeatmapCountsStops(countsMsgType, heatMapData)
  }, [countsMsgType, heatMapData])

  const clusterColorStops = useMemo(() => {
    return getClusterColorStops(countsMsgType, heatMapData)
  }, [countsMsgType, heatMapData])

  const clusterRadiusStops = useMemo(() => {
    return getClusterRadiusStops(countsMsgType, heatMapData)
  }, [countsMsgType, heatMapData])

  const clusterLabelSizeStops = useMemo(() => {
    return getClusterLabelSizeStops(countsMsgType, heatMapData)
  }, [countsMsgType, heatMapData])

  function closePopup() {
    setSelectedWZDxMarker(null)
  }

  const isOnline = () => {
    return selectedRsuLastOnline?.last_online ? new Date(selectedRsuLastOnline.last_online).toLocaleString() : 'No Data'
  }

  const getStatus = () => {
    return rsuIpv4 in rsuOnlineStatus &&
      Object.prototype.hasOwnProperty.call(rsuOnlineStatus[rsuIpv4], 'current_status')
      ? rsuOnlineStatus[rsuIpv4].current_status
      : 'Offline'
  }

  const handleScmsStatus = () => {
    setDisplayType('scms')
  }

  const handleOnlineStatus = () => {
    setDisplayType('online')
  }

  const handleRsuDisplayTypeChange = (event: React.SyntheticEvent) => {
    const target = event.target as HTMLInputElement
    if (target.value === 'online') handleOnlineStatus()
    else if (target.value === 'scms') handleScmsStatus()
    if (!activeLayers.includes(MAP_LAYERS.RSU.id)) {
      dispatch(toggleLayerActive(MAP_LAYERS.RSU.id))
    }
  }

  const MAP_LAYERS: Record<string, MapLayer> = {
    RSU: {
      id: 'rsu-layer',
      label: 'RSU Viewer',
      type: 'symbol',
      tag: 'rsu',
    },
    HEATMAP: {
      id: 'heatmap-layer',
      label: 'Heatmap',
      type: 'heatmap',
      maxzoom: 14,
      source: 'heatMapData',
      filter: ['all', ['has', 'count'], ['>', ['get', 'count'], 0]],
      paint: {
        'heatmap-weight': {
          property: 'count',
          type: 'exponential',
          stops: heatmapStops,
        },
        'heatmap-intensity': ['interpolate', ['linear'], ['zoom'], 0, 0, 9, 1, 10, 2],
        'heatmap-color': [
          'interpolate',
          ['linear'],
          ['heatmap-density'],
          0,
          'rgba(33,102,172,0)',
          0.2,
          'rgb(103,169,207)',
          0.4,
          'rgb(209,229,240)',
          0.6,
          'rgb(253,219,199)',
          0.8,
          'rgb(239,138,98)',
          0.9,
          'rgb(255,201,101)',
        ],
        'heatmap-opacity': ['interpolate', ['linear'], ['zoom'], 10, 1, 13, 0.6, 14, 0],
      },
      tag: 'rsu',
    },
    HEATMAP_CLUSTER: {
      id: 'heatmap-cluster',
      label: 'Heatmap Cluster',
      type: 'circle',
      tag: 'rsu',
    },
    MSG_VIEWER: {
      id: 'msg-viewer-layer',
      label: 'V2X Message Viewer',
      type: 'symbol',
      tag: 'rsu',
    },
    WZDX: {
      id: WZDX_LINE_LAYER_ID,
      label: 'WZDx Viewer',
      type: 'line',
      tag: 'wzdx',
    },
    INTERSECTION: {
      id: 'intersection-layer',
      label: 'Intersections',
      type: 'symbol',
      tag: 'intersection',
    },
    HAAS_ALERT: {
      id: 'haas-alert-layer',
      label: 'HAAS Alert Viewer',
      type: 'circle',
      tag: 'haas',
    },
  }

  const toggleLayer = (id: string) => {
    dispatch(toggleLayerActive(id))
    if (activeLayers.includes(id)) {
      switch (id) {
        case MAP_LAYERS.RSU.id:
          dispatch(selectRsu(null))
          dispatch(clearFirmware())
          break
        case MAP_LAYERS.WZDX.id:
          setSelectedWZDxMarker(null)
          break
        case MAP_LAYERS.HAAS_ALERT.id:
          setSelectedHaasIncident(null)
          break
      }
    } else {
      switch (id) {
        case MAP_LAYERS.WZDX.id:
          dispatch(getWzdxData())
          break
        case MAP_LAYERS.HEATMAP.id:
        case MAP_LAYERS.HEATMAP_CLUSTER.id:
          if (!menuSelection.includes('Display Message Counts')) {
            dispatch(toggleMapMenuSelection('Display Message Counts'))
          }
          break
      }
    }
  }

  const handleButtonToggle = (event: React.SyntheticEvent<Element, Event>, origin: 'config' | 'msgViewer') => {
    // Deselect any selected RSU when toggling point select tools
    dispatch(selectRsu(null))
    dispatch(clearFirmware())

    // Toggle the corresponding point select tool based on the origin
    if (origin === 'config') {
      dispatch(toggleConfigPointSelect())
      if (addGeoMsgPoint) dispatch(toggleGeoMsgPointSelect())
    } else if (origin === 'msgViewer') {
      dispatch(toggleGeoMsgPointSelect())
      if (addConfigPoint) dispatch(toggleConfigPointSelect())
    }
  }

  const messageViewerTypes = EnvironmentVars.getMessageViewerTypes()
  const messageTypeOptions = messageViewerTypes.map((type) => {
    return { value: type, label: type }
  })

  // Added logic to open RsuStatusDialog from Map popup
  const [statusDialogOpen, setStatusDialogOpen] = useState(false)
  const [selectedRsuIp, setSelectedRsuIp] = useState<string | null>(null)

  const token = useSelector(selectToken)

  const handlePopupClick = (rsuIp: string) => {
    setSelectedRsuIp(rsuIp)
    setStatusDialogOpen(true)
  }

  const handleGeoMsgDataSubmit = async () => {
    if (isGeoMsgLoading) return
    if (addGeoMsgPoint) {
      toast.error('Please complete the polygon (double click to close) before submitting')
      return
    }
    if (!evaluateFeatureFlags('rsu')) return
    if (!startGeoMsgDate || !endGeoMsgDate || geoMsgCoordinates.length <= 2) return

    const firstPoint = geoMsgCoordinates[0]
    const lastPoint = geoMsgCoordinates[geoMsgCoordinates.length - 1]
    if (firstPoint[0] !== lastPoint[0] || firstPoint[1] !== lastPoint[1]) {
      toast.error('Please complete the polygon (double click to close) before submitting')
      return
    }

    let requestBody
    try {
      if (!geoMsgType) throw new Error('Please select a message type')
      requestBody = {
        msg_type: geoMsgType,
        start: serializeTimestampAsUtc(startGeoMsgDate),
        end: serializeTimestampAsUtc(endGeoMsgDate),
        geometry: geoMsgCoordinates,
      }
    } catch (error) {
      toast.error(`Query failed: ${getGeoMsgApiErrorMessage(error)}`)
      dispatch(setGeoMsgDataResult([]))
      return
    }

    const responsePromise = getGeoMsgData(requestBody).unwrap()
    toast.promise(responsePromise, {
      loading: `Retrieving ${requestBody.msg_type} Data`,
      success: (features) =>
        Array.isArray(features) ? `Retrieved ${features.length.toLocaleString()} messages` : 'No data returned from API',
      error: (error) => `Query failed: ${getGeoMsgApiErrorMessage(error)}`,
    })

    try {
      const features = await responsePromise
      if (!Array.isArray(features)) {
        toast.error('No data returned from API')
        dispatch(setGeoMsgDataResult([]))
        return
      }

      if (features.length === 0) {
        toast.error('No messages found for the selected criteria')
        dispatch(setGeoMsgDataResult([]))
        return
      }

      const uniqueIds = Array.from(new Set(features.map((feature) => feature.properties?.id)))
      const idToColorIndex = new globalThis.Map(uniqueIds.map((id, index) => [id, index % 10]))
      const coloredFeatures = features.map((feature) => ({
        ...feature,
        properties: {
          ...feature.properties,
          colorIndex: idToColorIndex.get(feature.properties?.id),
        },
      }))

      dispatch(setGeoMsgDataResult(coloredFeatures))
    } catch (error) {
      console.error(error)
      dispatch(setGeoMsgDataResult([]))
    }
  }

  return (
    <div className="container">
      <div className="menu-container map-control-container">
        <Accordion
          style={{ backgroundColor: theme.palette.background.paper }}
          disableGutters={true}
          sx={{ '&.accordion': { marginBottom: 0 } }}
          defaultExpanded
          elevation={0}
        >
          <AccordionSummary
            expandIcon={<ExpandMoreIcon style={{ color: theme.palette.text.primary }} />}
            aria-controls="panel1-content"
            id="panel1-header"
          >
            <Typography className="accordion-header museo-slab" color={theme.palette.text.primary}>
              Map Layers
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Legend layers={Object.values(MAP_LAYERS)} activeLayers={activeLayers} onToggleLayer={toggleLayer} />
          </AccordionDetails>
        </Accordion>
        <ConditionalRenderRsu>
          <Divider />
          <Accordion
            style={{ backgroundColor: theme.palette.background.paper }}
            disableGutters={true}
            sx={{ '&.accordion': { marginBottom: 0 } }}
            defaultExpanded
            elevation={0}
          >
            <AccordionSummary
              expandIcon={<ExpandMoreIcon style={{ color: theme.palette.text.primary }} />}
              aria-controls="panel3-content"
              id="panel3-header"
            >
              <Typography className="accordion-header museo-slab" color={theme.palette.text.primary}>
                Filter RSUs
              </Typography>
            </AccordionSummary>
            <AccordionDetails>
              <FormControl fullWidth>
                <InputLabel htmlFor="vendor">Vendor</InputLabel>
                <Select
                  id="vendor"
                  label="Vendor"
                  value={selectedVendor}
                  defaultValue={selectedVendor}
                  onChange={(event) => {
                    const vendor = event.target.value as string
                    setVendor(vendor)
                  }}
                >
                  {vendorArray.map((vendor) => (
                    <MenuItem key={vendor} value={vendor}>
                      <Typography>{vendor}</Typography>
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Typography sx={{ marginTop: '12px' }}>RSU Status</Typography>
              <FormControl sx={{ ml: 2, mt: 1 }}>
                <RadioGroup value={displayType} onChange={handleRsuDisplayTypeChange}>
                  {[
                    { key: 'online', label: <Typography>Online Status</Typography> },
                    { key: 'scms', label: <Typography>SCMS Status</Typography> },
                  ].map((val) => (
                    <FormControlLabel
                      value={val.key}
                      key={val.key}
                      sx={{ mt: -1 }}
                      control={
                        <Radio
                          sx={{
                            color: theme.palette.text.primary,
                            '&.Mui-checked': {
                              color: theme.palette.primary.main,
                            },
                          }}
                        />
                      }
                      label={val.label}
                    />
                  ))}
                </RadioGroup>
              </FormControl>
            </AccordionDetails>
          </Accordion>
        </ConditionalRenderRsu>

        <ConditionalRenderRsu>
          {isAdminOrAbove && (
            <>
              <Divider />
              <Accordion
                style={{ backgroundColor: theme.palette.background.paper }}
                disableGutters={true}
                sx={{ '&.accordion': { marginBottom: 0 } }}
                defaultExpanded
                elevation={0}
              >
                <AccordionSummary
                  expandIcon={<ExpandMoreIcon style={{ color: theme.palette.text.primary }} />}
                  aria-controls="panel3-content"
                  id="panel3-header"
                >
                  <Typography className="accordion-header museo-slab" color={theme.palette.text.primary}>
                    RSU Configuration
                  </Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <FormGroup row className="form-group-row">
                    <FormControlLabel
                      control={<Switch checked={addConfigPoint} />}
                      label={<Typography>Add Points</Typography>}
                      onChange={(e) => handleButtonToggle(e, 'config')}
                      sx={{ ml: 1 }}
                    />
                    <Tooltip title="Clear Points">
                      <div>
                        {/* prevents warning of Tooltip not wanting to wrap a disabled button element */}
                        <IconButton
                          disabled={configCoordinates.length == 0}
                          onClick={() => {
                            dispatch(clearConfig())
                          }}
                          size="large"
                        >
                          <ClearIcon />
                        </IconButton>
                      </div>
                    </Tooltip>
                  </FormGroup>
                  <FormGroup row sx={{ justifyContent: 'center', alignItems: 'center' }}>
                    <Button
                      variant="outlined"
                      color="info"
                      sx={{
                        '&.Mui-disabled': {
                          backgroundColor: alpha(theme.palette.primary.light, 0.5),
                        },
                        width: '100%',
                      }}
                      disabled={!(configCoordinates.length > 2)}
                      onClick={() => {
                        dispatch(geoRsuQuery(selectedVendor))
                      }}
                      className="museo-slab capital-case"
                    >
                      Configure RSUs
                    </Button>
                  </FormGroup>
                </AccordionDetails>
              </Accordion>
            </>
          )}
        </ConditionalRenderRsu>
      </div>
      <ConditionalRenderRsu>
        <PrimaryButton
          sx={{
            zIndex: 90,
            position: 'absolute',
            top: `${headerTabHeight + 25}px`,
            right: '25px',
          }}
          className="museo-slab capital-case"
          onClick={() => dispatch(toggleMapMenuSelection('Display Message Counts'))}
        >
          Message Counts
        </PrimaryButton>
      </ConditionalRenderRsu>
      <ConditionalRenderRsu>
        <PrimaryButton
          sx={{
            zIndex: 90,
            position: 'absolute',
            top: `${headerTabHeight + 25}px`,
            right: '200px',
          }}
          className="museo-slab capital-case"
          onClick={() => dispatch(toggleMapMenuSelection('Display RSU Status'))}
        >
          Display RSU Status
        </PrimaryButton>
      </ConditionalRenderRsu>
      <Container
        fluid={true}
        style={{
          width: '100%',
          height: `calc(100vh - ${headerTabHeight}px)`,
          display: 'flex',
        }}
      >
        <Map
          initialViewState={viewState}
          ref={mapRef}
          mapboxAccessToken={EnvironmentVars.MAPBOX_TOKEN}
          mapStyle={mbStyle}
          style={{ width: '100%', height: '100%' }}
          onLoad={() => {
            syncMapToViewState()
            ensureMapIcons()
          }}
          onStyleData={ensureMapIcons}
          onMoveEnd={(evt) => dispatch(setMapViewState(evt.viewState))}
          onMouseMove={
            addGeoMsgPoint || addConfigPoint
              ? (e) => {
                  const point: GeoJSON.Feature<GeoJSON.Point> = {
                    type: 'Feature',
                    geometry: {
                      type: 'Point',
                      coordinates: [e.lngLat.lng, e.lngLat.lat],
                    },
                    properties: {},
                  }
                  setPreviewPoint(point)
                }
              : undefined
          }
          onClick={(e) => {
            // Prevent double click from triggering single click
            const clickTime = new Date().getTime()
            if (clickTime - lastClickTime < 300) {
              return
            }
            setLastClickTime(clickTime)

            const map = mapRef.current
            const clickableLayerIds = map ? MAP_CLICK_LAYER_IDS.filter((layerId) => map.getLayer(layerId)) : []
            const clickedFeatures =
              map && clickableLayerIds.length > 0
                ? map.queryRenderedFeatures(e.point, { layers: clickableLayerIds })
                : []

            const clickedIntersectionFeature = clickedFeatures.find(
              (feature) => feature.layer.id === INTERSECTION_POINT_LAYER_ID
            )
            if (clickedIntersectionFeature) {
              const intersectionId = Number(clickedIntersectionFeature.properties?.intersectionId)
              if (Number.isFinite(intersectionId)) {
                dispatch(setSelectedIntersectionId(intersectionId))
              }
              return
            }

            const clickedWzdxFeature = clickedFeatures.find((feature) => feature.layer.id === WZDX_POINT_LAYER_ID)
            if (clickedWzdxFeature) {
              const featureIndex = Number(clickedWzdxFeature.id)
              if (Number.isInteger(featureIndex) && wzdxFeatures[featureIndex]) {
                setSelectedWZDxMarker({
                  feature: wzdxFeatures[featureIndex],
                  coordinates: [e.lngLat.lng, e.lngLat.lat],
                })
                dispatch(selectRsu(null))
                dispatch(clearFirmware())
              }
              return
            }

            const clickedRsuFeature = clickedFeatures.find((feature) => feature.layer.id === RSU_POINT_LAYER_ID)
            if (clickedRsuFeature && !addConfigPoint && !addGeoMsgPoint) {
              const clickedRsu = (rsuData ?? []).find(
                (rsu) => rsu.properties.ipv4_address === clickedRsuFeature.properties?.ipv4_address
              )
              if (clickedRsu) {
                dispatch(selectRsu(clickedRsu))
                setSelectedWZDxMarker(null)
                dispatch(clearFirmware())
              }
            } else if (selectedRsu) {
              // The popup no longer receives a DOM-marker click to stop propagation, so close it explicitly on map clicks.
              dispatch(selectRsu(null))
              dispatch(clearFirmware())
            }

            if (addGeoMsgPoint) {
              addGeoMsgPointToCoordinates(e.lngLat)
            }
            if (addConfigPoint) {
              addConfigPointToCoordinates(e.lngLat)
            }
          }}
          onDblClick={(e) => {
            e.preventDefault() // Prevent map zoom
            if (addGeoMsgPoint) {
              dispatch(toggleGeoMsgPointSelect())
            }
            if (addConfigPoint) {
              dispatch(toggleConfigPointSelect())
            }
          }}
        >
          {/* Add preview sources and layers */}
          {activeLayers.includes(MAP_LAYERS.MSG_VIEWER.id) ||
            (activeLayers.includes(MAP_LAYERS.RSU.id) && previewPoint && (
              <Source id="preview-point" type="geojson" data={previewPoint}>
                <Layer
                  id="preview-point-layer"
                  type="circle"
                  paint={{
                    'circle-radius': 5,
                    'circle-color': addGeoMsgPoint ? 'rgba(255, 164, 0, 0.5)' : 'rgba(255, 0, 0, 0.5)',
                    'circle-stroke-width': 2,
                    'circle-stroke-color': addGeoMsgPoint ? 'rgb(255, 164, 0)' : 'rgb(255, 0, 0)',
                  }}
                />
              </Source>
            ))}

          {activeLayers.includes('rsu-layer') && (
            <div>
              {configCoordinates.length >= 1 ? (
                <Source id={MAP_LAYERS.RSU.id + '-fill'} type="geojson" data={configPolygonSource}>
                  <Layer {...getConfigOutlineLayer(addConfigPoint)} />
                  <Layer {...configFillLayer} />
                </Source>
              ) : null}
              {addConfigPoint && (
                <Source id={MAP_LAYERS.RSU.id + '-polygon-points'} type="geojson" data={configPolygonPointSource}>
                  <Layer {...configPointLayer} />
                </Source>
              )}
            </div>
          )}
          {activeLayers.includes(MAP_LAYERS.HEATMAP.id) && (
            <Source id={MAP_LAYERS.HEATMAP.id} type="geojson" data={heatMapData}>
              <Layer {...MAP_LAYERS.HEATMAP} />
            </Source>
          )}
          {activeLayers.includes(MAP_LAYERS.HEATMAP_CLUSTER.id) && (
            <Source
              id={MAP_LAYERS.HEATMAP_CLUSTER.id}
              type="geojson"
              data={heatMapData}
              cluster={true}
              clusterMaxZoom={14}
              clusterRadius={50}
              clusterProperties={{
                sum_count: ['+', ['get', 'count']],
              }}
            >
              {/* Clusters */}
              <Layer
                id="clusters"
                type="circle"
                filter={['has', 'point_count']}
                paint={{
                  'circle-color': ['interpolate', ['linear'], ['get', 'sum_count'], ...clusterColorStops.flat()],
                  'circle-radius': ['interpolate', ['linear'], ['get', 'sum_count'], ...clusterRadiusStops.flat()],
                  'circle-stroke-width': 3,
                  'circle-stroke-color': '#000',
                }}
              />
              {/* Cluster count labels */}
              <Layer
                id="cluster-count"
                type="symbol"
                filter={['has', 'point_count']}
                layout={{
                  'text-field': '{sum_count}',
                  'text-font': ['Open Sans Bold', 'Arial Unicode MS Bold'],
                  'text-size': 14,
                }}
                paint={{
                  'text-color': '#ffffff',
                  'text-halo-color': '#000000',
                  'text-halo-width': 2,
                }}
              />
              {/* Individual RSU points (when zoomed in) - sized by count */}
              <Layer
                id="unclustered-point"
                type="circle"
                filter={['!', ['has', 'point_count']]}
                paint={{
                  'circle-radius': 20,
                  'circle-color': ['interpolate', ['linear'], ['get', 'count'], ...clusterColorStops.flat()],
                  'circle-stroke-width': 3,
                  'circle-stroke-color': '#ffffff',
                  'circle-opacity': 0.8,
                }}
              />
              {/* Individual point count labels */}
              <Layer
                id="unclustered-point-label"
                type="symbol"
                filter={['!', ['has', 'point_count']]}
                layout={{
                  'text-field': ['to-string', ['get', 'count']],
                  'text-font': ['Open Sans Bold', 'Arial Unicode MS Bold'],
                  'text-size': ['interpolate', ['linear'], ['get', 'count'], ...clusterLabelSizeStops.flat()],
                  'text-allow-overlap': true,
                  'text-ignore-placement': true,
                }}
                paint={{
                  'text-color': '#ffffff',
                  'text-halo-color': '#000000',
                  'text-halo-width': 2,
                }}
              />
            </Source>
          )}
          {activeLayers.includes(MAP_LAYERS.MSG_VIEWER.id) && (
            <div>
              {geoMsgCoordinates.length >= 1 ? (
                <Source id={MAP_LAYERS.MSG_VIEWER.id + '-fill'} type="geojson" data={geoMsgPolygonSource}>
                  <Layer {...getGeoMsgOutlineLayer(addGeoMsgPoint)} />
                  <Layer {...geoMsgFillLayer} />
                </Source>
              ) : null}
              {addGeoMsgPoint && (
                <Source
                  id={MAP_LAYERS.MSG_VIEWER.id + '-polygon-points'}
                  type="geojson"
                  data={geoMsgPolygonPointSource}
                >
                  <Layer {...geoMsgPolygonPointLayer} />
                </Source>
              )}
              {filter && (
                <Source id={MAP_LAYERS.MSG_VIEWER.id + '-geo-msg-points'} type="geojson" data={geoMsgPointSource}>
                  <Layer {...geoMsgPointLayer} />
                </Source>
              )}
            </div>
          )}
          {activeLayers.includes(MAP_LAYERS.WZDX.id) && (
            <WzdxMapLayer data={safeWzdxData} />
          )}
          {selectedWZDxMarker ? (
            <Popup
              latitude={selectedWZDxMarker.coordinates[1]}
              longitude={selectedWZDxMarker.coordinates[0]}
              {...{ altitude: 12, offsetTop: -25 }} // TODO: Make sure this does something
              onClose={closePopup}
              maxWidth={'500px'}
            >
              <WzdxPopupTable feature={selectedWZDxMarker.feature} />
            </Popup>
          ) : null}
          {activeLayers.includes(MAP_LAYERS.INTERSECTION.id) && selectedIntersection && (
            <Popup
              latitude={selectedIntersection.latitude}
              longitude={selectedIntersection.longitude}
              closeOnClick={false}
              closeButton={false}
            >
              <div>SELECTED {selectedIntersection.intersectionID}</div>
            </Popup>
          )}
          {activeLayers.includes(MAP_LAYERS.INTERSECTION.id) && (
            <IntersectionMapLayer data={intersectionPointData} />
          )}
          {selectedRsu ? (
            <Popup
              latitude={selectedRsu.geometry.coordinates[1]}
              longitude={selectedRsu.geometry.coordinates[0]}
              closeOnClick={false}
              onClose={() => {
                if (pageOpen) {
                  dispatch(selectRsu(null))
                  dispatch(clearFirmware())
                }
              }}
              maxWidth="350px"
              className="rsu-popup"
            >
              <Box>
                {/* Header Section */}
                <Grid2
                  container
                  columnSpacing={0.5}
                  rowSpacing={0}
                  sx={{
                    color: theme.palette.text.secondary,
                    backgroundColor: theme.palette.background.paper,
                    paddingY: '8px',
                  }}
                >
                  <Grid2 size={1} display="flex" justifyContent="flex-start" sx={{ ml: '16px' }}>
                    <RoomOutlined color="info" fontSize="medium" />
                  </Grid2>
                  <Grid2 size={5}>
                    <Typography fontSize="Medium" color={theme.palette.text.primary} className="museo-slab">
                      {selectedRsu.properties.primary_route} Milepost {selectedRsu.properties.milepost}
                    </Typography>
                  </Grid2>
                  <Grid2 size={5} justifyContent="flex-start">
                    <Box
                      style={{
                        color: theme.palette.text.primary,
                        backgroundColor:
                          getStatus().toLowerCase() === 'online'
                            ? theme.palette.success.dark
                            : getStatus().toLowerCase() === 'unstable'
                              ? theme.palette.warning.main
                              : theme.palette.error.dark,
                        width: '4rem',
                        height: '1.5rem',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        borderRadius: '1rem',
                      }}
                    >
                      <Typography fontSize="medium">{getStatus()}</Typography>
                    </Box>
                  </Grid2>
                  <Grid2 size={4} justifyContent="flex-start" sx={{ ml: '16px' }}>
                    <Typography fontSize="small">{rsuIpv4}</Typography>
                  </Grid2>
                </Grid2>

                {/* Body Section */}
                <Grid2
                  container
                  columnSpacing={1}
                  rowSpacing={1}
                  sx={{
                    color: theme.palette.text.secondary,
                    backgroundColor: theme.palette.background.default,
                    paddingY: '10px',
                  }}
                >
                  <Grid2 size={5} justifyContent="flex-start">
                    <Typography fontSize="medium" sx={{ ml: '16px' }}>
                      Last Online:
                    </Typography>
                  </Grid2>
                  <Grid2 size={6} justifyContent="flex-start">
                    <Typography fontSize="medium">{isOnline()}</Typography>
                  </Grid2>
                  <Grid2 size={5} justifyContent="flex-start">
                    <Typography fontSize="medium" sx={{ ml: '16px' }}>
                      SCMS Health:
                    </Typography>
                  </Grid2>
                  <Grid2 size={6} justifyContent="flex-start">
                    {rsuIpv4 in issScmsStatusData && issScmsStatusData[rsuIpv4] ? (
                      <Grid2 container>
                        <Grid2 size={12} justifyContent="flex-start">
                          <Typography
                            sx={{
                              color: issScmsStatusData[rsuIpv4].health
                                ? theme.palette.success.light
                                : theme.palette.error.light,
                            }}
                          >
                            {issScmsStatusData[rsuIpv4].health ? 'Healthy' : 'Unhealthy'}
                          </Typography>
                        </Grid2>
                        <Grid2 size={12}>
                          <Typography fontSize="small">
                            {issScmsStatusData[rsuIpv4].expiration
                              ? formatScmsExpiration(issScmsStatusData[rsuIpv4].expiration)
                              : 'Never downloaded certificates'}
                          </Typography>
                        </Grid2>
                      </Grid2>
                    ) : (
                      <>
                        <Typography fontSize="medium">RSU is not enrolled with ISS SCMS</Typography>
                      </>
                    )}
                  </Grid2>
                </Grid2>

                {/* Footer Section */}
                <Box
                  sx={{
                    color: theme.palette.text.secondary,
                    backgroundColor: theme.palette.background.default,
                    borderRadius: '4px',
                    paddingY: '10px',
                  }}
                >
                  <Divider />
                  <Typography fontSize="small" sx={{ margin: '10px 0px 0px 16px' }}>
                    {selectedRsu.properties.manufacturer_name} #
                    {selectedRsu.properties.serial_number ? selectedRsu.properties.serial_number : 'Unknown'}
                  </Typography>
                </Box>
                <ConditionalRenderRsuStatusMonitor>
                  <Button onClick={() => handlePopupClick(selectedRsu.properties.ipv4_address)}>
                    View RSU Status Charts
                  </Button>
                </ConditionalRenderRsuStatusMonitor>
              </Box>
            </Popup>
          ) : null}
          {activeLayers.includes(MAP_LAYERS.HAAS_ALERT.id) && haasLocationData.data && (
            <HaasAlertVisualization
              menuSelection={menuSelection}
              haasLocationData={haasLocationData}
              theme={theme}
              selectedIncident={selectedHaasIncident}
              onIncidentClose={() => setSelectedHaasIncident(null)}
            />
          )}
          {activeLayers.includes(MAP_LAYERS.RSU.id) && (
            <RsuMapLayer data={rsuPointData} />
          )}
        </Map>
      </Container>

      {activeLayers.includes(MAP_LAYERS.MSG_VIEWER.id) &&
        (filter && geoMsgData.length > 0 ? (
          <div className="filterControl" style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}>
            <div id="timeContainer" style={{ textAlign: 'center' }}>
              <p id="timeHeader">
                {msgViewerSliderStartDate.toLocaleString([], dateTimeOptions)} -{' '}
                {msgViewerSliderEndDate.toLocaleTimeString([], dateTimeOptions)}
              </p>
            </div>
            <div id="sliderContainer" style={{ margin: '5px 10px' }}>
              <Slider
                min={0}
                max={geoMsgFilterMaxOffset}
                value={filterOffset}
                onChange={(_: Event, value: number | number[]) => {
                  dispatch(setGeoMsgFilterOffset(value as number))
                }}
                step={1}
                valueLabelDisplay="auto"
                valueLabelFormat={() => {
                  const { start, end } = calculateTimeWindow(startGeoMsgDate, filterOffset, filterStep)
                  return `${start.toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                  })} - ${end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
                }}
                marks={calculateDataAvailability.map(({ offset }) => ({
                  value: offset,
                  label: '', // No numbers, just bars
                }))}
                style={{ width: '100%' }}
              />
            </div>
            <div id="controlContainer">
              <Select
                id="stepSelect"
                onChange={(e) => {
                  const newStep = Number(e.target.value)
                  const maxOffset = geoMsgFilterMaxOffset

                  // Adjust offset if it would exceed the new maximum
                  if (filterOffset > maxOffset) {
                    dispatch(setGeoMsgFilterOffset(maxOffset))
                  }

                  dispatch(setGeoMsgFilterStep(newStep))
                }}
                value={stepValueToOption(filterStep)?.value?.toString()}
              >
                {stepOptions.map((option) => {
                  return (
                    <MenuItem value={option.value} key={option.value}>
                      {option.label}
                    </MenuItem>
                  )
                })}
              </Select>

              <Button variant="contained" size="small" onClick={() => dispatch(setGeoMsgFilter(false))}>
                <Typography fontSize="small">New Search</Typography>
              </Button>
            </div>
          </div>
        ) : filter && geoMsgData.length === 0 ? (
          <div className="filterControl" style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}>
            <div id="timeContainer">
              <Typography fontSize="small">
                No data found for the selected date range. Please try a new search with a different date range.
              </Typography>
            </div>
            <div id="controlContainer">
              <Button variant="contained" onClick={() => dispatch(setGeoMsgFilter(false))}>
                New Search
              </Button>
            </div>
          </div>
        ) : (
          <Paper className="control" style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}>
            <div className="buttonContainer">
              <Button
                variant="outlined"
                color="info"
                size="small"
                onClick={(e) => handleButtonToggle(e, 'msgViewer')}
                sx={{
                  position: 'absolute',
                  top: '10px',
                  left: '10px',
                }}
                className="museo-slab capital-case"
              >
                Add Point
              </Button>
              <Button
                variant="outlined"
                color="info"
                size="small"
                sx={{
                  position: 'absolute',
                  top: '10px',
                  right: '10px',
                }}
                onClick={() => {
                  dispatch(clearGeoMsg())
                }}
                className="museo-slab capital-case"
              >
                Clear
              </Button>
            </div>
            <div
              style={{
                marginBottom: '12px',
              }}
            >
              <FormControl fullWidth>
                <InputLabel htmlFor="message-type">Message Type</InputLabel>
                <Select
                  id="message-type"
                  label="Message Type"
                  value={geoMsgType}
                  sx={{ width: '100%' }}
                  onChange={(event) => dispatch(changeGeoMsgType(event.target.value as MessageType))}
                >
                  {messageTypeOptions.map((option) => {
                    return (
                      <MenuItem value={option.value} key={option.value}>
                        {option.label}
                      </MenuItem>
                    )
                  })}
                </Select>
              </FormControl>
            </div>
            <div style={{ marginBottom: 15 }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  label="Select start date"
                  value={dayjs(startGeoMsgDate)}
                  maxDateTime={dayjs(endGeoMsgDate)}
                  onChange={(e) => {
                    if (e !== null) {
                      dateChanged(e.toDate(), 'start')
                    }
                  }}
                />
              </LocalizationProvider>
            </div>
            <div style={{ marginBottom: 15 }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  label="Select end date"
                  value={dayjs(endGeoMsgDate === '' ? new Date() : endGeoMsgDate)}
                  minDateTime={startGeoMsgDate === '' ? null : dayjs(startGeoMsgDate)}
                  maxDateTime={dayjs(new Date())}
                  onChange={(e) => {
                    if (e !== null) {
                      dateChanged(e.toDate(), 'end')
                    }
                  }}
                />
              </LocalizationProvider>
            </div>
            <div>
              <Button
                variant="contained"
                size="small"
                disabled={isGeoMsgLoading}
                onClick={handleGeoMsgDataSubmit}
                className="museo-slab capital-case"
              >
                Submit
              </Button>
            </div>
          </Paper>
        ))}
      <RsuStatusDialog
        open={statusDialogOpen}
        onClose={() => setStatusDialogOpen(false)}
        rsuIp={selectedRsuIp}
        token={token}
      />
    </div>
  )
}

const configFillLayer: FillLayer = {
  id: 'configFill',
  type: 'fill',
  source: 'polygonSource',
  layout: {},
  paint: {
    'fill-color': '#0080ff',
    'fill-opacity': 0.2,
  },
}

const getConfigOutlineLayer = (isEditing: boolean): LineLayer => ({
  id: 'configMsgOutline',
  type: 'line',
  source: 'polygonSource',
  layout: {},
  paint: {
    'line-color': '#000',
    'line-width': 3,
    'line-dasharray': isEditing ? [2, 2] : undefined,
  },
})

const configPointLayer: CircleLayer = {
  id: 'configPointLayer',
  type: 'circle',
  source: 'pointSource',
  paint: {
    'circle-radius': 5,
    'circle-color': 'rgb(255, 0, 0)',
  },
}

const geoMsgFillLayer: FillLayer = {
  id: 'geoMsgFill',
  type: 'fill',
  source: 'polygonSource',
  layout: {},
  paint: {
    'fill-color': '#0080ff',
    'fill-opacity': 0.2,
  },
}

const getGeoMsgOutlineLayer = (isEditing: boolean): LineLayer => ({
  id: 'geoMsgOutline',
  type: 'line',
  source: 'polygonSource',
  layout: {},
  paint: {
    'line-color': '#000',
    'line-width': 3,
    'line-dasharray': isEditing ? [2, 2] : undefined,
  },
})

const geoMsgPolygonPointLayer: CircleLayer = {
  id: 'geoMsgPolygonPointLayer',
  type: 'circle',
  source: 'pointSource',
  paint: {
    'circle-radius': 5,
    'circle-color': 'rgb(255, 164, 0)',
  },
}

const geoMsgPointLayer: CircleLayer = {
  id: 'geoMsgPointLayer',
  type: 'circle',
  source: 'pointSource',
  paint: {
    'circle-radius': 5,
    'circle-color': [
      'match',
      ['get', 'colorIndex'],
      0,
      '#FF0000',
      1,
      '#00FF00',
      2,
      '#0000FF',
      3,
      '#FFFF00',
      4,
      '#FF00FF',
      5,
      '#00FFFF',
      6,
      '#FFA500',
      7,
      '#800080',
      8,
      '#A52A2A',
      9,
      '#008000',
      '#999999',
    ],
  },
}

const dateTimeOptions: Intl.DateTimeFormatOptions = {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
}

export default MapPage

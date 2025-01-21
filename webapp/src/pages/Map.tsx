import React, { ReactElement, useEffect, useState } from 'react'
import mapboxgl, { CircleLayer, FillLayer, LineLayer } from 'mapbox-gl' // This is a dependency of react-map-gl even if you didn't explicitly install it
import Map, { Marker, Popup, Source, Layer, LayerProps } from 'react-map-gl'
import { Container } from 'reactstrap'
import RsuMarker from '../components/RsuMarker'
import EnvironmentVars from '../EnvironmentVars'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'

import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import Slider from 'rc-slider'
import { DropdownList } from 'react-widgets'
import {
  selectRsuOnlineStatus,
  selectRsuData,
  selectRsuCounts,
  selectIssScmsStatusData,
  selectSelectedRsu,
  selectMsgType,
  selectRsuIpv4,
  selectDisplayMap,
  selectHeatMapData,
  selectAddGeoMsgPoint,
  selectGeoMsgStart,
  selectGeoMsgEnd,
  selectGeoMsgDateError,
  selectGeoMsgData,
  selectGeoMsgCoordinates,
  selectGeoMsgFilter,
  selectGeoMsgFilterStep,
  selectGeoMsgFilterOffset,

  // actions
  selectRsu,
  getRsuData,
  getIssScmsStatus,
  getRsuLastOnline,
  toggleGeoMsgPointSelect,
  clearGeoMsg,
  updateGeoMsgPoints,
  updateGeoMsgData,
  updateGeoMsgDate,
  setGeoMsgFilter,
  setGeoMsgFilterStep,
  setGeoMsgFilterOffset,
  changeGeoMsgType,
  selectGeoMsgType,
} from '../generalSlices/rsuSlice'
import { selectWzdxData, getWzdxData } from '../generalSlices/wzdxSlice'
import { selectOrganizationName } from '../generalSlices/userSlice'
import { SecureStorageManager } from '../managers'
import {
  selectConfigCoordinates,
  toggleConfigPointSelect,
  selectAddConfigPoint,
  updateConfigPoints,
  geoRsuQuery,
  clearConfig,
  clearFirmware,
} from '../generalSlices/configSlice'
import { useSelector, useDispatch } from 'react-redux'
import ClearIcon from '@mui/icons-material/Clear'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import ExpandLessIcon from '@mui/icons-material/ExpandLess'
import {
  Button,
  FormGroup,
  Grid2,
  IconButton,
  Switch,
  StyledEngineProvider,
  Tooltip,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
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
  Collapse,
} from '@mui/material'

import 'rc-slider/assets/index.css'
import './css/MsgMap.css'
import './css/Map.css'
import { WZDxFeature, WZDxWorkZoneFeed } from '../models/wzdx/WzdxWorkZoneFeed42'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import {
  intersectionMapLabelsLayer,
  selectIntersections,
  selectSelectedIntersection,
  setSelectedIntersectionId,
} from '../generalSlices/intersectionSlice'
import { evaluateFeatureFlags } from '../feature-flags'
import { headerTabHeight } from '../styles/index'
import { selectViewState, setMapViewState } from './mapSlice'
import { setDisplay } from '../features/menu/menuSlice'
import { MapLayer } from '../models/MapLayer'

// @ts-ignore: workerClass does not exist in typed mapboxgl
// eslint-disable-next-line import/no-webpack-loader-syntax
mapboxgl.workerClass = require('worker-loader!mapbox-gl/dist/mapbox-gl-csp-worker').default

const { DateTime } = require('luxon')

interface MapPageProps {
  auth: boolean
}

function MapPage(props: MapPageProps) {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const theme = useTheme()

  const mapRef = React.useRef(null)

  const organization = useSelector(selectOrganizationName)
  const rsuData = useSelector(selectRsuData)
  const rsuCounts = useSelector(selectRsuCounts)
  const selectedRsu = useSelector(selectSelectedRsu)
  const countsMsgType = useSelector(selectMsgType)
  const issScmsStatusData = useSelector(selectIssScmsStatusData)
  const rsuOnlineStatus = useSelector(selectRsuOnlineStatus)
  const rsuIpv4 = useSelector(selectRsuIpv4)
  const displayMap = useSelector(selectDisplayMap)
  const addConfigPoint = useSelector(selectAddConfigPoint)
  const configCoordinates = useSelector(selectConfigCoordinates)
  const geoMsgType = useSelector(selectGeoMsgType)

  const heatMapData = useSelector(selectHeatMapData)

  const geoMsgData = useSelector(selectGeoMsgData)
  const geoMsgCoordinates = useSelector(selectGeoMsgCoordinates)
  const addGeoMsgPoint = useSelector(selectAddGeoMsgPoint)
  const startGeoMsgDate = useSelector(selectGeoMsgStart)
  const endGeoMsgDate = useSelector(selectGeoMsgEnd)
  const msgViewerDateError = useSelector(selectGeoMsgDateError)

  const filter = useSelector(selectGeoMsgFilter)
  const filterStep = useSelector(selectGeoMsgFilterStep)
  const filterOffset = useSelector(selectGeoMsgFilterOffset)

  const wzdxData = useSelector(selectWzdxData)

  const intersectionsList = useSelector(selectIntersections)
  const selectedIntersection = useSelector(selectSelectedIntersection)

  // Mapbox local state variables

  const viewState = useSelector(selectViewState)

  // RSU layer local state variables
  const [selectedRsuCount, setSelectedRsuCount] = useState(null)
  const [displayType, setDisplayType] = useState('none')

  // Menu local state variable
  const [displayMenu, setDisplayMenu] = useState(false)
  const [menuSelection, setMenuSelection] = useState([])

  // Add these new state variables near the other source states
  const [previewPoint, setPreviewPoint] = useState<GeoJSON.Feature<GeoJSON.Point> | null>(null)

  const [configPolygonSource, setConfigPolygonSource] = useState<GeoJSON.Feature<GeoJSON.Geometry>>({
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: [],
    },
    properties: {},
  })
  const [configPointSource, setConfigPointSource] = useState<GeoJSON.FeatureCollection<GeoJSON.Geometry>>({
    type: 'FeatureCollection',
    features: [],
  })

  // BSM layer local state variables
  const [geoMsgPolygonSource, setGeoMsgPolygonSource] = useState<GeoJSON.Feature<GeoJSON.Geometry>>({
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: [],
    },
    properties: {},
  })
  const [bsmPointSource, setMsgPointSource] = useState<GeoJSON.FeatureCollection<GeoJSON.Geometry>>({
    type: 'FeatureCollection',
    features: [],
  })

  // baseDate is only used to set the startDate from a Date object
  const [baseDate, setBaseDate] = useState(new Date(startGeoMsgDate))

  const [msgViewerSliderStartDate, setMsgViewerSliderStartDate] = useState(
    new Date(baseDate.getTime() + 60000 * filterOffset * filterStep)
  )
  const [msgViewerSliderEndDate, setMsgViewerSliderEndDate] = useState(
    new Date(msgViewerSliderStartDate.getTime() + 60000 * filterStep)
  )

  // stepOptions is used to set the step options for the message viewer
  const stepOptions = [
    { value: 1, label: '1 minute' },
    { value: 5, label: '5 minutes' },
    { value: 15, label: '15 minutes' },
    { value: 30, label: '30 minutes' },
    { value: 60, label: '60 minutes' },
  ]
  // const [selectedOption, setSelectedOption] = useState({ value: 60, label: '60 minutes' })

  function stepValueToOption(val: number) {
    for (var i = 0; i < stepOptions.length; i++) {
      if (stepOptions[i].value === val) {
        return stepOptions[i]
      }
    }
  }

  // WZDx layer local state variables
  const [selectedWZDxMarkerIndex, setSelectedWZDxMarkerIndex] = useState(null)
  const [selectedWZDxMarker, setSelectedWZDxMarker] = useState(null)
  const [wzdxMarkers, setWzdxMarkers] = useState([])
  const [pageOpen, setPageOpen] = useState(true)

  const [activeLayers, setActiveLayers] = useState(
    [{ id: 'rsu-layer', tag: 'rsu' as FEATURE_KEY }]
      .filter((layer) => evaluateFeatureFlags(layer.tag))
      .map((layer) => layer.id)
  )
  const [expandedLayers, setExpandedLayers] = useState<string[]>([])

  // Vendor filter local state variable
  const [selectedVendor, setSelectedVendor] = useState('Select Vendor')
  const vendorArray: string[] = ['Select Vendor', 'Commsignia', 'Yunex', 'Kapsch']
  const setVendor = (newVal) => {
    setSelectedVendor(newVal)
  }

  const mbStyle = require(`../styles/${theme.palette.custom.mapStyleFilePath}`)

  // useEffects for Mapbox
  useEffect(() => {
    const listener = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        dispatch(selectRsu(null))
        dispatch(clearFirmware())
        setSelectedWZDxMarkerIndex(null)
      }
    }
    window.addEventListener('keydown', listener)

    return () => {
      window.removeEventListener('keydown', listener)
    }
  }, [selectedRsu, dispatch, setSelectedWZDxMarkerIndex])

  // useEffects for RSU layer
  useEffect(() => {
    dispatch(getRsuData())
    dispatch(selectRsu(null))
    dispatch(clearFirmware())
  }, [organization, dispatch])

  // useEffects for BSM layer
  useEffect(() => {
    const localBaseDate = new Date(startGeoMsgDate)
    const localStartDate = new Date(localBaseDate.getTime() + 60000 * filterOffset * filterStep)
    const localEndDate = new Date(new Date(localStartDate).getTime() + 60000 * filterStep)
    // setBaseDate(localBaseDate)
    setMsgViewerSliderStartDate(localStartDate)
    setMsgViewerSliderEndDate(localEndDate)

    console.log('Date range:', {
      base: localBaseDate.toISOString(),
      start: localStartDate.toISOString(),
      end: localEndDate.toISOString(),
      filterOffset,
      filterStep,
    })
  }, [startGeoMsgDate, filterOffset, filterStep])

  useEffect(() => {
    if (!startGeoMsgDate) {
      dateChanged(new Date(), 'start')
    }
    if (!endGeoMsgDate) {
      dateChanged(new Date(), 'end')
    }
  }, [])

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

  // Effect for handling polygon updates
  useEffect(() => {
    if (!activeLayers.includes('msg-viewer-layer')) return
    const pointSourceFeatures: Array<GeoJSON.Feature<GeoJSON.Geometry>> = []

    geoMsgCoordinates.forEach((point) => {
      pointSourceFeatures.push(createPointFeature(point))
    })

    setMsgPointSource((prevPointSource) => ({
      ...prevPointSource,
      features: pointSourceFeatures,
    }))

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

    setGeoMsgPolygonSource(
      (prevPolygonSource) =>
        ({
          ...prevPolygonSource,
          geometry: {
            type: polygonCoords.length === 2 ? 'LineString' : 'Polygon', // Use LineString for 2 points
            coordinates: polygonCoords.length === 2 ? polygonCoords : [polygonCoords],
          },
        } as GeoJSON.Feature<GeoJSON.Geometry>)
    )
  }, [geoMsgCoordinates, activeLayers, addGeoMsgPoint, previewPoint])

  // Effect for handling point source updates
  useEffect(() => {
    // if the msg-viewer-layer is not active, exit the effect
    if (!activeLayers.includes('msg-viewer-layer')) return

    const pointSourceFeatures: Array<GeoJSON.Feature<GeoJSON.Geometry>> = []

    // Handle case when we have message data
    if ((geoMsgData?.length ?? 0) > 0) {
      const firstMessageDate = new Date(geoMsgData[0]['properties']['time'])
      const lastMessageDate = new Date(geoMsgData[geoMsgData.length - 1]['properties']['time'])

      // Update date range if filter is enabled
      if (filter) {
        dateChanged(firstMessageDate, 'start')
        dateChanged(lastMessageDate, 'end')
      }

      // Filter messages within the selected time range
      geoMsgData.forEach((message) => {
        const messageDate = new Date(message['properties']['time'])
        if (isDateInRange(messageDate, msgViewerSliderStartDate, msgViewerSliderEndDate)) {
          pointSourceFeatures.push(message)
        }
      })
    }

    setMsgPointSource((prevPointSource) => ({
      ...prevPointSource,
      features: pointSourceFeatures,
    }))
  }, [geoMsgData, msgViewerSliderStartDate, msgViewerSliderEndDate, activeLayers, filter])

  // Helper function to calculate the maximum offset based on the start and end dates and the step
  const calculateMaxOffset = (start: string | Date, end: string | Date, step: number) => {
    return Math.floor((new Date(end).getTime() - new Date(start).getTime()) / (step * 60000))
  }

  useEffect(() => {
    if (activeLayers.includes('rsu-layer')) {
      setConfigPolygonSource((prevPolygonSource) => {
        return {
          ...prevPolygonSource,
          geometry: {
            ...prevPolygonSource.geometry,
            coordinates: [[...configCoordinates]],
          },
        } as GeoJSON.Feature<GeoJSON.Geometry>
      })
      const pointSourceFeatures = [] as Array<GeoJSON.Feature<GeoJSON.Geometry>>
      configCoordinates.forEach((point) => {
        pointSourceFeatures.push({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [...point],
          },
          properties: {},
        })
      })

      setConfigPointSource((prevPointSource) => {
        return { ...prevPointSource, features: pointSourceFeatures }
      })
    }
  }, [configCoordinates, activeLayers])

  function dateChanged(e: Date, type: 'start' | 'end') {
    try {
      let date = DateTime.fromISO(e.toISOString())
      date.setZone(DateTime.local().zoneName)
      console.log('dateChanged', type, date.toString())
      dispatch(updateGeoMsgDate({ type, date: date.toString() }))
    } catch (err) {
      console.error('Encountered issue updating date: ', err.message)
    }
  }

  const addGeoMsgPointToCoordinates = (point: { lat: number; lng: number }) => {
    const pointArray = [point.lng, point.lat]
    if (geoMsgCoordinates.length > 1) {
      if (geoMsgCoordinates[0] === geoMsgCoordinates.slice(-1)[0]) {
        let tmp = [...geoMsgCoordinates]
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
        let tmp = [...configCoordinates]
        tmp.pop()
        dispatch(updateConfigPoints([...tmp, pointArray, configCoordinates[0]]))
      } else {
        dispatch(updateConfigPoints([...configCoordinates, pointArray, configCoordinates[0]]))
      }
    } else {
      dispatch(updateConfigPoints([...configCoordinates, pointArray]))
    }
  }

  // useEffects for WZDx layers
  useEffect(() => {
    if (selectedWZDxMarkerIndex !== null) setSelectedWZDxMarker(wzdxMarkers[selectedWZDxMarkerIndex])
    else setSelectedWZDxMarker(null)
  }, [selectedWZDxMarkerIndex, wzdxMarkers])

  useEffect(() => {
    function createPopupTable(data: Array<Array<string>>) {
      let rows = []
      for (var i = 0; i < data.length; i++) {
        let rowID = `row${i}`
        let cell = []
        for (var idx = 0; idx < 2; idx++) {
          let cellID = `cell${i}-${idx}`
          if (i == 0) {
            cell.push(
              <th key={cellID} id={cellID} style={{ minWidth: '120px' }}>
                {data[i][idx]}
              </th>
            )
          } else {
            cell.push(
              <td key={cellID} id={cellID} style={{ minWidth: '120px' }}>
                <pre>{data[i][idx]}</pre>
              </td>
            )
          }
        }
        rows.push(
          <tr key={i} id={rowID}>
            {cell}
          </tr>
        )
      }
      return (
        <div className="container">
          <table id="simple-board">
            <tbody>{rows}</tbody>
          </table>
        </div>
      )
    }

    function getWzdxTable(obj: WZDxFeature): string[][] {
      let arr = []
      arr.push(['road_name', obj['properties']['core_details']['road_names'][0]])
      arr.push(['direction', obj['properties']['core_details']['direction']])
      arr.push(['vehicle_impact', obj['properties']['vehicle_impact']])
      arr.push(['workers_present', obj['properties']['worker_presence']?.['are_workers_present'].toString()])
      arr.push(['description', break_line(obj['properties']['core_details']['description'])])
      arr.push(['start_date', obj['properties']['start_date']])
      arr.push(['end_date', obj['properties']['end_date']])
      return arr
    }

    function openPopup(index: number) {
      setSelectedWZDxMarkerIndex(index)
      dispatch(selectRsu(null))
      dispatch(clearFirmware())
    }

    function customMarker(feature: GeoJSON.Feature<GeoJSON.Geometry>, index: number, lat: number, lng: number) {
      return (
        <Marker
          key={feature.id}
          latitude={lat}
          longitude={lng}
          {...{ offsetLeft: -30, offsetTop: -30, feature: feature, index: index }} // Avoid typescript errors. TODO: Make sure this does something
          onClick={(e) => {
            e.originalEvent.stopPropagation()
          }}
        >
          <div onClick={() => openPopup(index)}>
            <img src="/workzone_icon.png" height={60} alt="Work Zone Icon" />
          </div>
        </Marker>
      )
    }

    const getAllMarkers = (wzdxData: WZDxWorkZoneFeed) => {
      if (wzdxData?.features?.length > 0) {
        var i = -1
        var markers = wzdxData.features.map((feature) => {
          const localFeature: WZDxFeature = { ...feature, geometry: { ...feature.geometry, type: 'LineString' } }
          var center_coords_index = Math.round(feature.geometry.coordinates.length / 2)
          var lng = feature.geometry.coordinates[0][0]
          var lat = feature.geometry.coordinates[0][1]
          if (center_coords_index !== 1) {
            lat = feature.geometry.coordinates[center_coords_index][1]
            lng = feature.geometry.coordinates[center_coords_index][0]
          } else {
            lat = (feature.geometry.coordinates[0][1] + feature.geometry.coordinates[1][1]) / 2
            lng = (feature.geometry.coordinates[0][0] + feature.geometry.coordinates[1][0]) / 2
          }
          i++
          localFeature.properties = { ...feature.properties }
          localFeature.properties.table = createPopupTable(getWzdxTable(feature))
          return customMarker(localFeature, i, lat, lng)
        })
        return markers
      } else {
        return []
      }
    }

    setWzdxMarkers(getAllMarkers(wzdxData))
  }, [dispatch, wzdxData])

  function break_line(val: string) {
    var arr = []
    var remainingData = ''
    var maxLineLength = 40
    for (var i = 0; i < val.length; i += maxLineLength) {
      var data = remainingData + val.substring(i, i + maxLineLength)
      var index = data.lastIndexOf(' ')
      if (data[0] == ' ') {
        data = data.substring(1, data.length)
        remainingData = data.substring(index, data.length)
      } else if (data?.[i + maxLineLength + 1] == ' ') {
        remainingData = data.substring(index + 1, data.length)
      } else if (data[index] == ' ') {
        remainingData = data.substring(index + 1, data.length)
      }
      arr.push(data.substring(0, index))
    }
    return arr.join('\n')
  }

  function closePopup() {
    setSelectedWZDxMarkerIndex(null)
  }

  function getStops() {
    // populate tmp array with rsuCounts to get max count value
    let max = Math.max(...Object.entries(rsuCounts).map(([, value]) => value.count))
    let stopsArray = [[0, 0.25]]
    let weight = 0.5
    for (let i = 1; i < max; i += 500) {
      stopsArray.push([i, weight])
      weight += 0.25
    }
    return stopsArray
  }

  const isOnline = () => {
    return rsuIpv4 in rsuOnlineStatus && rsuOnlineStatus[rsuIpv4].hasOwnProperty('last_online')
      ? rsuOnlineStatus[rsuIpv4].last_online
      : 'No Data'
  }

  const getStatus = () => {
    return rsuIpv4 in rsuOnlineStatus && rsuOnlineStatus[rsuIpv4].hasOwnProperty('current_status')
      ? rsuOnlineStatus[rsuIpv4].current_status
      : 'Offline'
  }

  const handleScmsStatus = () => {
    dispatch(getIssScmsStatus())
    setDisplayType('scms')
  }

  const handleOnlineStatus = () => {
    setDisplayType('online')
  }

  const handleNoneStatus = () => {
    setDisplayType('none')
  }

  const handleRsuDisplayTypeChange = (event: React.SyntheticEvent) => {
    const target = event.target as HTMLInputElement
    if (target.value === 'online') handleOnlineStatus()
    else if (target.value === 'scms') handleScmsStatus()
    else if (target.value === 'none') handleNoneStatus()
  }

  const toggleExpandLayer = (layerId: string) => {
    setExpandedLayers((prev) => (prev.includes(layerId) ? prev.filter((id) => id !== layerId) : [...prev, layerId]))
  }

  const layers: MapLayer[] = [
    {
      id: 'rsu-layer',
      label: 'RSU Viewer',
      type: 'symbol',
      tag: 'rsu',
      control: (
        <>
          <Typography variant="h6">RSU Status</Typography>
          <FormControl sx={{ ml: 2, mt: 1 }}>
            <RadioGroup value={displayType} onChange={handleRsuDisplayTypeChange}>
              {[
                { key: 'none', label: 'None' },
                { key: 'online', label: 'Online Status' },
                { key: 'scms', label: 'SCMS Status' },
              ].map((val) => (
                <FormControlLabel
                  value={val.key}
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
        </>
      ),
    },
    {
      id: 'heatmap-layer',
      label: 'Heatmap',
      type: 'heatmap',
      maxzoom: 14,
      source: 'heatMapData',
      paint: {
        'heatmap-weight': {
          property: 'count',
          type: 'exponential',
          stops: getStops(),
        },
        'heatmap-intensity': ['interpolate', ['linear'], ['zoom'], 0, 0, 10, 1, 13, 2],
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
    {
      id: 'wzdx-layer',
      label: 'WZDx Viewer',
      type: 'line',
      tag: 'wzdx',
      paint: {
        'line-color': '#F29543',
        'line-width': 8,
      },
    },
    {
      id: 'intersection-layer',
      label: 'Intersections',
      type: 'symbol',
      tag: 'intersection',
    },
  ]

  const mapboxLayers = theme.palette.custom.mapStyleHasTraffic
    ? [
        {
          label: 'Mapbox Traffic',
          ids: [
            'traffic-tunnel-link-navigation',
            'traffic-tunnel-minor-navigation',
            'traffic-tunnel-street-navigation',
            'traffic-tunnel-secondary-tertiary-navigation',
            'traffic-tunnel-primary-navigation',
            'traffic-tunnel-major-link-navigation',
            'traffic-tunnel-motorway-trunk-navigation',
            'traffic-bridge-road-link-navigation',
            'traffic-bridge-road-minor-navigation',
            'traffic-bridge-road-street-navigation',
            'traffic-bridge-road-secondary-tertiary-navigation',
            'traffic-bridge-road-primary-navigation',
            'traffic-bridge-road-major-link-navigation',
            'traffic-bridge-road-motorway-trunk-case-navigation',
            'traffic-bridge-road-motorway-trunk-navigation',
          ],
        },
        {
          label: 'Mapbox Incidents',
          ids: [
            'incident-closure-lines-navigation',
            'incident-closure-line-highlights-navigation',
            'incident-endpoints-navigation',
            'incident-startpoints-navigation',
          ],
        },
      ]
    : []

  const Legend = () => {
    const toggleLayer = (id: string) => {
      if (activeLayers.includes(id)) {
        if (id === 'rsu-layer') {
          dispatch(selectRsu(null))
          dispatch(clearFirmware())
          setSelectedRsuCount(null)
        } else if (id === 'wzdx-layer') {
          setSelectedWZDxMarkerIndex(null)
        }
        setActiveLayers(activeLayers.filter((layerId) => layerId !== id))
      } else {
        if (id === 'wzdx-layer' && wzdxData?.features?.length === 0) {
          dispatch(getWzdxData())
        }
        setActiveLayers([...activeLayers, id])
      }
    }

    return (
      <FormGroup>
        {layers.map((layer) => (
          <div key={layer.id}>
            <Typography fontSize="small" display="flex" alignItems="center">
              {layer.control && (
                <IconButton
                  onClick={() => toggleExpandLayer(layer.id)}
                  size="small"
                  edge="start"
                  aria-label={expandedLayers.includes(layer.id) ? 'Collapse' : 'Expand'}
                >
                  {expandedLayers.includes(layer.id) ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                </IconButton>
              )}
              <FormControlLabel
                onClick={() => toggleLayer(layer.id)}
                label={layer.label}
                control={<Checkbox checked={activeLayers.includes(layer.id)} />}
              />
            </Typography>
            {layer.control && <Collapse in={expandedLayers.includes(layer.id)}>{layer.control}</Collapse>}
          </div>
        ))}
      </FormGroup>
    )
  }

  const handleButtonToggle = (event: React.SyntheticEvent<Element, Event>, origin: 'config' | 'msgViewer') => {
    console.log('handleButtonToggle', origin)
    if (origin === 'config') {
      dispatch(toggleConfigPointSelect())
      if (addGeoMsgPoint) dispatch(toggleGeoMsgPointSelect())
    } else if (origin === 'msgViewer') {
      dispatch(toggleGeoMsgPointSelect())
      if (addConfigPoint) {
        console.log('addConfigPoint', addConfigPoint)
        dispatch(toggleConfigPointSelect())
      }
    }
  }

  const messageViewerTypes = EnvironmentVars.getMessageViewerTypes()
  const messageTypeOptions = messageViewerTypes.map((type) => {
    return { value: type, label: type }
  })

  const handleMenuSelection = (label: string) => {
    if (menuSelection.includes(label)) {
      setMenuSelection(menuSelection.filter((item) => item !== label))
      switch (label) {
        case 'Display Message Counts':
          dispatch(setDisplay({ view: 'tab', display: '' }))
          break
        case 'Display RSU Status':
          dispatch(setDisplay({ view: 'tab', display: '' }))
          break
        case 'V2x Message Viewer':
          setActiveLayers(activeLayers.filter((layerId) => layerId !== 'msg-viewer-layer'))
      }
    } else {
      setMenuSelection([...menuSelection, label])
      switch (label) {
        case 'Display Message Counts':
          if (menuSelection.includes('Display RSU Status')) {
            setMenuSelection([
              ...menuSelection.filter((item) => item !== 'Display RSU Status'),
              'Display Message Counts',
            ])
          }
          dispatch(setDisplay({ view: 'tab', display: 'displayCounts' }))
          break
        case 'Display RSU Status':
          if (menuSelection.includes('Display Message Counts')) {
            setMenuSelection([
              ...menuSelection.filter((item) => item !== 'Display Message Counts'),
              'Display RSU Status',
            ])
          }
          dispatch(setDisplay({ view: 'tab', display: 'displayRsuErrors' }))
          break
        case 'V2x Message Viewer':
          setActiveLayers([...activeLayers, 'msg-viewer-layer'])
      }
    }
  }

  return (
    <div className="container">
      <div className="menu-container">
        <Accordion
          style={{ backgroundColor: alpha(theme.palette.custom.mapMenuBackground, 80) }}
          disableGutters={true}
          className="menuAccordion"
          sx={{ '&.accordion': { marginBottom: 0 } }}
        >
          <AccordionSummary
            expandIcon={<ExpandMoreIcon style={{ color: theme.palette.text.primary }} />}
            aria-controls="panel1-content"
            id="panel1-header"
          >
            <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="medium" color={theme.palette.text.primary}>
              Layers
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Legend />
          </AccordionDetails>
        </Accordion>
        <Accordion
          style={{ backgroundColor: alpha(theme.palette.custom.mapMenuBackground, 80) }}
          disableGutters={true}
          className="menuAccordion"
          sx={{ '&.accordion': { marginBottom: 0 } }}
        >
          <AccordionSummary
            expandIcon={<ExpandMoreIcon style={{ color: theme.palette.text.primary }} />}
            aria-controls="panel2-content"
            id="panel2-header"
          >
            <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="medium" color={theme.palette.text.primary}>
              Map Controls
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <List>
              <ListItem disablePadding>
                <ListItemButton
                  onClick={() => handleMenuSelection('Display Message Counts')}
                  sx={{
                    backgroundColor: menuSelection.includes('Display Message Counts')
                      ? theme.palette.custom.mapMenuItemBackgroundSelected
                      : theme.palette.custom.mapMenuBackground,
                    borderBottom: menuSelection.includes('Display Message Counts')
                      ? theme.palette.custom.mapMenuItemBorderSelected
                      : 'none',
                    ':hover': {
                      backgroundColor: menuSelection.includes('Display Message Counts')
                        ? theme.palette.custom.mapMenuItemHoverSelected
                        : theme.palette.custom.mapMenuItemHoverUnselected,
                    },
                  }}
                >
                  <ListItemText primary="Display Message Counts" />
                </ListItemButton>
              </ListItem>
              <ListItem disablePadding>
                <ListItemButton
                  onClick={() => handleMenuSelection('Display RSU Status')}
                  sx={{
                    backgroundColor: menuSelection.includes('Display RSU Status')
                      ? theme.palette.custom.mapMenuItemBackgroundSelected
                      : theme.palette.custom.mapMenuBackground,
                    borderBottom: menuSelection.includes('Display RSU Status')
                      ? theme.palette.custom.mapMenuItemBorderSelected
                      : 'none',
                    ':hover': {
                      backgroundColor: menuSelection.includes('Display RSU Status')
                        ? theme.palette.custom.mapMenuItemHoverSelected
                        : theme.palette.custom.mapMenuItemHoverUnselected,
                    },
                  }}
                >
                  <ListItemText primary="Display RSU Status" />
                </ListItemButton>
              </ListItem>
              <ListItem disablePadding>
                <ListItemButton
                  onClick={() => handleMenuSelection('V2x Message Viewer')}
                  sx={{
                    backgroundColor: menuSelection.includes('V2x Message Viewer')
                      ? theme.palette.custom.mapMenuItemBackgroundSelected
                      : theme.palette.custom.mapMenuBackground,
                    borderBottom: menuSelection.includes('V2x Message Viewer')
                      ? theme.palette.custom.mapMenuItemBorderSelected
                      : 'none',
                    ':hover': {
                      backgroundColor: menuSelection.includes('V2x Message Viewer')
                        ? theme.palette.custom.mapMenuItemHoverSelected
                        : theme.palette.custom.mapMenuItemHoverUnselected,
                    },
                  }}
                >
                  <ListItemText primary="Display V2X Message Viewer" />
                </ListItemButton>
              </ListItem>
              {SecureStorageManager.getUserRole() === 'admin' && (
                <ListItem disablePadding>
                  <ListItemButton
                    onClick={() => handleMenuSelection('Configure RSUs')}
                    sx={{
                      backgroundColor: menuSelection.includes('Configure RSUs')
                        ? theme.palette.custom.mapMenuItemBackgroundSelected
                        : theme.palette.custom.mapMenuBackground,
                      borderBottom: menuSelection.includes('Configure RSUs')
                        ? theme.palette.custom.mapMenuItemBorderSelected
                        : 'none',
                      ':hover': {
                        backgroundColor: menuSelection.includes('Configure RSUs')
                          ? theme.palette.custom.mapMenuItemHoverSelected
                          : theme.palette.custom.mapMenuItemHoverUnselected,
                      },
                    }}
                  >
                    <ListItemText primary="Configure RSUs" />
                  </ListItemButton>
                </ListItem>
              )}
            </List>
          </AccordionDetails>
        </Accordion>
        <Accordion
          style={{ backgroundColor: alpha(theme.palette.custom.mapMenuBackground, 80) }}
          disableGutters={true}
          className="menuAccordion"
          sx={{ '&.accordion': { marginBottom: 0 } }}
        >
          <AccordionSummary
            expandIcon={<ExpandMoreIcon style={{ color: theme.palette.text.primary }} />}
            aria-controls="panel3-content"
            id="panel3-header"
          >
            <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="medium" color={theme.palette.text.primary}>
              Filter RSUs
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <ListItem>
              <DropdownList
                dataKey="id"
                textField="name"
                data={vendorArray}
                value={selectedVendor}
                onChange={(value) => {
                  setVendor(value)
                }}
                style={{ width: '100%' }}
              />
            </ListItem>
          </AccordionDetails>
        </Accordion>
      </div>
      {SecureStorageManager.getUserRole() === 'admin' && menuSelection.includes('Configure RSUs') && (
        <>
          <div className="rsu-status-div" style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}>
            <h1 className="legend-header">RSU Configuration</h1>
            <StyledEngineProvider injectFirst>
              <FormGroup row className="form-group-row">
                <FormControlLabel
                  control={<Switch checked={addConfigPoint} />}
                  label={'Add Points'}
                  onChange={(e) => handleButtonToggle(e, 'config')}
                  sx={{ ml: 1 }}
                />
                {configCoordinates.length > 0 && (
                  <Tooltip title="Clear Points">
                    <IconButton
                      onClick={() => {
                        dispatch(clearConfig())
                      }}
                      size="large"
                    >
                      <ClearIcon />
                    </IconButton>
                  </Tooltip>
                )}
              </FormGroup>
              <FormGroup row>
                <Button
                  variant="contained"
                  className="contained-button"
                  sx={{
                    borderRadius: 4,
                    width: '100%',
                    '&.Mui-disabled': {
                      backgroundColor: alpha(theme.palette.primary.light, 0.5),
                    },
                  }}
                  disabled={!(configCoordinates.length > 2 && addConfigPoint)}
                  onClick={() => {
                    dispatch(geoRsuQuery(selectedVendor))
                  }}
                >
                  Configure RSUs
                </Button>
              </FormGroup>
            </StyledEngineProvider>
          </div>
        </>
      )}
      <Container
        fluid={true}
        style={{ width: '100%', height: props.auth ? 'calc(100vh - 136px)' : 'calc(100vh - 100px)', display: 'flex' }}
      >
        <Map
          {...viewState}
          ref={mapRef}
          mapboxAccessToken={EnvironmentVars.MAPBOX_TOKEN}
          mapStyle={mbStyle}
          style={{ width: '100%', height: '100%' }}
          onMove={(evt) => dispatch(setMapViewState(evt.viewState))}
          onMouseMove={(e) => {
            if ((addGeoMsgPoint || addConfigPoint) && activeLayers.includes('msg-viewer-layer')) {
              const point: GeoJSON.Feature<GeoJSON.Point> = {
                type: 'Feature',
                geometry: {
                  type: 'Point',
                  coordinates: [e.lngLat.lng, e.lngLat.lat],
                },
                properties: {},
              }
              setPreviewPoint(point)
            } else {
              setPreviewPoint(null)
            }
          }}
          onClick={(e) => {
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
          {activeLayers.includes('msg-viewer-layer') && previewPoint && (
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
          )}

          {activeLayers.includes('rsu-layer') && (
            <div>
              {configCoordinates?.length > 2 ? (
                <Source id={layers[0].id + '-fill'} type="geojson" data={configPolygonSource}>
                  <Layer {...configOutlineLayer} />
                  <Layer {...configFillLayer} />
                </Source>
              ) : null}
              <Source id={layers[0].id + '-points'} type="geojson" data={configPointSource}>
                <Layer {...configPointLayer} />
              </Source>
            </div>
          )}
          {rsuData?.map(
            (rsu) =>
              activeLayers.includes('rsu-layer') &&
              (selectedVendor === 'Select Vendor' || rsu['properties']['manufacturer_name'] === selectedVendor) && [
                <Marker
                  key={rsu.id}
                  latitude={rsu.geometry.coordinates[1]}
                  longitude={rsu.geometry.coordinates[0]}
                  onClick={(e) => {
                    e.originalEvent.stopPropagation()
                    dispatch(selectRsu(rsu))
                    setSelectedWZDxMarkerIndex(null)
                    dispatch(clearFirmware()) // TODO: Should remove??
                    dispatch(getRsuLastOnline(rsu.properties.ipv4_address))
                    dispatch(getIssScmsStatus())
                    if (rsuCounts.hasOwnProperty(rsu.properties.ipv4_address))
                      setSelectedRsuCount(rsuCounts[rsu.properties.ipv4_address].count)
                    else setSelectedRsuCount(0)
                  }}
                >
                  <button
                    className="marker-btn"
                    onClick={(e) => {
                      e.stopPropagation()
                      dispatch(selectRsu(rsu))
                      dispatch(clearFirmware()) // TODO: Should remove??
                      setSelectedWZDxMarkerIndex(null)
                      dispatch(getRsuLastOnline(rsu.properties.ipv4_address))
                      dispatch(getIssScmsStatus())
                      if (rsuCounts.hasOwnProperty(rsu.properties.ipv4_address))
                        setSelectedRsuCount(rsuCounts[rsu.properties.ipv4_address].count)
                      else setSelectedRsuCount(0)
                    }}
                  >
                    <RsuMarker
                      displayType={displayType}
                      onlineStatus={
                        rsuOnlineStatus.hasOwnProperty(rsu.properties.ipv4_address)
                          ? rsuOnlineStatus[rsu.properties.ipv4_address].current_status
                          : 'offline'
                      }
                      scmsStatus={
                        issScmsStatusData.hasOwnProperty(rsu.properties.ipv4_address) &&
                        issScmsStatusData[rsu.properties.ipv4_address]
                          ? issScmsStatusData[rsu.properties.ipv4_address].health
                          : '0'
                      }
                    />
                  </button>
                </Marker>,
              ]
          )}
          {activeLayers.includes('heatmap-layer') && (
            <Source id={layers[1].id} type="geojson" data={heatMapData}>
              <Layer {...layers[1]} />
            </Source>
          )}
          {activeLayers.includes('msg-viewer-layer') && (
            <div>
              {geoMsgCoordinates.length >= 1 ? (
                <Source id={layers[2].id + '-fill'} type="geojson" data={geoMsgPolygonSource}>
                  <Layer {...getGeoMsgOutlineLayer(addGeoMsgPoint)} />
                  <Layer {...geoMsgFillLayer} />
                </Source>
              ) : null}
              <Source id={layers[2].id + '-points'} type="geojson" data={bsmPointSource}>
                <Layer {...bsmPointLayer} />
              </Source>
            </div>
          )}
          {activeLayers.includes('wzdx-layer') && (
            <div>
              {wzdxMarkers}
              <Source id={layers[3].id} type="geojson" data={wzdxData}>
                <Layer {...layers[3]} />
              </Source>
            </div>
          )}
          {selectedWZDxMarker ? (
            <Popup
              latitude={selectedWZDxMarker.props.latitude}
              longitude={selectedWZDxMarker.props.longitude}
              {...{ altitude: 12, offsetTop: -25 }} // TODO: Make sure this does something
              onClose={closePopup}
              maxWidth={'500px'}
            >
              <div>{selectedWZDxMarker.props.feature.properties.table}</div>
            </Popup>
          ) : null}
          {activeLayers.includes('intersection-layer') &&
            intersectionsList
              .filter((intersection) => intersection.latitude != 0)
              .map((intersection) => {
                return (
                  <Marker
                    key={intersection.intersectionID}
                    latitude={intersection.latitude}
                    longitude={intersection.longitude}
                    onClick={(e) => {
                      e.originalEvent.preventDefault()
                      dispatch(setSelectedIntersectionId(intersection.intersectionID))
                    }}
                  >
                    <img src="/icons/intersection_icon.png" style={{ width: 70 }} />
                  </Marker>
                )
              })}
          {activeLayers.includes('intersection-layer') && selectedIntersection && (
            <Popup
              latitude={selectedIntersection.latitude}
              longitude={selectedIntersection.longitude}
              closeOnClick={false}
              closeButton={false}
            >
              <div>SELECTED {selectedIntersection.intersectionID}</div>
            </Popup>
          )}
          {activeLayers.includes('intersection-layer') && (
            <Source
              type="geojson"
              data={{
                type: 'FeatureCollection',
                features: intersectionsList.map((intersection) => ({
                  type: 'Feature',
                  properties: {
                    intersectionId: intersection.intersectionID,
                    intersectionName: intersection.intersectionID,
                  },
                  geometry: {
                    type: 'Point',
                    coordinates: [intersection.longitude, intersection.latitude],
                  },
                })),
              }}
            >
              <Layer {...intersectionMapLabelsLayer} />
            </Source>
          )}
          {selectedRsu ? (
            <Popup
              latitude={selectedRsu.geometry.coordinates[1]}
              longitude={selectedRsu.geometry.coordinates[0]}
              onClose={() => {
                if (pageOpen) {
                  console.debug('POPUP CLOSED', pageOpen)
                  dispatch(selectRsu(null))
                  dispatch(clearFirmware())
                  setSelectedRsuCount(null)
                }
              }}
            >
              <div style={{ color: theme.palette.common.black }}>
                <h2 className="popop-h2">{rsuIpv4}</h2>
                <p className="popop-p">Milepost: {selectedRsu.properties.milepost}</p>
                <p className="popop-p">
                  Serial Number:{' '}
                  {selectedRsu.properties.serial_number ? selectedRsu.properties.serial_number : 'Unknown'}
                </p>
                <p className="popop-p">Manufacturer: {selectedRsu.properties.manufacturer_name}</p>
                <p className="popop-p">RSU Status: {getStatus()}</p>
                <p className="popop-p">Last Online: {isOnline()}</p>
                {rsuIpv4 in issScmsStatusData && issScmsStatusData[rsuIpv4] ? (
                  <div>
                    <p className="popop-p">
                      SCMS Health: {issScmsStatusData[rsuIpv4].health === '1' ? 'Healthy' : 'Unhealthy'}
                    </p>
                    <p className="popop-p">
                      SCMS Expiration:
                      {issScmsStatusData[rsuIpv4].expiration
                        ? issScmsStatusData[rsuIpv4].expiration
                        : 'Never downloaded certificates'}
                    </p>
                  </div>
                ) : (
                  <div>
                    <p className="popop-p">RSU is not enrolled with ISS SCMS</p>
                  </div>
                )}
                <p className="popop-p">
                  {countsMsgType} Counts: {selectedRsuCount}
                </p>
              </div>
            </Popup>
          ) : null}
        </Map>
      </Container>

      {activeLayers.includes('msg-viewer-layer') &&
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
                allowCross={false}
                included={false}
                min={0}
                max={calculateMaxOffset(startGeoMsgDate, endGeoMsgDate, filterStep)}
                value={filterOffset}
                onChange={(e) => {
                  dispatch(setGeoMsgFilterOffset(e as number))
                }}
              />
            </div>
            <div id="controlContainer">
              <Select
                id="stepSelect"
                onChange={(e) => {
                  const newStep = Number(e.target.value)
                  const maxOffset = calculateMaxOffset(startGeoMsgDate, endGeoMsgDate, newStep)

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

              <Button variant="contained" onClick={() => dispatch(setGeoMsgFilter(false))}>
                New Search
              </Button>
            </div>
          </div>
        ) : filter && geoMsgData.length === 0 ? (
          <div
            className={menuSelection.includes('Configure RSUs') ? 'expandedFilterControl' : 'filterControl'}
            style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}
          >
            <div id="timeContainer">
              <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small">
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
          <Paper
            className={menuSelection.includes('Configure RSUs') ? 'expandedControl' : 'control'}
            style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}
          >
            <div className="buttonContainer" style={{ marginBottom: 15 }}>
              <Button variant="contained" size="small" onClick={(e) => handleButtonToggle(e, 'msgViewer')}>
                Add Point
              </Button>
              <Button
                variant="contained"
                size="small"
                onClick={(e) => {
                  dispatch(clearGeoMsg())
                }}
              >
                Clear
              </Button>
            </div>
            <div style={{ marginBottom: 15, marginLeft: 15 }}>
              <Select
                placeholder="Select Message Type"
                className="selectContainer"
                value={geoMsgType}
                onChange={(event) => dispatch(changeGeoMsgType(event.target.value))}
              >
                {messageTypeOptions.map((option) => {
                  return (
                    <MenuItem value={option.value} key={option.value}>
                      {option.label}
                    </MenuItem>
                  )
                })}
              </Select>
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
            <div style={{ marginBottom: 15 }} className="submitContainer">
              <Button
                variant="contained"
                size="small"
                onClick={(e) => {
                  dispatch(updateGeoMsgData())
                }}
              >
                Submit
              </Button>
            </div>
          </Paper>
        ))}
    </div>
  )
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

const configOutlineLayer: LineLayer = {
  id: 'configOutline',
  type: 'line',
  source: 'polygonSource',
  layout: {},
  paint: {
    'line-color': '#000',
    'line-width': 3,
  },
}

const configPointLayer: CircleLayer = {
  id: 'configPointLayer',
  type: 'circle',
  source: 'pointSource',
  paint: {
    'circle-radius': 5,
    'circle-color': 'rgb(255, 0, 0)',
  },
}
const bsmPointLayer: CircleLayer = {
  id: 'bsmPointLayer',
  type: 'circle',
  source: 'pointSource',
  paint: {
    'circle-radius': 5,
    'circle-color': 'rgb(255, 164, 0)',
  },
}

const dateTimeOptions: Intl.DateTimeFormatOptions = {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
}

export default MapPage

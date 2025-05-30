import React, { useEffect, useState, useMemo } from 'react'
import mapboxgl, { CircleLayer, FillLayer, LineLayer } from 'mapbox-gl' // This is a dependency of react-map-gl even if you didn't explicitly install it
import Map, { Marker, Popup, Source, Layer } from 'react-map-gl'
import { Container } from 'reactstrap'
import RsuMarker from '../components/RsuMarker'
import EnvironmentVars from '../EnvironmentVars'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'

import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import Slider from 'rc-slider'
import {
  selectRsuOnlineStatus,
  selectRsuData,
  selectRsuCounts,
  selectIssScmsStatusData,
  selectSelectedRsu,
  selectMsgType,
  selectRsuIpv4,
  selectHeatMapData,
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
import {
  selectMooveAiData,
  selectAddMooveAiPoint,
  selectMooveAiCoordinates,
  selectMooveAiFilter,

  // actions
  clearMooveAiData,
  updateMooveAiData,
  toggleMooveAiPointSelect,
  updateMooveAiPoints,
} from '../generalSlices/mooveAiSlice'
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
import ClearIcon from '@mui/icons-material/Clear'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import ExpandLessIcon from '@mui/icons-material/ExpandLess'
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
  Collapse,
  InputLabel,
  Box,
  Divider,
  Grid2,
  Stack,
} from '@mui/material'

import 'rc-slider/assets/index.css'
import './css/MsgMap.css'
import './css/Map.css'
import { WZDxFeature, WZDxWorkZoneFeed } from '../models/wzdx/WzdxWorkZoneFeed42'
import {
  intersectionMapLabelsLayer,
  selectIntersections,
  selectSelectedIntersection,
  setSelectedIntersectionId,
} from '../generalSlices/intersectionSlice'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { headerTabHeight } from '../styles/index'
import { selectActiveLayers, selectViewState, setMapViewState, toggleLayerActive } from './mapSlice'
import { selectMenuSelection, toggleMapMenuSelection } from '../features/menu/menuSlice'
import { MapLayer } from '../models/MapLayer'
import { toast } from 'react-hot-toast'
import { RoomOutlined } from '@mui/icons-material'
import MooveAiHardBrakingLegend from '../components/MooveAiHardBrakingLegend'
import { PrimaryButton } from '../styles/components/PrimaryButton'
import { ConditionalRenderRsu, evaluateFeatureFlags } from '../feature-flags'

// @ts-ignore: workerClass does not exist in typed mapboxgl
// eslint-disable-next-line import/no-webpack-loader-syntax
mapboxgl.workerClass = require('worker-loader!mapbox-gl/dist/mapbox-gl-csp-worker').default

const { DateTime } = require('luxon')

function MapPage() {
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
  const addConfigPoint = useSelector(selectAddConfigPoint)
  const configCoordinates = useSelector(selectConfigCoordinates)
  const geoMsgType = useSelector(selectGeoMsgType)

  const heatMapData = useSelector(selectHeatMapData)

  const geoMsgData = useSelector(selectGeoMsgData)
  const geoMsgCoordinates = useSelector(selectGeoMsgCoordinates)
  const addGeoMsgPoint = useSelector(selectAddGeoMsgPoint)
  const startGeoMsgDate = useSelector(selectGeoMsgStart)
  const endGeoMsgDate = useSelector(selectGeoMsgEnd)

  const filter = useSelector(selectGeoMsgFilter)
  const filterStep = useSelector(selectGeoMsgFilterStep)
  const filterOffset = useSelector(selectGeoMsgFilterOffset)

  const wzdxData = useSelector(selectWzdxData)

  const mooveAiData = useSelector(selectMooveAiData)
  const addMooveAiPoint = useSelector(selectAddMooveAiPoint)
  const mooveAiCoordinates = useSelector(selectMooveAiCoordinates)
  const mooveAiFilter = useSelector(selectMooveAiFilter)

  const intersectionsList = useSelector(selectIntersections)
  const selectedIntersection = useSelector(selectSelectedIntersection)

  // Mapbox local state variables
  const viewState = useSelector(selectViewState)
  const [lastClickTime, setLastClickTime] = useState<number>(0)
  const menuSelection = useSelector(selectMenuSelection)
  const activeLayers = useSelector(selectActiveLayers)

  // RSU layer local state variables
  const [selectedRsuCount, setSelectedRsuCount] = useState(null)
  const [displayType, setDisplayType] = useState('online')

  // Add these new state variables near the other source states
  const [previewPoint, setPreviewPoint] = useState<GeoJSON.Feature<GeoJSON.Point> | null>(null)

  const [geoMsgPointSource, setGeoMsgPointSource] = useState<GeoJSON.FeatureCollection<GeoJSON.Geometry>>({
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

  function stepValueToOption(val: number) {
    for (var i = 0; i < stepOptions.length; i++) {
      if (stepOptions[i].value === val) {
        return stepOptions[i]
      }
    }
  }

  // WZDx layer local state variables
  // The marker index is necessary because the marker callback becomes disconnected from the curernt state
  const [selectedWZDxMarkerIndex, setSelectedWZDxMarkerIndex] = useState(null)
  const [selectedWZDxMarker, setSelectedWZDxMarker] = useState(null)
  const [wzdxMarkers, setWzdxMarkers] = useState([])
  const [pageOpen, setPageOpen] = useState(true)

  const [expandedLayers, setExpandedLayers] = useState<string[]>([])

  // Vendor filter local state variable
  const [selectedVendor, setSelectedVendor] = useState('Select Vendor')
  const vendorArray: string[] = ['Select Vendor', 'Commsignia', 'Yunex', 'Kapsch']
  const setVendor = (newVal) => {
    setSelectedVendor(newVal)
  }

  // TODO: Remove??
  if (!wzdxMarkers) {
    setSelectedWZDxMarkerIndex(null)
    setSelectedWZDxMarker(null)
  }
  const mbStyle = require(`../styles/${theme.palette.custom.mapStyleFilePath}`)

  // useEffects for Mapbox
  useEffect(() => {
    const listener = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        dispatch(selectRsu(null))
        dispatch(clearFirmware())
        setSelectedWZDxMarkerIndex(null)
        setSelectedWZDxMarker(null)
      }
    }
    window.addEventListener('keydown', listener)

    return () => {
      window.removeEventListener('keydown', listener)
    }
  }, [selectedRsu, dispatch, setSelectedWZDxMarkerIndex, setSelectedWZDxMarker])

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
    if (wzdxData?.features?.length === 0) {
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

  const mooveAiPolygonPointSource = useMemo(
    () =>
      ({
        type: 'FeatureCollection',
        features: mooveAiCoordinates.map(createPointFeature),
      } as GeoJSON.FeatureCollection<GeoJSON.Geometry>),
    [mooveAiCoordinates]
  )

  const mooveAiPolygonSource = useMemo(() => {
    // Get coordinates including preview point if it exists
    let polygonCoords = [...mooveAiCoordinates]
    if (previewPoint && addMooveAiPoint) {
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
  }, [mooveAiCoordinates, addMooveAiPoint, previewPoint])

  const configPolygonPointSource = useMemo(
    () =>
      ({
        type: 'FeatureCollection',
        features: configCoordinates.map(createPointFeature),
      } as GeoJSON.FeatureCollection<GeoJSON.Geometry>),
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
    if (!activeLayers.includes('msg-viewer-layer') || activeLayers.includes('moove-ai-layer')) return

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
    return Math.floor((new Date(end).getTime() - new Date(start).getTime()) / (step * 60000))
  }

  const geoMsgFilterMaxOffset = useMemo(() => {
    return calculateMaxOffset(startGeoMsgDate, endGeoMsgDate, filterStep)
  }, [startGeoMsgDate, endGeoMsgDate, filterStep])

  function dateChanged(e: Date, type: 'start' | 'end') {
    try {
      let date = DateTime.fromISO(e.toISOString())
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

  const addMooveAiPointToCoordinates = (point: { lat: number; lng: number }) => {
    const pointArray = [point.lng, point.lat]
    if (mooveAiCoordinates.length > 1) {
      if (mooveAiCoordinates[0] === mooveAiCoordinates.slice(-1)[0]) {
        let tmp = [...mooveAiCoordinates]
        tmp.pop()
        dispatch(updateMooveAiPoints([...tmp, pointArray, mooveAiCoordinates[0]]))
      } else {
        dispatch(updateMooveAiPoints([...mooveAiCoordinates, pointArray, mooveAiCoordinates[0]]))
      }
    } else {
      dispatch(updateMooveAiPoints([...mooveAiCoordinates, pointArray]))
    }
  }

  // useEffects for WZDx layers
  useEffect(() => {
    // This is to handle the fact that the marker callback is disconnected from the current state
    if (selectedWZDxMarkerIndex !== null) setSelectedWZDxMarker(wzdxMarkers[selectedWZDxMarkerIndex])
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
    setSelectedWZDxMarker(null)
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

  const handleRsuDisplayTypeChange = (event: React.SyntheticEvent) => {
    const target = event.target as HTMLInputElement
    if (target.value === 'online') handleOnlineStatus()
    else if (target.value === 'scms') handleScmsStatus()
    if (!activeLayers.includes('rsu-layer')) {
      dispatch(toggleLayerActive('rsu-layer'))
    }
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
      id: 'msg-viewer-layer',
      label: 'V2x Message Viewer',
      type: 'symbol',
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
    {
      id: 'moove-ai-layer',
      label: 'Moove AI Viewer',
      type: 'line',
      tag: 'mooveai',
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
      dispatch(toggleLayerActive(id))
      if (activeLayers.includes(id)) {
        switch (id) {
          case 'rsu-layer':
            dispatch(selectRsu(null))
            dispatch(clearFirmware())
            setSelectedRsuCount(null)
            break
          case 'wzdx-layer':
            setSelectedWZDxMarkerIndex(null)
            setSelectedWZDxMarker(null)
            break
          case 'moove-ai-layer':
            dispatch(clearMooveAiData())
        }
      } else {
        switch (id) {
          case 'wzdx-layer':
            dispatch(getWzdxData())
            break
          case 'moove-ai-layer':
            if (activeLayers.includes('msg-viewer-layer')) dispatch(toggleLayerActive('msg-viewer-layer'))
          case 'msg-viewer-layer':
            if (activeLayers.includes('moove-ai-layer')) dispatch(toggleLayerActive('moove-ai-layer'))
        }
      }
    }

    return (
      <FormGroup>
        {layers
          .filter((layer) => evaluateFeatureFlags(layer.tag))
          .map((layer) => (
            <div key={layer.id}>
              <Typography fontSize="small" display="flex" alignItems="center">
                {layer.control && (
                  <IconButton
                    onClick={() => toggleExpandLayer(layer.id)}
                    size="small"
                    edge="end"
                    aria-label={expandedLayers.includes(layer.id) ? 'Collapse' : 'Expand'}
                  >
                    {expandedLayers.includes(layer.id) ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                  </IconButton>
                )}
                <FormControlLabel
                  onClick={() => toggleLayer(layer.id)}
                  label={<Typography>{layer.label}</Typography>}
                  control={<Checkbox checked={activeLayers.includes(layer.id)} />}
                />
              </Typography>
              {layer.control && <Collapse in={expandedLayers.includes(layer.id)}>{layer.control}</Collapse>}
            </div>
          ))}
      </FormGroup>
    )
  }

  const handleButtonToggle = (
    event: React.SyntheticEvent<Element, Event>,
    origin: 'config' | 'msgViewer' | 'mooveai'
  ) => {
    // Deselect any selected RSU when toggling point select tools
    dispatch(selectRsu(null))
    dispatch(clearFirmware())
    setSelectedRsuCount(null)

    // Toggle the corresponding point select tool based on the origin
    if (origin === 'config') {
      dispatch(toggleConfigPointSelect())
      if (addGeoMsgPoint) dispatch(toggleGeoMsgPointSelect())
      if (addMooveAiPoint) dispatch(toggleMooveAiPointSelect())
    } else if (origin === 'msgViewer') {
      dispatch(toggleGeoMsgPointSelect())
      if (addConfigPoint) dispatch(toggleConfigPointSelect())
      if (addMooveAiPoint) dispatch(toggleMooveAiPointSelect())
    } else if (origin === 'mooveai') {
      dispatch(toggleMooveAiPointSelect())
    }
  }

  const messageViewerTypes = EnvironmentVars.getMessageViewerTypes()
  const messageTypeOptions = messageViewerTypes.map((type) => {
    return { value: type, label: type }
  })

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
            <Legend />
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
                    console.log(vendor)
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
          {SecureStorageManager.getUserRole() === 'admin' && (
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
                      <IconButton
                        disabled={configCoordinates.length == 0}
                        onClick={() => {
                          dispatch(clearConfig())
                        }}
                        size="large"
                      >
                        <ClearIcon />
                      </IconButton>
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
          {...viewState}
          ref={mapRef}
          mapboxAccessToken={EnvironmentVars.MAPBOX_TOKEN}
          mapStyle={mbStyle}
          style={{ width: '100%', height: '100%' }}
          onMove={(evt) => dispatch(setMapViewState(evt.viewState))}
          interactiveLayerIds={['geoMsgPointLayer']}
          onMouseMove={(e) => {
            if (addGeoMsgPoint || addConfigPoint || addMooveAiPoint) {
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
            // Prevent double click from triggering single click
            const clickTime = new Date().getTime()
            if (clickTime - lastClickTime < 300) {
              return
            }
            setLastClickTime(clickTime)

            if (addGeoMsgPoint) {
              addGeoMsgPointToCoordinates(e.lngLat)
            }
            if (addConfigPoint) {
              addConfigPointToCoordinates(e.lngLat)
            }
            if (addMooveAiPoint) {
              addMooveAiPointToCoordinates(e.lngLat)
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
            if (addMooveAiPoint) {
              dispatch(toggleMooveAiPointSelect())
            }
          }}
        >
          {/* Add preview sources and layers */}
          {activeLayers.includes('msg-viewer-layer') ||
            activeLayers.includes('moove-ai-layer') ||
            (activeLayers.includes('rsu-layer') && previewPoint && (
              <Source id="preview-point" type="geojson" data={previewPoint}>
                <Layer
                  id="preview-point-layer"
                  type="circle"
                  paint={{
                    'circle-radius': 5,
                    'circle-color': addGeoMsgPoint
                      ? 'rgba(255, 164, 0, 0.5)'
                      : addMooveAiPoint
                      ? 'rgb(53, 121, 148)'
                      : 'rgba(255, 0, 0, 0.5)',
                    'circle-stroke-width': 2,
                    'circle-stroke-color': addGeoMsgPoint
                      ? 'rgb(255, 164, 0)'
                      : addMooveAiPoint
                      ? 'rgb(94, 206, 250)'
                      : 'rgb(255, 0, 0)',
                  }}
                />
              </Source>
            ))}

          {activeLayers.includes('rsu-layer') && (
            <div>
              {configCoordinates.length >= 1 ? (
                <Source id={layers[0].id + '-fill'} type="geojson" data={configPolygonSource}>
                  <Layer {...getConfigOutlineLayer(addConfigPoint)} />
                  <Layer {...configFillLayer} />
                </Source>
              ) : null}
              {addConfigPoint && (
                <Source id={layers[0].id + '-polygon-points'} type="geojson" data={configPolygonPointSource}>
                  <Layer {...configPointLayer} />
                </Source>
              )}
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
                    // Prevent RSU selection if adding points to geospatial polygon selection
                    if (addConfigPoint || addGeoMsgPoint || addMooveAiPoint) return
                    e.originalEvent.stopPropagation()
                    dispatch(selectRsu(rsu))
                    setSelectedWZDxMarkerIndex(null)
                    setSelectedWZDxMarker(null)
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
                      // Prevent RSU selection if adding points to geospatial polygon selection
                      if (addConfigPoint || addGeoMsgPoint || addMooveAiPoint) return
                      e.stopPropagation()
                      dispatch(selectRsu(rsu))
                      dispatch(clearFirmware()) // TODO: Should remove??
                      setSelectedWZDxMarkerIndex(null)
                      setSelectedWZDxMarker(null)
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
              {addGeoMsgPoint && (
                <Source id={layers[2].id + '-polygon-points'} type="geojson" data={geoMsgPolygonPointSource}>
                  <Layer {...geoMsgPolygonPointLayer} />
                </Source>
              )}
              {filter && (
                <Source id={layers[2].id + '-geo-msg-points'} type="geojson" data={geoMsgPointSource}>
                  <Layer {...geoMsgPointLayer} />
                </Source>
              )}
            </div>
          )}
          {activeLayers.includes('wzdx-layer') && (
            <div>
              <Source id={layers[3].id} type="geojson" data={wzdxData}>
                <Layer {...layers[3]} />
              </Source>
              {wzdxMarkers}
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
          {activeLayers.includes('moove-ai-layer') && (
            <div>
              {mooveAiCoordinates.length >= 1 ? (
                <Source id={layers[4].id + '-fill'} type="geojson" data={mooveAiPolygonSource}>
                  <Layer {...getMooveAiDataOutlineLayer(addMooveAiPoint)} />
                  <Layer {...mooveAiDataFillLayer} />
                </Source>
              ) : null}
              {addMooveAiPoint && (
                <Source id={layers[4].id + '-polygon-points'} type="geojson" data={mooveAiPolygonPointSource}>
                  <Layer {...mooveAiDataPolygonPointLayer} />
                </Source>
              )}
              {mooveAiFilter && (
                <Source id={layers[4].id + '-feature-lines'} type="geojson" data={mooveAiData}>
                  <Layer {...mooveAiDataLineLayer} />
                </Source>
              )}
            </div>
          )}
          {selectedRsu ? (
            <Popup
              latitude={selectedRsu.geometry.coordinates[1]}
              longitude={selectedRsu.geometry.coordinates[0]}
              onClose={() => {
                if (pageOpen) {
                  dispatch(selectRsu(null))
                  dispatch(clearFirmware())
                  setSelectedRsuCount(null)
                }
              }}
              maxWidth="350px"
            >
              <Stack
                sx={{
                  height: '240px',
                  width: '350px',
                }}
              >
                <Grid2
                  container
                  columnSpacing={0.5}
                  rowSpacing={0}
                  sx={{
                    color: theme.palette.text.secondary,
                    backgroundColor: theme.palette.background.paper,
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

                <Grid2
                  id="popup-body"
                  container
                  columnSpacing={1}
                  rowSpacing={0}
                  sx={{
                    color: theme.palette.text.secondary,
                    backgroundColor: theme.palette.background.default,
                    width: '350px',
                    height: '140px',
                    position: 'absolute',
                    left: '0px',
                    bottom: '40px',
                    paddingTop: '10px',
                  }}
                >
                  <Grid2 size={5} justifyContent="flex-start">
                    <Typography fontSize="medium" sx={{ ml: '16px' }}>
                      {countsMsgType} Counts:
                    </Typography>
                  </Grid2>
                  <Grid2 size={6} justifyContent="flex-start">
                    <Typography fontSize="medium">{selectedRsuCount}</Typography>
                  </Grid2>
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
                              color:
                                issScmsStatusData[rsuIpv4].health === '1'
                                  ? theme.palette.success.light
                                  : theme.palette.error.light,
                            }}
                          >
                            {issScmsStatusData[rsuIpv4].health === '1' ? 'Healthy' : 'Unhealthy'}
                          </Typography>
                        </Grid2>
                        <Grid2 size={12}>
                          <Typography fontSize="small">
                            {issScmsStatusData[rsuIpv4].expiration
                              ? issScmsStatusData[rsuIpv4].expiration
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
                <Box
                  sx={{
                    position: 'absolute',
                    bottom: '0px',
                    left: '0px',
                    width: '350px',
                    height: '40px',
                    color: theme.palette.text.secondary,
                    backgroundColor: theme.palette.background.default,
                    borderRadius: '4px',
                  }}
                >
                  <Divider />
                  <Typography fontSize="small" sx={{ margin: '10px 0px 0px 16px' }}>
                    {selectedRsu.properties.manufacturer_name} #
                    {selectedRsu.properties.serial_number ? selectedRsu.properties.serial_number : 'Unknown'}
                  </Typography>
                </Box>
              </Stack>
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
                max={geoMsgFilterMaxOffset}
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

              <Button variant="contained" onClick={() => dispatch(setGeoMsgFilter(false))}>
                New Search
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
                onClick={(e) => {
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
                onClick={(e) => {
                  if (!addGeoMsgPoint) {
                    dispatch(updateGeoMsgData())
                  } else {
                    toast.error('Please complete the polygon (double click to close) before submitting')
                  }
                }}
                className="museo-slab capital-case"
              >
                Submit
              </Button>
            </div>
          </Paper>
        ))}
      {activeLayers.includes('moove-ai-layer') &&
        (mooveAiData.features.length > 0 ? (
          <div className="filterControl" style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}>
            <MooveAiHardBrakingLegend />
            <div id="controlContainer">
              <Button variant="contained" onClick={() => dispatch(clearMooveAiData())}>
                New Search
              </Button>
            </div>
          </div>
        ) : mooveAiFilter && mooveAiData.features.length === 0 ? (
          <div
            className={menuSelection.includes('Configure RSUs') ? 'expandedFilterControl' : 'filterControl'}
            style={{ backgroundColor: theme.palette.custom.mapLegendBackground }}
          >
            <div id="timeContainer">
              <Typography fontSize="small">
                No data found for the selected polygon. Please try a new search for different geospatial area.
              </Typography>
            </div>
            <div id="controlContainer">
              <Button variant="contained" onClick={() => dispatch(clearMooveAiData())}>
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
              <Button
                className="museo-slab capital-case"
                variant="contained"
                size="small"
                onClick={(e) => handleButtonToggle(e, 'mooveai')}
              >
                Add Point
              </Button>
              <Button
                className="museo-slab capital-case"
                variant="contained"
                size="small"
                onClick={(e) => {
                  dispatch(clearMooveAiData())
                }}
              >
                Clear
              </Button>
            </div>
            <div id="mooveAiDescription" style={{ marginBottom: 15 }}>
              <Typography fontSize="small">
                Add points on the map to create a geospatial polygon to query for Moove AI harsh braking data
              </Typography>
            </div>
            <div style={{ marginBottom: 5 }} className="submitContainer">
              <Button
                className="museo-slab capital-case"
                variant="contained"
                size="small"
                onClick={(e) => {
                  if (!addMooveAiPoint) {
                    dispatch(updateMooveAiData())
                  } else {
                    toast.error('Please complete the polygon (double click to close) before submitting')
                  }
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

const getMooveAiDataOutlineLayer = (isEditing: boolean): LineLayer => ({
  id: 'mooveAiDataOutline',
  type: 'line',
  source: 'mooveAiPolygonSource',
  layout: {},
  paint: {
    'line-color': '#000',
    'line-width': 3,
    'line-dasharray': isEditing ? [2, 2] : undefined,
  },
})

const mooveAiDataFillLayer: FillLayer = {
  id: 'mooveAiDataFill',
  type: 'fill',
  source: 'mooveAiPolygonSource',
  layout: {},
  paint: {
    'fill-color': '#0080ff',
    'fill-opacity': 0.2,
  },
}

const mooveAiDataPolygonPointLayer: CircleLayer = {
  id: 'mooveAiDataPolygonPoint',
  type: 'circle',
  source: 'mooveAiPolygonPointSource',
  paint: {
    'circle-radius': 5,
    'circle-color': 'rgb(94, 206, 250)',
  },
}

const mooveAiDataLineLayer: LineLayer = {
  id: 'mooveAiDataLine',
  type: 'line',
  source: 'mooveAiData',
  layout: {},
  paint: {
    'line-color': [
      'case',
      ['>=', ['get', 'total_hard_brake_count'], 750],
      'rgb(255, 0, 0)', // Red for values 750 and above
      ['>=', ['get', 'total_hard_brake_count'], 500],
      'rgb(255, 165, 0)', // Orange for values between 500 and 750
      ['>=', ['get', 'total_hard_brake_count'], 250],
      'rgb(255, 255, 0)', // Yellow for values between 250 and 500
      'rgb(0, 255, 0)', // Green for values below 250
    ],
    'line-width': 5,
  },
}

const dateTimeOptions: Intl.DateTimeFormatOptions = {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
}

export default MapPage

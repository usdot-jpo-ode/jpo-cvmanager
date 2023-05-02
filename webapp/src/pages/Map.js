import React, { useEffect, useState } from 'react'
import ReactMapGL, { Marker, Popup, Source, Layer } from 'react-map-gl'
import RsuMarker from '../components/RsuMarker'
import Grid from '@material-ui/core/Grid'
import mbStyle from '../styles/mb_style.json'
import EnvironmentVars from '../EnvironmentVars'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import TextField from '@mui/material/TextField'
import Slider from 'rc-slider'
import Select from 'react-select'
import {
    selectRsuOnlineStatus,
    selectMapList,
    selectRsuData,
    selectRsuCounts,
    selectIssScmsStatusData,
    selectSelectedRsu,
    selectMsgType,
    selectRsuIpv4,
    selectDisplayMap,
    selectHeatMapData,
    selectAddBsmPoint,
    selectBsmStart,
    selectBsmEnd,
    selectBsmDateError,
    selectBsmData,
    selectBsmCoordinates,
    selectBsmFilter,
    selectBsmFilterStep,
    selectBsmFilterOffset,

    // actions
    selectRsu,
    toggleMapDisplay,
    getIssScmsStatus,
    getMapData,
    getRsuLastOnline,
    toggleBsmPointSelect,
    clearBsm,
    updateBsmPoints,
    updateBsmData,
    updateBsmDate,
    setBsmFilter,
    setBsmFilterStep,
    setBsmFilterOffset,
} from '../slices/rsuSlice'
import { selectWzdxData, getWzdxData } from '../slices/wzdxSlice'
import { selectOrganizationName } from '../slices/userSlice'
import {
    selectConfigCoordinates,
    toggleConfigPointSelect,
    selectAddConfigPoint,
    updateConfigPoints,
    geoRsuQuery,
    clearConfig,
} from '../slices/configSlice'
import { useSelector, useDispatch } from 'react-redux'
import Switch from '@mui/material/Switch'
import ToggleButton from '@mui/material/ToggleButton'
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup'
import EditIcon from '@mui/icons-material/Edit'
import ClearIcon from '@mui/icons-material/Clear'
import 'rc-slider/assets/index.css'
import '../components/css/BsmMap.css'
import '../components/css/Map.css'
import {
    Button,
    FormControlLabel,
    FormGroup,
    IconButton,
    ThemeProvider,
    Tooltip,
    createTheme,
} from '@mui/material'

const { DateTime } = require('luxon')

const fillLayer = {
    id: 'fill',
    type: 'fill',
    source: 'polygonSource',
    layout: {},
    paint: {
        'fill-color': '#0080ff',
        'fill-opacity': 0.2,
    },
}

const outlineLayer = {
    id: 'outline',
    type: 'line',
    source: 'polygonSource',
    layout: {},
    paint: {
        'line-color': '#000',
        'line-width': 3,
    },
}

const pointLayer = {
    id: 'pointLayer',
    type: 'circle',
    source: 'pointSource',
    paint: {
        'circle-radius': 5,
        'circle-color': 'rgb(255, 0, 0)',
    },
}
const bsmPointLayer = {
    id: 'bsmPointLayer',
    type: 'circle',
    source: 'bsmPointLayer',
    paint: {
        'circle-radius': 5,
        'circle-color': 'rgb(255, 164, 0)',
    },
}

const theme = createTheme({
    palette: {
        primary: {
            main: '#d16d15',
            light: '#0e2052',
            contrastTextColor: '#0e2052',
        },
        secondary: {
            main: '#d16d15',
            light: '#0e2052',
            contrastTextColor: '#0e2052',
        },
        text: {
            primary: '#ffffff',
            secondary: '#ffffff',
            disabled: '#ffffff',
            hint: '#ffffff',
        },
    },
    components: {
        MuiSvgIcon: {
            styleOverrides: {
                root: {
                    color: '#d16d15',
                },
            },
        },
    },
    input: {
        color: '#11ff00',
    },
    typography: {
        allVariants: {
            color: '#ffffff',
        },
    },
})

function Map(props) {
    const dispatch = useDispatch()

    const organization = useSelector(selectOrganizationName)
    const rsuData = useSelector(selectRsuData)
    const rsuCounts = useSelector(selectRsuCounts)
    const selectedRsu = useSelector(selectSelectedRsu)
    const mapList = useSelector(selectMapList)
    const msgType = useSelector(selectMsgType)
    const issScmsStatusData = useSelector(selectIssScmsStatusData)
    const rsuOnlineStatus = useSelector(selectRsuOnlineStatus)
    const rsuIpv4 = useSelector(selectRsuIpv4)
    const displayMap = useSelector(selectDisplayMap)
    const addConfigPoint = useSelector(selectAddConfigPoint)
    const configCoordinates = useSelector(selectConfigCoordinates)

    const heatMapData = useSelector(selectHeatMapData)

    const bsmData = useSelector(selectBsmData)
    const bsmCoordinates = useSelector(selectBsmCoordinates)
    const addBsmPoint = useSelector(selectAddBsmPoint)
    const startBsmDate = useSelector(selectBsmStart)
    const endBsmDate = useSelector(selectBsmEnd)
    const bsmDateError = useSelector(selectBsmDateError)

    const filter = useSelector(selectBsmFilter)
    const filterStep = useSelector(selectBsmFilterStep)
    const filterOffset = useSelector(selectBsmFilterOffset)

    const wzdxData = useSelector(selectWzdxData)

    // Mapbox local state variables
    const [viewport, setViewport] = useState({
        latitude: 39.7392,
        longitude: -104.9903,
        width: '100%',
        height: props.auth ? 'calc(100vh - 135px)' : 'calc(100vh - 100px)',
        zoom: 10,
    })

    // RSU layer local state variables
    const [selectedRsuCount, setSelectedRsuCount] = useState(null)
    const [displayType, setDisplayType] = useState('')

    const [configPolygonSource, setConfigPolygonSource] = useState({
        type: 'Feature',
        geometry: {
            type: 'Polygon',
            coordinates: [],
        },
    })
    const [configPointSource, setConfigPointSource] = useState({
        type: 'FeatureCollection',
        features: [],
    })

    // BSM layer local state variables
    const [bsmPolygonSource, setBsmPolygonSource] = useState({
        type: 'Feature',
        geometry: {
            type: 'Polygon',
            coordinates: [],
        },
    })
    const [bsmPointSource, setBsmPointSource] = useState({
        type: 'FeatureCollection',
        features: [],
    })

    const [baseDate, setBaseDate] = useState(new Date(startBsmDate))
    const [startDate, setStartDate] = useState(
        new Date(baseDate.getTime() + 60000 * filterOffset * filterStep)
    )
    const [endDate, setEndDate] = useState(
        new Date(startDate.getTime() + 60000 * filterStep)
    )
    const stepOptions = [
        { value: 1, label: '1 minute' },
        { value: 5, label: '5 minutes' },
        { value: 15, label: '15 minutes' },
        { value: 30, label: '30 minutes' },
        { value: 60, label: '60 minutes' },
    ]

    // WZDx layer local state variables
    const [selectedMarkerIndex, setSelectedMarkerIndex] = useState(null)
    const [selectedMarker, setSelectedMarker] = useState(null)
    const [wzdxMarkers, setWzdxMarkers] = useState([])

    // useEffects for Mapbox
    useEffect(() => {
        const listener = (e) => {
            if (e.key === 'Escape') {
                dispatch(selectRsu(null))
                setSelectedMarkerIndex(null)
            }
        }
        window.addEventListener('keydown', listener)

        return () => {
            window.removeEventListener('keydown', listener)
        }
    }, [selectedRsu, dispatch, setSelectedMarkerIndex])

    // useEffects for RSU layer
    useEffect(() => {
        dispatch(selectRsu(null))
    }, [organization, dispatch])

    // useEffects for BSM layer
    useEffect(() => {
        const localBaseDate = new Date(startBsmDate)
        const localStartDate = new Date(
            localBaseDate.getTime() + 60000 * filterOffset * filterStep
        )
        const localEndDate = new Date(
            new Date(localStartDate).getTime() + 60000 * filterStep
        )
        setBaseDate(localBaseDate)
        setStartDate(localStartDate)
        setEndDate(localEndDate)
    }, [startBsmDate, filterOffset, filterStep])

    useEffect(() => {
        setBsmPolygonSource((prevPolygonSource) => {
            return {
                ...prevPolygonSource,
                geometry: {
                    ...prevPolygonSource.geometry,
                    coordinates: [[...bsmCoordinates]],
                },
            }
        })

        const pointSourceFeatures = []
        if ((bsmData?.length ?? 0) > 0) {
            for (const [, val] of Object.entries([...bsmData])) {
                const bsmDate = new Date(val['properties']['time'])
                if (bsmDate >= startDate && bsmDate <= endDate) {
                    pointSourceFeatures.push(val)
                }
            }
        } else {
            bsmCoordinates.forEach((point) => {
                pointSourceFeatures.push({
                    type: 'Feature',
                    geometry: {
                        type: 'Point',
                        coordinates: [...point],
                    },
                })
            })
        }

        setBsmPointSource((prevPointSource) => {
            return { ...prevPointSource, features: pointSourceFeatures }
        })
    }, [bsmCoordinates, bsmData, startDate, endDate])

    useEffect(() => {
        setConfigPolygonSource((prevPolygonSource) => {
            return {
                ...prevPolygonSource,
                geometry: {
                    ...prevPolygonSource.geometry,
                    coordinates: [[...configCoordinates]],
                },
            }
        })

        const pointSourceFeatures = []
        configCoordinates.forEach((point) => {
            pointSourceFeatures.push({
                type: 'Feature',
                geometry: {
                    type: 'Point',
                    coordinates: [...point],
                },
            })
        })

        setConfigPointSource((prevPointSource) => {
            return { ...prevPointSource, features: pointSourceFeatures }
        })
    }, [configCoordinates])

    function dateChanged(e, type) {
        try {
            let mst = DateTime.fromISO(e.toISOString())
            mst.setZone('America/Denver')
            dispatch(updateBsmDate({ type, date: mst.toString() }))
        } catch (err) {
            console.error('Encountered issue updating date: ', err.message)
        }
    }

    const addBsmPointToCoordinates = (point) => {
        if (bsmCoordinates.length > 1) {
            if (bsmCoordinates[0] === bsmCoordinates.slice(-1)[0]) {
                let tmp = [...bsmCoordinates]
                tmp.pop()
                dispatch(updateBsmPoints([...tmp, point, bsmCoordinates[0]]))
            } else {
                dispatch(
                    updateBsmPoints([
                        ...bsmCoordinates,
                        point,
                        bsmCoordinates[0],
                    ])
                )
            }
        } else {
            dispatch(updateBsmPoints([...bsmCoordinates, point]))
        }
    }

    const addRsuPointToCoordinates = (point) => {
        if (configCoordinates?.length > 1) {
            if (configCoordinates[0] === configCoordinates.slice(-1)[0]) {
                let tmp = [...configCoordinates]
                tmp.pop()
                dispatch(
                    updateConfigPoints([...tmp, point, configCoordinates[0]])
                )
            } else {
                dispatch(
                    updateConfigPoints([
                        ...configCoordinates,
                        point,
                        configCoordinates[0],
                    ])
                )
            }
        } else {
            dispatch(updateConfigPoints([...configCoordinates, point]))
        }
    }

    function defaultSlider(val) {
        for (var i = 0; i < stepOptions.length; i++) {
            if (stepOptions[i].value === val) {
                return stepOptions[i].label
            }
        }
    }

    // useEffects for WZDx layers
    useEffect(() => {
        if (selectedMarkerIndex !== null)
            setSelectedMarker(wzdxMarkers[selectedMarkerIndex])
        else setSelectedMarker(null)
    }, [selectedMarkerIndex, wzdxMarkers])

    useEffect(() => {
        function createPopupTable(data) {
            let rows = []
            for (var i = 0; i < data.length; i++) {
                let rowID = `row${i}`
                let cell = []
                for (var idx = 0; idx < 2; idx++) {
                    let cellID = `cell${i}-${idx}`
                    cell.push(
                        <td key={cellID} id={cellID}>
                            <pre>{data[i][idx]}</pre>
                        </td>
                    )
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

        function getWzdxTable(obj) {
            let arr = []
            arr.push([
                'road_name',
                obj['properties']['core_details']['road_names'][0],
            ])
            arr.push([
                'direction',
                obj['properties']['core_details']['direction'],
            ])
            arr.push(['vehicle_impact', obj['properties']['vehicle_impact']])
            arr.push(['workers_present', obj['properties']['workers_present']])
            arr.push([
                'description',
                break_line(obj['properties']['core_details']['description']),
            ])
            arr.push(['start_date', obj['properties']['start_date']])
            arr.push(['end_date', obj['properties']['end_date']])
            return arr
        }

        function openPopup(index) {
            setSelectedMarkerIndex(index)
            dispatch(selectRsu(null))
        }

        function customMarker(feature, index, lat, lng) {
            return (
                <Marker
                    key={feature.id}
                    latitude={lat}
                    longitude={lng}
                    offsetLeft={-30}
                    offsetTop={-30}
                    feature={feature}
                    index={index}
                >
                    <div onClick={() => openPopup(index)}>
                        <img
                            src="./workzone_icon.png"
                            height={60}
                            alt="Work Zone Icon"
                        />
                    </div>
                </Marker>
            )
        }

        const getAllMarkers = (wzdxData) => {
            var i = -1
            var markers = wzdxData.features.map((feature) => {
                const localFeature = { ...feature }
                var center_coords_index = Math.round(
                    feature.geometry.coordinates.length / 2
                )
                var lng = feature.geometry.coordinates[0][0]
                var lat = feature.geometry.coordinates[0][1]
                if (center_coords_index !== 1) {
                    lat = feature.geometry.coordinates[center_coords_index][1]
                    lng = feature.geometry.coordinates[center_coords_index][0]
                } else {
                    lat =
                        (feature.geometry.coordinates[0][1] +
                            feature.geometry.coordinates[1][1]) /
                        2
                    lng =
                        (feature.geometry.coordinates[0][0] +
                            feature.geometry.coordinates[1][0]) /
                        2
                }
                i++
                localFeature.properties = { ...feature.properties }
                localFeature.properties.table = createPopupTable(
                    getWzdxTable(feature)
                )
                return customMarker(localFeature, i, lat, lng)
            })
            return markers
        }

        setWzdxMarkers(getAllMarkers(wzdxData))
    }, [wzdxData])

    const setMapDisplayRsu = async () => {
        let display = !displayMap
        if (display === true) {
            dispatch(getMapData())
        }
        dispatch(toggleMapDisplay())
    }

    function break_line(val) {
        var arr = []
        for (var i = 0; i < val.length; i += 100) {
            arr.push(val.substring(i, i + 100))
        }
        return arr.join('\n')
    }

    function closePopup() {
        setSelectedMarkerIndex(null)
    }

    const CustomPopup = ({ marker, closePopup }) => {
        return (
            <Popup
                latitude={marker.props.latitude}
                longitude={marker.props.longitude}
                altitude={12}
                onClose={closePopup}
                closeButton={true}
                closeOnClick={false}
                offsetTop={-25}
            >
                {marker.props.feature.properties.table}
            </Popup>
        )
    }

    function getStops() {
        // populate tmp array with rsuCounts to get max count value
        let max = Math.max(
            ...Object.entries(rsuCounts).map(([, value]) => value.count)
        )
        let stopsArray = [[0, 0.25]]
        let weight = 0.5
        for (let i = 1; i < max; i += 500) {
            stopsArray.push([i, weight])
            weight += 0.25
        }
        return stopsArray
    }

    const layers = [
        {
            id: 'rsu-layer',
            label: 'RSU',
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
                'heatmap-intensity': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    0,
                    0,
                    10,
                    1,
                    13,
                    2,
                ],
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
                'heatmap-opacity': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    10,
                    1,
                    13,
                    0.6,
                    14,
                    0,
                ],
            },
        },
        {
            id: 'bsm-layer',
            label: 'BSM Viewer',
        },
        {
            id: 'wzdx-layer',
            label: 'WZDx',
            type: 'line',
            paint: {
                'line-color': '#F29543',
                'line-width': 8,
            },
        },
    ]

    const [activeLayers, setActiveLayers] = useState(['rsu-layer'])

    // useEffect(() => {
    //     console.log('configpoint', addConfigPoint)
    //     console.log('togglebsmpoint', addBsmPoint)
    //     if (addBsmPoint && addConfigPoint) {
    //         //disable configpoint
    //         dispatch(toggleConfigPointSelect())
    //     }
    // }, [addConfigPoint, addBsmPoint])

    const Legend = () => {
        const toggleLayer = (id) => {
            if (activeLayers.includes(id)) {
                if (id === 'rsu-layer') {
                    dispatch(selectRsu(null))
                    setSelectedRsuCount(null)
                } else if (id === 'wzdx-layer') {
                    setSelectedMarkerIndex(null)
                }
                setActiveLayers(
                    activeLayers.filter((layerId) => layerId !== id)
                )
            } else {
                if (id === 'wzdx-layer' && wzdxData.features.length === 0) {
                    dispatch(getWzdxData())
                }
                setActiveLayers([...activeLayers, id])
            }
        }

        return (
            <div className="legend">
                <h1 className="legend-header">Map Layers</h1>
                {layers.map((layer) => (
                    <div key={layer.id} className="legend-item">
                        <label className="legend-label">
                            <input
                                className="legend-input"
                                type="checkbox"
                                checked={activeLayers.includes(layer.id)}
                                onChange={() => toggleLayer(layer.id)}
                            />
                            {layer.label}
                        </label>
                    </div>
                ))}
            </div>
        )
    }

    const isOnline = () => {
        return rsuIpv4 in rsuOnlineStatus &&
            rsuOnlineStatus[rsuIpv4].hasOwnProperty('last_online')
            ? rsuOnlineStatus[rsuIpv4].last_online
            : 'No Data'
    }

    const getStatus = () => {
        return rsuIpv4 in rsuOnlineStatus &&
            rsuOnlineStatus[rsuIpv4].hasOwnProperty('current_status')
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

    const handleRsuDisplayTypeChange = (event) => {
        if (event.target.value === 'online') handleOnlineStatus()
        else if (event.target.value === 'scms') handleScmsStatus()
    }

    const handleButtonToggle = (event, origin) => {
        console.log(event.target.value, origin)
        if (origin === 'config') {
            dispatch(toggleConfigPointSelect())
            if (addBsmPoint) dispatch(toggleBsmPointSelect())
        } else if (origin === 'bsm') {
            dispatch(toggleBsmPointSelect())
            if (addConfigPoint) dispatch(toggleConfigPointSelect())
        }
    }

    return (
        <div className="container">
            <Grid container className="legend-grid" direction="row">
                <Legend />
                {activeLayers.includes('rsu-layer') && (
                    <div className="rsu-status-div">
                        <h1 className="legend-header">RSU Status</h1>
                        <label className="rsu-status-label">
                            <input
                                className="rsu-status-input"
                                type="radio"
                                name="online-status-radio"
                                value="online"
                                checked={displayType === 'online'}
                                onChange={handleRsuDisplayTypeChange}
                            />
                            Online Status
                        </label>

                        <label className="rsu-status-label">
                            <input
                                className="rsu-status-input"
                                type="radio"
                                name="scms-status-radio"
                                value="scms"
                                checked={displayType === 'scms'}
                                onChange={handleRsuDisplayTypeChange}
                            />
                            SCMS Status
                        </label>
                        <h1 className="legend-header">RSU Configuration</h1>
                        <ThemeProvider theme={theme}>
                            <FormGroup row sx={{ gap: 5 }}>
                                <FormControlLabel
                                    control={
                                        <Switch checked={addConfigPoint} />
                                    }
                                    label={'Add Points'}
                                    onChange={(e) =>
                                        handleButtonToggle(e, 'config')
                                    }
                                    // onChange={() => {
                                    //     dispatch(toggleConfigPointSelect())
                                    // }}
                                />
                                <Tooltip title="Clear Points">
                                    <IconButton
                                        onClick={() => {
                                            dispatch(clearConfig())
                                        }}
                                    >
                                        <ClearIcon />
                                    </IconButton>
                                </Tooltip>
                            </FormGroup>
                            <FormGroup row sx={{ gap: 5 }}>
                                <Button
                                    variant="contained"
                                    disabled={!(configCoordinates.length > 2)}
                                    onClick={() => {
                                        dispatch(geoRsuQuery())
                                    }}
                                >
                                    Configure RSU's
                                </Button>
                            </FormGroup>
                        </ThemeProvider>
                    </div>
                )}
                {activeLayers.includes('rsu-layer') &&
                selectedRsu !== null &&
                mapList.includes(rsuIpv4) ? (
                    <button
                        className="map-button"
                        onClick={(e) => setMapDisplayRsu()}
                    >
                        Show Intersection
                    </button>
                ) : null}
            </Grid>
            <ReactMapGL
                {...viewport}
                mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
                mapStyle={mbStyle}
                onViewportChange={(viewport) => {
                    setViewport(viewport)
                }}
                onClick={
                    addBsmPoint || addConfigPoint
                        ? (e) => {
                              if (addBsmPoint) {
                                  addBsmPointToCoordinates(e.lngLat)
                              }
                              if (addConfigPoint) {
                                  addRsuPointToCoordinates(e.lngLat)
                              }
                          }
                        : () => {
                              setSelectedMarkerIndex(null)
                          }
                }
            >
                {activeLayers.includes('rsu-layer') && (
                    <div>
                        {configCoordinates?.length > 2 ? (
                            <Source
                                id={layers[0].id + '-fill'}
                                type="geojson"
                                data={configPolygonSource}
                            >
                                <Layer {...outlineLayer} />
                                <Layer {...fillLayer} />
                            </Source>
                        ) : null}
                        <Source
                            id={layers[0].id + '-points'}
                            type="geojson"
                            data={configPointSource}
                        >
                            <Layer {...pointLayer} />
                        </Source>
                    </div>
                )}

                {rsuData?.map(
                    (rsu) =>
                        activeLayers.includes('rsu-layer') && [
                            <Marker
                                className="rsu-marker"
                                key={rsu.id}
                                latitude={rsu.geometry.coordinates[1]}
                                longitude={rsu.geometry.coordinates[0]}
                            >
                                <button
                                    className="marker-btn"
                                    onClick={(e) => {
                                        e.preventDefault()
                                        dispatch(selectRsu(rsu))
                                        setSelectedMarkerIndex(null)
                                        dispatch(
                                            getRsuLastOnline(
                                                rsu.properties.ipv4_address
                                            )
                                        )
                                        dispatch(getIssScmsStatus())
                                        if (
                                            rsuCounts.hasOwnProperty(
                                                rsu.properties.ipv4_address
                                            )
                                        )
                                            setSelectedRsuCount(
                                                rsuCounts[
                                                    rsu.properties.ipv4_address
                                                ].count
                                            )
                                        else setSelectedRsuCount(0)
                                    }}
                                >
                                    <RsuMarker
                                        displayType={displayType}
                                        onlineStatus={
                                            rsuOnlineStatus.hasOwnProperty(
                                                rsu.properties.ipv4_address
                                            )
                                                ? rsuOnlineStatus[
                                                      rsu.properties
                                                          .ipv4_address
                                                  ].current_status
                                                : 'offline'
                                        }
                                        scmsStatus={
                                            issScmsStatusData.hasOwnProperty(
                                                rsu.properties.ipv4_address
                                            ) &&
                                            issScmsStatusData[
                                                rsu.properties.ipv4_address
                                            ]
                                                ? issScmsStatusData[
                                                      rsu.properties
                                                          .ipv4_address
                                                  ].health
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
                {activeLayers.includes('bsm-layer') && (
                    <div>
                        {bsmCoordinates.length > 2 ? (
                            <Source
                                id={layers[2].id + '-fill'}
                                type="geojson"
                                data={bsmPolygonSource}
                            >
                                <Layer {...outlineLayer} />
                                <Layer {...fillLayer} />
                            </Source>
                        ) : null}
                        <Source
                            id={layers[2].id + '-points'}
                            type="geojson"
                            data={bsmPointSource}
                        >
                            <Layer {...bsmPointLayer} />
                        </Source>
                    </div>
                )}
                {activeLayers.includes('wzdx-layer') && (
                    <div>
                        <Source
                            id={layers[3].id}
                            type="geojson"
                            data={wzdxData}
                        >
                            <Layer {...layers[3]} />
                        </Source>
                        {wzdxMarkers}
                        {selectedMarker !== null && (
                            <CustomPopup
                                marker={selectedMarker}
                                closePopup={closePopup}
                            />
                        )}
                    </div>
                )}
                {selectedRsu ? (
                    <Popup
                        latitude={selectedRsu.geometry.coordinates[1]}
                        longitude={selectedRsu.geometry.coordinates[0]}
                        onClose={() => {
                            dispatch(selectRsu(null))
                            setSelectedRsuCount(null)
                        }}
                    >
                        <div>
                            <h2 className="popop-h2">{rsuIpv4}</h2>
                            <p className="popop-p">
                                Milepost: {selectedRsu.properties.milepost}
                            </p>
                            <p className="popop-p">
                                Serial Number:{' '}
                                {selectedRsu.properties.serial_number
                                    ? selectedRsu.properties.serial_number
                                    : 'Unknown'}
                            </p>
                            <p className="popop-p">
                                Manufacturer:{' '}
                                {selectedRsu.properties.manufacturer_name}
                            </p>
                            <p className="popop-p"> {getStatus()}</p>
                            <p className="popop-p">Last Online: {isOnline()}</p>
                            {rsuIpv4 in issScmsStatusData &&
                            issScmsStatusData[rsuIpv4] ? (
                                <div>
                                    <p className="popop-p">
                                        SCMS Health:{' '}
                                        {issScmsStatusData[rsuIpv4].health ===
                                        '1'
                                            ? 'Healthy'
                                            : 'Unhealthy'}
                                    </p>
                                    <p className="popop-p">
                                        SCMS Expiration:{' '}
                                        {issScmsStatusData[rsuIpv4].expiration
                                            ? issScmsStatusData[rsuIpv4]
                                                  .expiration
                                            : 'Never downloaded certificates'}
                                    </p>
                                </div>
                            ) : (
                                <div>
                                    <p className="popop-p">
                                        RSU is not enrolled with ISS SCMS
                                    </p>
                                </div>
                            )}
                            <p className="popop-p">
                                {msgType} Counts: {selectedRsuCount}
                            </p>
                        </div>
                    </Popup>
                ) : null}
            </ReactMapGL>

            {activeLayers.includes('bsm-layer') &&
                (filter ? (
                    <div className="filterControl">
                        <div id="timeContainer">
                            <p id="timeHeader">
                                {startDate.toLocaleTimeString([], {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                })}{' '}
                                -{' '}
                                {endDate.toLocaleTimeString([], {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                })}
                            </p>
                        </div>
                        <div id="sliderContainer">
                            <Slider
                                allowCross={false}
                                included={false}
                                max={
                                    (new Date(endBsmDate).getTime() -
                                        baseDate.getTime()) /
                                    (filterStep * 60000)
                                }
                                value={filterOffset}
                                onChange={(e) => {
                                    dispatch(setBsmFilterOffset(e))
                                }}
                            />
                        </div>
                        <div id="controlContainer">
                            <Select
                                id="stepSelect"
                                options={stepOptions}
                                defaultValue={filterStep}
                                placeholder={defaultSlider(filterStep)}
                                onChange={(e) =>
                                    dispatch(setBsmFilterStep(e.value))
                                }
                            />
                            <button
                                className="searchButton"
                                onClick={() => dispatch(setBsmFilter(false))}
                            >
                                New Search
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="control">
                        <div className="buttonContainer">
                            <button
                                className={addBsmPoint ? 'selected' : 'button'}
                                onClick={(e) => handleButtonToggle(e, 'bsm')}
                            >
                                Add Point
                            </button>
                            <button
                                className="button"
                                onClick={(e) => {
                                    dispatch(clearBsm())
                                }}
                            >
                                Clear
                            </button>
                        </div>
                        <div className="dateContainer">
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    label="Select start date"
                                    value={dayjs(
                                        startBsmDate === ''
                                            ? new Date()
                                            : startBsmDate
                                    )}
                                    maxDateTime={dayjs(
                                        endBsmDate === ''
                                            ? new Date()
                                            : endBsmDate
                                    )}
                                    onChange={(e) => {
                                        dateChanged(e.toDate(), 'start')
                                    }}
                                    renderInput={(params) => (
                                        <TextField {...params} />
                                    )}
                                />
                            </LocalizationProvider>
                        </div>
                        <div className="dateContainer">
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    label="Select end date"
                                    value={dayjs(
                                        endBsmDate === ''
                                            ? new Date()
                                            : endBsmDate
                                    )}
                                    minDateTime={
                                        startBsmDate === ''
                                            ? null
                                            : dayjs(startBsmDate)
                                    }
                                    maxDateTime={dayjs(new Date())}
                                    onChange={(e) => {
                                        dateChanged(e.toDate(), 'end')
                                    }}
                                    renderInput={(params) => (
                                        <TextField {...params} />
                                    )}
                                />
                            </LocalizationProvider>
                        </div>
                        <div className="submitContainer">
                            <button
                                id="submitButton"
                                onClick={(e) => {
                                    dispatch(updateBsmData())
                                }}
                            >
                                Submit
                            </button>
                        </div>
                        {bsmDateError ? (
                            <div id="dateMessage">
                                Date ranges longer than 24 hours are not
                                supported due to their large data sets
                            </div>
                        ) : null}
                    </div>
                ))}
        </div>
    )
}

export default Map

import React, { useEffect, useState } from 'react'
import ReactMapGL, { Marker, Popup } from 'react-map-gl'
import RsuMarker from '../components/RsuMarker'
import Grid from '@material-ui/core/Grid'
import mbStyle from '../styles/mb_style.json'
import EnvironmentVars from '../EnvironmentVars'
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

    // actions
    selectRsu,
    toggleMapDisplay,
    getIssScmsStatus,
    getMapData,
    getRsuLastOnline,
} from '../slices/rsuSlice'
import { selectOrganizationName } from '../slices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../components/css/Map.css'

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

    const [viewport, setViewport] = useState({
        latitude: 39.7392,
        longitude: -104.9903,
        width: '100%',
        height: props.auth ? 'calc(100vh - 135px)' : 'calc(100vh - 100px)',
        zoom: 10,
    })

    const [selectedRsuCount, setSelectedRsuCount] = useState(null)

    const [displayType, setDisplayType] = useState('online')

    useEffect(() => {
        const listener = (e) => {
            if (e.key === 'Escape') dispatch(selectRsu(null))
        }
        window.addEventListener('keydown', listener)

        return () => {
            window.removeEventListener('keydown', listener)
        }
    }, [selectedRsu, dispatch])

    useEffect(() => {
        dispatch(selectRsu(null))
    }, [organization, dispatch])

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

    const setMapDisplayRsu = async () => {
        let display = !displayMap
        if (display === true) {
            dispatch(getMapData())
        }
        dispatch(toggleMapDisplay())
    }

    const layers = [
        {
            id: 'rsu-layer',
            label: 'RSU',
        },
        {
            id: 'bsm-layer',
            label: 'BSM',
        },
        {
            id: 'heatmap-layer',
            label: 'Heatmap',
        },
        {
            id: 'wzdx-layer',
            label: 'WZDx',
        },
    ]

    const [activeLayers, setActiveLayers] = useState(
        layers.map((layer) => layer.id)
    )

    const Legend = () => {
        const toggleLayer = (id) => {
            if (activeLayers.includes(id)) {
                setActiveLayers(
                    activeLayers.filter((layerId) => layerId !== id)
                )
            } else {
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

    const buttonStyle = {
        height: '35px',
        padding: '1px 12px',
        marginTop: '-120px',
        textAlign: 'center',
        textDecoration: 'none',
        fontSize: '18px',
        background: '#d16d15',
        borderRadius: '30px',
        border: 'none',
        cursor: 'pointer',
        color: 'white',
        zIndex: '90',
    }

    const gridStyle = {
        position: 'absolute',
        alignItems: 'top',
    }

    return (
        <div className="container">
            <Grid
                style={gridStyle}
                container
                alignItems="center"
                direction="row"
            >
                <Legend />
                {displayType === 'online' ? (
                    <button
                        style={buttonStyle}
                        onClick={(e) => handleScmsStatus()}
                    >
                        SCMS Status
                    </button>
                ) : (
                    <button
                        style={buttonStyle}
                        onClick={(e) => handleOnlineStatus()}
                    >
                        Online Status
                    </button>
                )}
            </Grid>

            <ReactMapGL
                {...viewport}
                mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
                mapStyle={mbStyle}
                onViewportChange={(viewport) => {
                    setViewport(viewport)
                }}
            >
                {rsuData?.map((rsu) => (
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
                                dispatch(
                                    getRsuLastOnline(
                                        rsu.properties.ipv4_address
                                    )
                                )
                                if (
                                    rsuCounts.hasOwnProperty(
                                        rsu.properties.ipv4_address
                                    )
                                )
                                    setSelectedRsuCount(
                                        rsuCounts[rsu.properties.ipv4_address]
                                            .count
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
                                              rsu.properties.ipv4_address
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
                                              rsu.properties.ipv4_address
                                          ].health
                                        : '0'
                                }
                            />
                        </button>
                    </Marker>
                ))}

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
        </div>
    )
}

export default Map

import React, { useEffect, useState } from 'react'
import ReactMapGL, { Marker, Popup, Source, Layer } from 'react-map-gl'
import RsuMarker from '../components/RsuMarker'
import Grid from '@material-ui/core/Grid'
import mbStyle from '../styles/mb_style.json'
import EnvironmentVars from '../EnvironmentVars'
import {
    selectRsuOnlineStatus,
    selectMapList,
    selectAddRsuPoint,
    selectRsuData,
    selectRsuCounts,
    selectIssScmsStatusData,
    selectSelectedRsu,
    selectRsuCoordinates,
    selectMsgType,
    selectRsuIpv4,
    selectDisplayMap,
    toggleRsuPointSelect,
    // actions
    selectRsu,
    toggleMapDisplay,
    getIssScmsStatus,
    getMapData,
    updateRsuPoints,
    getRsuLastOnline,
} from '../slices/rsuSlice'
import { selectOrganizationName } from '../slices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../components/css/Map.css'

const fillLayer = {
    id: "fill",
    type: "fill",
    source: "polygonSource",
    layout: {},
    paint: {
        "fill-color": "#0080ff",
        "fill-opacity": 0.2,
    },
};

const outlineLayer = {
    id: "outline",
    type: "line",
    source: "polygonSource",
    layout: {},
    paint: {
        "line-color": "#000",
        "line-width": 3,
    },
};

const pointLayer = {
    id: "pointLayer",
    type: "circle",
    source: "pointSource",
    paint: {
        "circle-radius": 5,
        "circle-color": "rgb(255, 164, 0)",
    },
};

function Map(props) {
    const dispatch = useDispatch()
    const organization = useSelector(selectOrganizationName)
    const rsuData = useSelector(selectRsuData)
    const addRsuPoint = useSelector(selectAddRsuPoint);
    const rsuCounts = useSelector(selectRsuCounts)
    const selectedRsu = useSelector(selectSelectedRsu)
    const mapList = useSelector(selectMapList)
    const msgType = useSelector(selectMsgType)
    const issScmsStatusData = useSelector(selectIssScmsStatusData)
    const rsuOnlineStatus = useSelector(selectRsuOnlineStatus)
    const rsuIpv4 = useSelector(selectRsuIpv4)
    const displayMap = useSelector(selectDisplayMap)
    const rsuCoordinates = useSelector(selectRsuCoordinates);

    const [viewport, setViewport] = useState({
        latitude: 39.7392,
        longitude: -104.9903,
        width: '100%',
        height: props.auth ? 'calc(100vh - 135px)' : 'calc(100vh - 100px)',
        zoom: 10,
    })
    const [polygonSource, setPolygonSource] = useState({
        type: "Feature",
        geometry: {
            type: "Polygon",
            coordinates: [],
        },
    });

    const [pointSource, setPointSource] = useState({
        type: "FeatureCollection",
        features: [],
    });


    const [selectedRsuCount, setSelectedRsuCount] = useState(null)

    const [displayType, setDisplayType] = useState('online')
    const [selectMultiplePoints, setSelectMultiplePoints] = useState('selectingpoints')

    useEffect(() => {
        setPolygonSource((prevPolygonSource) => {
            return {
                ...prevPolygonSource,
                geometry: {
                    ...prevPolygonSource.geometry,
                    coordinates: [[...rsuCoordinates]],
                },
            };
        });
    }, [rsuCoordinates]);


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

    const handleSelectingMultiplePoints = () => {
        //dispatch(getIssScmsStatus())
        dispatch(toggleRsuPointSelect());
        setSelectMultiplePoints('selectingpoints')
    }
    const handleDeSelectingMultiplePoints = () => {

        setSelectMultiplePoints('deselectpoints')
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
    const addPointToCoordinates = (point) => {
        console.log("in addpoint")
        console.log(point)
        if (rsuCoordinates.length > 1) {
            if (rsuCoordinates[0] === rsuCoordinates.slice(-1)[0]) {
                let tmp = [...rsuCoordinates];
                tmp.pop();
                dispatch(updateRsuPoints([...tmp, point, rsuCoordinates[0]]));
            } else {
                dispatch(updateRsuPoints([...rsuCoordinates, point, rsuCoordinates[0]]));
            }
        } else {
            dispatch(updateRsuPoints([...rsuCoordinates, point]));
        }
    };

    const buttonStyle = {
        height: '35px',
        padding: '1px 12px',
        margin: '10px 10px',
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
    }

    return (
        <div className="container">
            <Grid
                style={gridStyle}
                container
                alignItems="center"
                direction="row"
            >
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
                {selectMultiplePoints === 'deselectpoints' ? (
                    <button
                        style={buttonStyle}
                        onClick={(e) => handleSelectingMultiplePoints()}
                    >
                        Select Points
                    </button>

                ) : (
                    <button
                        style={buttonStyle}
                        onClick={(e) => handleDeSelectingMultiplePoints()}
                    >
                        Complete point selection
                    </button>
                )}
                {selectedRsu !== null && mapList.includes(rsuIpv4) ? (
                    <button
                        style={buttonStyle}
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
                onClick={
                    addRsuPoint
                        ? (e) => {
                            addPointToCoordinates(e.lngLat);
                        }
                        : null
                }
                onViewportChange={(viewport) => {
                    setViewport(viewport)
                }}
            >
                {rsuCoordinates.length > 2 ? (
                    <Source type="geojson" data={polygonSource}>
                        <Layer {...outlineLayer} />
                        <Layer {...fillLayer} />
                    </Source>
                ) : null}
                <Source type="geojson" data={pointSource}>
                    <Layer {...pointLayer} />
                </Source>

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

export default Map;

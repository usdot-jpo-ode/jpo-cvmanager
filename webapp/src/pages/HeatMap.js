import React, { useEffect, useState } from 'react'
import MapGL, { Source, Layer, Popup } from 'react-map-gl'
import mbStyle from '../styles/mb_style.json'
import EnvironmentVars from '../EnvironmentVars'
import { useSelector, useDispatch } from 'react-redux'
import {
  selectLoading,
  selectRsuData,
  selectRsuCounts,
  selectSelectedRsu,
  selectMsgType,
  selectRsuIpv4,

  // actions
  selectRsu,
} from '../generalSlices/rsuSlice'

function HeatMap(props) {
  const dispatch = useDispatch()

  const loading = useSelector(selectLoading)

  const rsuData = useSelector(selectRsuData)
  const rsuCounts = useSelector(selectRsuCounts)
  const selectedRsu = useSelector(selectSelectedRsu)
  const msgType = useSelector(selectMsgType)
  const rsuIpv4 = useSelector(selectRsuIpv4)

  const [viewport, setViewport] = useState({
    latitude: 39.7392,
    longitude: -104.9903,
    width: '100%',
    height: props.auth ? 'calc(100vh - 135px)' : 'calc(100vh - 100px)',
    zoom: 10,
  })

  const [selectedRsuCount, setSelectedRsuCount] = useState(null)
  const [heatMapData, setHeatMapData] = useState({
    type: 'FeatureCollection',
    features: [],
  })

  useEffect(() => {
    const listener = (e) => {
      if (e.key === 'Escape') dispatch(selectRsu(null))
    }
    window.addEventListener('keydown', listener)

    return () => {
      window.removeEventListener('keydown', listener)
    }
  }, [dispatch])

  useEffect(() => {
    const localHeatMapData = { ...heatMapData }
    const heatMapFeatures = []
    rsuData.forEach((rsu) => {
      heatMapFeatures.push({
        type: 'Feature',
        geometry: {
          type: 'Point',
          coordinates: [rsu.geometry.coordinates[0], rsu.geometry.coordinates[1]],
        },
        properties: {
          ...rsu.properties,
          count: rsu.properties.ipv4_address in rsuCounts ? rsuCounts[rsu.properties.ipv4_address].count : 0,
        },
      })
    })
    localHeatMapData.features = heatMapFeatures
    setHeatMapData(localHeatMapData)
  }, [rsuData, rsuCounts])

  if (loading === false) {
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

  const heatMapLayer = {
    id: 'heatmap',
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
  }

  const rsuLayer = {
    id: 'rsuMarker',
    type: 'circle',
    source: 'heatMapData',
    minzoom: 13,
    paint: {
      'circle-radius': 8,
      'circle-color': 'rgb(255,201,101)',
      'circle-opacity': {
        stops: [[13, 1]],
      },
    },
  }

  return (
    <div className="container">
      <MapGL
        {...viewport}
        mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
        mapStyle={mbStyle}
        onClick={(e) => {
          if (e.features.length > 0 && e.features[0].layer !== undefined && e.features[0].layer.id === 'rsuMarker') {
            let rsu = {
              geometry: {
                coordinates: e.lngLat,
              },
              properties: e.features[0].properties,
            }
            dispatch(selectRsu(rsu))
            if (rsuCounts.hasOwnProperty(rsu.properties.ipv4_address))
              setSelectedRsuCount(rsuCounts[rsu.properties.ipv4_address].count)
            else setSelectedRsuCount(0)
          } else {
            console.debug('rsu not clicked')
          }
        }}
        onViewportChange={(viewport) => {
          setViewport(viewport)
        }}
      >
        <Source type="geojson" data={heatMapData}>
          <Layer {...heatMapLayer} />
          <Layer {...rsuLayer} />
        </Source>
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
              <p />
              <p className="popop-p">Milepost: {selectedRsu.properties.milepost}</p>
              <p className="popop-p">
                Serial Number: {selectedRsu.properties.serial_number ? selectedRsu.properties.serial_number : 'Unknown'}
              </p>
              <p className="popop-p">
                {msgType} Counts: {selectedRsuCount}
              </p>
            </div>
          </Popup>
        ) : null}
      </MapGL>
    </div>
  )
}

export default HeatMap

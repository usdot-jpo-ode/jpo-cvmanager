import React, { useState } from 'react'
import MapGL, { Source, Layer } from 'react-map-gl'
import mbStyle from '../styles/mb_style.json'
import EnvironmentVars from '../EnvironmentVars'
import { useSelector } from 'react-redux'
import { selectRsuCounts, selectHeatMapData } from '../slices/rsuSlice'

function HeatMap(props) {
    const rsuCounts = useSelector(selectRsuCounts)
    const heatMapData = useSelector(selectHeatMapData)

    const [viewport, setViewport] = useState({
        latitude: 39.7392,
        longitude: -104.9903,
        width: '100%',
        height: props.auth ? 'calc(100vh - 135px)' : 'calc(100vh - 100px)',
        zoom: 10,
    })

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
    }

    return (
        <div className="container">
            <MapGL
                {...viewport}
                mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
                mapStyle={mbStyle}
                onViewportChange={(viewport) => {
                    setViewport(viewport)
                }}
            >
                <Source type="geojson" data={heatMapData}>
                    <Layer {...heatMapLayer} />
                </Source>
            </MapGL>
        </div>
    )
}

export default HeatMap

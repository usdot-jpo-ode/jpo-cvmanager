import React, { useState, useEffect } from 'react'
import Map, { Layer, MapRef, Marker, Popup, Source, SymbolLayer } from 'react-map-gl'
import mbStyle from '../../../styles/intersectionMapStyle.json'

import { Container, Col } from 'reactstrap'
import EnvironmentVars from '../../../EnvironmentVars'

const getBoundsForIntersections = (
  selectedIntersection: IntersectionReferenceData | undefined,
  intersections: IntersectionReferenceData[]
) => {
  let bounds = {
    xMin: -105.0907089,
    xMax: -105.0907089,
    yMin: 39.587905,
    yMax: 39.587905,
  }
  if (selectedIntersection != undefined) {
    bounds = {
      xMin: selectedIntersection.longitude,
      xMax: selectedIntersection.longitude,
      yMin: selectedIntersection.latitude,
      yMax: selectedIntersection.latitude,
    }
  } else if (intersections.length >= 1) {
    bounds = {
      xMin: intersections[0].longitude,
      xMax: intersections[0].longitude,
      yMin: intersections[0].latitude,
      yMax: intersections[0].latitude,
    }
  }

  var latitude: number, longitude: number

  for (var i = 0; i < intersections.length; i++) {
    longitude = intersections[i].longitude
    latitude = intersections[i].latitude
    if (longitude >= bounds.xMin && longitude <= bounds.xMax && latitude <= bounds.yMin && latitude >= bounds.yMax) {
      if (bounds.xMin === undefined) {
        bounds = {
          xMin: longitude,
          xMax: longitude,
          yMin: latitude,
          yMax: latitude,
        }
      } else {
        bounds.xMin = longitude < bounds.xMin ? longitude : bounds.xMin
        bounds.xMax = longitude > bounds.xMax ? longitude : bounds.xMax
        bounds.yMin = latitude < bounds.yMin ? latitude : bounds.yMin
        bounds.yMax = latitude > bounds.yMax ? latitude : bounds.yMax
      }
    }
  }

  return [bounds.xMin, bounds.yMin, bounds.xMax, bounds.yMax]
}

const zoomToBounds = (mapRef: React.RefObject<MapRef>, bounds: number[], padding: number = 50) => {
  if (bounds) {
    const [long1, lat1, long2, lat2] = bounds
    mapRef?.current?.fitBounds(
      [
        [long1, lat1],
        [long2, lat2],
      ],
      {
        padding: {
          top: padding,
          bottom: padding,
          left: padding,
          right: padding + 300,
        },
        animate: true,
        duration: 1000,
        maxZoom: 15,
      }
    )
  }
}

const intersectionLabelsLayer: SymbolLayer = {
  id: 'intersection-labels',
  type: 'symbol',
  layout: {
    'text-field': ['to-string', ['get', 'intersectionName']],
    'text-size': 20,
    'text-offset': [0, 2],
    'text-variable-anchor': ['top', 'left', 'right', 'bottom'],
    'text-allow-overlap': true,
    'icon-text-fit': 'both',
  },
  paint: {
    'text-color': '#000000',
    'text-halo-color': '#ffffff',
    'text-halo-width': 5,
  },
}

type Props = {
  intersections: IntersectionReferenceData[]
  selectedIntersection: IntersectionReferenceData | undefined
  onSelectIntersection: (id: number, roadRegulatorId?: number) => void
}

const IntersectionMap = (props: Props) => {
  const [selectedIntersection, setSelectedIntersection] = useState<IntersectionReferenceData | undefined>(
    props.selectedIntersection
  )
  const [viewState, setViewState] = useState({
    latitude: props.selectedIntersection?.latitude ?? 39.587905,
    longitude: props.selectedIntersection?.longitude ?? -105.0907089,
    zoom: 11,
  })
  const myRef = React.createRef<MapRef>()

  const viewBounds = getBoundsForIntersections(selectedIntersection, props.intersections)
  useEffect(() => {
    zoomToBounds(myRef, viewBounds)
  }, [])

  const markers = props.intersections
    .filter((intersection) => intersection.latitude != 0)
    .map((intersection) => {
      return (
        <Marker
          key={intersection.intersectionID}
          latitude={intersection.latitude}
          longitude={intersection.longitude}
          onClick={(e) => {
            e.originalEvent.preventDefault()
            props.onSelectIntersection(intersection.intersectionID, intersection.roadRegulatorID)
            setSelectedIntersection(intersection)
          }}
        >
          <img src="/icons/intersection_icon.png" style={{ width: 70 }} />
        </Marker>
      )
    })

  return (
    <Container fluid={true} style={{ width: '100%', height: '100%', display: 'flex' }}>
      <Col className="mapContainer" style={{ overflow: 'hidden' }}>
        <Map
          {...viewState}
          ref={myRef}
          mapStyle={mbStyle as mapboxgl.Style}
          mapboxAccessToken={EnvironmentVars.MAPBOX_TOKEN}
          attributionControl={true}
          customAttribution={['<a href="https://www.cotrip.com/" target="_blank">© CDOT</a>']}
          styleDiffing
          style={{ width: '100%', height: '100%' }}
          onMove={(evt) => setViewState(evt.viewState)}
          onLoad={() => {
            zoomToBounds(myRef, viewBounds)
          }}
        >
          {markers}
          {selectedIntersection && (
            <Popup
              latitude={selectedIntersection.latitude}
              longitude={selectedIntersection.longitude}
              closeOnClick={false}
              closeButton={false}
            >
              <div>SELECTED {selectedIntersection.intersectionID}</div>
            </Popup>
          )}
          <Source
            type="geojson"
            data={{
              type: 'FeatureCollection',
              features: props.intersections.map((intersection) => ({
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
            <Layer {...intersectionLabelsLayer} />
          </Source>
        </Map>
      </Col>
    </Container>
  )
}

export default IntersectionMap

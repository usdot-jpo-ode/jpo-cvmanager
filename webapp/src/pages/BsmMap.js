import React, { useState, useEffect } from 'react'
import ReactMapGL, { Source, Layer } from 'react-map-gl'
import mbStyle from '../styles/mb_style.json'
import EnvironmentVars from '../EnvironmentVars'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import TextField from '@mui/material/TextField'
import Slider from 'rc-slider'
import Select from 'react-select'
import 'rc-slider/assets/index.css'
import './css/BsmMap.css'
import {
  selectAddPoint,
  selectBsmStart,
  selectBsmEnd,
  selectBsmDateError,
  selectBsmData,
  selectBsmCoordinates,
  selectBsmFilter,
  selectBsmFilterStep,
  selectBsmFilterOffset,

  // actions
  togglePointSelect,
  clearBsm,
  updatePoints,
  updateBsmData,
  updateBsmDate,
  setBsmFilter,
  setBsmFilterStep,
  setBsmFilterOffset,
} from '../generalSlices/rsuSlice'
import { useSelector, useDispatch } from 'react-redux'

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
    'circle-color': 'rgb(255, 164, 0)',
  },
}

function BsmMap(props) {
  const dispatch = useDispatch()

  const bsmData = useSelector(selectBsmData)
  const bsmCoordinates = useSelector(selectBsmCoordinates)
  const addPoint = useSelector(selectAddPoint)
  const startBsmDate = useSelector(selectBsmStart)
  const endBsmDate = useSelector(selectBsmEnd)
  const bsmDateError = useSelector(selectBsmDateError)
  const filter = useSelector(selectBsmFilter)
  const filterStep = useSelector(selectBsmFilterStep)
  const filterOffset = useSelector(selectBsmFilterOffset)

  const [viewport, setViewport] = useState({
    latitude: 39.7392,
    longitude: -104.9903,
    width: '100%',
    height: props.auth ? 'calc(100vh - 136px)' : 'calc(100vh - 100px)',
    zoom: 10,
  })
  const [polygonSource, setPolygonSource] = useState({
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: [],
    },
  })
  const [pointSource, setPointSource] = useState({
    type: 'FeatureCollection',
    features: [],
  })

  const [baseDate, setBaseDate] = useState(new Date(startBsmDate))
  const [startDate, setStartDate] = useState(new Date(baseDate.getTime() + 60000 * filterOffset * filterStep))
  const [endDate, setEndDate] = useState(new Date(startDate.getTime() + 60000 * filterStep))

  useEffect(() => {
    const localBaseDate = new Date(startBsmDate)
    const localStartDate = new Date(localBaseDate.getTime() + 60000 * filterOffset * filterStep)
    const localEndDate = new Date(new Date(localStartDate).getTime() + 60000 * filterStep)
    setBaseDate(localBaseDate)
    setStartDate(localStartDate)
    setEndDate(localEndDate)
  }, [startBsmDate, filterOffset, filterStep])

  const stepOptions = [
    { value: 1, label: '1 minute' },
    { value: 5, label: '5 minutes' },
    { value: 15, label: '15 minutes' },
    { value: 30, label: '30 minutes' },
    { value: 60, label: '60 minutes' },
  ]

  useEffect(() => {
    if (!startBsmDate) {
      dateChanged(new Date(), 'start')
    }
    if (!endBsmDate) {
      dateChanged(new Date(), 'end')
    }
  }, [])

  useEffect(() => {
    setPolygonSource((prevPolygonSource) => {
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

    setPointSource((prevPointSource) => {
      return { ...prevPointSource, features: pointSourceFeatures }
    })
  }, [bsmCoordinates, bsmData, startDate, endDate])

  function dateChanged(e, type) {
    try {
      let mst = DateTime.fromISO(e.toISOString())
      mst.setZone('America/Denver')
      dispatch(updateBsmDate({ type, date: mst.toString() }))
    } catch (err) {
      console.error('Encountered issue updating date: ', err.message)
    }
  }

  const addPointToCoordinates = (point) => {
    if (bsmCoordinates.length > 1) {
      if (bsmCoordinates[0] === bsmCoordinates.slice(-1)[0]) {
        let tmp = [...bsmCoordinates]
        tmp.pop()
        dispatch(updatePoints([...tmp, point, bsmCoordinates[0]]))
      } else {
        dispatch(updatePoints([...bsmCoordinates, point, bsmCoordinates[0]]))
      }
    } else {
      dispatch(updatePoints([...bsmCoordinates, point]))
    }
  }

  function defaultSlider(val) {
    for (var i = 0; i < stepOptions.length; i++) {
      if (stepOptions[i].value === val) {
        return stepOptions[i].label
      }
    }
  }

  return (
    <div className="container">
      {filter ? (
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
              max={(new Date(endBsmDate).getTime() - baseDate.getTime()) / (filterStep * 60000)}
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
              onChange={(e) => dispatch(setBsmFilterStep(e.value))}
            />
            <button className="searchButton" onClick={() => dispatch(setBsmFilter(false))}>
              New Search
            </button>
          </div>
        </div>
      ) : (
        <div className="control">
          <div className="buttonContainer">
            <button
              className={addPoint ? 'selected' : 'button'}
              onClick={(e) => {
                dispatch(togglePointSelect())
              }}
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
                value={dayjs(startBsmDate === '' ? new Date() : startBsmDate)}
                maxDateTime={dayjs(endBsmDate === '' ? new Date() : endBsmDate)}
                onChange={(e) => {
                  dateChanged(e.toDate(), 'start')
                }}
                renderInput={(params) => <TextField {...params} />}
              />
            </LocalizationProvider>
          </div>
          <div className="dateContainer">
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DateTimePicker
                label="Select end date"
                value={dayjs(endBsmDate === '' ? new Date() : endBsmDate)}
                minDateTime={startBsmDate === '' ? null : dayjs(startBsmDate)}
                maxDateTime={dayjs(new Date())}
                onChange={(e) => {
                  dateChanged(e.toDate(), 'end')
                }}
                renderInput={(params) => <TextField {...params} />}
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
            <div id="dateMessage">Date ranges longer than 24 hours are not supported due to their large data sets</div>
          ) : null}
        </div>
      )}
      <ReactMapGL
        {...viewport}
        mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
        mapStyle={mbStyle}
        onClick={
          addPoint
            ? (e) => {
                addPointToCoordinates(e.lngLat)
              }
            : null
        }
        onViewportChange={(viewport) => {
          setViewport(viewport)
        }}
      >
        {bsmCoordinates.length > 2 ? (
          <Source type="geojson" data={polygonSource}>
            <Layer {...outlineLayer} />
            <Layer {...fillLayer} />
          </Source>
        ) : null}
        <Source type="geojson" data={pointSource}>
          <Layer {...pointLayer} />
        </Source>
      </ReactMapGL>
    </div>
  )
}

export default BsmMap

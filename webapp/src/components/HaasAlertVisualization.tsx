import React, { useState, useEffect } from 'react'
import { Source, Layer, Popup, useMap } from 'react-map-gl'
import { SymbolLayer } from 'mapbox-gl'
import { Paper, Typography, FormGroup, FormControlLabel, Switch, Button } from '@mui/material'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { useDispatch } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import dayjs from 'dayjs'
import { getHaasLocationData } from '../generalSlices/haasAlertSlice'
import { HaasLocationProperties, HaasWebsocketLocationParams } from '../models/haas/HaasWebsocketLocation'
import { Feature, Point } from 'geojson'

interface HaasAlertVisualizationProps {
  menuSelection: string[]
  haasLocationData: any
  theme: any
  selectedIncident: Feature<Point, HaasLocationProperties> | null
  onIncidentClose: () => void
}

const haasAlertLayer: SymbolLayer = {
  id: 'haas-alert-points',
  type: 'symbol',
  layout: {
    'icon-image': 'haas-alert-icon',
    'icon-size': 0.15,
    'icon-allow-overlap': true,
    'icon-anchor': 'top',
  },
  paint: {
    'icon-opacity': 0.9,
  },
}

export const HaasAlertVisualization: React.FC<HaasAlertVisualizationProps> = ({
  menuSelection,
  haasLocationData,
  theme,
  selectedIncident,
  onIncidentClose,
}) => {
  const { current: map } = useMap()
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const [haasActiveOnly, setHaasActiveOnly] = useState(false)
  const [haasStartDate, setHaasStartDate] = useState(dayjs().subtract(24, 'hour'))
  const [haasEndDate, setHaasEndDate] = useState(dayjs())

  useEffect(() => {
    if (map) {
      map.loadImage('/icons/haas_location_icon.png', (error, image) => {
        if (error) throw error
        if (!map.hasImage('haas-alert-icon') && image) {
          map.addImage('haas-alert-icon', image)
        }
      })
    }
  }, [map])

  return (
    <>
      <Paper
        className={menuSelection.includes('Configure RSUs') ? 'expandedControl' : 'control'}
        style={{
          backgroundColor: theme.palette.custom.mapLegendBackground,
          padding: '10px',
          position: 'absolute',
          right: '10px',
          top:
            menuSelection.includes('V2x Message Viewer') && menuSelection.includes('Configure RSUs')
              ? '540px'
              : menuSelection.includes('V2x Message Viewer')
              ? '380px'
              : menuSelection.includes('Configure RSUs')
              ? '180px'
              : '30px',
          width: '250px',
          zIndex: 1,
        }}
      >
        <Typography variant="h6" sx={{ fontSize: '1rem', mb: 1 }}>
          HAAS Alert Query Options
        </Typography>

        <FormGroup>
          <FormControlLabel
            control={
              <Switch checked={haasActiveOnly} onChange={(e) => setHaasActiveOnly(e.target.checked)} size="small" />
            }
            label={<Typography variant="body2">Active Incidents Only</Typography>}
          />

          <div style={{ marginTop: '10px' }}>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DateTimePicker
                label="Start Date/Time"
                value={haasStartDate}
                maxDateTime={haasEndDate}
                onChange={(newValue) => setHaasStartDate(newValue)}
                sx={{
                  width: '100%',
                  marginBottom: '8px',
                  '& .MuiInputLabel-root': {
                    fontSize: '0.875rem',
                  },
                  '& .MuiInputBase-input': {
                    fontSize: '0.875rem',
                    padding: '8px',
                  },
                }}
              />

              <DateTimePicker
                label="End Date/Time"
                value={haasEndDate}
                minDateTime={haasStartDate}
                maxDateTime={dayjs()}
                onChange={(newValue) => setHaasEndDate(newValue)}
                sx={{
                  width: '100%',
                  marginBottom: '10px',
                  '& .MuiInputLabel-root': {
                    fontSize: '0.875rem',
                  },
                  '& .MuiInputBase-input': {
                    fontSize: '0.875rem',
                    padding: '8px',
                  },
                }}
              />
            </LocalizationProvider>
          </div>

          <Button
            variant="contained"
            size="small"
            onClick={() => {
              const params: HaasWebsocketLocationParams = {
                active_only: haasActiveOnly,
                start_time_utc_millis: haasStartDate.valueOf(),
                end_time_utc_millis: haasEndDate.valueOf(),
              }
              dispatch(getHaasLocationData(params))
            }}
          >
            Search
          </Button>
        </FormGroup>
      </Paper>

      <Source id="haas-alert-source" type="geojson" data={haasLocationData.data}>
        <Layer {...haasAlertLayer} />
      </Source>

      {selectedIncident && (
        <Popup
          latitude={selectedIncident.geometry.coordinates[1]}
          longitude={selectedIncident.geometry.coordinates[0]}
          onClose={onIncidentClose}
        >
          <div style={{ color: theme.palette.common.black }}>
            <h3>HAAS Alert Incident</h3>
            <p>Type: {selectedIncident.properties.location_type}</p>
            <p>Street Name: {selectedIncident.properties.street_name}</p>
            <p>Status: {selectedIncident.properties.is_active ? 'Active' : 'Inactive'}</p>
            <p>Start Time: {new Date(selectedIncident.properties.start_time).toLocaleString()}</p>
            {selectedIncident.properties.end_time && (
              <p>End Time: {new Date(selectedIncident.properties.end_time).toLocaleString()}</p>
            )}
          </div>
        </Popup>
      )}
    </>
  )
}

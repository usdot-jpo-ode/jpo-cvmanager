import React, { useState, useEffect } from 'react'
import { Source, Layer, Popup, useMap } from 'react-map-gl'
import { SymbolLayer } from 'mapbox-gl'
import { Paper, Typography, FormGroup, FormControlLabel, Switch, Button, Box, Grid2, Stack } from '@mui/material'
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
import { WarningAmber } from '@mui/icons-material'
import './css/HaasAlertVisualization.css'

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
  const [haasActiveOnly, setHaasActiveOnly] = useState(true)
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
          maxWidth="350px"
        >
          <Stack className="haas-alert-popup">
            <Grid2 container columnSpacing={0.5} rowSpacing={0} className="haas-alert-header">
              <Grid2 size={1} className="haas-alert-icon-container">
                <WarningAmber color="warning" fontSize="large" />
              </Grid2>
              <Grid2 size={6} className="haas-alert-title-container">
                <Typography variant="h6" color="primary" className="haas-alert-title">
                  HAAS Alert Incident
                </Typography>
                <Typography variant="body2" color="secondary" className="haas-alert-subtitle">
                  {selectedIncident.properties.detailed_type || selectedIncident.properties.location_type}
                </Typography>
              </Grid2>
              <Grid2 size={4} className="haas-alert-status-container">
                <Box
                  className={`haas-alert-status-badge ${selectedIncident.properties.is_active ? 'active' : 'inactive'}`}
                >
                  {selectedIncident.properties.is_active ? 'Active' : 'Inactive'}
                </Box>
              </Grid2>
            </Grid2>

            <Grid2 id="popup-body" container columnSpacing={1} rowSpacing={1} className="haas-alert-body">
              <Grid2 size={4} justifyContent="flex-start">
                <Typography variant="body2" className="haas-alert-label">
                  Street Name:
                </Typography>
              </Grid2>
              <Grid2 size={8} justifyContent="flex-start">
                <Typography variant="body2" className="haas-alert-value">
                  {selectedIncident.properties.street_name || 'N/A'}
                </Typography>
              </Grid2>

              <Grid2 size={4} justifyContent="flex-start">
                <Typography variant="body2" className="haas-alert-label">
                  Type:
                </Typography>
              </Grid2>
              <Grid2 size={8} justifyContent="flex-start">
                <Typography variant="body2" sx={{ textTransform: 'capitalize' }}>
                  {selectedIncident.properties.type || 'N/A'}
                </Typography>
              </Grid2>

              <Grid2 size={4} justifyContent="flex-start">
                <Typography variant="body2" className="haas-alert-label">
                  Start Time:
                </Typography>
              </Grid2>
              <Grid2 size={8} justifyContent="flex-start">
                <Typography variant="body2">
                  {selectedIncident.properties.start_time
                    ? new Date(selectedIncident.properties.start_time).toLocaleString()
                    : 'N/A'}
                </Typography>
              </Grid2>

              {selectedIncident.properties.end_time && (
                <>
                  <Grid2 size={4} justifyContent="flex-start">
                    <Typography variant="body2" className="haas-alert-label">
                      End Time:
                    </Typography>
                  </Grid2>
                  <Grid2 size={8} justifyContent="flex-start">
                    <Typography variant="body2">
                      {new Date(selectedIncident.properties.end_time).toLocaleString()}
                    </Typography>
                  </Grid2>
                </>
              )}

              {selectedIncident.properties.alt && (
                <>
                  <Grid2 size={4} justifyContent="flex-start">
                    <Typography variant="body2" className="haas-alert-label">
                      Altitude:
                    </Typography>
                  </Grid2>
                  <Grid2 size={8} justifyContent="flex-start">
                    <Typography variant="body2">{selectedIncident.properties.alt.toFixed(0)} m</Typography>
                  </Grid2>
                </>
              )}

              {selectedIncident.properties.things_active && selectedIncident.properties.things_active.length > 0 && (
                <>
                  <Grid2 size={4} justifyContent="flex-start">
                    <Typography variant="body2" sx={{ fontWeight: 600, color: theme.palette.text.primary }}>
                      Active Things:
                    </Typography>
                  </Grid2>
                  <Grid2 size={8} justifyContent="flex-start">
                    <Typography variant="body2">{selectedIncident.properties.things_active.length}</Typography>
                  </Grid2>
                </>
              )}

              {selectedIncident.properties.things_inactive &&
                selectedIncident.properties.things_inactive.length > 0 && (
                  <>
                    <Grid2 size={4} justifyContent="flex-start">
                      <Typography variant="body2" className="haas-alert-label">
                        Inactive Things:
                      </Typography>
                    </Grid2>
                    <Grid2 size={8} justifyContent="flex-start">
                      <Typography variant="body2">{selectedIncident.properties.things_inactive.length}</Typography>
                    </Grid2>
                  </>
                )}
            </Grid2>

            <Box className="haas-alert-footer">
              <Typography variant="caption" className="haas-alert-footer-label">
                Incident ID:
              </Typography>
              <Typography variant="body2" className="haas-alert-footer-id">
                {selectedIncident.properties.id || 'Unknown'}
              </Typography>
            </Box>
          </Stack>
        </Popup>
      )}
    </>
  )
}

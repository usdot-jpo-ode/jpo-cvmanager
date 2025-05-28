import React, { useState, useEffect, ChangeEvent, useMemo } from 'react'
import Slider from '@mui/material/Slider'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import {
  Box,
  Typography,
  TextField,
  Button,
  InputAdornment,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  Grid2,
  AccordionSummary,
  Paper,
} from '@mui/material'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import { styled, useTheme } from '@mui/material/styles'
import { format } from 'date-fns'
import JSZip from 'jszip'
import {
  BSM_COUNTS_CHART_DATA,
  downloadMapData,
  handleImportedMapMessageData,
  onTimeQueryChanged,
  selectBsmEventsByMinute,
  selectBsmTrailLength,
  selectDecoderModeEnabled,
  selectPlaybackModeActive,
  selectSliderTimeValue,
  setBsmTrailLength,
  setSliderValue,
  setTimeWindowSeconds,
  toggleLiveDataActive,
  togglePlaybackModeActive,
} from './map-slice'
import {
  selectLiveDataActive,
  selectMapSpatTimes,
  selectQueryParams,
  selectSliderValue,
  selectTimeWindowSeconds,
} from './map-slice'
import { getTimeRange } from './utilities/map-utils'
import {
  selectIntersections,
  setSelectedIntersection,
  selectSelectedIntersectionId,
} from '../../../generalSlices/intersectionSlice'
import { BarChart, XAxis, Bar, ResponsiveContainer, Tooltip } from 'recharts'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { decoderModeToggled, setAsn1DecoderDialogOpen } from '../decoder/asn1-decoder-slice'
import toast from 'react-hot-toast'
import { ExpandMoreOutlined, Pause, PlayArrowOutlined, UploadFile } from '@mui/icons-material'
import { getNewAccurateTimeMillis, selectTimeOffsetMillis } from '../../../generalSlices/timeSyncSlice'

export const getNumber = (value: string | undefined): number | undefined => {
  if (value == null) return undefined
  const num = parseInt(value)
  if (isNaN(num)) {
    return undefined
  }
  return num
}

const formatMinutesAfterMidnightTime = (minutes: number) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`
}

const TimelineTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div
        className="custom-tooltip"
        style={{
          backgroundColor: '#fff',
          padding: '10px',
          border: '1px solid #ccc',
          position: 'relative',
          bottom: '15px',
        }}
      >
        <p className="label" style={{ color: '#333' }}>{`Time: ${payload[0].payload.timestamp}`}</p>
        <p className="intro" style={{ color: '#333' }}>{`Events: ${payload[0].payload.count}`}</p>
      </div>
    )
  }
  return null
}

interface TimelineCursorProps {
  x?: number
  y?: number
  width?: number
  height?: number
}

const TimelineCursor: React.FC<TimelineCursorProps & { bsmEventsByMinute: BSM_COUNTS_CHART_DATA[] }> = ({
  x = 0,
  y = 0,
  width = 0,
  height = 0,
  bsmEventsByMinute,
}) => {
  return (
    <rect
      x={x + width / 2 - 6}
      y={y - 1}
      width={12}
      height={height + 3}
      fill={bsmEventsByMinute !== null && bsmEventsByMinute.length > 0 ? '#10B981' : 'transparent'}
      style={{ pointerEvents: 'none' }}
    />
  )
}

interface TimelineAxisTickProps {
  x?: number
  y?: number
  payload?: {
    value: any
  }
}

const TimelineAxisTick: React.FC<TimelineAxisTickProps> = ({ x = 0, y = 0, payload }) => {
  const timeString = formatMinutesAfterMidnightTime(payload?.value)
  return (
    <g transform={`translate(${x},${y})`}>
      <text x={0} y={0} dy={10} textAnchor="middle" fill="#FFFFFF">
        {timeString}
      </text>
    </g>
  )
}

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  ({ theme }) => ({})
)

const VisuallyHiddenInput = styled('input')({
  clip: 'rect(0 0 0 0)',
  clipPath: 'inset(50%)',
  height: 1,
  overflow: 'hidden',
  position: 'absolute',
  bottom: 0,
  left: 0,
  whiteSpace: 'nowrap',
  width: 1,
})

const AccordionDetails = styled(MuiAccordionDetails)(({ theme }) => ({}))

function ControlPanel() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const queryParams = useSelector(selectQueryParams)
  const timeWindowSeconds = useSelector(selectTimeWindowSeconds)
  const sliderValue = useSelector(selectSliderValue)
  const mapSpatTimes = useSelector(selectMapSpatTimes)
  const liveDataActive = useSelector(selectLiveDataActive)
  const sliderTimeValue = useSelector(selectSliderTimeValue)
  const bsmTrailLength = useSelector(selectBsmTrailLength)
  const selectedIntersectionId = useSelector(selectSelectedIntersectionId)
  const intersectionsList = useSelector(selectIntersections)
  const decoderModeEnabled = useSelector(selectDecoderModeEnabled)

  const bsmEventsByMinute = useSelector(selectBsmEventsByMinute)
  const playbackModeActive = useSelector(selectPlaybackModeActive)
  const timeOffsetMillis = useSelector(selectTimeOffsetMillis)

  const theme = useTheme()

  const getQueryParams = ({ startDate, endDate, eventDate }: { startDate: Date; endDate: Date; eventDate: Date }) => {
    return {
      eventTime: eventDate,
      timeBefore: Math.round((eventDate.getTime() - startDate.getTime()) / 1000),
      timeAfter: Math.round((endDate.getTime() - eventDate.getTime()) / 1000),
    }
  }

  const [bsmTrailLengthLocal, setBsmTrailLengthLocal] = useState<string | undefined>(bsmTrailLength.toString())
  const [eventTime, setEventTime] = useState<dayjs.Dayjs | null>(
    dayjs(getQueryParams(queryParams).eventTime.toString())
  )
  const [timeBefore, setTimeBefore] = useState<string | undefined>(getQueryParams(queryParams).timeBefore.toString())
  const [timeAfter, setTimeAfter] = useState<string | undefined>(getQueryParams(queryParams).timeAfter.toString())
  const [timeWindowSecondsLocal, setTimeWindowSecondsLocal] = useState<string | undefined>(
    timeWindowSeconds?.toString()
  )

  const [isExpandedTimeQuery, setIsExpandedTimeQuery] = useState(true)
  const [isExpandedDownload, setIsExpandedDownload] = useState(false)
  const [isExpandedSettings, setIsExpandedSettings] = useState(false)
  const [isExpandedDecoder, setIsExpandedDecoder] = useState(false)

  const isQueryParamFormValid = () => {
    try {
      const d = eventTime?.toDate().getTime()!
      return (
        !isNaN(d) &&
        getNumber(timeBefore) !== null &&
        getNumber(timeAfter) !== null &&
        getNumber(timeWindowSecondsLocal) !== null
      )
    } catch (e) {
      return false
    }
  }

  const isNewQueryAllowed = useMemo(() => {
    if (!isQueryParamFormValid()) return false
    const eventTimeDate = eventTime?.toDate()
    const timeBeforeNum = getNumber(timeBefore)
    const timeAfterNum = getNumber(timeAfter)
    const currentQueryParams = {
      eventDate: eventTimeDate,
      startDate: new Date(eventTimeDate.getTime() - (timeBeforeNum ?? 0) * 1000),
      endDate: new Date(eventTimeDate.getTime() + (timeAfterNum ?? 0) * 1000),
    }
    return (
      currentQueryParams.eventDate.getTime() !== queryParams.eventDate.getTime() ||
      currentQueryParams.startDate.getTime() !== queryParams.startDate.getTime() ||
      currentQueryParams.endDate.getTime() !== queryParams.endDate.getTime()
    )
  }, [eventTime, timeBefore, timeAfter, queryParams])

  useEffect(() => {
    const newDateParams = getQueryParams(queryParams)
    setEventTime(dayjs(newDateParams.eventTime))
    setTimeBefore(newDateParams.timeBefore.toString())
    setTimeAfter(newDateParams.timeAfter.toString())
  }, [queryParams])

  useEffect(() => {
    setTimeWindowSecondsLocal(timeWindowSeconds.toString())
  }, [timeWindowSeconds])

  useEffect(() => {
    if (getNumber(timeWindowSecondsLocal) != null && getNumber(timeWindowSecondsLocal) !== timeWindowSeconds) {
      dispatch(setTimeWindowSeconds(getNumber(timeWindowSecondsLocal)))
    }
  }, [timeWindowSecondsLocal])

  const timelineTicks = [120, 240, 360, 480, 600, 720, 840, 960, 1080, 1200, 1320]

  const openMessageData = (files: FileList | null) => {
    if (files == null) return
    const file = files[0]
    var jsZip = new JSZip()
    const messageData: {
      mapData: ProcessedMap[]
      bsmData: OdeBsmData[]
      spatData: ProcessedSpat[]
      notificationData: any
    } = {
      mapData: [],
      bsmData: [],
      spatData: [],
      notificationData: undefined,
    }
    jsZip
      .loadAsync(file)
      .then(async (zip) => {
        const zipObjects: { relativePath: string; zipEntry: JSZip.JSZipObject }[] = []
        zip.forEach((relativePath, zipEntry) => zipObjects.push({ relativePath, zipEntry }))
        for (let i = 0; i < zipObjects.length; i++) {
          const { relativePath, zipEntry } = zipObjects[i]
          if (relativePath.endsWith('_MAP_data.json')) {
            const data = await zipEntry.async('string')
            messageData.mapData = JSON.parse(data)
          } else if (relativePath.endsWith('_BSM_data.json')) {
            const data = await zipEntry.async('string')
            messageData.bsmData = JSON.parse(data)
            // TODO: Add notification data to ZIP download
          } else if (relativePath.endsWith('_SPAT_data.json')) {
            const data = await zipEntry.async('string')
            messageData.spatData = JSON.parse(data)
          }
        }
        dispatch(handleImportedMapMessageData(messageData))
      })
      .catch((e) => {
        toast.error(`Error loading message data. Make sure to upload a previously generated ZIP archive`)
        console.error(`Error loading message data: ${e.message}`)
      })
  }

  return (
    <div
      style={{
        width: '100%',
      }}
    >
      <Paper
        sx={{
          display: 'flex',
          alignItems: 'center',
          mb: 1,
          mt: 1.5,
          backgroundColor: theme.palette.background.paper,
          py: 1,
        }}
      >
        <button
          style={{ marginLeft: '1rem', border: 'none', background: 'none' }}
          onClick={() => dispatch(togglePlaybackModeActive())}
          color="info"
        >
          {playbackModeActive ? <Pause /> : <PlayArrowOutlined />}
        </button>
        <Slider
          sx={{ ml: 2, width: 'calc(100% - 80px)' }}
          value={sliderValue}
          onChange={(event: Event, value: number | number[], activeThumb: number) => dispatch(setSliderValue(value))}
          min={0}
          max={getTimeRange(queryParams.startDate, queryParams.endDate)}
          valueLabelDisplay="auto"
          disableSwap
          color="primary"
        />
      </Paper>

      <Paper
        sx={{
          maxHeight: '600px',
          overflow: 'auto',
          scrollbarColor: `${theme.palette.text.primary} ${theme.palette.background.paper}`,
        }}
      >
        <Accordion
          disableGutters
          disabled={decoderModeEnabled}
          expanded={!decoderModeEnabled && isExpandedTimeQuery}
          onChange={() => setIsExpandedTimeQuery(!isExpandedTimeQuery)}
          sx={{
            py: 0.5,
            borderRadius: '4px',
            '& .Mui-expanded': {
              backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
            },
          }}
        >
          <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
            <Typography fontSize="16px">
              Time Query
              {liveDataActive && <Chip label="Live Data Active" sx={{ ml: 2 }} color="success" size="small" />}
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Box sx={{ mt: 1 }}>
              <Grid2 container columnSpacing={2} rowSpacing={2} sx={{ mt: 1 }}>
                <Grid2 size={{ xs: 12, md: 5 }}>
                  <FormControl fullWidth sx={{ mt: 1 }}>
                    <InputLabel id="intersection-label">Intersection ID</InputLabel>
                    <Select
                      labelId="intersection-label"
                      label="Intersection ID"
                      value={selectedIntersectionId}
                      onChange={(e) => {
                        dispatch(setSelectedIntersection(e.target.value as number))
                      }}
                    >
                      {/* TODO: Update to display intersection Name */}
                      {intersectionsList.map((intersection) => (
                        <MenuItem value={intersection.intersectionID} key={intersection.intersectionID}>
                          {intersection.intersectionID}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid2>
                <Grid2 size={{ xs: 12, md: 5 }}>
                  <FormControl fullWidth>
                    <TextField
                      label="Time Before Event"
                      name="timeRangeBefore"
                      type="number"
                      sx={{ mt: 1 }}
                      onChange={(e) => {
                        setTimeBefore(e.target.value)
                      }}
                      slotProps={{
                        input: {
                          endAdornment: <InputAdornment position="end">seconds</InputAdornment>,
                        },
                      }}
                      value={timeBefore}
                    />
                  </FormControl>
                </Grid2>
                <Grid2 size={{ xs: 12, md: 10 }}>
                  <FormControl fullWidth>
                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                      <DateTimePicker
                        label="Event Date"
                        disabled={liveDataActive}
                        value={dayjs(eventTime ?? new Date())}
                        onChange={(e) => {
                          setEventTime(e)
                        }}
                      />
                    </LocalizationProvider>
                  </FormControl>
                </Grid2>
                <Grid2 size={{ xs: 12, md: 5 }}>
                  <FormControl fullWidth>
                    <TextField
                      label="Time After Event"
                      name="timeRangeAfter"
                      type="number"
                      sx={{ mt: 1 }}
                      onChange={(e) => {
                        setTimeAfter(e.target.value)
                      }}
                      slotProps={{
                        input: {
                          endAdornment: <InputAdornment position="end">seconds</InputAdornment>,
                        },
                      }}
                      value={timeAfter}
                    />
                  </FormControl>
                </Grid2>
                <Grid2 size={{ xs: 12, md: 5 }}>
                  <FormControl fullWidth>
                    <TextField
                      label="Time Render Window"
                      name="timeRangeAfter"
                      type="number"
                      sx={{ mt: 1 }}
                      onChange={(e) => {
                        if (Number.isInteger(Number(e.target.value))) {
                          dispatch(setTimeWindowSeconds(parseInt(e.target.value)))
                        }
                      }}
                      slotProps={{
                        input: {
                          endAdornment: <InputAdornment position="end">seconds</InputAdornment>,
                        },
                      }}
                      value={timeWindowSeconds}
                    />
                  </FormControl>
                </Grid2>
              </Grid2>
              <Button
                sx={{ mt: 2 }}
                onClick={() => {
                  dispatch(toggleLiveDataActive())
                }}
                color="info"
                variant="outlined"
                className="capital-case"
              >
                {liveDataActive ? 'Stop Live Data' : 'Render Live Data'}
              </Button>
            </Box>
          </AccordionDetails>
        </Accordion>

        <Accordion
          disableGutters
          disabled={decoderModeEnabled}
          expanded={!decoderModeEnabled && isExpandedDownload}
          onChange={() => setIsExpandedDownload(!isExpandedDownload)}
          sx={{
            py: 0.5,
            '& .Mui-expanded': {
              backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
            },
          }}
        >
          <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
            <Typography fontSize="16px">Message Times & Download</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <div>
              <Typography fontSize="16px">
                Visualization Time: {format(sliderTimeValue.start, 'MM/dd/yyyy HH:mm:ss')} -{' '}
                {format(sliderTimeValue.end, 'MM/dd/yyyy HH:mm:ss')}
              </Typography>
              <Typography fontSize="16px">
                MAP Message Time:{' '}
                {mapSpatTimes.mapTime === 0 ? 'No Data' : format(mapSpatTimes.mapTime * 1000, 'MM/dd/yyyy HH:mm:ss')}
              </Typography>

              <Typography fontSize="16px">
                SPAT Message Time:{' '}
                {mapSpatTimes.spatTime === 0 ? 'No Data' : format(mapSpatTimes.spatTime * 1000, 'MM/dd/yyyy HH:mm:ss')}
              </Typography>

              {liveDataActive && (
                <Typography fontSize="16px">
                  Live Spat Offset: {mapSpatTimes.spatTime * 1000 - getNewAccurateTimeMillis(timeOffsetMillis)}
                </Typography>
              )}
              <Typography fontSize="16px">Activity Chart for {format(sliderTimeValue.start, 'MM/dd/yyyy')}:</Typography>

              <ResponsiveContainer
                width="100%"
                height={80}
                style={{
                  borderRadius: '4px',
                }}
              >
                <BarChart
                  data={bsmEventsByMinute}
                  barGap={0}
                  barCategoryGap={0}
                  onClick={(data) => {
                    if (data !== null && data.activePayload !== undefined && data.activePayload !== null) {
                      setEventTime(dayjs(data.activePayload[0].payload.minute))
                    }
                  }}
                  style={{
                    fill: theme.palette.text.primary,
                  }}
                >
                  <XAxis
                    dataKey="minutesAfterMidnight"
                    type="number"
                    domain={[0, 1440]}
                    tick={<TimelineAxisTick />}
                    ticks={timelineTicks}
                  />
                  <Bar dataKey="count" barSize={10} minPointSize={10}></Bar>
                  <Tooltip
                    cursor={<TimelineCursor bsmEventsByMinute={[]} />}
                    content={({ active, payload, label }) => (
                      <TimelineTooltip active={active} payload={payload} label={label} />
                    )}
                  />
                </BarChart>
              </ResponsiveContainer>
              <Button
                sx={{ m: 1 }}
                variant="outlined"
                color="info"
                className="capital-case"
                onClick={() => dispatch(downloadMapData())}
              >
                Download All Message Data
              </Button>
              <Box sx={{ display: 'flex', justifyContent: 'flex-start', alignItems: 'center' }}>
                <Typography fontSize="medium" sx={{ mt: 1, mr: 1 }}>
                  Upload Message Data:
                </Typography>
                <Button variant="text" component="label" sx={{ mt: 1 }} startIcon={<UploadFile color="primary" />}>
                  <VisuallyHiddenInput
                    accept=".zip"
                    id="upload"
                    name="upload"
                    type="file"
                    multiple={false}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => {
                      openMessageData(e.target.files)
                    }}
                  />
                  Choose File
                </Button>
              </Box>
            </div>
          </AccordionDetails>
        </Accordion>

        <Accordion
          disableGutters
          expanded={isExpandedDecoder}
          onChange={() => setIsExpandedDecoder(!isExpandedDecoder)}
          sx={{
            py: 0.5,
            borderRadius: '4px',
            '& .Mui-expanded': {
              backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
            },
          }}
        >
          <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
            <Typography fontSize="16px">
              ASN.1 Decoding {decoderModeEnabled && <Chip label="Decoder Mode Active" sx={{ ml: 2 }} color="warning" />}
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <div>
              <div>
                <Typography>To decode and render ASN.1 encoded data:</Typography>
                <Typography>
                  1. Enable Decoder Mode. This will disable all other map data until Decoder Mode is disabled.
                </Typography>
                <Typography>
                  2. Hit "Decode + Render Data" to open the decoder dialog, where you can enter/upload asn.1 encoded
                </Typography>
                <Typography>
                  3. Return to the map to render your data. Data can be toggled on/off in the dialog menu.
                </Typography>
              </div>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'flex-start',
                  alignItems: 'center',
                  width: '100%',
                }}
              >
                <Switch
                  checked={decoderModeEnabled}
                  onChange={(event) => {
                    dispatch(decoderModeToggled(event.target.checked))
                  }}
                />
                <Typography
                  fontSize="medium"
                  sx={{
                    ml: 2,
                  }}
                >
                  Decoder Mode
                </Typography>
              </Box>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'flex-start',
                  alignItems: 'center',
                  width: '100%',
                }}
              >
                <Button
                  sx={{ my: 1, ml: 2 }}
                  variant="contained"
                  disabled={!decoderModeEnabled}
                  onClick={() => dispatch(setAsn1DecoderDialogOpen(true))}
                  className="capital-case"
                >
                  Decode + Render Data
                </Button>
              </Box>
            </div>
          </AccordionDetails>
        </Accordion>
      </Paper>
    </div>
  )
}

export default ControlPanel

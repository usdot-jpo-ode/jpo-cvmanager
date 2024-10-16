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
  Checkbox,
  InputAdornment,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import MuiAccordionSummary, { AccordionSummaryProps } from '@mui/material/AccordionSummary'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp'
import { styled } from '@mui/material/styles'
import { format } from 'date-fns'
import JSZip from 'jszip'
import {
  downloadMapData,
  handleImportedMapMessageData,
  onTimeQueryChanged,
  selectBsmEventsByMinute,
  selectBsmTrailLength,
  selectPlaybackModeActive,
  selectSliderTimeValue,
  setBsmTrailLength,
  setLaneLabelsVisible,
  setShowPopupOnHover,
  setSigGroupLabelsVisible,
  setSliderValue,
  toggleLiveDataActive,
  togglePlaybackModeActive,
} from './map-slice'
import {
  selectLaneLabelsVisible,
  selectLiveDataActive,
  selectMapSpatTimes,
  selectQueryParams,
  selectShowPopupOnHover,
  selectSigGroupLabelsVisible,
  selectSliderValue,
  selectTimeWindowSeconds,
} from './map-slice'
import { selectSignalStateLayerStyle, setSignalLayerLayout } from './map-layer-style-slice'
import { getTimeRange } from './utilities/map-utils'
import {
  selectIntersections,
  setSelectedIntersection,
  selectSelectedIntersectionId,
} from '../../../generalSlices/intersectionSlice'
import pauseIcon from '../../../icons/pause.png'
import playIcon from '../../../icons/play.png'
import { BarChart, XAxis, Bar, ResponsiveContainer, Tooltip } from 'recharts'
import { useAppDispatch, useAppSelector } from '../../../hooks'
import toast from 'react-hot-toast'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  ({ theme }) => ({})
)

const AccordionSummary = styled((props: AccordionSummaryProps) => (
  <MuiAccordionSummary expandIcon={<ArrowForwardIosSharpIcon sx={{ fontSize: '0.8rem' }} />} {...props} />
))(({ theme }) => ({
  minHeight: 0,
  paddingLeft: 10,
  flexDirection: 'row-reverse',
  '& .MuiAccordionSummary-expandIconWrapper.Mui-expanded': {
    transform: 'rotate(90deg)',
  },
  '& .MuiAccordionSummary-content': {
    marginLeft: theme.spacing(1),
    marginTop: 0,
    marginBottom: 0,
  },
}))

const AccordionDetails = styled(MuiAccordionDetails)(({ theme }) => ({}))

function ControlPanel() {
  const dispatch = useAppDispatch()

  const signalStateLayerStyle = useAppSelector(selectSignalStateLayerStyle)

  const queryParams = useAppSelector(selectQueryParams)
  const timeWindowSeconds = useAppSelector(selectTimeWindowSeconds)
  const sliderValue = useAppSelector(selectSliderValue)
  const mapSpatTimes = useAppSelector(selectMapSpatTimes)
  const sigGroupLabelsVisible = useAppSelector(selectSigGroupLabelsVisible)
  const laneLabelsVisible = useAppSelector(selectLaneLabelsVisible)
  const showPopupOnHover = useAppSelector(selectShowPopupOnHover)
  const liveDataActive = useAppSelector(selectLiveDataActive)
  const sliderTimeValue = useAppSelector(selectSliderTimeValue)
  const bsmTrailLength = useAppSelector(selectBsmTrailLength)
  const selectedIntersectionId = useAppSelector(selectSelectedIntersectionId)
  const intersectionsList = useAppSelector(selectIntersections)

  const bsmEventsByMinute = useAppSelector(selectBsmEventsByMinute)
  const playbackModeActive = useAppSelector(selectPlaybackModeActive)

  const getQueryParams = ({
    startDate,
    endDate,
    eventDate,
    timeWindowSeconds,
  }: {
    startDate: Date
    endDate: Date
    eventDate: Date
    timeWindowSeconds: number
  }) => {
    return {
      eventTime: eventDate,
      timeBefore: Math.round((eventDate.getTime() - startDate.getTime()) / 1000),
      timeAfter: Math.round((endDate.getTime() - eventDate.getTime()) / 1000),
      timeWindowSeconds: timeWindowSeconds,
    }
  }

  const [shouldReRenderEventTime, setShouldReRenderEventTime] = useState(true)
  const [shouldReRenderTimeBefore, setShouldReRenderTimeBefore] = useState(true)
  const [shouldReRenderTimeAfter, setShouldReRenderTimeAfter] = useState(true)
  const [shouldReRenderTimeWindowSeconds, setShouldReRenderTimeWindowSeconds] = useState(true)
  const [shouldReRenderBsmTrail, setShouldReRenderBsmTrail] = useState(true)
  const [bsmTrailLengthLocal, setBsmTrailLengthLocal] = useState<string | undefined>(bsmTrailLength.toString())
  const [prevBsmTrailLength, setPrevBsmTrailLength] = useState<number>(bsmTrailLength)
  const [oldDateParams, setOldDateParams] = useState(getQueryParams({ ...queryParams, timeWindowSeconds }))
  const [eventTime, setEventTime] = useState<dayjs.Dayjs | null>(
    dayjs(getQueryParams({ ...queryParams, timeWindowSeconds }).eventTime.toString())
  )
  const [timeBefore, setTimeBefore] = useState<string | undefined>(
    getQueryParams({ ...queryParams, timeWindowSeconds }).timeBefore.toString()
  )
  const [timeAfter, setTimeAfter] = useState<string | undefined>(
    getQueryParams({ ...queryParams, timeWindowSeconds }).timeAfter.toString()
  )
  const [timeWindowSecondsLocal, setTimeWindowSeconds] = useState<string | undefined>(
    getQueryParams({ ...queryParams, timeWindowSeconds }).timeWindowSeconds.toString()
  )

  useEffect(() => {
    const newDateParams = getQueryParams({ ...queryParams, timeWindowSeconds })
    if (newDateParams.eventTime.getTime() !== oldDateParams.eventTime.getTime()) {
      setShouldReRenderEventTime(false)
      setEventTime(dayjs(newDateParams.eventTime))
    }
    if (newDateParams.timeBefore !== oldDateParams.timeBefore) {
      setShouldReRenderTimeBefore(false)
      setTimeBefore(newDateParams.timeBefore.toString())
    }
    if (newDateParams.timeAfter !== oldDateParams.timeAfter) {
      setShouldReRenderTimeAfter(false)
      setTimeAfter(newDateParams.timeAfter.toString())
    }
    if (newDateParams.timeWindowSeconds !== oldDateParams.timeWindowSeconds) {
      setShouldReRenderTimeWindowSeconds(false)
      setTimeWindowSeconds(newDateParams.timeWindowSeconds.toString())
    }
  }, [{ ...queryParams, timeWindowSeconds }])

  useEffect(() => {
    if (bsmTrailLength !== prevBsmTrailLength) {
      setShouldReRenderTimeAfter(false)
      setPrevBsmTrailLength(bsmTrailLength)
      setBsmTrailLengthLocal(bsmTrailLength.toString())
    }
  }, [bsmTrailLength])

  useEffect(() => {
    if (shouldReRenderEventTime && isFormValid()) {
      setOldDateParams({
        eventTime: eventTime!.toDate(),
        timeBefore: getNumber(timeBefore)!,
        timeAfter: getNumber(timeAfter)!,
        timeWindowSeconds: getNumber(timeWindowSecondsLocal)!,
      })
      dispatch(
        onTimeQueryChanged({
          eventTime: eventTime!.toDate(),
          timeBefore: getNumber(timeBefore),
          timeAfter: getNumber(timeAfter),
          timeWindowSeconds: getNumber(timeWindowSecondsLocal),
        })
      )
    } else {
      setShouldReRenderEventTime(true)
    }
  }, [eventTime])

  useEffect(() => {
    if (shouldReRenderTimeBefore && isFormValid()) {
      setOldDateParams({
        eventTime: eventTime!.toDate(),
        timeBefore: getNumber(timeBefore)!,
        timeAfter: getNumber(timeAfter)!,
        timeWindowSeconds: getNumber(timeWindowSecondsLocal)!,
      })
      dispatch(
        onTimeQueryChanged({
          eventTime: eventTime!.toDate(),
          timeBefore: getNumber(timeBefore),
          timeAfter: getNumber(timeAfter),
          timeWindowSeconds: getNumber(timeWindowSecondsLocal),
        })
      )
    } else {
      setShouldReRenderTimeBefore(true)
    }
  }, [timeBefore])

  useEffect(() => {
    if (shouldReRenderTimeAfter && isFormValid()) {
      setOldDateParams({
        eventTime: eventTime!.toDate(),
        timeBefore: getNumber(timeBefore)!,
        timeAfter: getNumber(timeAfter)!,
        timeWindowSeconds: getNumber(timeWindowSecondsLocal)!,
      })
      dispatch(
        onTimeQueryChanged({
          eventTime: eventTime!.toDate(),
          timeBefore: getNumber(timeBefore),
          timeAfter: getNumber(timeAfter),
          timeWindowSeconds: getNumber(timeWindowSecondsLocal),
        })
      )
    } else {
      setShouldReRenderTimeAfter(true)
    }
  }, [timeAfter])

  useEffect(() => {
    if (shouldReRenderTimeWindowSeconds && isFormValid()) {
      setOldDateParams({
        eventTime: eventTime!.toDate(),
        timeBefore: getNumber(timeBefore)!,
        timeAfter: getNumber(timeAfter)!,
        timeWindowSeconds: getNumber(timeWindowSecondsLocal)!,
      })
      dispatch(
        onTimeQueryChanged({
          eventTime: eventTime!.toDate(),
          timeBefore: getNumber(timeBefore),
          timeAfter: getNumber(timeAfter),
          timeWindowSeconds: getNumber(timeWindowSecondsLocal),
        })
      )
    } else {
      setShouldReRenderTimeWindowSeconds(true)
    }
  }, [timeWindowSeconds])

  useEffect(() => {
    if (shouldReRenderBsmTrail && getNumber(bsmTrailLengthLocal) !== null) {
      dispatch(setBsmTrailLength(getNumber(bsmTrailLengthLocal)!))
    } else {
      setShouldReRenderBsmTrail(true)
    }
  }, [bsmTrailLengthLocal])

  const isFormValid = () => {
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

  const getNumber = (value: string | undefined): number | undefined => {
    if (value == null) return undefined
    const num = parseInt(value)
    if (isNaN(num)) {
      return undefined
    }
    return num
  }

  const timelineTicks = [120, 240, 360, 480, 600, 720, 840, 960, 1080, 1200, 1320]

  const formatMinutesAfterMidnightTime = useMemo(() => {
    return (minutes) => {
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`
    }
  }, [])

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

  const TimelineCursor: React.FC<TimelineCursorProps> = ({ x = 0, y = 0, width = 0, height = 0 }) => {
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
        <text x={0} y={0} dy={10} textAnchor="middle">
          {timeString}
        </text>
      </g>
    )
  }

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
        padding: '10px 10px 10px 10px',
      }}
    >
      <Accordion disableGutters>
        <AccordionSummary>
          <Typography variant="h5">
            Time Query
            {liveDataActive && <Chip label="Live Data Active" className="blink_me" sx={{ ml: 1 }} color="success" />}
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ mt: 1 }}>
            <Box sx={{ mt: 1 }}>
              <FormControl sx={{ mt: 1, minWidth: 200 }}>
                <InputLabel>Intersection ID</InputLabel>
                <Select
                  value={selectedIntersectionId}
                  onChange={(e) => {
                    dispatch(setSelectedIntersection(e.target.value as number))
                  }}
                >
                  {/* TODO: Update to display intersection Name */}
                  {intersectionsList.map((intersection) => (
                    <MenuItem value={intersection.intersectionID}>{intersection.intersectionID}</MenuItem>
                  ))}
                </Select>
              </FormControl>

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
              <div style={{ marginTop: '9px', display: 'inline-flex' }}>
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
              </div>
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
              <TextField
                label="Time Render Window"
                name="timeRangeAfter"
                type="number"
                sx={{ mt: 1 }}
                onChange={(e) => {
                  if (e.target.value === '' || Number.isInteger(Number(e.target.value))) {
                    setTimeWindowSeconds(e.target.value)
                  }
                }}
                slotProps={{ input: { endAdornment: <InputAdornment position="end">seconds</InputAdornment> } }}
                value={timeWindowSeconds}
              />
            </Box>
            <Chip
              label={liveDataActive ? 'Stop Live Data' : 'Render Live Data'}
              sx={{ mt: 1 }}
              onClick={() => {
                dispatch(toggleLiveDataActive())
              }}
              color={liveDataActive ? 'success' : 'default'}
              variant={liveDataActive ? undefined : 'outlined'}
            />
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion disableGutters defaultExpanded={true}>
        <AccordionSummary>
          <Typography variant="h5">Message Times & Download</Typography>
        </AccordionSummary>
        <AccordionDetails>
          <div
            className="control-panel"
            style={{
              padding: '10px 30px 0px 20px',
            }}
          >
            <h4>
              Visualization Time: {format(sliderTimeValue.start, 'MM/dd/yyyy HH:mm:ss')} -{' '}
              {format(sliderTimeValue.end, 'MM/dd/yyyy HH:mm:ss')}
            </h4>
            <h4>
              MAP Message Time:{' '}
              {mapSpatTimes.mapTime === 0 ? 'No Data' : format(mapSpatTimes.mapTime * 1000, 'MM/dd/yyyy HH:mm:ss')}
            </h4>

            <h4>
              SPAT Message Time:{' '}
              {mapSpatTimes.spatTime === 0 ? 'No Data' : format(mapSpatTimes.spatTime * 1000, 'MM/dd/yyyy HH:mm:ss')}
            </h4>
            <h4>Activity Chart for {format(sliderTimeValue.start, 'MM/dd/yyyy')}:</h4>

            <ResponsiveContainer width="100%" height={80}>
              <BarChart
                data={bsmEventsByMinute}
                barGap={0}
                barCategoryGap={0}
                onClick={(data) => {
                  if (data !== null && data.activePayload !== undefined && data.activePayload !== null) {
                    setEventTime(dayjs(data.activePayload[0].payload.minute))
                  }
                }}
              >
                <XAxis
                  dataKey="minutesAfterMidnight"
                  type="number"
                  domain={[0, 1440]}
                  tick={<TimelineAxisTick />}
                  ticks={timelineTicks}
                />
                <Bar dataKey="count" fill="#D14343" barSize={10} minPointSize={10}></Bar>
                <Tooltip
                  cursor={<TimelineCursor />}
                  content={({ active, payload, label }) => (
                    <TimelineTooltip active={active} payload={payload} label={label} />
                  )}
                />
              </BarChart>
            </ResponsiveContainer>
            <Button sx={{ m: 1 }} variant="contained" onClick={() => dispatch(downloadMapData())}>
              Download All Message Data
            </Button>
            <h4>
              Upload Message Data:{' '}
              <label htmlFor="upload">
                <input
                  accept=".zip"
                  id="upload"
                  name="upload"
                  type="file"
                  multiple={false}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => {
                    openMessageData(e.target.files)
                  }}
                />
              </label>
            </h4>
          </div>
        </AccordionDetails>
      </Accordion>

      <Accordion disableGutters defaultExpanded={false}>
        <AccordionSummary>
          <Typography variant="h5">Visual Settings</Typography>
        </AccordionSummary>
        <AccordionDetails sx={{ overflowY: 'auto' }}>
          <div
            className="control-panel"
            style={{
              padding: '10px 30px 0px 20px',
            }}
          >
            <div>
              <h4 style={{ float: 'left', marginTop: '10px' }}>Rotate Signal Head Icons With Map </h4>
              <Checkbox
                checked={signalStateLayerStyle?.layout?.['icon-rotation-alignment'] === 'map'}
                onChange={(event) =>
                  dispatch(
                    setSignalLayerLayout({
                      ...signalStateLayerStyle.layout,
                      'icon-rotation-alignment': event.target.checked ? 'map' : 'viewport',
                      'icon-rotate': event.target.checked ? ['get', 'orientation'] : 0,
                    })
                  )
                }
              />
            </div>
            <div>
              <h4 style={{ float: 'left', marginTop: '10px' }}>Show Lane IDs </h4>
              <Checkbox
                checked={laneLabelsVisible}
                onChange={(event) => dispatch(setLaneLabelsVisible(event.target.checked))}
              />
            </div>
            <div>
              <h4 style={{ float: 'left', marginTop: '10px' }}>Show Signal Group IDs </h4>
              <Checkbox
                checked={sigGroupLabelsVisible}
                onChange={(event) => dispatch(setSigGroupLabelsVisible(event.target.checked))}
              />
            </div>
            <div>
              <h4 style={{ float: 'left', marginTop: '10px' }}>Show Popup on Hover </h4>
              <Checkbox
                checked={showPopupOnHover}
                onChange={(event) => dispatch(setShowPopupOnHover(event.target.checked))}
              />
            </div>
            <div>
              <TextField
                label="BSM Trail length"
                name="bsmTrailLength"
                type="number"
                sx={{ mt: 1 }}
                onChange={(e) => {
                  setBsmTrailLengthLocal(e.target.value)
                }}
                value={bsmTrailLengthLocal}
              />
            </div>
          </div>
        </AccordionDetails>
      </Accordion>
      <div style={{ display: 'flex', alignItems: 'center', marginTop: '1rem' }}>
        <button
          style={{ marginLeft: '1rem', border: 'none', background: 'none' }}
          onClick={() => dispatch(togglePlaybackModeActive())}
        >
          {playbackModeActive ? <img src={pauseIcon} alt="Pause" /> : <img src={playIcon} alt="Play" />}
        </button>
        <Slider
          sx={{ ml: 2, width: 'calc(100% - 80px)' }}
          value={sliderValue}
          onChange={(event: Event, value: number | number[], activeThumb: number) => dispatch(setSliderValue(value))}
          min={0}
          max={getTimeRange(queryParams.startDate, queryParams.endDate)}
          valueLabelDisplay="auto"
          disableSwap
        />
      </div>
    </div>
  )
}

export default ControlPanel

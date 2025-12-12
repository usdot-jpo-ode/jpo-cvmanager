import React, { useState, useEffect, useMemo } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  Typography,
  TextField,
  IconButton,
  Button,
  MenuItem,
  Select,
  SelectChangeEvent,
  FormControl,
  InputLabel,
} from '@mui/material'
import {
  ScatterChart,
  Scatter,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceArea,
} from 'recharts'
import { Close } from '@mui/icons-material'
import RsuApi, { type RsuState } from '../../apis/intersections/rsu-api'

type RsuStatusDialogProps = {
  open: boolean
  onClose: () => void
  rsuIp: string | null
  token: string
}

const RsuStatusDialog: React.FC<RsuStatusDialogProps> = ({ open, onClose, rsuIp, token }) => {
  const [latestRsuState, setLatestRsuState] = useState<RsuState | null>(null)
  const [historicalData, setHistoricalData] = useState<RsuState[] | null>(null)
  const [startDate, setStartDate] = useState<string>(new Date().toISOString().split('T')[0])
  const [endDate, setEndDate] = useState<string>(new Date().toISOString().split('T')[0])
  const [showCharts, setShowCharts] = useState(false)
  const [intervalMinutes, setIntervalMinutes] = useState<number>(5)
  const [chartStartDate, setChartStartDate] = useState<string>(startDate)
  const [chartEndDate, setChartEndDate] = useState<string>(endDate)
  // Track whether the generate button has been pressed
  const [hasGenerated, setHasGenerated] = useState(false)

  useEffect(() => {
    if (open && rsuIp) {
      // Query latest RSU status
      RsuApi.getLatestRsuStatus({ token, rsuIp })
        .then((data) => setLatestRsuState(data ?? null))
        .catch(() => setLatestRsuState(null))
    }
  }, [open, rsuIp, token])

  useEffect(() => {
    if (!open) {
      setShowCharts(false)
      setHasGenerated(false)
    }
  }, [open])

  const handleStartDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setStartDate(event.target.value)
  }

  const handleEndDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setEndDate(event.target.value)
  }

  const handleIntervalChange = (event: SelectChangeEvent<number>) => {
    setIntervalMinutes(Number(event.target.value))
  }

  const handleGenerateCharts = () => {
    setHasGenerated(true) // Mark that the generate button has been pressed
    const [earlierDate, laterDate] =
      new Date(startDate) <= new Date(endDate) ? [startDate, endDate] : [endDate, startDate]

    const start = new Date(earlierDate).setUTCHours(0, 0, 0, 0)
    const end = new Date(laterDate).setUTCHours(23, 59, 59, 999)

    RsuApi.getAggregatedRsuStatus({
      token,
      rsuIp,
      startTime: new Date(start),
      endTime: new Date(end),
      intervalMinutes,
    })
      .then((data) => {
        setHistoricalData(data ?? null)
        setChartStartDate(earlierDate)
        setChartEndDate(laterDate)
        setShowCharts(true)
      })
      .catch(() => {
        setHistoricalData(null)
        setChartStartDate(earlierDate)
        setChartEndDate(laterDate)
        setShowCharts(true)
      })
  }

  const temperatureData =
    historicalData?.map((data) => ({
      time: data.timestamp,
      value: parseFloat((data.temperature * (9 / 5) + 32).toFixed(1)),
    })) || []

  const uptimeData =
    historicalData?.map((data) => ({
      time: data.timestamp,
      value: data.uptime,
    })) || []

  // Ensure data arrays are non-empty before calculating domains and offsets
  const temperatureDomain = useMemo(() => {
    if (temperatureData.length === 0) {
      return [0, 0] // Default domain when no data is available
    }
    return [
      Math.floor(Math.min(...temperatureData.map((d) => d.value)) / 5) * 5,
      Math.ceil(Math.max(...temperatureData.map((d) => d.value)) / 5) * 5,
    ]
  }, [temperatureData])

  const uptimeDomain = useMemo(() => {
    if (uptimeData.length === 0) {
      return [0, 0] // Default domain when no data is available
    }
    return [
      Math.round(Math.min(...uptimeData.map((d) => d.value)) - 1),
      Math.round(Math.max(...uptimeData.map((d) => d.value)) + 1),
    ]
  }, [uptimeData])

  const timeDomain = useMemo(() => {
    if (temperatureData.length === 0) {
      return [0, 0] // Default domain when no data is available
    }
    return [Math.min(...temperatureData.map((d) => d.time)), Math.max(...temperatureData.map((d) => d.time))]
  }, [temperatureData])

  // Updated formatTimestamp to include hours and minutes for X-axis
  const formatTimestamp = (unixTime: number) => {
    const date = new Date(unixTime)
    return `${date.getUTCMonth() + 1}/${date.getUTCDate()} ${date.getUTCHours()}:${date
      .getUTCMinutes()
      .toString()
      .padStart(2, '0')}` // Format as MM/DD HH:mm in UTC
  }

  // Define color gradients for charts
  const redTempTarget = 140
  const yellowTempTarget = 120
  const greenTempTarget = 100

  // Offset for red gradient stop based on temperature domain
  const redOffset = 1 - (redTempTarget - temperatureDomain[0]) / (temperatureDomain[1] - temperatureDomain[0])
  // Offset for yellow gradient stop based on temperature domain
  const yellowOffset = 1 - (yellowTempTarget - temperatureDomain[0]) / (temperatureDomain[1] - temperatureDomain[0])
  // Offset for green gradient stop based on temperature domain
  const greenOffset = 1 - (greenTempTarget - temperatureDomain[0]) / (temperatureDomain[1] - temperatureDomain[0])

  const yellowUptimeTarget = 7776000 // 90 days in seconds
  const greenUptimeTarget = yellowUptimeTarget * 0.9 // 90% of yellow target

  // Offset for green uptime gradient stop
  const greenUptimeOffset = 1 - (greenUptimeTarget - uptimeDomain[0]) / (uptimeDomain[1] - uptimeDomain[0])
  // Offset for yellow uptime gradient stop
  const yellowUptimeOffset = 1 - (yellowUptimeTarget - uptimeDomain[0]) / (uptimeDomain[1] - uptimeDomain[0])

  // Identify reboot points (local minima after a decrease)
  const rebootPoints = uptimeData.reduce((acc, curr, index, arr) => {
    if (index > 0 && arr[index - 1].value > curr.value) {
      acc.push(curr.time) // Identify local minima after a decrease as reboot points
    }
    return acc
  }, [])

  // Utility to get days, hours, minutes from a duration in milliseconds
  function splitDurationIntoParts(durationMs: number) {
    const durationSec = Math.floor(durationMs / 1000)
    const days = Math.floor(durationSec / 86400)
    const hours = Math.floor((durationSec % 86400) / 3600)
    const minutes = Math.floor((durationSec % 3600) / 60)
    return { days, hours, minutes }
  }

  // Calculate reboot zones (from local max to subsequent local min)
  const rebootZones = uptimeData.reduce((zones, curr, index, arr) => {
    if (index > 0 && arr[index - 1].value > curr.value) {
      const start = arr[index - 1].time
      const end = curr.time - curr.value * 1000 // Subtract uptime (in milliseconds) from the current timestamp
      const durationMs = end - start

      const { days, hours, minutes } = splitDurationIntoParts(durationMs)
      let durationLabel = ''
      if (days > 0) {
        durationLabel = `${days} day${days > 1 ? 's' : ''}`
      } else if (hours > 0) {
        durationLabel = `${hours} hr ${minutes} min`
      } else {
        durationLabel = `${minutes} min`
      }

      zones.push({
        start,
        end,
        label: `Offline ${durationLabel}`,
      })
    }
    return zones
  }, [])

  // Formats uptime in days and hours
  const formatUptime = (seconds: number, forTooltip = false) => {
    const days = Math.floor(seconds / 86400)
    const hours = Math.floor((seconds % 86400) / 3600)

    if (days > 0 && hours > 0) {
      return forTooltip ? `${days} days, ${hours} ${hours === 1 ? 'hour' : 'hours'}` : `${days}d ${hours}hr`
    }

    if (days > 0 && hours === 0) {
      return forTooltip ? `${days} days` : `${days}d`
    }

    if (days === 0 && hours > 0) {
      return forTooltip ? `${hours} ${hours === 1 ? 'hour' : 'hours'}` : `${hours}hr`
    }

    return forTooltip ? `0 hours` : `0hr`
  }

  // Updated formatTooltip to include hours and minutes for tooltips
  const formatTooltip = (value: number, name: string, props: any) => {
    if (name === 'Time') {
      const date = new Date(value)
      return [
        `${date.getUTCMonth() + 1}/${date.getUTCDate()}/${date.getUTCFullYear()} ${date.getUTCHours()}:${date
          .getUTCMinutes()
          .toString()
          .padStart(2, '0')}`, // Format as MM/DD/YYYY HH:mm in UTC
        name,
      ]
    }
    if (name === 'Temperature') {
      return [`${value}°F`, 'Temperature']
    }
    if (name === 'Uptime') {
      return [formatUptime(value, true), 'Uptime']
    }
    return [value, name]
  }

  // Clear and recalculate reboot points and zones whenever the historical data changes
  useEffect(() => {
    // Clear reboot points and zones when historical data changes
    rebootPoints.splice(0, rebootPoints.length)
    rebootZones.splice(0, rebootZones.length)

    // Recalculate reboot points
    uptimeData.reduce((acc, curr, index, arr) => {
      if (index > 0 && arr[index - 1].value > curr.value) {
        acc.push(curr.time)
      }
      return acc
    }, rebootPoints)

    // Recalculate reboot zones
    uptimeData.reduce((zones, curr, index, arr) => {
      if (index > 0 && arr[index - 1].value > curr.value) {
        const start = arr[index - 1].time
        const end = curr.time - curr.value * 1000 // Subtract uptime (in milliseconds) from the current timestamp
        const durationMs = end - start
        const { days, hours, minutes } = splitDurationIntoParts(durationMs)
        let durationLabel = ''
        if (days > 0) {
          durationLabel = `${days} day${days > 1 ? 's' : ''}`
        } else if (hours > 0) {
          durationLabel = `${hours} hr ${minutes} min`
        } else {
          durationLabel = `${minutes} min`
        }

        zones.push({
          start,
          end,
          label: `Offline ${durationLabel}`,
        })
      }
      return zones
    }, rebootZones)
  }, [historicalData])

  // Calculate whether to position the box in the top right corner
  const isEarlyReboot = rebootPoints.some((time) => time < timeDomain[0] + (timeDomain[1] - timeDomain[0]) * 0.4)

  // Set the warning box position dynamically to one of the top corners of the chart
  const warningBoxWidth = 160 // Width of the warning box in pixels
  const warningBoxHeight = 80 // Height of the warning box in pixels
  const warningBoxX = isEarlyReboot ? `calc(98% - ${warningBoxWidth}px)` : 100
  const warningBoxY = 20 // Keep the Y position constant

  // Adjust the position to align the top-right corner of the box

  // Calculate ticks at 6-hour intervals and include start and end of the time domain
  const sixHoursInMs = 6 * 60 * 60 * 1000 // 6 hours in milliseconds
  const startOfFirstInterval = Math.ceil(timeDomain[0] / sixHoursInMs) * sixHoursInMs // Align to the next 6-hour mark
  const xAxisTicks = []

  // Add 6-hour interval ticks
  for (let tick = startOfFirstInterval; tick <= timeDomain[1]; tick += sixHoursInMs) {
    xAxisTicks.push(tick)
  }

  // Ensure start and end of the time domain are included
  if (!xAxisTicks.includes(timeDomain[0])) {
    xAxisTicks.unshift(timeDomain[0])
  }
  if (!xAxisTicks.includes(timeDomain[1])) {
    xAxisTicks.push(timeDomain[1])
  }

  // Sort ticks to maintain order
  xAxisTicks.sort((a, b) => a - b)

  const formatChartTitle = (startDate: string, endDate: string, label: string) => {
    const [start, end] = new Date(startDate) <= new Date(endDate) ? [startDate, endDate] : [endDate, startDate]
    if (start === end) {
      return `${label} for ${start.split('-')[1]}/${start.split('-')[2]}/${start.split('-')[0]}`
    }
    return `${label} for ${start.split('-')[1]}/${start.split('-')[2]}/${start.split('-')[0]} - ${end.split('-')[1]}/${
      end.split('-')[2]
    }/${end.split('-')[0]}`
  }

  // Render the dialog with controls and conditional "no data" message
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xl" fullWidth>
      <DialogTitle>
        {`Status of RSU at ${rsuIp || 'unknown IP'}`}
        <IconButton onClick={onClose} style={{ position: 'absolute', right: 8, top: 8 }}>
          <Close />
        </IconButton>
      </DialogTitle>
      <DialogContent>
        <Typography variant="subtitle2">
          {latestRsuState
            ? `Last status update: ${new Date(latestRsuState.timestamp).toUTCString()} (${formatUptime(
                Math.floor((Date.now() - latestRsuState.timestamp) / 1000),
                true
              )} ago)`
            : 'No RSU status data.'}
        </Typography>
        <Typography variant="subtitle2">
          {latestRsuState
            ? `Last reboot: ${new Date(
                latestRsuState.timestamp - latestRsuState.uptime * 1000
              ).toUTCString()} (${formatUptime(
                Math.floor((Date.now() - (latestRsuState.timestamp - latestRsuState.uptime * 1000)) / 1000),
                true
              )} ago)`
            : 'No RSU reboot data.'}
        </Typography>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', margin: '24px 0 16px' }}>
          <div style={{ display: 'flex', gap: '0px', margin: '0px 0' }}>
            <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
              <Typography variant="subtitle2">Start Date</Typography>
              <TextField
                type="date"
                value={startDate}
                onChange={handleStartDateChange}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
              <Typography variant="subtitle2">End Date</Typography>
              <TextField
                type="date"
                value={endDate}
                onChange={handleEndDateChange}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', margin: '10px 0' }}>
            <div style={{ display: 'flex', flexDirection: 'column', maxWidth: '150px' }}>
              <Typography variant="subtitle2">Interval</Typography>
              <FormControl>
                <Select value={intervalMinutes} onChange={handleIntervalChange}>
                  <MenuItem value={5}>5 minutes</MenuItem>
                  <MenuItem value={10}>10 minutes</MenuItem>
                  <MenuItem value={15}>15 minutes</MenuItem>
                  <MenuItem value={30}>30 minutes</MenuItem>
                  <MenuItem value={60}>1 hour</MenuItem>
                  <MenuItem value={240}>4 hours</MenuItem>
                  <MenuItem value={360}>6 hours</MenuItem>
                  <MenuItem value={720}>12 hours</MenuItem>
                  <MenuItem value={1440}>24 hours</MenuItem>
                </Select>
              </FormControl>
            </div>
            <Button
              variant="contained"
              color="primary"
              onClick={handleGenerateCharts}
              style={{ padding: '10px', fontSize: '16px' }}
            >
              Generate
            </Button>
          </div>
        </div>
        {hasGenerated && (temperatureData.length === 0 || uptimeData.length === 0) ? (
          <Typography variant="subtitle2">No data available for the selected time range.</Typography>
        ) : (
          showCharts && (
            <>
              <Typography variant="h6">{formatChartTitle(chartStartDate, chartEndDate, 'Temperature')}</Typography>
              <ResponsiveContainer width="100%" height={300}>
                <ScatterChart margin={{ top: 10, right: 10, bottom: 20, left: 10 }}>
                  <defs>
                    <linearGradient id="temperatureGradient" x1="0" y1="0" x2="0" y2="1">
                      {yellowTempTarget < temperatureDomain[1] && <stop offset={`${redOffset}`} stopColor="#ca8282" />}
                      <stop offset={`${yellowOffset}`} stopColor="#f0e68c" />
                      {yellowTempTarget > temperatureDomain[0] && (
                        <stop offset={`${greenOffset}`} stopColor="#82ca9d" />
                      )}
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="time"
                    domain={timeDomain}
                    name="Time"
                    tickFormatter={formatTimestamp}
                    type="number"
                    label={{ value: 'Time (UTC)', position: 'insideBottom', offset: -5 }}
                    ticks={xAxisTicks} // Use calculated ticks including start and end
                  />
                  <YAxis
                    dataKey="value"
                    domain={temperatureDomain}
                    name="Temperature"
                    label={{ value: 'Temperature (°F)', angle: -90, position: 'center', dx: -20 }}
                    ticks={Array.from(
                      { length: (temperatureDomain[1] - temperatureDomain[0]) / 5 + 1 },
                      (_, i) => temperatureDomain[0] + i * 5
                    )}
                  />
                  <Tooltip isAnimationActive={false} formatter={formatTooltip} />
                  <Scatter
                    data={temperatureData}
                    line={{ stroke: 'url(#temperatureGradient)', strokeWidth: 3 }}
                    fill="rgba(0, 0, 0, 0)"
                    lineJointType="monotoneX"
                  />
                </ScatterChart>
              </ResponsiveContainer>

              <Typography variant="h6">{formatChartTitle(chartStartDate, chartEndDate, 'Uptime')}</Typography>
              <ResponsiveContainer width="100%" height={300}>
                <ScatterChart margin={{ top: 10, right: 10, bottom: 20, left: 10 }}>
                  <defs>
                    <linearGradient id="uptimeGradient" x1="0" y1="0" x2="0" y2="1">
                      {yellowUptimeTarget <= uptimeDomain[1] && (
                        <stop offset={`${yellowUptimeOffset}`} stopColor="#f0e68c" />
                      )}
                      {yellowUptimeTarget >= uptimeDomain[0] && (
                        <stop offset={`${greenUptimeOffset}`} stopColor="#82ca9d" />
                      )}
                      <stop offset={`${greenUptimeOffset}`} stopColor="#82ca9d" />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="time"
                    domain={timeDomain}
                    name="Time"
                    tickFormatter={formatTimestamp}
                    type="number"
                    label={{ value: 'Time (UTC)', position: 'insideBottom', offset: -5 }}
                    ticks={xAxisTicks} // Use calculated ticks including start and end
                  />
                  <YAxis
                    dataKey="value"
                    domain={uptimeDomain}
                    name="Uptime"
                    label={{ value: 'Uptime', angle: -90, position: 'center', dx: -40 }}
                    tickFormatter={(value) => formatUptime(value)}
                    ticks={Array.from(
                      { length: 3 },
                      (_, i) => uptimeDomain[0] + (i * (uptimeDomain[1] - uptimeDomain[0])) / 2
                    )}
                    width={85}
                  />
                  <Tooltip isAnimationActive={false} formatter={formatTooltip} />
                  {/* Render shaded areas for reboot zones with labels starting at the right end */}
                  {rebootZones.map((zone, index) => (
                    <ReferenceArea
                      key={index}
                      x1={zone.start}
                      x2={zone.end}
                      stroke="red"
                      strokeOpacity={0.3}
                      fill="red"
                      fillOpacity={0.1}
                      label={{
                        value: zone.label,
                        position: 'insideRight',
                        fill: 'red',
                        fontSize: 14,
                        angle: 90,
                        dy: 0,
                        dx: 20,
                      }}
                    />
                  ))}
                  <Scatter
                    data={uptimeData}
                    line={{ stroke: 'url(#uptimeGradient)', strokeWidth: 3 }}
                    fill="rgba(0, 0, 0, 0)"
                    lineJointType="monotoneX"
                  />
                  {/* Conditional textbox for warning */}
                  {yellowUptimeTarget < uptimeDomain[1] && (
                    <foreignObject x={warningBoxX} y={warningBoxY} width={warningBoxWidth} height={warningBoxHeight}>
                      <div
                        style={{
                          border: `2px solid ${
                            window.matchMedia('(prefers-color-scheme: dark)').matches ? 'white' : 'black'
                          }`,
                          backgroundColor: window.matchMedia('(prefers-color-scheme: dark)').matches
                            ? 'black'
                            : 'white',
                          padding: '5px',
                          borderRadius: '5px',
                          fontSize: '14px',
                          fontWeight: 'bold',
                          whiteSpace: 'pre-wrap',
                          color: window.matchMedia('(prefers-color-scheme: dark)').matches ? 'white' : 'black',
                        }}
                      >
                        Yellow indicates more
                        <br />
                        than 90 days since
                        <br />
                        previous reboot.
                      </div>
                    </foreignObject>
                  )}
                </ScatterChart>
              </ResponsiveContainer>
            </>
          )
        )}
      </DialogContent>
    </Dialog>
  )
}

export default RsuStatusDialog

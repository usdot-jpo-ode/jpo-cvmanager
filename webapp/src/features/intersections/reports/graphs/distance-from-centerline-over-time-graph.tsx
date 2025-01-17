import React from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ReferenceLine,
  ReferenceArea,
  TooltipProps,
} from 'recharts'
import { Box, Typography } from '@mui/material'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'
import reportColorPalette from '../report-color-palette'
import { LaneDirectionOfTravelReportData } from '../report-utils'

interface DistanceFromCenterlineOverTimeGraphProps {
  data: LaneDirectionOfTravelReportData[]
  laneNumber: string
  distanceTolerance: number // New prop
}

const CustomTooltip = ({ active, payload, label }: TooltipProps<ValueType, NameType>) => {
  if (active && payload && payload.length) {
    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">{label}</Typography>
        {payload.map((entry, index) => (
          <Typography key={`item-${index}`} variant="body2">{`${entry.name}: ${Number(entry.value).toFixed(
            2
          )} cm`}</Typography>
        ))}
      </Box>
    )
  }

  return null
}

const DistanceFromCenterlineOverTimeGraph: React.FC<DistanceFromCenterlineOverTimeGraphProps> = ({
  data,
  laneNumber,
  distanceTolerance,
}) => {
  // Sort data by timestamp in ascending order
  const sortedData = data.sort((a, b) => a.timestamp - b.timestamp)

  // Extract unique segment IDs for the lines and sort them numerically
  const segmentIDs = Array.from(new Set(data.map((item) => item.segmentID))).sort((a, b) => a - b)

  // Aggregate data by minute
  const aggregatedData = sortedData.reduce((acc, item) => {
    const minute = new Date(item.timestamp).setSeconds(0, 0)
    if (!acc[minute]) {
      acc[minute] = {}
    }
    if (!acc[minute][`Segment ${item.segmentID}`]) {
      acc[minute][`Segment ${item.segmentID}`] = []
    }
    acc[minute][`Segment ${item.segmentID}`].push(item.medianCenterlineDistance)
    return acc
  }, {} as { [minute: number]: { [segment: string]: number[] } })

  // Process aggregated data to calculate the average for each segment per minute
  const processedData = Object.keys(aggregatedData).map((minute) => {
    const date = new Date(Number(minute))
    const formattedDate = `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date
      .getMinutes()
      .toString()
      .padStart(2, '0')}`
    const segments = Object.keys(aggregatedData[minute]).reduce(
      (acc, segment) => {
        const values = aggregatedData[minute][segment]
        const average = values.reduce((sum, value) => sum + value, 0) / values.length
        acc[segment] = average
        return acc
      },
      { name: formattedDate }
    )
    return segments
  })

  const uniqueDates = Array.from(new Set(processedData.map((d) => d.name.split(' ')[0])))
  const tickFormatter = (tick: string) => {
    const date = new Date(Date.parse(tick))
    if (uniqueDates.length <= 5) {
      return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date
        .getDate()
        .toString()
        .padStart(2, '0')} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
    } else {
      return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
  }

  const lines = segmentIDs.map((segmentID, index) => ({
    dataKey: `Segment ${segmentID}`,
    stroke: reportColorPalette[(index * 3 + 2) % reportColorPalette.length],
    strokeWidth: 2,
    name: `Segment ${segmentID}`,
  }))

  const allValues = processedData.flatMap((d) =>
    Object.values(d)
      .filter((value) => typeof value === 'number')
      .map(Number)
  )
  const minValue = Math.min(...allValues)
  const maxValue = Math.max(...allValues)
  const domainMin = Math.floor(minValue / 5) * 5
  const domainMax = Math.ceil(maxValue / 5) * 5

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
      <Box>
        <Typography
          variant="h6"
          align="center"
          sx={{ mt: 2 }}
        >{`Distance From Lane ${laneNumber} Centerline Over Time`}</Typography>
        <LineChart
          width={750}
          height={450}
          data={processedData}
          margin={{
            top: 20,
            right: 30,
            left: 20,
            bottom: 70,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="name"
            label={{ value: 'Time', position: 'insideBottom', offset: -60 }}
            tickFormatter={tickFormatter}
            angle={-45}
            textAnchor="end"
          />
          <YAxis
            label={{ value: 'Distance from Centerline (cm)', angle: -90, position: 'insideLeft', dy: 80 }}
            domain={[domainMin, domainMax]} // Extend to nearest multiple of 5
          />
          <Tooltip content={<CustomTooltip />} />
          <Legend verticalAlign="top" height={36} />
          {lines.map((line, index) => (
            <Line
              key={index}
              type="monotone"
              dataKey={line.dataKey}
              stroke={line.stroke}
              name={line.name}
              connectNulls
              dot={false}
              isAnimationActive={false}
              strokeWidth={3}
            />
          ))}
          {distanceTolerance !== domainMax && (
            <>
              <ReferenceLine
                y={distanceTolerance}
                stroke={reportColorPalette[0]}
                strokeDasharray="3 3"
                label={{ value: `Tolerance: ${distanceTolerance} cm`, position: 'top', offset: 5 }}
              />
              <ReferenceArea y1={distanceTolerance} y2={domainMax} fill={reportColorPalette[3]} fillOpacity={0.1} />
            </>
          )}
          {distanceTolerance < minValue && (
            <ReferenceArea y1={domainMin} y2={domainMax} fill={reportColorPalette[3]} fillOpacity={0.1} />
          )}
        </LineChart>
      </Box>
    </Box>
  )
}

export default DistanceFromCenterlineOverTimeGraph

import React from 'react'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  TooltipProps,
  ReferenceLine,
  ReferenceArea,
} from 'recharts'
import { Box, Typography } from '@mui/material'
import { ValueType, NameType } from 'recharts/types/component/DefaultTooltipContent'
import reportColorPalette from '../report-color-palette'
import { formatAxisTickNumber } from '../report-utils'

interface LaneDirectionDistanceGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
  distanceTolerance: number
}

const CustomTooltip = ({ active, payload, label }: TooltipProps<ValueType, NameType>) => {
  if (active && payload && payload.length) {
    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">{label}</Typography>
        <Typography variant="body2">events: {payload[0].value}</Typography>
      </Box>
    )
  }

  return null
}

// Convert cm to feet
const cmToFeet = (cm: number) => cm / 30.48

// Calculate the mean of the data
const calculateMean = (data: { name: number; value: number }[]) => {
  const total = data.reduce((sum, item) => sum + item.name * item.value, 0)
  const count = data.reduce((sum, item) => sum + item.value, 0)
  return total / count
}

// Calculate the median of the data
const calculateMedian = (data: { name: number; value: number }[]) => {
  const totalCount = data.reduce((sum, item) => sum + item.value, 0)
  const halfCount = totalCount / 2
  let cumulativeCount = 0

  for (const item of data) {
    cumulativeCount += item.value
    if (cumulativeCount >= halfCount) {
      return item.name
    }
  }
  return 0 // Default return value if something goes wrong
}

// Calculate the percentage of events outside the tolerance
const calculatePercentageOutsideTolerance = (data: { name: number; value: number }[], tolerance: number) => {
  const totalEvents = data.reduce((sum, item) => sum + item.value, 0)
  const outsideToleranceEvents = data.reduce((sum, item) => {
    if (Math.abs(item.name) > tolerance) {
      return sum + item.value
    }
    return sum
  }, 0)
  return (outsideToleranceEvents / totalEvents) * 100
}

const LaneDirectionDistanceGraph: React.FC<LaneDirectionDistanceGraphProps> = ({
  data,
  getInterval,
  distanceTolerance,
}) => {
  // Convert the name property to a number
  const numericData = data.map((d) => ({ ...d, name: Number(d.name) }))
  const minX = Math.min(...numericData.map((d) => d.name))
  const maxX = Math.max(...numericData.map((d) => d.name))

  // Calculate the mean, median, min, and max
  const mean = calculateMean(numericData).toFixed(2)
  const median = Math.round(calculateMedian(numericData))
  const min = Math.round(Math.min(...numericData.map((d) => d.name)))
  const max = Math.round(Math.max(...numericData.map((d) => d.name)))
  const percentageOutsideTolerance = calculatePercentageOutsideTolerance(numericData, cmToFeet(distanceTolerance))

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
      <Box>
        <Typography variant="h6" align="center" sx={{ mt: 2 }}>
          Median Distance Deviation from Lane Centerline
        </Typography>
        <BarChart
          width={750}
          height={450}
          data={numericData}
          margin={{
            top: 20,
            right: 30,
            left: 20,
            bottom: 20,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="name"
            label={{ value: 'Distance (ft)', position: 'insideBottom', offset: -10 }}
            scale="linear"
            type="number"
            domain={[minX - 0.5, maxX + 0.5]}
            interval={getInterval(data.length)}
          />
          <YAxis
            label={{ value: 'Event Count', angle: -90, position: 'insideLeft', offset: 0 }}
            tickFormatter={formatAxisTickNumber}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="value" fill={reportColorPalette.cyan} />
          <ReferenceLine x={mean} stroke={reportColorPalette.blueGrey} />
          <ReferenceLine x={median} stroke={reportColorPalette.green} />
          <ReferenceLine x={cmToFeet(distanceTolerance)} stroke={reportColorPalette.pink} strokeDasharray="3 3" />
          <ReferenceLine x={-cmToFeet(distanceTolerance)} stroke={reportColorPalette.pink} strokeDasharray="3 3" />
          <ReferenceArea
            x1={cmToFeet(distanceTolerance)}
            x2={maxX + 0.5}
            fill={reportColorPalette.grey}
            fillOpacity={0.1}
          />
          <ReferenceArea
            x1={minX - 0.5}
            x2={-cmToFeet(distanceTolerance)}
            fill={reportColorPalette.grey}
            fillOpacity={0.1}
          />
        </BarChart>
        {data.length > 0 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1 }}>
            <Typography variant="body2" sx={{ color: reportColorPalette.grey, mx: 1 }}>
              Mean: {mean} ft
            </Typography>
            <Typography variant="body2" sx={{ color: reportColorPalette.green, mx: 1 }}>
              Median: {median} ft
            </Typography>
            <Typography variant="body2" sx={{ color: reportColorPalette.pink, mx: 1 }}>
              Tolerance: {cmToFeet(distanceTolerance).toFixed(2)} ft
            </Typography>
            <Typography variant="body2" sx={{ mx: 1 }}>
              Out-of-Tolerance Events: {percentageOutsideTolerance.toFixed(2)}%
            </Typography>
            <Typography variant="body2" sx={{ mx: 1 }}>
              Min: {min} ft
            </Typography>
            <Typography variant="body2" sx={{ mx: 1 }}>
              Max: {max} ft
            </Typography>
          </Box>
        )}
      </Box>
    </Box>
  )
}

export default LaneDirectionDistanceGraph

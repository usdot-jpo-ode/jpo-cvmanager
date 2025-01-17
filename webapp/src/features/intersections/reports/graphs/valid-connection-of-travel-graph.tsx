import React from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, TooltipProps } from 'recharts'
import { Box, Typography } from '@mui/material'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'
import reportColorPalette from '../report-color-palette'

interface ValidConnectionOfTravelGraphProps {
  data: { connectionID: number; ingressLaneID: number; egressLaneID: number; eventCount: number }[]
}

const CustomTooltip = ({ active, payload }: TooltipProps<ValueType, NameType>) => {
  if (active && payload && payload.length) {
    const obj = payload[0].payload
    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">Connection ID: {obj.connectionID}</Typography>
        <Typography variant="body2">Ingress Lane ID: {obj.ingressLaneID}</Typography>
        <Typography variant="body2">Egress Lane ID: {obj.egressLaneID}</Typography>
        <Typography variant="body2">Event Count: {obj.eventCount}</Typography>
      </Box>
    )
  }
  return null
}

const ValidConnectionOfTravelGraph: React.FC<ValidConnectionOfTravelGraphProps> = ({ data }) => {
  // Sort data by ingress and egress lane IDs
  const sortedData = data.sort((a, b) => {
    if (a.ingressLaneID < b.ingressLaneID) {
      return -1
    }
    if (a.ingressLaneID > b.ingressLaneID) {
      return 1
    }
    if (a.egressLaneID < b.egressLaneID) {
      return -1
    }
    if (a.egressLaneID > b.egressLaneID) {
      return 1
    }
    return 0
  })

  // Calculate the maximum event count to set the ticks
  const maxEventCount = Math.max(...sortedData.map((d) => d.eventCount))
  const ticks = Array.from({ length: maxEventCount + 1 }, (_, i) => i)

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
      <Box>
        <Typography variant="h6" align="center" sx={{ mt: 2 }}>
          Valid Connection of Travel
        </Typography>
        <BarChart
          width={750}
          height={450}
          data={sortedData}
          margin={{
            top: 20,
            right: 30,
            left: 20,
            bottom: 50,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="ingressLaneID"
            interval={0}
            angle={-45}
            height={50}
            textAnchor="end"
            label={{ value: 'Ingress - Egress Lane ID', position: 'center', dy: 40 }}
            tickFormatter={(tick, index) => `${sortedData[index].ingressLaneID} - ${sortedData[index].egressLaneID}`}
          />
          <YAxis
            label={{ value: 'Event Count', angle: -90, position: 'insideLeft' }}
            tickFormatter={(tick) => tick.toString()}
            ticks={ticks}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="eventCount" fill={reportColorPalette[8]} />
        </BarChart>
      </Box>
    </Box>
  )
}

export default ValidConnectionOfTravelGraph

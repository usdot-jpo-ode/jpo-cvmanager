import React from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, TooltipProps, Legend } from 'recharts'
import { Box, Typography } from '@mui/material'
import reportColorPalette from '../report-color-palette'
import { ValueType, NameType } from 'recharts/types/component/DefaultTooltipContent'

interface StopLineStackedGraphProps {
  passageData: { name: string; value: number }[]
  stopData: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const CustomTooltip: React.FC<TooltipProps<ValueType, NameType>> = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const stopped = payload.find((item) => item.dataKey === 'stop')?.value || 0
    const didNotStop = payload.find((item) => item.dataKey === 'noStop')?.value || 0

    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">{label}</Typography>
        <Typography variant="body2">Stopped: {stopped}</Typography>
        <Typography variant="body2">Did Not Stop: {didNotStop}</Typography>
      </Box>
    )
  }

  return null
}

const StopLineStackedGraph: React.FC<StopLineStackedGraphProps> = ({ passageData, stopData, getInterval }) => {
  // Combine the data for the stacked bar chart
  const combinedData = passageData.map((passage, _) => {
    const stop = stopData.find((s) => s.name === passage.name) || { name: passage.name, value: 0 }
    return {
      name: passage.name,
      stop: stop.value,
      noStop: passage.value - stop.value,
    }
  })

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
      <Box>
        <Typography variant="h6" align="center" sx={{ mt: 2 }}>
          Stop Line Events Per Day
        </Typography>
        {combinedData.length === 0 ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
            <Typography variant="h6" align="center" sx={{ mt: 2 }}>
              No Data Available
            </Typography>
          </Box>
        ) : (
          <BarChart
            width={750}
            height={450}
            data={combinedData}
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
              label={{ value: 'Day', position: 'insideBottom', offset: -60, dx: -30 }}
              angle={-45}
              textAnchor="end"
              interval={getInterval(combinedData.length)}
            />
            <YAxis label={{ value: 'Event Count', angle: -90, position: 'insideLeft' }} />
            <Tooltip content={<CustomTooltip />} />
            <Legend wrapperStyle={{ paddingTop: '70px' }} />
            <Bar dataKey="stop" stackId="a" fill={reportColorPalette.pink} name="Stopped" />
            <Bar dataKey="noStop" stackId="a" fill={reportColorPalette.green} name="Did Not Stop" />
          </BarChart>
        )}
      </Box>
    </Box>
  )
}

export default StopLineStackedGraph

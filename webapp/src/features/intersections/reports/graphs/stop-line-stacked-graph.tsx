import React from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts'
import { Box, Typography } from '@mui/material'
import reportColorPalette from '../report-color-palette'

interface StopLineStackedGraphProps {
  passageData: { name: string; value: number }[]
  stopData: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const StopLineStackedGraph: React.FC<StopLineStackedGraphProps> = ({ passageData, stopData, getInterval }) => {
  // Combine the data for the stacked bar chart
  const combinedData = passageData.map((passage, index) => {
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
          <Tooltip />
          <Legend wrapperStyle={{ paddingTop: '70px' }} />
          <Bar dataKey="stop" stackId="a" fill={reportColorPalette.pink} name="Stopped" />
          <Bar dataKey="noStop" stackId="a" fill={reportColorPalette.green} name="Did Not Stop" />
        </BarChart>
      </Box>
    </Box>
  )
}

export default StopLineStackedGraph

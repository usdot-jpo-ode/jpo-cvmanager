import React from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, TooltipProps } from 'recharts'
import { Box, Typography } from '@mui/material'
import { ValueType, NameType } from 'recharts/types/component/DefaultTooltipContent'
import { formatAxisTickNumber } from '../report-utils'

interface BarChartComponentProps {
  title: string
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
  barColor: string
}

const CustomTooltip = ({ active, payload, label }: TooltipProps<ValueType, NameType>) => {
  if (active && payload?.length) {
    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">{label}</Typography>
        <Typography variant="body2">events: {payload[0].value}</Typography>
      </Box>
    )
  }

  return null
}

const BarChartComponent: React.FC<BarChartComponentProps> = ({ title, data, getInterval, barColor }) => (
  <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
    <Box>
      <Typography variant="h6" align="center" sx={{ mt: 2 }}>
        {title}
      </Typography>
      <BarChart
        width={750}
        height={450}
        data={data}
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
          label={{ value: 'Day', position: 'insideBottom', offset: -60 }}
          angle={-45}
          textAnchor="end"
          interval={getInterval(data.length)}
        />
        <YAxis
          label={{ value: 'Event Count', angle: -90, position: 'insideLeft', offset: 0 }}
          tickFormatter={formatAxisTickNumber}
        />
        <Tooltip content={<CustomTooltip />} />
        <Bar dataKey="value" fill={barColor} isAnimationActive={false} />
      </BarChart>
    </Box>
  </Box>
)

export default BarChartComponent

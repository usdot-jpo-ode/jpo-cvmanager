import React from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, TooltipProps } from 'recharts'
import { Box, Typography } from '@mui/material'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'
import reportColorPalette from '../report-color-palette'
import { StopLinePassageReportData } from '../../../../models/ReportData'

interface SignalGroupPassageGraphProps {
  data: StopLinePassageReportData[]
}

const roundToTwoDecimals = (num: number) => Math.round(num * 100) / 100
const roundToInteger = (num: number) => Math.round(num)

const CustomTooltip = ({ active, payload }: TooltipProps<ValueType, NameType>) => {
  if (active && payload && payload.length) {
    const obj = payload[0].payload
    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">Signal Group: {obj.signalGroup}</Typography>
        <Typography variant="body2">Total Events: {roundToInteger(obj.totalEvents)}</Typography>
        <Typography variant="body2">
          Red Events: {roundToInteger(obj.redEvents)}, {roundToTwoDecimals(obj.red)}%
        </Typography>
        <Typography variant="body2">
          Yellow Events: {roundToInteger(obj.yellowEvents)}, {roundToTwoDecimals(obj.yellow)}%
        </Typography>
        <Typography variant="body2">
          Green Events: {roundToInteger(obj.greenEvents)}, {roundToTwoDecimals(obj.green)}%
        </Typography>
        <Typography variant="body2">
          Dark Events: {roundToInteger(obj.darkEvents)}, {roundToTwoDecimals(obj.dark)}%
        </Typography>
      </Box>
    )
  }
  return null
}

const SignalGroupPassageGraph: React.FC<SignalGroupPassageGraphProps> = ({ data }) => {
  const formattedData = data
    .map((group) => {
      const total = Math.max(group.redEvents + group.yellowEvents + group.greenEvents + group.darkEvents, 1) / 100
      return {
        name: `${group.signalGroup}`,
        signalGroup: `${group.signalGroup}`,
        totalEvents: total * 100,
        red: Math.floor((group.redEvents / total) * 100) / 100,
        redEvents: group.redEvents,
        yellow: Math.floor((group.yellowEvents / total) * 100) / 100,
        yellowEvents: group.yellowEvents,
        green: Math.floor((group.greenEvents / total) * 100) / 100,
        greenEvents: group.greenEvents,
        dark: Math.floor((group.darkEvents / total) * 100) / 100,
        darkEvents: group.darkEvents,
      }
    })
    .sort((a, b) => a.name.localeCompare(b.name))

  const hasDark = formattedData.some((item) => item.dark > 0)

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
      <Box>
        <Typography variant="h6" align="center" sx={{ mt: 2 }}>
          Signal Group Passage Events
        </Typography>
        {formattedData.length === 0 ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
            <Typography variant="h6" align="center" sx={{ mt: 2 }}>
              No Data Available
            </Typography>
          </Box>
        ) : (
          <BarChart
            width={750}
            height={450}
            data={formattedData}
            margin={{
              top: 20,
              right: 30,
              left: 20,
              bottom: 20,
            }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" label={{ value: 'Signal Group', position: 'insideBottom', offset: -10, dx: -40 }} />
            <YAxis unit="%" label={{ value: 'Event Count (%)', angle: -90, position: 'insideLeft', dy: 60 }} />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{
                paddingTop: '10px',
                height: '50px',
              }}
              payload={
                [
                  { value: 'Red', type: 'square', id: 'red', color: reportColorPalette.pink },
                  { value: 'Yellow', type: 'square', id: 'yellow', color: reportColorPalette.yellow },
                  { value: 'Green', type: 'square', id: 'green', color: reportColorPalette.green },
                  hasDark ? { value: 'Dark', type: 'square', id: 'dark', color: '#505050' } : null,
                ].filter((item) => item !== null) as any[]
              }
            />
            <Bar dataKey="red" stackId="a" fill={reportColorPalette.pink} />
            <Bar dataKey="yellow" stackId="a" fill={reportColorPalette.yellow} />
            <Bar dataKey="green" stackId="a" fill={reportColorPalette.green} />
            <Bar dataKey="dark" stackId="a" fill="#505050" />
          </BarChart>
        )}
      </Box>
    </Box>
  )
}

export default SignalGroupPassageGraph

import React from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, TooltipProps } from 'recharts'
import { Box, Typography } from '@mui/material'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'
import { StopLineStopReportData } from '../report-utils'
import reportColorPalette from '../report-color-palette'

interface SignalGroupStopGraphProps {
  data: StopLineStopReportData[]
}

const roundToTwoDecimals = (num: number) => Math.round(num * 100) / 100

const CustomTooltip = ({ active, payload }: TooltipProps<ValueType, NameType>) => {
  if (active && payload && payload.length) {
    const obj = payload[0].payload
    return (
      <Box sx={{ backgroundColor: 'white', border: '1px solid #ccc', padding: '10px' }}>
        <Typography variant="body2">Signal Group: {obj.signalGroup}</Typography>
        <Typography variant="body2">Total Avg Time Stopped: {roundToTwoDecimals(obj.total)}s</Typography>
        <Typography variant="body2">
          Red Time: {roundToTwoDecimals(obj.redTime)}s, {roundToTwoDecimals(obj.red)}%
        </Typography>
        <Typography variant="body2">
          Yellow Time: {roundToTwoDecimals(obj.yellowTime)}s, {roundToTwoDecimals(obj.yellow)}%
        </Typography>
        <Typography variant="body2">
          Green Time: {roundToTwoDecimals(obj.greenTime)}s, {roundToTwoDecimals(obj.green)}%
        </Typography>
        <Typography variant="body2">
          Dark Time: {roundToTwoDecimals(obj.darkTime)}s, {roundToTwoDecimals(obj.dark)}%
        </Typography>
      </Box>
    )
  }
  return null
}

const SignalGroupStopGraph: React.FC<SignalGroupStopGraphProps> = ({ data }) => {
  const formattedData = data
    .map((group) => {
      const total =
        Math.max(
          group.timeStoppedOnRed + group.timeStoppedOnYellow + group.timeStoppedOnGreen + group.timeStoppedOnDark,
          1
        ) / 100
      return {
        name: `${group.signalGroup}`,
        signalGroup: `${group.signalGroup}`,
        total: total * 100,
        red: Math.floor((group.timeStoppedOnRed / total) * 100) / 100,
        redTime: group.timeStoppedOnRed,
        yellow: Math.floor((group.timeStoppedOnYellow / total) * 100) / 100,
        yellowTime: group.timeStoppedOnYellow,
        green: Math.floor((group.timeStoppedOnGreen / total) * 100) / 100,
        greenTime: group.timeStoppedOnGreen,
        dark: Math.floor((group.timeStoppedOnDark / total) * 100) / 100,
        darkTime: group.timeStoppedOnDark,
      }
    })
    .sort((a, b) => a.name.localeCompare(b.name))

  const hasDark = formattedData.some((item) => item.dark > 0)

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', height: 'auto' }}>
      <Box>
        <Typography variant="h6" align="center" sx={{ mt: 2 }}>
          Signal Group Stop Line Assessment
        </Typography>
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
          <YAxis unit="%" label={{ value: 'Time Stopped (%)', angle: -90, position: 'insideLeft', dy: 60 }} />
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
      </Box>
    </Box>
  )
}

export default SignalGroupStopGraph

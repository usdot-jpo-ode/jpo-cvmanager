import { Card, CardContent, Grid2, Typography, useTheme } from '@mui/material'
import React from 'react'
import {
  BarChart,
  CartesianGrid,
  XAxis,
  YAxis,
  Legend,
  Bar,
  Tooltip,
  TooltipProps,
  ResponsiveContainer,
} from 'recharts'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'

export const StopLineStopAssessmentCard = (props: {
  assessment: StopLineStopAssessment | undefined
  minWidth: number
}) => {
  const { assessment } = props
  const theme = useTheme()

  const CustomTooltip = ({ active, payload }: TooltipProps<ValueType, NameType>) => {
    if (active && payload) {
      const obj = payload[0].payload
      return (
        <div
          key={obj.laneId}
          style={{
            padding: '6px',
            backgroundColor: theme.palette.background.paper,
            border: '1px solid grey',
          }}
        >
          <b>Signal Group: {obj.signalGroup}</b>
          <p>Total Avg Time Stopped: {obj.total}s</p>
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
            }}
          >
            <div style={{ height: 20, width: 20, backgroundColor: '#e74b4b', marginRight: '5px' }}></div>
            <p>
              Red Time: {obj.redTime}s, {Math.round(obj.red)}%
            </p>
          </div>
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
            }}
          >
            <div style={{ height: 20, width: 20, backgroundColor: '#ffe600', marginRight: '5px' }}></div>
            <p>
              Yellow Time: {obj.yellowTime}s, {Math.round(obj.yellow)}%
            </p>
          </div>
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
            }}
          >
            <div style={{ height: 20, width: 20, backgroundColor: '#44db51', marginRight: '5px' }}></div>
            <p>
              Green Time: {obj.greenTime}s, {Math.round(obj.green)}%
            </p>
          </div>
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
            }}
          >
            <div style={{ height: 20, width: 20, backgroundColor: '#505050', marginRight: '5px' }}></div>
            <p>
              Dark Time: {obj.darkTime}s, {Math.round(obj.dark)}%
            </p>
          </div>
        </div>
      )
    }
    return null
  }

  function sortByName(a, b) {
    if (a.name < b.name) {
      return -1
    }
    if (a.name > b.name) {
      return 1
    }
    return 0
  }

  const data = assessment?.stopLineStopAssessmentGroup
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
    .sort(sortByName)

  const hasDark = data?.some((item) => item.dark > 0)

  return (
    <Grid2 sx={{ height: '100%', minHeight: assessment === undefined ? 200 : 500 }}>
      <Card sx={{ height: '100%', overflowX: 'auto' }}>
        <CardContent>
          <Grid2 container spacing={1} sx={{ justifyContent: 'left' }}>
            <Grid2 sx={{ width: '100%' }}>
              <Typography gutterBottom variant="h6">
                Stop Line Stop Assessment
              </Typography>
              {assessment === undefined || assessment.stopLineStopAssessmentGroup === undefined ? (
                <Typography color="textSecondary" fontSize="small" key={''}>
                  No Data
                </Typography>
              ) : (
                <ResponsiveContainer width="100%" minWidth={`${props.minWidth}px`} height={350}>
                  <BarChart width={250} height={350} data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" label={{ value: 'Signal Group', position: 'insideBottom', offset: -15 }} />
                    <YAxis unit="%" label={{ value: 'Time Stopped (%)', angle: -90, position: 'insideLeft' }} />
                    <Tooltip content={CustomTooltip} />
                    <Legend
                      layout="horizontal"
                      verticalAlign="bottom"
                      align="center"
                      wrapperStyle={{
                        position: 'relative',
                      }}
                      payload={
                        [
                          { value: 'Red', type: 'square', id: 'red', color: '#e74b4b' },
                          { value: 'Yellow', type: 'square', id: 'yellow', color: '#ffe600' },
                          { value: 'Green', type: 'square', id: 'green', color: '#44db51' },
                          hasDark ? { value: 'Dark', type: 'square', id: 'dark', color: '#505050' } : null,
                        ].filter((item) => item !== null) as any[]
                      }
                    />
                    <Bar dataKey="red" stackId="a" fill="#e74b4b" />
                    <Bar dataKey="yellow" stackId="a" fill="#ffe600" />
                    <Bar dataKey="green" stackId="a" fill="#44db51" />
                    <Bar dataKey="dark" stackId="a" fill="#505050" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </Grid2>
          </Grid2>
        </CardContent>
      </Card>
    </Grid2>
  )
}

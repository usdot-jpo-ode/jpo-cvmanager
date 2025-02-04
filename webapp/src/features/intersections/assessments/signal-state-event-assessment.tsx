import { Card, CardContent, Grid2, Typography } from '@mui/material'
import React from 'react'
import { BarChart, CartesianGrid, XAxis, YAxis, Legend, Bar, Tooltip, TooltipProps } from 'recharts'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'

export const SignalStateEventAssessmentCard = (props: { assessment: SignalStateEventAssessment | undefined }) => {
  const { assessment } = props

  function getWidthFactorFromData(data: any[] | undefined): number {
    if (!data) return 0.1
    const maxFactor = 0.9
    const numRowsForMax = 40
    return 0.1 + Math.min(maxFactor, data.length / numRowsForMax)
  }

  const widthFactor = getWidthFactorFromData(assessment?.signalStateEventAssessmentGroup)

  const CustomTooltip = ({ active, payload }: TooltipProps<ValueType, NameType>) => {
    if (active && payload) {
      const obj = payload[0].payload
      return (
        <div
          key={obj.laneId}
          style={{
            padding: '6px',
            backgroundColor: '#333',
            border: '1px solid grey',
          }}
        >
          <b>Signal Group: {obj.signalGroup}</b>
          <p>Total Events: {obj.total}</p>
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
            }}
          >
            <div style={{ height: 20, width: 20, backgroundColor: '#e74b4b', marginRight: '5px' }}></div>
            <p>
              Red Events: {obj.redCount}, {Math.round(obj.red)}%
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
              Yellow Events: {obj.yellowCount}, {Math.round(obj.yellow)}%
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
              Green Events: {obj.greenCount}, {Math.round(obj.green)}%
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
              Dark Events: {obj.darkCount}, {Math.round(obj.dark)}%
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

  const data = assessment?.signalStateEventAssessmentGroup
    .map((group) => {
      const total = Math.max(group.redEvents + group.yellowEvents + group.greenEvents + group.darkEvents, 1) / 100
      return {
        name: `${group.signalGroup}`,
        signalGroup: `${group.signalGroup}`,
        total: total * 100,
        red: Math.floor((group.redEvents / total) * 100) / 100,
        redCount: group.redEvents,
        yellow: Math.floor((group.yellowEvents / total) * 100) / 100,
        yellowCount: group.yellowEvents,
        green: Math.floor((group.greenEvents / total) * 100) / 100,
        greenCount: group.greenEvents,
        dark: Math.floor((group.darkEvents / total) * 100) / 100,
        darkCount: group.darkEvents,
      }
    })
    .sort(sortByName)

  const hasDark = data?.some((item) => item.darkCount > 0)

  return (
    <Grid2 width={assessment === undefined ? 200 : 80 + widthFactor * 1200}>
      <Card sx={{ height: '100%', overflow: 'visible' }}>
        <CardContent>
          <Grid2 container spacing={3} sx={{ justifyContent: 'space-between' }}>
            <Grid2>
              <Typography color="textSecondary" gutterBottom variant="overline">
                Signal State Passage Assessment
              </Typography>
              {assessment === undefined ? (
                <Typography color="textPrimary" variant="h5" key={''}>
                  No Data
                </Typography>
              ) : (
                <BarChart
                  width={widthFactor * 1200}
                  height={350}
                  data={data}
                  margin={{
                    top: 20,
                    right: 30,
                    left: 20,
                    bottom: 5,
                  }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" label={{ value: 'Signal Group', position: 'insideBottomRight', offset: -5 }} />
                  <YAxis unit="%" label={{ value: 'Event Count (%)', angle: -90, position: 'insideLeft' }} />
                  <Tooltip content={CustomTooltip} />
                  <Legend
                    wrapperStyle={{
                      paddingTop: '10px',
                      height: '50px',
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
              )}
            </Grid2>
          </Grid2>
        </CardContent>
      </Card>
    </Grid2>
  )
}

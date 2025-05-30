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

export const ConnectionOfTravelAssessmentCard = (props: {
  assessment: ConnectionOfTravelAssessment | undefined
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
          <b>Connection ID: {obj.connectionID}</b>
          <p>Ingress Lane ID: {obj.ingressLaneID}</p>
          <p>EgressLaneID: {obj.egressLaneID}</p>
          <p>Event Count: {Math.max(obj.eventCountValid, obj.eventCountInvalid)}</p>
        </div>
      )
    }
    return null
  }

  function sortByName(a, b) {
    if (a.ingressLaneID < b.ingressLaneID) {
      return -1
    }
    if (a.ingressLaneID > b.ingressLaneID) {
      return 1
    }
    if (a.egressLaneId < b.egressLaneId) {
      return -1
    }
    if (a.egressLaneId > b.egressLaneId) {
      return 1
    }
    return 0
  }

  const data = assessment?.connectionOfTravelAssessmentGroups
    .map((group) => {
      return {
        name: `${group.ingressLaneID}_${group.egressLaneID}`,
        eventCountValid: group.connectionID == -1 ? 0 : group.eventCount,
        eventCountInvalid: group.connectionID == -1 ? group.eventCount : 0,
        connectionID: group.connectionID,
        ingressLaneID: group.ingressLaneID,
        egressLaneID: group.egressLaneID,
      }
    })
    .sort(sortByName)

  const hasValidEvents = data?.some((item) => item.eventCountValid > 0)
  const hasInvalidEvents = data?.some((item) => item.eventCountInvalid > 0)

  return (
    <Grid2 sx={{ height: '100%', minHeight: assessment === undefined ? 200 : 500 }}>
      <Card sx={{ height: '100%', overflowX: 'auto' }}>
        <CardContent>
          <Grid2 container spacing={1} sx={{ justifyContent: 'left' }}>
            <Grid2 sx={{ width: '100%', height: '100%' }}>
              <Typography gutterBottom variant="h6">
                Connection of Travel Assessment
              </Typography>
              {assessment === undefined ? (
                <Typography color="textSecondary" fontSize="small" key={''}>
                  No Data
                </Typography>
              ) : (
                <ResponsiveContainer width="100%" minWidth={`${props.minWidth}px`} height={350}>
                  <BarChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="name"
                      interval={0}
                      angle={-45}
                      height={50}
                      textAnchor="end"
                      label={{ value: 'Connection ID', position: 'insideBottom', offset: -15 }}
                    />
                    <YAxis label={{ value: 'Event Count', angle: -90, position: 'insideLeft' }} />
                    <Tooltip content={CustomTooltip} />
                    <Legend
                      layout="horizontal"
                      verticalAlign="bottom"
                      align="center"
                      wrapperStyle={{
                        position: 'relative',
                      }}
                      payload={[
                        ...(hasValidEvents
                          ? [
                              {
                                value: `Valid Connection ID`,
                                id: 'eventCountValid',
                                color: '#463af1',
                              },
                            ]
                          : []),
                        ...(hasInvalidEvents
                          ? [
                              {
                                value: `Invalid Connection ID`,
                                id: 'eventCountInvalid',
                                color: '#f35555',
                              },
                            ]
                          : []),
                      ]}
                    />
                    <Bar dataKey="eventCountValid" stackId="a" fill="#463af1" />
                    <Bar dataKey="eventCountInvalid" stackId="a" fill="#f35555" />
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

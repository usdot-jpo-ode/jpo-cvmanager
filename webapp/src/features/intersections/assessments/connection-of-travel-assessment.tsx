import { Card, CardContent, Grid2, Typography } from '@mui/material'
import React from 'react'
import { BarChart, CartesianGrid, XAxis, YAxis, Legend, Bar, Tooltip, TooltipProps } from 'recharts'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'

export const ConnectionOfTravelAssessmentCard = (props: { assessment: ConnectionOfTravelAssessment | undefined }) => {
  const { assessment } = props

  function getWidthFactorFromData(data: any[] | undefined): number {
    if (!data) return 0.1
    const maxFactor = 0.9
    const numRowsForMax = 40
    return 0.1 + Math.min(maxFactor, data.length / numRowsForMax)
  }
  const widthFactor = getWidthFactorFromData(assessment?.connectionOfTravelAssessmentGroups)

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
    <Grid2 width={assessment === undefined ? 200 : 80 + widthFactor * 1600}>
      <Card sx={{ height: '100%', overflow: 'visible' }}>
        <CardContent>
          <Grid2 container spacing={3} sx={{ justifyContent: 'space-between' }}>
            <Grid2>
              <Typography color="textSecondary" gutterBottom variant="overline">
                Connection of Travel Assessment
              </Typography>
              {assessment === undefined ? (
                <Typography color="textPrimary" variant="h5" key={''}>
                  No Data
                </Typography>
              ) : (
                <BarChart
                  width={widthFactor * 1600}
                  height={350}
                  data={data}
                  margin={{
                    top: 5,
                    right: 30,
                    left: 20,
                    bottom: 5,
                  }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="name"
                    interval={0}
                    angle={-45}
                    height={50}
                    textAnchor="end"
                    label={{ value: 'Connection ID', position: 'insideBottomRight', offset: -15 }}
                  />
                  <YAxis label={{ value: 'Event Count', angle: -90, position: 'insideLeft' }} />
                  <Tooltip content={CustomTooltip} />
                  <Legend
                    wrapperStyle={{
                      paddingTop: '20px',
                      height: hasValidEvents && hasInvalidEvents && (data?.length ?? 5) <= 4 ? '90px' : '50px',
                    }}
                    payload={[
                      ...(hasValidEvents
                        ? [
                            {
                              value: `Event Count Valid Connection ID`,
                              id: 'eventCountValid',
                              color: '#463af1',
                            },
                          ]
                        : []),
                      ...(hasInvalidEvents
                        ? [
                            {
                              value: `Event Count Invalid Connection ID`,
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
              )}
            </Grid2>
          </Grid2>
        </CardContent>
      </Card>
    </Grid2>
  )
}

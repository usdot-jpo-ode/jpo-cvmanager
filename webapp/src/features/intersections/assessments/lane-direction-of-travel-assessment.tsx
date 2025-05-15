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

export const LaneDirectionOfTravelAssessmentCard = (props: {
  assessment: LaneDirectionOfTravelAssessment | undefined
  minWidth: number
}) => {
  const { assessment } = props
  const theme = useTheme()

  const SegColors = [
    ['#d55d01'],
    ['#d59201'],
    ['#c5c900'],
    ['#88ff00'],
    ['#00bc0d'],
    ['#00c295'],
    ['#007add'],
    ['#0014c7'],
    ['#7006fc'],
    ['#5800a0'],
    ['#900bae'],
    ['#850068'],
    ['#a30336'],
    ['#c00000'],
    ['#750000'],
  ]

  function onlyUnique(value, index, array) {
    return array.indexOf(value) === index
  }

  const segmentIds =
    assessment?.laneDirectionOfTravelAssessmentGroup
      .map((group) => group.segmentID)
      .sort((a, b) => a - b)
      .filter(onlyUnique) ?? []
  const maxSegmentId = Math.max(...segmentIds)

  const compressedGroups: {
    [key: number]: {
      laneId: number
      inTolerance: {
        [key: string]: number
      }
      outOfTolerance: {
        [key: string]: number
      }
    }
  } = {}
  for (let i = 0; i < (assessment?.laneDirectionOfTravelAssessmentGroup ?? []).length; i++) {
    const group = assessment?.laneDirectionOfTravelAssessmentGroup[i]!
    if (!compressedGroups[group.laneID]) {
      compressedGroups[group.laneID] = {
        laneId: group.laneID,
        inTolerance: {},
        outOfTolerance: {},
      }
    }
    compressedGroups[group.laneID].inTolerance[group.segmentID] = group.inToleranceEvents
    compressedGroups[group.laneID].outOfTolerance[group.segmentID] = group.outOfToleranceEvents
  }

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
          <b>Lane {obj.laneId}</b>
          <div style={{ display: 'flex' }}>
            <div style={{ marginRight: '10px' }}>
              <b>
                In Tolerance:{' '}
                {segmentIds.map((segId) => obj.inTolerance[segId]).reduce((partialSum, a) => partialSum + (a ?? 0), 0)}
              </b>
              {segmentIds.map((segmentId, index) => {
                return (
                  <p>
                    {segmentId}: {obj.inTolerance[segmentId] ?? 0}
                  </p>
                )
              })}
            </div>
            <div>
              <b>
                Out of Tolerance:{' '}
                {segmentIds
                  .map((segId) => obj.outOfTolerance[segId])
                  .reduce((partialSum, a) => partialSum + (a ?? 0), 0)}
              </b>
              {segmentIds.map((segmentId, index) => {
                return (
                  <p>
                    {segmentId}: {obj.outOfTolerance[segmentId] ?? 0}
                  </p>
                )
              })}
            </div>
          </div>
        </div>
      )
    }
    return null
  }

  return (
    <Grid2 sx={{ height: '100%', minHeight: assessment === undefined ? 200 : 500 }}>
      <Card sx={{ height: '100%', overflowX: 'auto' }}>
        <CardContent>
          <Grid2 container spacing={1} sx={{ justifyContent: 'left' }}>
            <Grid2 sx={{ width: '100%' }}>
              <Typography gutterBottom variant="h6">
                Lane Direction of Travel Assessment
              </Typography>
              {assessment === undefined ? (
                <Typography color="textSecondary" fontSize="small" key={''}>
                  No Data
                </Typography>
              ) : (
                <ResponsiveContainer width="100%" minWidth={`${props.minWidth}px`} height={350}>
                  <BarChart
                    height={350}
                    data={Object.values(compressedGroups).map((group) => {
                      return {
                        name: `${group.laneId}`,
                        ...group,
                      }
                    })}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" label={{ value: 'Lane ID', position: 'insideBottom', offset: -15 }} />
                    <YAxis label={{ value: 'Events', angle: -90, position: 'insideLeft' }} />
                    <Tooltip content={CustomTooltip} />
                    <Legend
                      layout="horizontal"
                      verticalAlign="bottom"
                      align="center"
                      wrapperStyle={{
                        position: 'relative',
                      }}
                      payload={segmentIds.map((segmentId, index) => {
                        return {
                          value: `Segment ${segmentId}`,
                          id: `inTolerance.${segmentId}`,
                          color:
                            SegColors[
                              (index * Math.max(Math.floor(SegColors.length / maxSegmentId), 1)) % SegColors.length
                            ][0],
                        }
                      })}
                    />
                    {segmentIds.map((segmentId, index) => {
                      return (
                        <Bar
                          dataKey={`inTolerance.${segmentId}`}
                          stackId={`inTolerance`}
                          name={`${segmentId}`}
                          fill={
                            SegColors[
                              (index * Math.max(Math.floor(SegColors.length / maxSegmentId), 1)) % SegColors.length
                            ][0]
                          }
                        />
                      )
                    })}
                    {segmentIds.map((segmentId, index) => {
                      return (
                        <Bar
                          dataKey={`outOfTolerance.${segmentId}`}
                          stackId={`outOfTolerance`}
                          name={`${segmentId}`}
                          fill={
                            SegColors[
                              (index * Math.max(Math.floor(SegColors.length / maxSegmentId), 1)) % SegColors.length
                            ][0]
                          }
                        />
                      )
                    })}
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

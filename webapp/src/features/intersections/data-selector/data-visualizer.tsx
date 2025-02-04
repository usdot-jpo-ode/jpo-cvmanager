import { Box, Button, Card, Container, Divider, Grid2, CardHeader } from '@mui/material'
import React from 'react'
import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip, Legend } from 'recharts'

export const DataVisualizer = (props: { data: any[]; onDownload: () => void }) => {
  const { data, onDownload } = props

  const dateFormatter = (unix_timestamp) => {
    const date = new Date(unix_timestamp)

    // return date in YY/MM/DD
    return `${date.getFullYear().toString()}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date
      .getDate()
      .toString()
      .padStart(2, '0')}`
  }

  return (
    <>
      <Container maxWidth={false}>
        <Card>
          <>
            <CardHeader title="Counts" />
            <Divider />
          </>
          <LineChart width={1000} height={550} data={data} margin={{ right: 30 }}>
            <XAxis dataKey="id" tickFormatter={dateFormatter} />
            <YAxis />
            <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
            <Line type="monotone" dataKey="ConnectionOfTravelEventCount" stroke="#82ca9d" />
            <Line type="monotone" dataKey="IntersectionReferenceAlignmentEventCount" stroke="#1171c0" />
            <Line type="monotone" dataKey="LaneDirectionOfTravelEventCount" stroke="#ff0000" />
            <Line type="monotone" dataKey="ProcessingTimePeriodCount" stroke="#00ff00" />
            <Line type="monotone" dataKey="SignalGroupAlignmentEventCount" stroke="#0000ff" />
            <Line type="monotone" dataKey="SignalStateConflictEventCount" stroke="#ff00ff" />
            <Line type="monotone" dataKey="SignalStateEventCount" stroke="#ffff00" />
            <Line type="monotone" dataKey="SignalStateStopEventCount" stroke="#00ffff" />
            <Line type="monotone" dataKey="TimeChangeDetailsEventCount" stroke="#ff8000" />
            <Line type="monotone" dataKey="MapMinimumDataEventCount" stroke="#8000ff" />
            <Line type="monotone" dataKey="SpatMinimumDataEventCount" stroke="#ff0080" />
            <Line type="monotone" dataKey="MapBroadcastRateEventCount" stroke="#0080ff" />
            <Line type="monotone" dataKey="SpatBroadcastRateEventCount" stroke="#80ff00" />
            <Legend verticalAlign="middle" align="right" layout="vertical" wrapperStyle={{ right: 20 }} />
            <Tooltip
              contentStyle={{ backgroundColor: '#333', color: '#fff' }}
              labelFormatter={(value) => dateFormatter(value)}
              formatter={(value, name, props) => [
                `${value}`, // Display the value
                `${name}`, // Display the name
                // Display a square with the color of the line
                `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${props.color};"></span>`,
              ]}
            />
          </LineChart>
        </Card>
        <Box sx={{ mb: 4 }}>
          <Box
            sx={{
              m: -1,
              mt: 3,
            }}
          >
            <Grid2 container justifyContent="left" spacing={3}>
              <Grid2>
                <Button
                  sx={{ m: 1 }}
                  variant="contained"
                  onClick={onDownload}
                  disabled={data.length <= 0 ? true : false}
                >
                  Download
                </Button>
              </Grid2>
            </Grid2>
          </Box>
        </Box>
      </Container>
    </>
  )
}

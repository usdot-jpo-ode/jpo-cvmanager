import { Box, Button, Card, Container, Divider, Grid, CardHeader } from "@mui/material";
import React from "react";
import { LineChart, Line, CartesianGrid, XAxis, YAxis } from "recharts";

export const DataVisualizer = (props: { data: any[]; onDownload: () => void }) => {
  const { data, onDownload } = props;

  const dateFormatter = (unix_timestamp) => {
    const date = new Date(unix_timestamp);

    // return date in YY/MM/DD
    return `${date.getFullYear().toString().slice(-2)}${(date.getMonth() + 1).toString().padStart(2, "0")}/${date
      .getDate()
      .toString()
      .padStart(2, "0")}/`;
  };

  return (
    <>
      <Container maxWidth={false}>
        <Card>
          <>
            <CardHeader title="Counts" />
            <Divider />
          </>
          <LineChart width={500} height={300} data={data}>
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
          </LineChart>
        </Card>
        <Box sx={{ mb: 4 }}>
          <Box
            sx={{
              m: -1,
              mt: 3,
            }}
          >
            <Grid container justifyContent="left" spacing={3}>
              <Grid item>
                <Button
                  sx={{ m: 1 }}
                  variant="contained"
                  onClick={onDownload}
                  disabled={data.length <= 0 ? true : false}
                >
                  Download
                </Button>
              </Grid>
            </Grid>
          </Box>
        </Box>
      </Container>
    </>
  );
};

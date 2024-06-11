import {
  Box,
  Card,
  Container,
  Grid,
  TextField,
  Typography,
  CardHeader,
  CardContent,
} from "@mui/material";
import React, { useEffect, useState } from "react";
import MessageMonitorApi from '../../apis/mm-api';
import { DateTimePicker, LocalizationProvider } from '@mui/x-date-pickers';
import AdapterDateFns from '@date-io/date-fns';

export const MessageCountWidget = (props: {
  accessToken: string | undefined;
  intersectionId: number;
}) => {
  const { accessToken, intersectionId } = props;

  const [startDate, setStartDate] = useState(new Date());
  const [endDate, setEndDate] = useState(new Date());
  const [bsmCount, setBsmCount] = useState(0);
  const [spatCount, setSpatCount] = useState(0);
  const [mapCount, setMapCount] = useState(0);

  useEffect(() => {
    const dayStart = new Date();
    dayStart.setHours(0, 0, 0, 0);
    setStartDate(dayStart);
    const dayEnd = new Date();
    dayEnd.setHours(23, 59, 59, 0);
    setEndDate(dayEnd);
  }, []);

  useEffect(() => {
    if (accessToken) {
      const bsmCountPromise = MessageMonitorApi.getMessageCount(
        accessToken,
        "bsm",
        intersectionId,
        startDate,
        endDate
      );
      bsmCountPromise.then((count) => setBsmCount(count))
      .catch(error => console.error(error));

      const spatCountPromise = MessageMonitorApi.getMessageCount(
        accessToken,
        "spat",
        intersectionId,
        startDate,
        endDate
      );
      spatCountPromise.then((count) => setSpatCount(count))
      .catch(error => console.error(error));

      const mapCountPromise = MessageMonitorApi.getMessageCount(
        accessToken,
        "map",
        intersectionId,
        startDate,
        endDate
      );
      mapCountPromise.then((count) => setMapCount(count))
      .catch(error => console.error(error));
    }

  }, [startDate, endDate, intersectionId]);

  return (
    <Box>
      <Container maxWidth="md">
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={5}>
              <Card>
                <CardHeader 
                  title={
                    <Typography color="textSecondary" gutterBottom variant="overline">
                    {`Select time range`}                            
                    </Typography>
                    }
                  sx={{ pb: 0 }} 
                />
                <CardContent>
                  <DateTimePicker
                    label="Start Date"
                    value={startDate}
                    onChange={(newStartDate) => setStartDate(newStartDate || new Date())}
                    renderInput={(params) => <TextField {...params} />}
                  />
                  <DateTimePicker
                    label="End Date"
                    value={endDate}
                    onChange={(newEndDate) => setEndDate(newEndDate || new Date())}
                    renderInput={(params) => <TextField {...params} />}
                  />
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={7}>
              <Grid container spacing={0}>

                <Grid item xs={6}>
                  <Card sx={{ marginBottom: 1.5, marginRight: 1.5 }}>
                    <CardHeader 
                      title={
                      <Typography color="textSecondary" gutterBottom variant="overline">
                        {`BSM Count`}                            
                        </Typography>
                        }
                      sx={{ pb: 0 }} 
                    />
                    <CardContent sx={{ pt: 0 }}>
                      <Typography variant="h2" color="primary" fontWeight="bold" align="center">
                        {bsmCount !== 0 ? bsmCount : "-"}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={6}>
                  <Card sx={{ marginBottom: 1.5, marginLeft: 1.5 }}>
                    <CardHeader 
                      title={
                      <Typography color="textSecondary" gutterBottom variant="overline">
                        {`SPAT Count`}                            
                        </Typography>
                      }
                      sx={{ pb: 0 }} 
                    />
                    <CardContent sx={{ pt: 0 }}>
                      <Typography variant="h2" color="secondary" fontWeight="bold" align="center">
                        {spatCount !== 0 ? spatCount : "-"}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={6}>
                  <Card sx={{marginTop: 1.5, marginRight: 1.5 }}>
                    <CardHeader 
                      title={
                      <Typography color="textSecondary" gutterBottom variant="overline">
                        {`MAP Count`}                            
                        </Typography>
                      }
                      sx={{ pb: 0 }} 
                    />
                    <CardContent sx={{ pt: 0 }}>
                      <Typography variant="h2" color="#900bae" fontWeight="bold" align="center">
                        {mapCount !== 0 ? mapCount : "-"}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Grid>        
          </Grid>
        </LocalizationProvider>
      </Container>
    </Box>
  );
};
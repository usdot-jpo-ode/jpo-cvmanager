import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Checkbox,
  Divider,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
  Typography,
  Switch,
} from "@mui/material";
import React, { useEffect } from "react";

export const SettingsNotifications = (props: {
  value?: EmailPreferences;
  onSave: (v: EmailPreferences) => void;
  userRole: UserRole;
}) => {
  const [value, setValue] = React.useState<EmailPreferences>(
    props.value ?? {
      receiveAnnouncements: true,
      notificationFrequency: "ONCE_PER_DAY",
      receiveCeaseBroadcastRecommendations: true,
      receiveCriticalErrorMessages: true,
      receiveNewUserRequests: false,
    }
  );

  useEffect(() => {
    setValue(
      props.value ?? {
        receiveAnnouncements: true,
        notificationFrequency: "ONCE_PER_DAY",
        receiveCeaseBroadcastRecommendations: true,
        receiveCriticalErrorMessages: true,
        receiveNewUserRequests: false,
      }
    );
  }, [props.value]);

  return (
    <form {...props}>
      <Card>
        <CardHeader subheader="Manage Email Settings" title="Communication" />
        <Divider />
        <CardContent>
          <Grid container spacing={6} wrap="wrap">
            <Grid
              item
              md={4}
              sm={6}
              sx={{
                display: "flex",
                flexDirection: "column",
              }}
              xs={12}
            >
              <Typography color="textPrimary" gutterBottom variant="h6">
                Notifications Email Rate
              </Typography>
              <RadioGroup
                aria-labelledby="demo-controlled-radio-buttons-group"
                name="controlled-radio-buttons-group"
                value={value.notificationFrequency}
                onChange={(e, v) => {
                  setValue((prevValue) => ({ ...prevValue, notificationFrequency: v as EmailFrequency }));
                }}
              >
                <FormControlLabel value="ALWAYS" control={<Radio />} label="Always" />
                <FormControlLabel value="ONCE_PER_HOUR" control={<Radio />} label="Once Per Hour" />
                <FormControlLabel value="ONCE_PER_DAY" control={<Radio />} label="Once Per Day" />
                <FormControlLabel value="ONCE_PER_WEEK" control={<Radio />} label="Once Per Week" />
                <FormControlLabel value="ONCE_PER_MONTH" control={<Radio />} label="Once Per Month" />
                <FormControlLabel value="NEVER" control={<Radio />} label="Never" />
              </RadioGroup>
            </Grid>
            <Grid
              item
              md={4}
              sm={6}
              sx={{
                display: "flex",
                flexDirection: "column",
              }}
              xs={12}
            >
              <Typography color="textPrimary" gutterBottom variant="h6">
                Receive Cease Broadcast Recommendation Emails
              </Typography>
              <Switch
                checked={value.receiveCeaseBroadcastRecommendations}
                onChange={(e, v) =>
                  setValue((prevValue) => ({ ...prevValue, receiveCeaseBroadcastRecommendations: v }))
                }
                inputProps={{ "aria-label": "controlled" }}
              />
            </Grid>
            <Grid
              item
              md={4}
              sm={6}
              sx={{
                display: "flex",
                flexDirection: "column",
              }}
              xs={12}
            >
              <Typography color="textPrimary" gutterBottom variant="h6">
                Receive Announcement Emails
              </Typography>
              <Switch
                checked={value.receiveAnnouncements}
                onChange={(e, v) => setValue((prevValue) => ({ ...prevValue, receiveAnnouncements: v }))}
                inputProps={{ "aria-label": "controlled" }}
              />
            </Grid>
            <Grid
              item
              md={4}
              sm={6}
              sx={{
                display: "flex",
                flexDirection: "column",
              }}
              xs={12}
            >
              <Typography color="textPrimary" gutterBottom variant="h6">
                Receive Critical Error Message Emails
              </Typography>
              <Switch
                checked={value.receiveCriticalErrorMessages}
                onChange={(e, v) => setValue((prevValue) => ({ ...prevValue, receiveCriticalErrorMessages: v }))}
                inputProps={{ "aria-label": "controlled" }}
              />
            </Grid>
            {props.userRole === "ADMIN" && (
              <Grid
                item
                md={4}
                sm={6}
                sx={{
                  display: "flex",
                  flexDirection: "column",
                }}
                xs={12}
              >
                <Typography color="textPrimary" gutterBottom variant="h6">
                  Receive New User Request Emails (Admins Only)
                </Typography>
                <Switch
                  checked={value.receiveNewUserRequests}
                  onChange={(e, v) => setValue((prevValue) => ({ ...prevValue, receiveNewUserRequests: v }))}
                  inputProps={{ "aria-label": "controlled" }}
                />
              </Grid>
            )}
          </Grid>
        </CardContent>
        <Divider />
        <Box
          sx={{
            display: "flex",
            justifyContent: "flex-end",
            p: 2,
          }}
        >
          <Button
            color="primary"
            variant="contained"
            onClick={() => {
              props.onSave(value);
            }}
            disabled={props.value == value}
          >
            Save
          </Button>
        </Box>
      </Card>
    </form>
  );
};

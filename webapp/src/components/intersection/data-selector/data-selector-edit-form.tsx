import React, { useState } from "react";
import NextLink from "next/link";
import { useRouter } from "next/router";
import PropTypes from "prop-types";
import toast from "react-hot-toast";
import * as Yup from "yup";
import { useFormik } from "formik";
import dayjs, { Dayjs } from "dayjs";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { Theme, useTheme } from "@mui/material/styles";
import {
  Autocomplete,
  Button,
  Box,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Chip,
  Divider,
  Grid,
  TextField,
  InputLabel,
  MenuItem,
  Select,
  InputAdornment,
  OutlinedInput,
  Checkbox,
} from "@mui/material";
import CheckBoxOutlineBlankIcon from "@mui/icons-material/CheckBoxOutlineBlank";
import CheckBoxIcon from "@mui/icons-material/CheckBox";
import { FormikCheckboxList } from "./formik-checkbox-list";
import { useQueryContext } from "../../contexts/query-context";

interface Item {
  label: string;
  value: string;
}

const EVENT_TYPES: Item[] = [
  { label: "All", value: "All" },
  { label: "ConnectionOfTravelEvent", value: "connection_of_travel" },
  { label: "IntersectionReferenceAlignmentEvent", value: "intersection_reference_alignment" },
  { label: "LaneDirectionOfTravelEvent", value: "lane_direction_of_travel" },
  //   { label: "ProcessingTimePeriod", value: "processing_time_period" },
  { label: "SignalGroupAlignmentEvent", value: "signal_group_alignment" },
  { label: "SignalStateConflictEvent", value: "signal_state_conflict" },
  { label: "SignalStateEvent", value: "signal_state" },
  { label: "SignalStateStopEvent", value: "signal_state_stop" },
  { label: "TimeChangeDetailsEvent", value: "time_change_details" },
  { label: "MapMinimumDataEvent", value: "map_minimum_data" },
  { label: "SpatMinimumDataEvent", value: "spat_minimum_data" },
  { label: "MapBroadcastRateEvent", value: "map_broadcast_rate" },
  { label: "SpatBroadcastRateEvent", value: "spat_broadcast_rate" },
];

const ASSESSMENT_TYPES: Item[] = [
  { label: "All", value: "All" },
  { label: "SignalStateEventAssessment", value: "signal_state_event_assessment" },
  { label: "StopLineStopAssessment", value: "signal_state_assessment" },
  { label: "LaneDirectionOfTravelAssessment", value: "lane_direction_of_travel" },
  { label: "ConnectionOfTravelAssessment", value: "connection_of_travel" },
];

export const DataSelectorEditForm = (props: {
  onQuery: (query: any) => void;
  onVisualize: (query: any) => void;
  dbIntersectionId: number | undefined;
  roadRegulatorIntersectionIds: { [roadRegulatorId: number]: number[] };
}) => {
  const { onQuery, onVisualize, dbIntersectionId, roadRegulatorIntersectionIds, ...other } = props;
  const [visualize, setVisualize] = useState(false);

  const { dataSelectorForm, setDataSelectorForm } = useQueryContext();

  const router = useRouter();
  const formik = useFormik({
    initialValues: {
      ...dataSelectorForm,
      intersectionId: dataSelectorForm.intersectionId ?? dbIntersectionId,
    },
    validationSchema: Yup.object({
      //   type: Yup.string().required("Type is required"),
      //   startDate: Yup.date().required("Start date is required"),
      //   timeRange: Yup.number().required("Time interval is required"),
      //   intersectionId: Yup.string().required("Intersection ID is required"),
      //   roadRegulatorId: Yup.string().required("Road Regulator ID is required"),
      //   bsmVehicleId: Yup.string(),
    }),
    onSubmit: async (values, helpers) => {
      // const endTime = new Date(startDate.getTime() + timeRange * 60 * 1000);
      // convert timeRange to endTime using the timeUnit field
      const endTime = dayjs(values.startDate).add(values.timeRange, values.timeUnit).toDate();
      try {
        if (visualize) {
          onVisualize({
            intersectionId: values.intersectionId,
            roadRegulatorId: values.roadRegulatorId,
            startDate: values.startDate,
            endTime: endTime,
            eventTypes: values.eventTypes.map((e) => e.value).filter((e) => e !== "All"),
          });
        } else {
          setDataSelectorForm(values);
          helpers.setStatus({ success: true });
          helpers.setSubmitting(false);
          onQuery({
            type: values.type,
            intersectionId: values.intersectionId,
            roadRegulatorId: values.roadRegulatorId,
            startDate: values.startDate,
            endTime: endTime,
            eventTypes: values.eventTypes.map((e) => e.value).filter((e) => e !== "All"),
            assessmentTypes: values.assessmentTypes.map((e) => e.value).filter((e) => e !== "All"),
            bsmVehicleId: values.bsmVehicleId,
          });
        }
      } catch (err) {
        console.error(err);
        toast.error("Something went wrong!");
        helpers.setStatus({ success: false });
        helpers.setErrors({ submit: err.message });
        helpers.setSubmitting(false);
      }
    },
  });

  const onTypeChange = (newType) => {
    formik.setFieldValue("eventTypes", [] as Item[]);
    formik.setFieldValue("assessmentTypes", [] as Item[]);
  };

  const getTypeSpecificFilters = (type) => {
    switch (type) {
      case "bsm":
        return (
          <>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.bsmVehicleId && formik.errors.bsmVehicleId)}
                fullWidth
                helperText={formik.touched.bsmVehicleId && formik.errors.bsmVehicleId}
                label="Vehicle ID"
                name="bsmVehicleId"
                onChange={formik.handleChange}
                value={formik.values.bsmVehicleId}
              />
            </Grid>
          </>
        );
      case "events":
        return (
          <>
            <Grid item md={6} xs={12}>
              <InputLabel variant="standard" htmlFor="uncontrolled-native">
                Event Type
              </InputLabel>
              <FormikCheckboxList
                values={EVENT_TYPES}
                selectedValues={formik.values.eventTypes}
                setValues={(val) => formik.setFieldValue("eventTypes", val)}
              />
            </Grid>
          </>
        );
      case "assessments":
        return (
          <>
            <Grid item md={6} xs={12}>
              <InputLabel variant="standard" htmlFor="uncontrolled-native">
                Assessment Type
              </InputLabel>
              <FormikCheckboxList
                values={ASSESSMENT_TYPES}
                selectedValues={formik.values.assessmentTypes}
                setValues={(val) => formik.setFieldValue("assessmentTypes", val)}
              />
            </Grid>
          </>
        );
      default:
        return <></>;
    }
  };

  return (
    <form onSubmit={formik.handleSubmit} {...other}>
      <Card>
        {/* <CardHeader title="Edit Configuration Parameter" /> */}
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <Select
                error={Boolean(formik.touched.intersectionId && formik.errors.intersectionId)}
                fullWidth
                value={formik.values.intersectionId}
                label="Intersection ID"
                name="intersectionId"
                onChange={(e) => {
                  formik.setFieldValue("intersectionId", Number(e.target.value));
                }}
                onBlur={formik.handleBlur}
              >
                {roadRegulatorIntersectionIds?.[formik.values.roadRegulatorId]?.map((intersectionId) => {
                  return (
                    <MenuItem value={intersectionId} key={intersectionId}>
                      {intersectionId}
                    </MenuItem>
                  );
                })}
              </Select>
              {/* <TextField
                error={Boolean(formik.touched.intersectionId && formik.errors.intersectionId)}
                fullWidth
                // helperText={formik.touched.intersectionId && formik.errors.intersectionId}
                label="Intersection ID"
                name="intersectionId"
                onChange={formik.handleChange}
                value={formik.values.intersectionId}
              /> */}
            </Grid>
            <Grid item md={6} xs={12}>
              <Select
                error={Boolean(formik.touched.roadRegulatorId && formik.errors.roadRegulatorId)}
                fullWidth
                value={formik.values.roadRegulatorId}
                label="Road Regulator ID"
                name="roadRegulatorId"
                onChange={(e) => {
                  formik.setFieldValue("roadRegulatorId", Number(e.target.value));
                }}
                onBlur={formik.handleBlur}
              >
                {Object.keys(roadRegulatorIntersectionIds)?.map((roadRegulatorId) => {
                  return (
                    <MenuItem value={roadRegulatorId} key={roadRegulatorId}>
                      {roadRegulatorId}
                    </MenuItem>
                  );
                })}
              </Select>
              {/* <TextField
                error={Boolean(formik.touched.roadRegulatorId && formik.errors.roadRegulatorId)}
                fullWidth
                helperText={formik.touched.roadRegulatorId && formik.errors.roadRegulatorId}
                label="Road Regulator ID"
                name="roadRegulatorId"
                onChange={formik.handleChange}
                value={formik.values.roadRegulatorId}
              /> */}
            </Grid>
            <Grid item md={4} xs={12}>
              <Select
                error={Boolean(formik.touched.type && formik.errors.type)}
                // fullWidth
                value={formik.values.type}
                // label="Query Type"
                label="Type"
                // helperText={formik.touched.type && formik.errors.type}
                onChange={(e) => {
                  onTypeChange(e.target.value);
                  formik.setFieldValue("type", e.target.value);
                }}
                onBlur={formik.handleBlur}
              >
                {/* <MenuItem value={"map"}>MAP</MenuItem>
                            <MenuItem value={"spat"}>SPAT</MenuItem>
                            <MenuItem value={"bsm"}>BSM</MenuItem> */}
                <MenuItem value={"events"}>Events</MenuItem>
                <MenuItem value={"assessments"}>Assessments</MenuItem>
                {/* <MenuItem value={"notifications"}>Notifications</MenuItem> */}
              </Select>
            </Grid>
            <Grid item md={4} xs={12}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  renderInput={(props) => (
                    <TextField
                      {...props}
                      error={Boolean(formik.touched.startDate && formik.errors.startDate)}
                      //   helperText={formik.touched.startDate && formik.errors.startDate}
                      name="startDate"
                      label="Start Date"
                      //   fullWidth
                    />
                  )}
                  value={formik.values.startDate}
                  onChange={(e) => formik.setFieldValue("startDate", (e as Dayjs | null)?.toDate(), true)}
                />
              </LocalizationProvider>
            </Grid>
            <Grid item md={4} xs={12}>
              <TextField
                // fullWidth
                helperText={formik.touched.timeRange && formik.errors.timeRange}
                label="Time Range"
                name="timeRange"
                type="number"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <Select
                        error={Boolean(formik.touched.timeUnit && formik.errors.timeUnit)}
                        value={formik.values.timeUnit}
                        label="Unit"
                        onChange={(e) => {
                          formik.setFieldValue("timeUnit", e.target.value);
                        }}
                        onBlur={formik.handleBlur}
                      >
                        <MenuItem value={"minutes"}>minutes</MenuItem>
                        <MenuItem value={"hours"}>hours</MenuItem>
                        <MenuItem value={"days"}>days</MenuItem>
                      </Select>
                    </InputAdornment>
                  ),
                }}
                value={formik.values.timeRange}
              />
            </Grid>
            {getTypeSpecificFilters(formik.values.type)}
          </Grid>
        </CardContent>
        <CardActions
          sx={{
            flexWrap: "wrap",
            m: -1,
          }}
        >
          <Button
            disabled={formik.isSubmitting}
            type="submit"
            sx={{ m: 1 }}
            variant="contained"
            onClick={() => setVisualize(false)}
          >
            Query Data
          </Button>
          <Button
            disabled={formik.isSubmitting && formik.values.type === "events"}
            type="submit"
            sx={{ m: 1 }}
            variant="contained"
            onClick={() => setVisualize(true)}
          >
            View Counts
          </Button>
        </CardActions>
      </Card>
    </form>
  );
};

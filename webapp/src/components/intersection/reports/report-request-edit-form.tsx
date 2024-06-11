import toast from "react-hot-toast";
import * as Yup from "yup";
import { useFormik } from "formik";
import { Dayjs } from "dayjs";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { Button, Card, CardActions, CardContent, Divider, Grid, TextField } from "@mui/material";

type Props = {
  onGenerateReport: ({
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
  }: {
    intersectionId?: number;
    roadRegulatorId?: number;
    startTime: Date;
    endTime: Date;
  }) => void;
  dbIntersectionId?: number;
};

export const ReportRequestEditForm = (props: Props) => {
  const { onGenerateReport, dbIntersectionId } = props;
  const formik = useFormik({
    initialValues: {
      startDate: new Date(Date.now() - 86400000), //yesterday
      endDate: new Date(),
      intersectionId: dbIntersectionId,
      roadRegulatorId: -1,
      submit: null,
    },
    validationSchema: Yup.object({
      startDate: Yup.date().required("Start date is required"),
      endDate: Yup.date()
        .required("End date is required")
        .min(Yup.ref("startDate"), "end date must be after start date"),
      intersectionId: Yup.string().required("Intersection ID is required"),
      // roadRegulatorId: Yup.string().required("Road Regulator ID is required"),
    }),
    onSubmit: async (values, helpers) => {
      try {
        helpers.setStatus({ success: true });
        helpers.setSubmitting(false);
        onGenerateReport({
          intersectionId: values.intersectionId,
          roadRegulatorId: values.roadRegulatorId,
          startTime: values.startDate,
          endTime: values.endDate,
        });
      } catch (err) {
        console.error(err);
        toast.error("Something went wrong!");
        helpers.setStatus({ success: false });
        helpers.setErrors({ submit: err.message });
        helpers.setSubmitting(false);
      }
    },
  });

  return (
    <form onSubmit={formik.handleSubmit}>
      <Card>
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.intersectionId && formik.errors.intersectionId)}
                fullWidth
                label="Intersection ID"
                name="intersectionId"
                onChange={formik.handleChange}
                value={formik.values.intersectionId}
              />
            </Grid>
            {/* <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.roadRegulatorId && formik.errors.roadRegulatorId)}
                fullWidth
                helperText={formik.touched.roadRegulatorId && formik.errors.roadRegulatorId}
                label="Road Regulator ID"
                name="roadRegulatorId"
                onChange={formik.handleChange}
                value={formik.values.roadRegulatorId}
              />
            </Grid> */}
            <Grid item md={4} xs={12}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  renderInput={(props) => (
                    <TextField
                      {...props}
                      error={Boolean(formik.touched.startDate && formik.errors.startDate)}
                      name="startDate"
                      label="Start Date"
                    />
                  )}
                  value={formik.values.startDate}
                  onChange={(e) => formik.setFieldValue("startDate", (e as Dayjs | null)?.toDate(), true)}
                  disableFuture
                />
              </LocalizationProvider>
            </Grid>
            <Grid item md={4} xs={12}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  renderInput={(props) => (
                    <TextField
                      {...props}
                      error={Boolean(formik.touched.endDate && formik.errors.endDate)}
                      name="endDate"
                      label="End Date"
                    />
                  )}
                  value={formik.values.endDate}
                  onChange={(e) => formik.setFieldValue("endDate", (e as Dayjs | null)?.toDate(), true)}
                  disableFuture
                />
              </LocalizationProvider>
            </Grid>
          </Grid>
        </CardContent>
        <CardActions
          sx={{
            flexWrap: "wrap",
            m: -1,
          }}
        >
          <Button disabled={formik.isSubmitting} type="submit" sx={{ m: 1 }} variant="contained">
            Generate Performance Report
          </Button>
        </CardActions>
      </Card>
    </form>
  );
};

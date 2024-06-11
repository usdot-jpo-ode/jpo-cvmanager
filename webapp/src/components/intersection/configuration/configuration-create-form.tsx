import NextLink from "next/link";
import { useRouter } from "next/router";
import PropTypes from "prop-types";
import toast from "react-hot-toast";
import * as Yup from "yup";
import { useFormik } from "formik";
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid, TextField } from "@mui/material";
import { configParamApi } from "../../apis/configuration-param-api";
import { useSession } from "next-auth/react";
import { useDashboardContext } from "../../contexts/dashboard-context";

export const ConfigParamCreateForm = (props) => {
  const { parameter }: { parameter: Config } = props;
  const router = useRouter();
  const { data: session } = useSession();
  const { intersectionId, roadRegulatorId } = useDashboardContext();
  const formik = useFormik({
    initialValues: {
      name: parameter.key,
      unit: parameter.units,
      value: parameter.value,
      description: parameter.description,
      submit: null,
    },
    validationSchema: Yup.object({
      name: Yup.string(),
      value: Yup.string().required("New value is required"),
    }),
    onSubmit: async (values, helpers) => {
      if (!session?.accessToken || intersectionId == -1) {
        console.error(
          "Did not attempt to create configuration param. Access token:",
          session?.accessToken,
          "Intersection ID:",
          intersectionId
        );
        return;
      }
      try {
        const updatedConfig: IntersectionConfig = {
          ...parameter,
          value: values.value,
          intersectionID: intersectionId,
          roadRegulatorID: roadRegulatorId,
          rsuID: "",
        };
        await configParamApi.updateIntersectionParameter(session?.accessToken, values.name, updatedConfig);
        helpers.setStatus({ success: true });
        helpers.setSubmitting(false);
        router
          .push({
            pathname: "/configuration",
            query: { returnUrl: router.asPath },
          })
          .catch(console.error);
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
        <CardHeader title="Override Configuration Parameter" />
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.name && formik.errors.name)}
                fullWidth
                helperText={formik.touched.name && formik.errors.name}
                label="Parameter Name"
                name="name"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.name}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.unit && formik.errors.unit)}
                fullWidth
                helperText={formik.touched.unit && formik.errors.unit}
                label="Unit"
                name="unit"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.unit}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.value && formik.errors.value)}
                fullWidth
                // helperText={formik.touched.value && formik.errors.value}
                label="Value"
                name="value"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                required
                value={formik.values.value}
              />
            </Grid>
            <Grid item md={12} xs={12}>
              <TextField
                error={Boolean(formik.touched.description && formik.errors.description)}
                fullWidth
                helperText={formik.touched.description && formik.errors.description}
                label="Description"
                name="description"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                multiline={true}
                disabled
                value={formik.values.description}
              />
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
            Overrride
          </Button>
          <NextLink href="/configuration" passHref>
            <Button
              component="a"
              disabled={formik.isSubmitting}
              sx={{
                m: 1,
                mr: "auto",
              }}
              variant="outlined"
            >
              Cancel
            </Button>
          </NextLink>
        </CardActions>
      </Card>
    </form>
  );
};

ConfigParamCreateForm.propTypes = {
  parameter: PropTypes.object.isRequired,
};

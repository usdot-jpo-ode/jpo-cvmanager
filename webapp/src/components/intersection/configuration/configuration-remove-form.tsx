import NextLink from "next/link";
import { useRouter } from "next/router";
import PropTypes from "prop-types";
import toast from "react-hot-toast";
import * as Yup from "yup";
import { useFormik } from "formik";
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid, TextField } from "@mui/material";
import { configParamApi } from "../../apis/configuration-param-api";
import { useDashboardContext } from "../../contexts/dashboard-context";
import { useSession } from "next-auth/react";

export const ConfigParamRemoveForm = (props) => {
  const { parameter, defaultParameter, ...other } = props;
  const router = useRouter();
  const { data: session } = useSession();
  const { intersectionId } = useDashboardContext();
  const formik = useFormik({
    initialValues: {
      name: parameter.key,
      unit: parameter.unit,
      value: parameter.value,
      defaultValue: defaultParameter.value,
      description: parameter.description,
      submit: null,
    },
    validationSchema: Yup.object({}),
    onSubmit: async (values, helpers) => {
      if (!session?.accessToken || intersectionId == -1) {
        console.error(
          "Did not attempt to remove configuration parameter. Access token:",
          session?.accessToken,
          "Intersection ID:",
          intersectionId
        );
        return;
      }
      try {
        await configParamApi.removeOverriddenParameter(session?.accessToken, values.name, parameter);
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
    <form onSubmit={formik.handleSubmit} {...other}>
      <Card>
        <CardHeader title="Edit Configuration Parameter" />
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.name && formik.errors.name)}
                fullWidth
                // helperText={formik.touched.name && formik.errors.name}
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
                // helperText={formik.touched.unit && formik.errors.unit}
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
                label="Overriden Value"
                name="value"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.value}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.defaultValue && formik.errors.defaultValue)}
                fullWidth
                // helperText={formik.touched.defaultValue && formik.errors.defaultValue}
                label="Default Value"
                name="defaultValue"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.defaultValue}
              />
            </Grid>
            <Grid item md={12} xs={12}>
              <TextField
                error={Boolean(formik.touched.description && formik.errors.description)}
                fullWidth
                // helperText={formik.touched.description && formik.errors.description}
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
            Remove Parameter Override
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

ConfigParamRemoveForm.propTypes = {
  parameter: PropTypes.object.isRequired,
  defaultParameter: PropTypes.object.isRequired,
};

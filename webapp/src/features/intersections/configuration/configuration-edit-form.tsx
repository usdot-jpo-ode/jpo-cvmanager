import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Chip,
  Divider,
  Grid,
  TextField,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import {
  useUpdateDefaultParameterMutation,
  useUpdateIntersectionParameterMutation,
} from '../../api/intersectionConfigParamApiSlice'

export const ConfigParamEditForm = (props) => {
  const { parameter }: { parameter: DefaultConfig | IntersectionConfig } = props
  const navigate = useNavigate()

  const [updateIntersectionParameter, {}] = useUpdateIntersectionParameterMutation()
  const [updateDefaultParameter, {}] = useUpdateDefaultParameterMutation()

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
      value: Yup.string().required('New value is required'),
    }),
    onSubmit: async (values, helpers) => {
      try {
        const valueType = parameter.type
        let typedValue: number | string | boolean | null = values.value
        switch (valueType) {
          case 'java.lang.Integer':
            try {
              parseInt(values.value)
              typedValue = values.value.toString()
              break
            } catch (e) {
              toast.error('Invalid integer value')
              helpers.setStatus({ success: false })
              helpers.setSubmitting(false)
              return
            }
          case 'java.lang.Boolean':
            typedValue = values.value == 'true'
            break
          case 'java.lang.Long':
            try {
              parseInt(values.value)
              typedValue = values.value.toString()
              break
            } catch (e) {
              toast.error('Invalid long value')
              helpers.setStatus({ success: false })
              helpers.setSubmitting(false)
              return
            }
          case 'java.lang.Double':
            try {
              Number(values.value)
              typedValue = values.value.toString()
              break
            } catch (e) {
              toast.error('Invalid double value')
              helpers.setStatus({ success: false })
              helpers.setSubmitting(false)
              return
            }
          case 'java.lang.String':
            break
          default:
            break
        }
        if ('intersectionID' in parameter) {
          const updatedConfig: IntersectionConfig = {
            ...(parameter as IntersectionConfig),
            value: typedValue,
          }
          await updateIntersectionParameter(updatedConfig)
        } else {
          const updatedConfig = {
            ...parameter,
            value: typedValue,
          }
          await updateDefaultParameter(updatedConfig)
        }
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        navigate('../')
      } catch (err) {
        console.error(err)
        toast.error('Something went wrong!')
        helpers.setStatus({ success: false })
        helpers.setErrors({ submit: err.message })
        helpers.setSubmitting(false)
      }
    },
  })

  return (
    <form onSubmit={formik.handleSubmit}>
      <Card>
        <CardHeader
          title={
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="h6">Edit Configuration Parameter</Typography>
              {'intersectionID' in parameter && (
                <Chip color="secondary" sx={{ ml: 2 }} label={<Typography>Overriden</Typography>} size="small" />
              )}
            </Box>
          }
        />
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.name && formik.errors.name)}
                fullWidth
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
            flexWrap: 'wrap',
            m: -1,
          }}
        >
          <Button disabled={formik.isSubmitting} type="submit" sx={{ m: 1 }} variant="contained">
            Update
          </Button>

          <Button
            component="a"
            disabled={formik.isSubmitting}
            sx={{
              m: 1,
              mr: 'auto',
            }}
            variant="outlined"
            onClick={() => navigate(`../`)}
          >
            Cancel
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

ConfigParamEditForm.propTypes = {
  parameter: PropTypes.object.isRequired,
}

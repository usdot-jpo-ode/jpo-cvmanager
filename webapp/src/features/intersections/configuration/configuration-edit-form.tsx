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
  Grid2,
  TextField,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import {
  useUpdateDefaultParameterMutation,
  useUpdateIntersectionParameterMutation,
} from '../../api/intersectionApiSlice'

export const ConfigParamEditForm = (props) => {
  const { parameter }: { parameter: DefaultConfig | IntersectionConfig } = props
  const navigate = useNavigate()

  const [updateIntersectionParameter, {}] = useUpdateIntersectionParameterMutation()
  const [updateDefaultParameter, {}] = useUpdateDefaultParameterMutation()

  console.log('ConfigParamEditForm', parameter)

  const formik = useFormik({
    initialValues: {
      value: parameter.value,
      submit: null,
    },
    validationSchema: Yup.object({
      value: Yup.string()
        .required('New value is required')
        .test('not-same-as-parameter', 'New value must be different from the previous value', function (value) {
          return value?.toString() !== parameter.value?.toString()
        }),
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
        navigate('/dashboard/intersectionDashboard/configuration')
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
                <Chip color="secondary" sx={{ ml: 2 }} label={<Typography>Overridden</Typography>} size="small" />
              )}
            </Box>
          }
        />
        <Divider />
        <CardContent>
          <Grid2 container spacing={3}>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Parameter Name" disabled value={parameter.key} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Unit" disabled value={parameter.units} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Initial Value" disabled value={parameter.value} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.value && formik.errors.value)}
                fullWidth
                helperText={formik.touched.value && formik.errors.value}
                label="New Value"
                name="value"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                required
                value={formik.values.value}
              />
            </Grid2>
            <Grid2 size={{ md: 12, xs: 12 }}>
              <TextField
                fullWidth
                label="Description"
                name="description"
                multiline={true}
                disabled
                value={parameter.description}
              />
            </Grid2>
          </Grid2>
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
            onClick={() => navigate(`/dashboard/intersectionDashboard/configuration`)}
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

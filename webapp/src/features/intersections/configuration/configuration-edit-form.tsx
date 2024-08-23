import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid, TextField } from '@mui/material'
import { configParamApi } from '../../../apis/intersections/configuration-param-api'
import { selectToken } from '../../../generalSlices/userSlice'
import { useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'

export const ConfigParamEditForm = (props) => {
  const { parameter }: { parameter: DefaultConfig | IntersectionConfig } = props
  const token = useSelector(selectToken)
  const navigate = useNavigate()
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
          await configParamApi.updateIntersectionParameter(token, values.name, updatedConfig)
        } else {
          const updatedConfig = {
            ...parameter,
            value: typedValue,
          }
          await configParamApi.updateDefaultParameter(token, values.name, updatedConfig)
        }
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        navigate('/configuration')
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
            onClick={() => navigate(`/configuration`)}
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

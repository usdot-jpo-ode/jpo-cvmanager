import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid2, TextField } from '@mui/material'
import { configParamApi } from '../../../apis/intersections/configuration-param-api'
import { useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'

export const ConfigParamRemoveForm = (props) => {
  const { parameter, defaultParameter, ...other } = props
  const navigate = useNavigate()
  const token = useSelector(selectToken)
  const intersectionId = useSelector(selectSelectedIntersectionId)
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
      if (intersectionId == -1) {
        console.error('Did not attempt to remove configuration parameter. Intersection ID:', intersectionId)
        return
      }
      try {
        await configParamApi.removeOverriddenParameter(token, values.name, parameter)
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        navigate(`/configuration`)
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
    <form onSubmit={formik.handleSubmit} {...other}>
      <Card>
        <CardHeader title="Edit Configuration Parameter" />
        <Divider />
        <CardContent>
          <Grid2 container spacing={3}>
            <Grid2 size={{ md: 6, xs: 12 }}>
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
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
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
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.value && formik.errors.value)}
                fullWidth
                label="Overriden Value"
                name="value"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.value}
              />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.defaultValue && formik.errors.defaultValue)}
                fullWidth
                label="Default Value"
                name="defaultValue"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.defaultValue}
              />
            </Grid2>
            <Grid2 size={{ md: 12, xs: 12 }}>
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
            Remove Parameter Override
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

ConfigParamRemoveForm.propTypes = {
  parameter: PropTypes.object.isRequired,
  defaultParameter: PropTypes.object.isRequired,
}

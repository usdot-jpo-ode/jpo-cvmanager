import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid, TextField } from '@mui/material'
import { configParamApi } from '../../../apis/intersections/configuration-param-api'
import { useNavigate } from 'react-router-dom'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'

export const ConfigParamCreateForm = (props) => {
  const navigate = useNavigate()
  const { parameter }: { parameter: Config } = props
  const token = useSelector(selectToken)
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)
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
      if (intersectionId == -1) {
        console.error('Did not attempt to create configuration param. Intersection ID:', intersectionId)
        return
      }
      try {
        const updatedConfig: IntersectionConfig = {
          ...parameter,
          value: values.value,
          intersectionID: intersectionId,
          roadRegulatorID: roadRegulatorId,
          rsuID: '',
        }
        await configParamApi.updateIntersectionParameter(token, values.name, updatedConfig)
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
            flexWrap: 'wrap',
            m: -1,
          }}
        >
          <Button disabled={formik.isSubmitting} type="submit" sx={{ m: 1 }} variant="contained">
            Overrride
          </Button>
          <Button
            onClick={() => navigate(`/configuration`)}
            component="a"
            disabled={formik.isSubmitting}
            sx={{
              m: 1,
              mr: 'auto',
            }}
            variant="outlined"
          >
            Cancel
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

ConfigParamCreateForm.propTypes = {
  parameter: PropTypes.object.isRequired,
}

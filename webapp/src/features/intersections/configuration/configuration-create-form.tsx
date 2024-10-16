import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid2, TextField } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { useAppSelector } from '../../../hooks'
import { useUpdateIntersectionParameterMutation } from '../../api/intersectionApiSlice'

export const ConfigParamCreateForm = (props) => {
  const navigate = useNavigate()
  const { parameter }: { parameter: Config } = props
  const intersectionId = useAppSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useAppSelector(selectSelectedRoadRegulatorId)

  const [updateIntersectionParameter, {}] = useUpdateIntersectionParameterMutation()

  const formik = useFormik({
    initialValues: {
      value: parameter.value,
      submit: null,
    },
    validationSchema: Yup.object({
      value: Yup.string()
        .required('New value is required')
        .test('not-same-as-parameter', 'New value must be different from the default value', function (value) {
          return value?.toString() !== parameter.value?.toString()
        }),
    }),
    onSubmit: async (values, helpers) => {
      try {
        const updatedConfig: IntersectionConfig = {
          ...parameter,
          value: values.value,
          intersectionID: intersectionId,
          roadRegulatorID: roadRegulatorId,
          rsuID: '',
        }
        await updateIntersectionParameter(updatedConfig)
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        navigate(`../`)
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
          <Grid2 container spacing={3}>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Parameter Name" disabled value={parameter.key} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Unit" disabled value={parameter.units} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Default Value" disabled value={parameter.value} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.value && formik.errors.value)}
                fullWidth
                helperText={formik.touched.value && formik.errors.value}
                label="Override Value"
                name="value"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                required
                value={formik.values.value}
              />
            </Grid2>
            <Grid2 size={{ md: 12, xs: 12 }}>
              <TextField fullWidth label="Description" multiline={true} disabled value={parameter.description} />
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
            Overrride
          </Button>
          <Button
            onClick={() => navigate(`/dashboard/intersectionDashboard/configuration`)}
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

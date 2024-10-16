import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { Button, Card, CardActions, CardContent, CardHeader, Divider, Grid2, TextField } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useAppSelector } from '../../../hooks'
import { useRemoveOverriddenParameterMutation } from '../../api/intersectionApiSlice'

export const ConfigParamRemoveForm = (props) => {
  const { parameter, defaultParameter, ...other } = props
  const navigate = useNavigate()
  const intersectionId = useAppSelector(selectSelectedIntersectionId)

  const [removeOverriddenParameter, {}] = useRemoveOverriddenParameterMutation()

  const formik = useFormik({
    initialValues: {
      value: parameter.value,
      submit: null,
    },
    validationSchema: Yup.object({}),
    onSubmit: async (values, helpers) => {
      try {
        await removeOverriddenParameter(parameter)
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        navigate(`/dashboard/intersectionDashboard/configuration`)
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
              <TextField fullWidth label="Parameter Name" disabled value={parameter.key} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Unit" disabled value={parameter.units} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Overridden Value" disabled value={formik.values.value} />
            </Grid2>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField fullWidth label="Default Value" disabled value={defaultParameter.value} />
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
            onClick={() => navigate(`/dashboard/intersectionDashboard/configuration`)}
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

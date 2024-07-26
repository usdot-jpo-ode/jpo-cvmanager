import React from 'react'
import PropTypes from 'prop-types'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import {
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Divider,
  Grid,
  TextField,
  Typography,
  Select,
  MenuItem,
} from '@mui/material'
import userManagementApi from '../../../apis/intersections/user-management-api'
import { useSelector } from 'react-redux'
import { selectToken } from '../../../generalSlices/userSlice'
import { useNavigate } from 'react-router-dom'

export const UserCreateForm = (props) => {
  const navigate = useNavigate()
  const token = useSelector(selectToken)
  const { user }: { user: User } = props
  const formik = useFormik({
    initialValues: {
      email: user.email,
      first_name: user.first_name,
      last_name: user.last_name,
      role: user.role,
      submit: null,
    },
    validationSchema: Yup.object({
      email: Yup.string().email('Must be a valid email').max(255).required('Username is required'),
      first_name: Yup.string(),
      last_name: Yup.string(),
    }),
    onSubmit: async (values, helpers) => {
      try {
        await userManagementApi.createUser({
          token: token,
          email: values.email,
          first_name: values.first_name,
          last_name: values.last_name,
          role: values.role,
        })
        navigate('/users')
      } catch (err) {
        console.error(err)
        helpers.setStatus({ success: false })
        helpers.setErrors({ submit: err.message })
        helpers.setSubmitting(false)
      }
    },
  })

  return (
    <form onSubmit={formik.handleSubmit}>
      <Card>
        <CardHeader title="Create User" />
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.email && formik.errors.email)}
                fullWidth
                helperText={formik.touched.email && formik.errors.email}
                label="Email"
                name="email"
                type="email"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                value={formik.values.email}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.first_name && formik.errors.first_name)}
                fullWidth
                helperText={formik.touched.first_name && formik.errors.first_name}
                label="First Name"
                name="first_name"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                value={formik.values.first_name}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.last_name && formik.errors.last_name)}
                fullWidth
                helperText={formik.touched.last_name && formik.errors.last_name}
                label="Last Name"
                name="last_name"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                value={formik.values.last_name}
              />
            </Grid>
            <Grid item md={12} xs={12}>
              <Typography>Role</Typography>
              <Select
                value={formik.values.role}
                label="Role"
                name="role"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
              >
                <MenuItem value={'ADMIN'}>Admin</MenuItem>
                <MenuItem value={'USER'}>User</MenuItem>
              </Select>
            </Grid>
          </Grid>
          <Grid item md={12} xs={12} mt={3}>
            <Typography>
              {' '}
              After creating the user, an email will be send to their email address with instructions to set up a
              password.
            </Typography>
          </Grid>
        </CardContent>
        <CardActions
          sx={{
            flexWrap: 'wrap',
            m: -1,
          }}
        >
          <Button disabled={formik.isSubmitting} type="submit" sx={{ m: 1 }} variant="contained">
            Create User
          </Button>
          <Button
            component="a"
            disabled={formik.isSubmitting}
            sx={{
              m: 1,
              mr: 'auto',
            }}
            variant="outlined"
            onClick={() => navigate(`/users`)}
          >
            Cancel
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

UserCreateForm.propTypes = {
  user: PropTypes.object,
}

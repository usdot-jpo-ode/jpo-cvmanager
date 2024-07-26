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
  Divider,
  Grid,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material'
import keycloakApi from '../../../apis/intersections/keycloak-api'
import { useSelector } from 'react-redux'
import { selectToken } from '../../../generalSlices/userSlice'
import { useNavigate } from 'react-router-dom'

export const UserEditForm = (props: { user: User }) => {
  const { user, ...other } = props
  const navigate = useNavigate()
  const token = useSelector(selectToken)

  const initialValues = {
    email: user.email || '',
    firstName: user.first_name || '',
    lastName: user.last_name || '',
    role: user.role || 'USER',
    submit: null,
  }

  const removeUser = (userId: string) => {
    keycloakApi.removeUser({ token: token, id: userId })
    navigate('/users')
  }

  const formik = useFormik({
    initialValues: initialValues,
    validationSchema: Yup.object({
      email: Yup.string().email('Must be a valid email').max(255).required('Email is required'),
      firstName: Yup.string().max(50),
      lastName: Yup.string().max(50),
      role: Yup.string().max(50),
    }),
    onSubmit: async (values, helpers) => {
      try {
        if (values.role !== initialValues.role) {
          keycloakApi.removeUserFromGroup({ token: token, id: user.id, role: initialValues.role })
          keycloakApi.addUserToGroup({ token: token, id: user.id, role: values.role })
        }
        keycloakApi.updateUserInfo({
          token: token,
          id: user.id,
          email: values.email == initialValues.email ? undefined : values.email,
          first_name: values.firstName == initialValues.firstName ? undefined : values.firstName,
          last_name: values.lastName == initialValues.lastName ? undefined : values.lastName,
        })
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        toast.success('User Profile Updated!')
        navigate('/users')
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
        <CardHeader title="Edit User" />
        <Divider />
        <CardContent>
          <Grid container spacing={3}>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.firstName && formik.errors.firstName)}
                fullWidth
                helperText={formik.touched.firstName && formik.errors.firstName}
                label="First name"
                name="firstName"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                value={formik.values.firstName}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.lastName && formik.errors.lastName)}
                fullWidth
                helperText={formik.touched.lastName && formik.errors.lastName}
                label="Last name"
                name="lastName"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                value={formik.values.lastName}
              />
            </Grid>
            <Grid item md={6} xs={12}>
              <TextField
                error={Boolean(formik.touched.email && formik.errors.email)}
                fullWidth
                helperText={formik.touched.email && formik.errors.email}
                label="Email address"
                name="email"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                disabled
                value={formik.values.email}
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
          <Divider sx={{ my: 3 }} />
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
            onClick={() => navigate('/users')}
          >
            Cancel
          </Button>
          <Button
            color="error"
            disabled={formik.isSubmitting}
            onClick={(event) => {
              removeUser(user.id)
            }}
          >
            Delete user
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

UserEditForm.propTypes = {
  user: PropTypes.object.isRequired,
}

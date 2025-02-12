import React, { useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  // actions
  addOrg,
} from './adminAddOrganizationSlice'
import { useDispatch } from 'react-redux'
import toast from 'react-hot-toast'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'

import Dialog from '@mui/material/Dialog'
import CloseIcon from '@mui/icons-material/Close'
import {
  Button,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  OutlinedInput,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'

export type AdminAddOrgForm = {
  name: string
  email: string
}

const AdminAddOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const notifySuccess = (message: string) => toast.success(message)
  const notifyError = (message: string) => toast.error(message)
  const [open, setOpen] = useState(true)
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AdminAddOrgForm>()

  const onSubmit = (data: AdminAddOrgForm) => {
    dispatch(addOrg({ json: data })).then((data: any) => {
      data.payload.success
        ? notifySuccess(data.payload.message)
        : notifyError('Failed to add organization due to error: ' + data.payload.message)
    })
    setOpen(false)
    navigate('/dashboard/admin/organizations')
  }

  return (
    <Dialog open={open}>
      <DialogTitle>Add Organization</DialogTitle>
      <IconButton
        aria-label="close"
        onClick={() => {
          setOpen(false)
          navigate('..')
        }}
        sx={(theme) => ({
          position: 'absolute',
          right: 8,
          top: 8,
          color: theme.palette.text.primary,
        })}
      >
        <CloseIcon />
      </IconButton>
      <DialogContent>
        <Form
          id="add-organization-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="name">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="org-name">Organization Name</InputLabel>
              <OutlinedInput
                id="org-name"
                type="text"
                {...register('name', {
                  required: 'Please enter the organization name',
                })}
                label="Organization Name"
              />
              {errors.name && (
                <p className="errorMsg" role="alert">
                  {errors.name.message}
                </p>
              )}
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="org-email">Organization Email</InputLabel>
              <OutlinedInput id="org-email" type="text" label="Organization Email" {...register('email')} />
            </FormControl>
          </Form.Group>
        </Form>
      </DialogContent>
      <DialogActions sx={{ padding: '20px' }}>
        <Button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/organizations')
          }}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
        >
          Cancel
        </Button>
        <Button
          form="add-organization-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
        >
          Add Organization
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddOrganization

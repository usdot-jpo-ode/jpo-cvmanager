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
import { Button, DialogActions, DialogContent, FormControl, TextField } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import '../../styles/fonts/museo-slab.css'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

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
      <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
        <SideBarHeader
          onClick={() => {
            setOpen(false)
            navigate('..')
          }}
          title="Add Organization"
        />
        <Form id="add-organization-form" onSubmit={handleSubmit(onSubmit)}>
          <Form.Group controlId="name">
            <FormControl fullWidth margin="normal">
              <TextField
                label="Organization Name"
                placeholder="Enter Organization Name"
                color="info"
                variant="outlined"
                required
                {...register('name', {
                  required: 'Please enter the organization name',
                })}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
              {errors.name && (
                <p className="errorMsg" role="alert">
                  {errors.name.message}
                </p>
              )}
            </FormControl>
            <FormControl fullWidth margin="normal">
              <TextField
                label="Organization Email"
                placeholder="Enter Organization Email"
                color="info"
                variant="outlined"
                required
                {...register('email')}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
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
          className="museo-slab capital-case"
        >
          Cancel
        </Button>
        <Button
          form="add-organization-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
          className="museo-slab capital-case"
        >
          Add Organization
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddOrganization

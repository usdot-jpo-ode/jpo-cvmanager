import React, { useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  // actions
  addOrg,
} from './adminAddOrganizationSlice'
import { useSelector, useDispatch } from 'react-redux'
import toast from 'react-hot-toast'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'

import Dialog from '@mui/material/Dialog'
import { DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { AdminButton } from '../../styles/components/AdminButton'

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
      <DialogContent>
        <Form
          id="add-organization-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="name">
            <Form.Label>Organization Name *</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter organization name"
              {...register('name', {
                required: 'Please enter the organization name',
              })}
            />
            <Form.Label>Organization Email</Form.Label>
            <Form.Control type="text" placeholder="Enter organization email" {...register('email')} />
            {errors.name && (
              <p className="errorMsg" role="alert">
                {errors.name.message}
              </p>
            )}
          </Form.Group>
        </Form>
      </DialogContent>
      <DialogActions>
        <AdminButton
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/organizations')
          }}
        >
          Close
        </AdminButton>
        <AdminButton form="add-organization-form" type="submit">
          Add Organization
        </AdminButton>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddOrganization

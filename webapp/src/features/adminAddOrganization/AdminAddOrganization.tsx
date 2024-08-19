import React from 'react'
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

export type AdminAddOrgForm = {
  name: string
  email: string
}

const AdminAddOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const notifySuccess = (message: string) => toast.success(message)
  const notifyError = (message: string) => toast.error(message)
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
  }

  return (
    <div>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Form.Group className="mb-3" controlId="name">
          <Form.Label>Organization Name</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter organization name"
            {...register('name', {
              required: 'Please enter the organization name',
            })}
          />
          <Form.Label>Organization Email</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter organization email"
            {...register('email', {
              required: 'Please enter the organization email',
            })}
          />
          {errors.name && (
            <p className="errorMsg" role="alert">
              {errors.name.message}
            </p>
          )}
        </Form.Group>

        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">
            Add Organization
          </button>
        </div>
      </Form>
    </div>
  )
}

export default AdminAddOrganization

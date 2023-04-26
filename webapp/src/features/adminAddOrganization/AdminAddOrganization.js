import React from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,

  // actions
  addOrg,
} from './adminAddOrganizationSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'

const AdminAddOrganization = (props) => {
  const dispatch = useDispatch()
  const successMsg = useSelector(selectSuccessMsg)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  const onSubmit = (data) => {
    dispatch(addOrg({ json: data, reset, updateOrgData: props.updateOrganizationData }))
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
          {errors.name && <p className="errorMsg">{errors.name.message}</p>}
        </Form.Group>

        {successMsg && <p className="success-msg">{successMsg}</p>}
        {errorState && <p className="error-msg">Failed to add organization due to error: {errorMsg}</p>}
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

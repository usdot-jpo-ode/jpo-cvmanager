import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,

  // actions
  updateStates,
  editOrganization,
} from './adminEditOrganizationSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { adminOrgPatch, selectSelectedOrg } from '../adminOrganizationTab/adminOrganizationTabSlice'

const AdminEditOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const successMsg = useSelector(selectSuccessMsg)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const selectedOrg = useSelector(selectSelectedOrg)
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<adminOrgPatch>({
    defaultValues: {
      name: '',
    },
  })

  useEffect(() => {
    updateStates(setValue, selectedOrg.name)
  }, [setValue, selectedOrg.name])

  const onSubmit = (data: adminOrgPatch) => {
    dispatch(editOrganization({ json: data, setValue, selectedOrg: selectedOrg.name }))
  }

  return (
    <div>
      <Form onSubmit={handleSubmit((data) => onSubmit(data))}>
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
        {errorState && <p className="error-msg">Failed to apply changes to organization due to error: {errorMsg}</p>}
        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">
            Apply Changes
          </button>
        </div>
      </Form>
    </div>
  )
}

export default AdminEditOrganization

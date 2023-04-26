import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import {
  selectSuccessMsg,
  selectSelectedOrganizationNames,
  selectSelectedOrganizations,
  selectOrganizationNames,
  selectAvailableRoles,
  selectErrorState,
  selectErrorMsg,
  selectSubmitAttempt,
  selectApiData,
  setSelectedRole,

  // actions
  getUserData,
  submitForm,
  updateOrganizations,
} from './adminEditUserSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'

const AdminEditUser = (props) => {
  const dispatch = useDispatch()
  const successMsg = useSelector(selectSuccessMsg)
  const selectedOrganizationNames = useSelector(selectSelectedOrganizationNames)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const organizationNames = useSelector(selectOrganizationNames)
  const availableRoles = useSelector(selectAvailableRoles)
  const apiData = useSelector(selectApiData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const submitAttempt = useSelector(selectSubmitAttempt)
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm({
    defaultValues: {
      orig_email: '',
      email: '',
      first_name: '',
      last_name: '',
      super_user: '',
      organizations_to_add: [],
      organizations_to_modify: [],
      organizations_to_remove: [],
    },
  })

  const { userData } = props

  useEffect(() => {
    dispatch(getUserData(userData.email))
  }, [userData])

  useEffect(() => {
    if (apiData && Object.keys(apiData).length !== 0) {
      setValue('orig_email', apiData.user_data.email)
      setValue('email', apiData.user_data.email)
      setValue('first_name', apiData.user_data.first_name)
      setValue('last_name', apiData.user_data.last_name)
      setValue('super_user', apiData.user_data.super_user)
    }
  }, [apiData])

  const onSubmit = (data) => {
    dispatch(submitForm({ data, updateUserData: props.updateUserData }))
  }

  return (
    <div>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Form.Group className="mb-3" controlId="email">
          <Form.Label>Email</Form.Label>
          <Form.Control
            type="email"
            placeholder="Enter user email"
            {...register('email', {
              required: 'Please enter user email',
              pattern: {
                value: /^[^@ ]+@[^@ ]+\.[^@ .]{2,}$/,
                message: 'Please enter a valid email',
              },
            })}
          />
          {errors.email && <p className="errorMsg">{errors.email.message}</p>}
        </Form.Group>

        <Form.Group className="mb-3" controlId="first_name">
          <Form.Label>First Name</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter user's first name"
            {...register('first_name', {
              required: "Please enter user's first name",
            })}
          />
          {errors.first_name && <p className="errorMsg">{errors.first_name.message}</p>}
        </Form.Group>

        <Form.Group className="mb-3" controlId="last_name">
          <Form.Label>Last Name</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter user's last name"
            {...register('last_name', {
              required: "Please enter user's last name",
            })}
          />
          {errors.last_name && <p className="errorMsg">{errors.last_name.message}</p>}
        </Form.Group>

        <Form.Group className="mb-3" controlId="super_user">
          <Form.Check label=" Super User" type="switch" {...register('super_user')} />
        </Form.Group>

        <Form.Group className="mb-3" controlId="organizations">
          <Form.Label>Organizations</Form.Label>
          <Multiselect
            className="form-multiselect"
            dataKey="name"
            textField="name"
            data={organizationNames}
            placeholder="Select organizations"
            value={selectedOrganizationNames}
            onChange={(value) => {
              dispatch(updateOrganizations(value))
            }}
          />
        </Form.Group>

        {selectedOrganizations.length > 0 && (
          <Form.Group className="mb-3" controlId="roles">
            <Form.Label>Roles</Form.Label>
            <p className="spacer" />
            {selectedOrganizations.map((organization) => {
              let role = { role: organization.role }

              return (
                <Form.Group className="mb-3" controlId={organization.id}>
                  <Form.Label>{organization.name}</Form.Label>
                  <DropdownList
                    className="form-dropdown"
                    dataKey="role"
                    textField="role"
                    data={availableRoles}
                    value={role}
                    onChange={(value) => {
                      dispatch(setSelectedRole({ ...organization, role: value.role }))
                    }}
                  />
                </Form.Group>
              )
            })}
          </Form.Group>
        )}

        {selectedOrganizations.length === 0 && submitAttempt && (
          <p className="error-msg">Must select at least one organization</p>
        )}

        {successMsg && <p className="success-msg">{successMsg}</p>}
        {errorState && <p className="error-msg">Failed to apply changes due to error: {errorMsg}</p>}
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

export default AdminEditUser

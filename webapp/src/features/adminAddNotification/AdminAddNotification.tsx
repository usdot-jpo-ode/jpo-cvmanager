import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { DropdownList } from 'react-widgets'
import {
  selectSuccessMsg,
  selectSelectedType,
  selectAvailableTypes,
  selectApiData,
  selectErrorState,
  selectErrorMsg,
  selectSubmitAttempt,

  // actions
  submitForm,
  AdminNotificationForm,
  setSelectedType,
  updateEmailTypesApiData,
  getNotificationData,
} from './adminAddNotificationSlice'
import { selectEmail } from '../../generalSlices/userSlice'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { useAppDispatch, useAppSelector } from '../../hooks'

const AdminAddNotification = () => {
  const dispatch = useAppDispatch()
  const successMsg = useAppSelector(selectSuccessMsg)
  const apiData = useAppSelector(selectApiData)
  const errorState = useAppSelector(selectErrorState)
  const errorMsg = useAppSelector(selectErrorMsg)
  const submitAttempt = useAppSelector(selectSubmitAttempt)
  const selectedType = useAppSelector(selectSelectedType)
  const availableTypes = useAppSelector(selectAvailableTypes)
  const userEmail = useAppSelector(selectEmail)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AdminNotificationForm>()

  useEffect(() => {
    dispatch(getNotificationData())
  }, [dispatch])

  useEffect(() => {
    dispatch(updateEmailTypesApiData())
  }, [apiData, dispatch])

  const onSubmit = (data: AdminNotificationForm) => {
    data.email = userEmail
    dispatch(submitForm({ data, reset }))
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center' }}>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Form.Group className="mb-3" controlId="email">
          <Form.Label>Email</Form.Label>
          <br />
          <p style={{ color: 'white' }}>{userEmail}</p>
        </Form.Group>

        <Form.Group className="mb-3" controlId="email_type">
          <Form.Label>Email Notification</Form.Label>
          <DropdownList
            className="form-dropdown"
            dataKey="type"
            textField="type"
            placeholder="Select email notification (Required)"
            data={availableTypes}
            value={selectedType}
            onChange={(value) => {
              dispatch(setSelectedType(value))
            }}
          />
        </Form.Group>

        {selectedType.type === '' && submitAttempt && (
          <p className="error-msg" role="alert">
            Must select at least one email notification type
          </p>
        )}

        {successMsg && (
          <p className="success-msg" role="status">
            {successMsg}
          </p>
        )}
        {errorState && (
          <p className="error-msg" role="alert">
            Failed to add email notification due to error: {errorMsg}
          </p>
        )}
        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">
            Add Email Notification
          </button>
        </div>
      </Form>
    </div>
  )
}

export default AdminAddNotification

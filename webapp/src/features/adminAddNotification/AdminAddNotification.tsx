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
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminButton } from '../../styles/components/AdminButton'

const AdminAddNotification = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const successMsg = useSelector(selectSuccessMsg)
  const apiData = useSelector(selectApiData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const submitAttempt = useSelector(selectSubmitAttempt)
  const selectedType = useSelector(selectSelectedType)
  const availableTypes = useSelector(selectAvailableTypes)
  const userEmail = useSelector(selectEmail)
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
          <AdminButton type="submit">Add Email Notification</AdminButton>
        </div>
      </Form>
    </div>
  )
}

export default AdminAddNotification

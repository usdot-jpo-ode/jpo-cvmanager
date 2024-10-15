import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { DropdownList } from 'react-widgets'
import {
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,
  selectSubmitAttempt,
  selectApiData,
  setSelectedType,

  // actions
  submitForm,
  updateEmailTypesApiData,
  getNotificationData,
  selectSelectedType,
  selectAvailableTypes,
} from './adminEditNotificationSlice'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { Link, useParams } from 'react-router-dom'
import { selectEditNotificationRowData, selectTableData } from '../adminNotificationTab/adminNotificationTabSlice'
import { AdminNotificationForm } from '../adminAddNotification/adminAddNotificationSlice'
import { selectEmail } from '../../generalSlices/userSlice'
import { useAppDispatch, useAppSelector } from '../../hooks'

const AdminEditNotification = () => {
  const dispatch = useAppDispatch()
  const successMsg = useAppSelector(selectSuccessMsg)
  const apiData = useAppSelector(selectApiData)
  const errorState = useAppSelector(selectErrorState)
  const errorMsg = useAppSelector(selectErrorMsg)
  const submitAttempt = useAppSelector(selectSubmitAttempt)
  const selectedType = useAppSelector(selectSelectedType)
  const availableTypes = useAppSelector(selectAvailableTypes)
  const notificationEditTableData = useAppSelector(selectEditNotificationRowData)
  const userEmail = useAppSelector(selectEmail)
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AdminNotificationForm>()

  const { email } = useParams<{ email: string }>()

  useEffect(() => {
    dispatch(getNotificationData())
    dispatch(setSelectedType({ type: '' }))
  }, [dispatch])

  useEffect(() => {
    dispatch(updateEmailTypesApiData())
  }, [apiData, dispatch])

  const onSubmit = (data: AdminNotificationForm) => {
    data.email = userEmail
    dispatch(submitForm({ data }))
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
            data={availableTypes}
            value={selectedType.type === '' ? { type: notificationEditTableData.email_type } : selectedType}
            onChange={(value) => {
              dispatch(setSelectedType(value))
            }}
          />
        </Form.Group>

        {selectedType.type === '' && submitAttempt && (
          <p className="error-msg" role="alert">
            Must select a new email notification type
          </p>
        )}

        {successMsg && (
          <p className="success-msg" role="status">
            {successMsg}
          </p>
        )}
        {errorState && (
          <p className="error-msg" role="alert">
            Failed to update email notification due to error: {errorMsg}
          </p>
        )}
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

export default AdminEditNotification

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
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Link, useParams } from 'react-router-dom'
import { selectEditNotificationRowData, selectTableData } from '../adminNotificationTab/adminNotificationTabSlice'
import { AdminNotificationForm } from '../adminAddNotification/adminAddNotificationSlice'
import { selectEmail } from '../../generalSlices/userSlice'
import { AdminButton } from '../../styles/components/AdminButton'
import { ErrorMessageText, SuccessMessageText } from '../../styles/components/Messages'

const AdminEditNotification = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const successMsg = useSelector(selectSuccessMsg)
  const apiData = useSelector(selectApiData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const submitAttempt = useSelector(selectSubmitAttempt)
  const selectedType = useSelector(selectSelectedType)
  const availableTypes = useSelector(selectAvailableTypes)
  const notificationEditTableData = useSelector(selectEditNotificationRowData)
  const userEmail = useSelector(selectEmail)
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
          <p>{userEmail}</p>
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
          <ErrorMessageText role="alert">Must select a new email notification type</ErrorMessageText>
        )}

        {successMsg && <SuccessMessageText role="status">{successMsg}</SuccessMessageText>}
        {errorState && (
          <ErrorMessageText role="alert">Failed to update email notification due to error: {errorMsg}</ErrorMessageText>
        )}
        <div className="form-control">
          <label></label>
          <AdminButton type="submit">Apply Changes</AdminButton>
        </div>
      </Form>
    </div>
  )
}

export default AdminEditNotification

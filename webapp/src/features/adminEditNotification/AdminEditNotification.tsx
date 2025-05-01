import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
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
import { useNavigate, useParams } from 'react-router-dom'
import { selectEditNotificationRowData, selectTableData } from '../adminNotificationTab/adminNotificationTabSlice'
import { AdminNotificationForm } from '../adminAddNotification/adminAddNotificationSlice'
import { selectEmail } from '../../generalSlices/userSlice'
import { ErrorMessageText, SuccessMessageText } from '../../styles/components/Messages'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Typography,
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

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
  const [open, setOpen] = useState(true)
  const navigate = useNavigate()

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
    <Dialog
      open={open}
      onClose={() => {
        setOpen(false)
        navigate('..')
      }}
    >
      <DialogContent sx={{ width: '600px' }}>
        <SideBarHeader
          onClick={() => {
            setOpen(false)
            navigate('..')
          }}
          title="Edit Email Notification"
        />
        <Form
          id="edit-notification-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group controlId="email">
            <InputLabel>Email</InputLabel>
            <Typography fontSize="small">{userEmail}</Typography>
          </Form.Group>

          <Form.Group controlId="edit_email_type">
            <FormControl fullWidth margin="normal">
              <InputLabel>Notification Type</InputLabel>
              <Select
                id="edit_email_type"
                value={selectedType.type === '' ? notificationEditTableData.email_type : selectedType.type}
                defaultValue={notificationEditTableData.email_type}
                label="Notification Type"
                onChange={(event) => {
                  const value = event.target.value as string
                  dispatch(setSelectedType({ type: value }))
                }}
              >
                <MenuItem value={notificationEditTableData.email_type}>{notificationEditTableData.email_type}</MenuItem>
                {availableTypes.map((type) => (
                  <MenuItem value={type.type}>{type.type}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Form.Group>
          {selectedType.type === '' && submitAttempt && (
            <ErrorMessageText role="alert">Must select a new email notification type</ErrorMessageText>
          )}
          {successMsg && <SuccessMessageText role="status">{successMsg}</SuccessMessageText>}
          {errorState && (
            <ErrorMessageText role="alert">
              Failed to update email notification due to error: {errorMsg}
            </ErrorMessageText>
          )}
        </Form>
      </DialogContent>
      <DialogActions sx={{ padding: '20px' }}>
        <Button
          onClick={() => {
            setOpen(false)
            navigate('..')
          }}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
          className="museo-slab capital-case"
        >
          Cancel
        </Button>
        <Button
          form="edit-notification-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
          className="museo-slab capital-case"
        >
          Apply Changes
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminEditNotification

import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
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
import { selectEditNotificationRowData } from '../adminNotificationTab/adminNotificationTabSlice'
import { AdminNotificationForm } from '../adminAddNotification/adminAddNotificationSlice'
import { selectEmail } from '../../generalSlices/userSlice'
import { ErrorMessageText } from '../../styles/components/Messages'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Typography,
} from '@mui/material'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import toast from 'react-hot-toast'

const AdminEditNotification = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const apiData = useSelector(selectApiData)
  const selectedType = useSelector(selectSelectedType)
  const availableTypes = useSelector(selectAvailableTypes)
  const notificationEditTableData = useSelector(selectEditNotificationRowData)
  const userEmail = useSelector(selectEmail)
  const [open, setOpen] = useState(true)
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { isSubmitted },
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
    if (selectedType.type === '') {
      return;
    }
    data.email = userEmail
    dispatch(submitForm({ data })).then((data: any) => {
      if (data.payload.success) {
        toast.success('Notification updated successfully')
      } else {
        toast.error('Failed to update Notification: ' + data.payload.message)
      }
    })
    setOpen(false)
    navigate('..')
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
          {selectedType.type === '' && isSubmitted && (
            <ErrorMessageText role="alert">Must select a new email notification type</ErrorMessageText>
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

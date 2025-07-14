import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectSelectedType,
  selectAvailableTypes,
  selectApiData,

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
import { useNavigate } from 'react-router-dom'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import toast from 'react-hot-toast'

const AdminAddNotification = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const apiData = useSelector(selectApiData)
  const selectedType = useSelector(selectSelectedType)
  const availableTypes = useSelector(selectAvailableTypes)
  const userEmail = useSelector(selectEmail)
  const [open, setOpen] = useState(true)
  const navigate = useNavigate()
  const {
    handleSubmit,
    reset,
    formState: { isSubmitted },
  } = useForm<AdminNotificationForm>()

  useEffect(() => {
    dispatch(getNotificationData())
  }, [dispatch])

  useEffect(() => {
    dispatch(updateEmailTypesApiData())
  }, [apiData, dispatch])

  const notifySuccess = (message: string) => toast.success(message)
  const notifyError = (message: string) => toast.error(message)

  const onSubmit = (data: AdminNotificationForm) => {
    if (selectedType.type === '') {
      return;
    }
    data.email = userEmail
    dispatch(submitForm({ data, reset })).then((data: any) => {
      data.payload.success
        ? notifySuccess(data.payload.message)
        : notifyError('Failed to add user notification due to error: ' + data.payload.message)
      setOpen(false)
      navigate('..')
    })
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
          title="Add Email Notification"
        />
        <Form
          id="add-notification-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group controlId="email">
            <InputLabel>Email</InputLabel>
            <Typography fontSize="small">{userEmail}</Typography>
          </Form.Group>

          <Form.Group controlId="email_type">
            <FormControl fullWidth margin="normal">
              <InputLabel>Notification Type</InputLabel>
              <Select
                id="email_type"
                value={selectedType.type}
                label="Notification Type"
                onChange={(event) => {
                  const value = event.target.value as string
                  dispatch(setSelectedType({ type: value }))
                }}
              >
                {availableTypes.map((type) => (
                  <MenuItem value={type.type}>{type.type}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Form.Group>

          {(selectedType.type === '') && isSubmitted && (
            <ErrorMessageText role="alert">Must select at least one email notification type</ErrorMessageText>
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
          form="add-notification-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
          className="museo-slab capital-case"
        >
          Add Email Notification
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddNotification

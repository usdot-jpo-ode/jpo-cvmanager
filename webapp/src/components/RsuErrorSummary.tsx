import React, { useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'

import 'react-widgets/styles.css'
import RsuApi from '../apis/rsu-api'

import './css/ContactSupportMenu.css'
import '../styles/fonts/museo-slab.css'
import toast from 'react-hot-toast'
import Dialog from '@mui/material/Dialog'
import { DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { RsuOnlineStatus } from '../models/RsuApi'
import { AdminButton } from '../styles/components/AdminButton'

type RsuErrorSummaryType = {
  rsu: string
  online_status: RsuOnlineStatus | string
  scms_status: string
  hidden: boolean
  setHidden: () => void
}

const RsuErrorSummary = (props: RsuErrorSummaryType) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  const onSubmit = async (data: Object) => {
    try {
      const res = await RsuApi.postRsuErrorSummary(data)
      const status = res.status
      if (status === 200) {
        toast.success('Successfully sent RSU summary email')
        reset()
      } else {
        toast.error('Something went wrong: ' + status)
      }
    } catch (exception_var) {
      toast.error('An exception occurred, please try again later')
    }
    props.setHidden()
  }

  const messageTable = `
    <table>
        <tr>
            <th>Online Status</th>
            <th>SCMS Status</th>
        </tr>
        <tr>
            <td>${'RSU ' + props.online_status}</td>
            <td>${props.scms_status}</td>
        </tr>
    </table>
`

  const message = `
    <h2>RSU Error Summary Email</h2>
    <br />
    <p>Hello,</p>
    <p>Below is the error summary for RSU ${props.rsu} at ${new Date().toISOString()} UTC:</p>
    ${messageTable}
`

  return (
    <Dialog open={!props.hidden}>
      <DialogTitle style={{ textAlign: 'center' }}>RSU Error Summary Email</DialogTitle>
      <DialogContent>
        <Form
          id="rsu-error-summary-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="email">
            <Form.Label className="label">Send To</Form.Label>
            <Form.Control
              type="text"
              placeholder="Comma-delimited list of emails (Required)"
              {...register('emails', {
                required: 'Email list is required',
              })}
              style={{ marginTop: '0.5rem', borderRadius: '5px' }}
            />
            {errors.email && <Form.Text className="text-danger">{errors.email.message}</Form.Text>}
          </Form.Group>
          <Form.Group className="mb-3" controlId="subject">
            <Form.Label className="label">Subject</Form.Label>
            <Form.Control
              type="text"
              defaultValue={`RSU Error Summary for ${props.rsu}`}
              {...register('subject', {
                required: 'Subject is required',
              })}
              style={{ marginTop: '0.5rem', borderRadius: '5px' }}
            />
            {errors.subject && <Form.Text className="text-danger">{errors.subject.message}</Form.Text>}
          </Form.Group>
          <Form.Group className="mb-3" controlId="message">
            <Form.Label className="label">Message</Form.Label>
            <Form.Control
              as="textarea"
              rows={5}
              defaultValue={message}
              {...register('message', {
                required: 'Message is required',
              })}
              style={{ marginTop: '0.5rem', borderRadius: '5px' }}
            />
            {errors.message && <Form.Text className="text-danger">{errors.message.message}</Form.Text>}
          </Form.Group>
        </Form>
      </DialogContent>
      <DialogActions>
        <AdminButton
          onClick={() => {
            props.setHidden()
          }}
        >
          Close
        </AdminButton>
        <AdminButton form="rsu-error-summary-form" type="submit">
          Send Email
        </AdminButton>
      </DialogActions>
    </Dialog>
  )
}

export default RsuErrorSummary

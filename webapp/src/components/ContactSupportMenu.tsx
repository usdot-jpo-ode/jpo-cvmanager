import { useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'

import 'react-widgets/styles.css'

import './css/ContactSupportMenu.css'
import toast from 'react-hot-toast'
import Dialog from '@mui/material/Dialog'
import { Button, DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { AdminButton } from '../styles/components/AdminButton'
import '../styles/fonts/museo-slab.css'
import { useSendContactSupportEmailMutation } from '../features/api/emailApiSlice'

const ContactSupportMenu = () => {
  const [hidden, setHidden] = useState(true) // hidden by default
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  const [submitSupportRequest] = useSendContactSupportEmailMutation()

  const onSubmit = async (data: SupportRequestEmailContents) => {
    try {
      const response = await submitSupportRequest(data).unwrap()
      if (response.failureCount === 0) {
        toast.success(`Successfully sent support request`)
        reset()
      } else {
        toast.error(`Failed to send support request`)
      }
    } catch (exception_var) {
      console.error('Error in ContactSupportMenu onSubmit', exception_var)
      toast.error('An exception occurred, please try again later')
    }
    setHidden(true)
  }

  if (hidden) {
    return (
      <div className="contactWrapper">
        <Button
          variant="contained"
          onClick={() => {
            setHidden(!hidden)
          }}
        >
          Contact Support
        </Button>
      </div>
    )
  }

  return (
    <Dialog open={true}>
      <DialogTitle>Contact Support</DialogTitle>
      <DialogContent>
        <Form
          id="contact-support-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="email">
            <Form.Label className="label">Your Email</Form.Label>
            <Form.Control
              type="email"
              placeholder="Enter your email (Required)"
              {...register('email', {
                required: 'Email is required',
              })}
            />
            {errors.email && <Form.Text className="text-danger">{errors.email.message}</Form.Text>}
          </Form.Group>
          <Form.Group className="mb-3" controlId="subject">
            <Form.Label className="label">Subject</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter your subject (Required)"
              {...register('subject', {
                required: 'Subject is required',
              })}
            />
            {errors.subject && <Form.Text className="text-danger">{errors.subject.message}</Form.Text>}
          </Form.Group>
          <Form.Group className="mb-3" controlId="message">
            <Form.Label className="label">Message</Form.Label>
            <Form.Control
              as="textarea"
              rows={5}
              placeholder="Enter your message (Required)"
              {...register('message', {
                required: 'Message is required',
              })}
            />
            {errors.message && <Form.Text className="text-danger">{errors.message.message}</Form.Text>}
          </Form.Group>
        </Form>
      </DialogContent>
      <DialogActions>
        <AdminButton
          onClick={() => {
            setHidden(!hidden)
          }}
        >
          Close
        </AdminButton>
        <AdminButton form="contact-support-form" type="submit">
          Send Email
        </AdminButton>
      </DialogActions>
    </Dialog>
  )
}

export default ContactSupportMenu

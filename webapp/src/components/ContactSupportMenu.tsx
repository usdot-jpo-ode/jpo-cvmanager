import React, { useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'

import 'react-widgets/styles.css'
import RsuApi from '../apis/rsu-api'

import './css/ContactSupportMenu.css'
import toast from 'react-hot-toast'

const ContactSupportMenu = () => {
  const [hidden, setHidden] = useState(true) // hidden by default
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  const onSubmit = async (data: Object) => {
    try {
      const res = await RsuApi.postContactSupport(data)
      const status = res.status
      if (status === 200) {
        toast.success('Successfully sent email')
        reset()
      } else {
        toast.error('Something went wrong: ' + status)
      }
    } catch (exception_var) {
      toast.error('An exception occurred, please try again later')
    }
  }

  if (hidden) {
    return (
      <div>
        <button
          type="button"
          className="showbutton"
          onClick={() => {
            setHidden(!hidden)
          }}
        >
          Contact Support
        </button>
      </div>
    )
  }

  return (
    <div id="ContactSupportMenu">
      <Form onSubmit={handleSubmit(onSubmit)}>
        <h5>Contact Support</h5>
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
        <div className="form-control">
          <label></label>
          <button type="submit" className="btn btn-primary">
            Send Email
          </button>
        </div>
        <div>
          <button
            type="button"
            className="hidebutton"
            onClick={() => {
              setHidden(!hidden)
            }}
          >
            x
          </button>
        </div>
      </Form>
    </div>
  )
}

export default ContactSupportMenu

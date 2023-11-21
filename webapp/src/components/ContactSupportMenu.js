import React, { useState, useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import 'react-widgets/styles.css'
import RsuApi from '../apis/rsu-api'
import './css/ContactSupportMenu.css'

const ContactSupportMenu = () => {
  const [hidden, setHidden] = useState(true) // hidden by default
  const [successMsg, setSuccessMsg] = useState('')
  const [errorState, setErrorState] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  useEffect(() => {
    const delayTimer = setTimeout(() => {
      setHidden(false)
    }, 5000)

    return () => clearTimeout(delayTimer)
  }, [])

  const onSubmit = async (data) => {
    try {
      const res = await RsuApi.postContactSupport(data)
      const status = res.status
      if (status === 200) {
        console.debug('Successfully sent email: ' + status)
        setSuccessMsg('Successfully sent email')
        setErrorState(false)
        reset()
      } else {
        console.error('Something went wrong: ' + status)
        setSuccessMsg('')
        setErrorState(true)
        setErrorMessage('Something went wrong, please try again later')
      }
    } catch (exception_var) {
      console.error(exception_var)
      setSuccessMsg('')
      setErrorState(true)
      setErrorMessage('An exception occurred, please try again later')
    }
  }

  if (hidden) {
    return <div id="contactsupportbtndiv">{/* Button will be hidden initially and shown after the delay */}</div>
  }

  return <div id="ContactSupportMenu">{}</div>
}

export default ContactSupportMenu

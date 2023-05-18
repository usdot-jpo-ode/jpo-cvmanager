import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import EnvironmentVars from '../EnvironmentVars'

import '../components/css/Admin.css'
import 'react-widgets/styles.css'

const AdminAddUser = (props) => {
    const { setLoading, isLoginActive, authLoginData } = props
    const [successMsg, setSuccessMsg] = useState('')
    const [selectedOrganizationNames, setSelectedOrganizationNames] = useState(
        []
    )
    const [selectedOrganizations, setSelectedOrganizations] = useState([])
    const [organizationNames, setOrganizationNames] = useState([])
    const [availableRoles, setAvailableRoles] = useState([])
    const [apiData, setApiData] = useState({})
    const [errorState, setErrorState] = useState(false)
    const [errorMessage, setErrorMessage] = useState('')
    const [submitAttempt, setSubmitAttempt] = useState(false)
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm()

    const fetchGetData = async () => {
        if (isLoginActive()) {
            setLoading(true)
            setErrorState(false)

            try {
                const res = await fetch(EnvironmentVars.adminAddUser, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: authLoginData['token'],
                    },
                })

                const status = res.status
                const data = await res.json()

                setLoading(false)
                if (status === 200) {
                    setApiData(data)
                } else if (status === 400) {
                    setErrorMessage(data.message)
                    setErrorState(true)
                } else if (status === 500) {
                    setErrorMessage(data.message)
                    setErrorState(true)
                }
            } catch (exception_var) {
                setErrorState(true)
                setErrorMessage(exception_var.message)
                console.error(exception_var)
            }
        }
    }

    useEffect(() => {
        fetchGetData()
    }, [])

    useEffect(() => {
        if (Object.keys(apiData).length !== 0) {
            let orgData = []
            for (let i = 0; i < apiData.organizations.length; i++) {
                let organization = apiData.organizations[i]
                let temp = { id: i, name: organization }
                orgData.push(temp)
            }
            setOrganizationNames(orgData)

            let roleData = []
            for (let i = 0; i < apiData.roles.length; i++) {
                let role = {}
                role.role = apiData.roles[i]
                roleData.push(role)
            }
            setAvailableRoles(roleData)
        }
    }, [apiData])

    const updateOrganizations = (values) => {
        let newOrganizations = []
        for (const name of values) {
            if (selectedOrganizations.some((e) => e.name === name.name)) {
                var index = selectedOrganizations.findIndex(function (item, i) {
                    return item.name === name.name
                })
                newOrganizations.push(selectedOrganizations[index])
            } else if (
                !selectedOrganizations.some((e) => e.name === name.name)
            ) {
                name.role = availableRoles[0].role
                newOrganizations.push(name)
            }
        }
        setSelectedOrganizations(newOrganizations)
        setSelectedOrganizationNames(values)
    }

    const sendPostData = async (json) => {
        if (props.isLoginActive()) {
            props.setLoading(true)
            setErrorState(false)

            try {
                const res = await fetch(EnvironmentVars.adminAddUser, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: props.authLoginData['token'],
                    },
                    body: JSON.stringify(json),
                })

                const status = res.status
                const data = await res.json()
                if (status === 200) {
                    setSuccessMsg('User Creation is successful.')
                    resetForm()
                    props.updateUserData()
                } else if (status === 400) {
                    setErrorMessage(data.message)
                    setErrorState(true)
                } else if (status === 500) {
                    setErrorMessage(data.message)
                    setErrorState(true)
                }
            } catch (exception_var) {
                setErrorState(true)
                setErrorMessage(String(exception_var))
                console.error(exception_var)
            }
        }
        props.setLoading(false)
    }

    const onSubmit = (data) => {
        if (selectedOrganizations.length !== 0) {
            console.log(data, props.authLoginData)
            setSubmitAttempt(false)
            let submitOrgs = selectedOrganizations
            submitOrgs.forEach((elm) => delete elm.id)
            data['organizations'] = submitOrgs
            sendPostData(data)
        } else {
            setSubmitAttempt(true)
        }
    }

    function resetForm() {
        setSelectedOrganizationNames([])
        setSelectedOrganizations([])
        reset()

        setTimeout(() => setSuccessMsg(''), 5000)
    }

    return (
        <div>
            <Form onSubmit={handleSubmit(onSubmit)}>
                <Form.Group className="mb-3" controlId="email">
                    <Form.Label>Email</Form.Label>
                    <Form.Control
                        type="email"
                        placeholder="Enter user email"
                        {...register('email', {
                            required: 'Please enter user email',
                            pattern: {
                                value: /^[^@ ]+@[^@ ]+\.[^@ .]{2,}$/,
                                message: 'Please enter a valid email',
                            },
                        })}
                    />
                    {errors.email && (
                        <p className="errorMsg">{errors.email.message}</p>
                    )}
                </Form.Group>

                <Form.Group className="mb-3" controlId="first_name">
                    <Form.Label>First Name</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Enter user's first name"
                        {...register('first_name', {
                            required: "Please enter user's first name",
                        })}
                    />
                    {errors.first_name && (
                        <p className="errorMsg">{errors.first_name.message}</p>
                    )}
                </Form.Group>

                <Form.Group className="mb-3" controlId="last_name">
                    <Form.Label>Last Name</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Enter user's last name"
                        {...register('last_name', {
                            required: "Please enter user's last name",
                        })}
                    />
                    {errors.last_name && (
                        <p className="errorMsg">{errors.last_name.message}</p>
                    )}
                </Form.Group>

                <Form.Group className="mb-3" controlId="super_user">
                    <Form.Check
                        label=" Super User"
                        type="switch"
                        {...register('super_user')}
                    />
                </Form.Group>

                <Form.Group className="mb-3" controlId="organizations">
                    <Form.Label>Organizations</Form.Label>
                    <Multiselect
                        className="form-multiselect"
                        dataKey="id"
                        textField="name"
                        placeholder="Select organizations"
                        data={organizationNames}
                        value={selectedOrganizationNames}
                        onChange={(value) => {
                            updateOrganizations(value)
                        }}
                    />
                </Form.Group>

                {selectedOrganizations.length > 0 && (
                    <Form.Group className="mb-3" controlId="roles">
                        <Form.Label>Roles</Form.Label>
                        <p className="spacer" />
                        {selectedOrganizations.map((organization) => {
                            let role = { role: organization.role }

                            return (
                                <Form.Group
                                    className="mb-3"
                                    controlId={organization.id}
                                >
                                    <Form.Label>{organization.name}</Form.Label>
                                    <DropdownList
                                        className="form-dropdown"
                                        dataKey="role"
                                        textField="role"
                                        placeholder="Select Role"
                                        data={availableRoles}
                                        value={role}
                                        onChange={(value) => {
                                            role.role = value.role
                                            organization.role = value.role
                                        }}
                                    />
                                </Form.Group>
                            )
                        })}
                    </Form.Group>
                )}

                {selectedOrganizations.length === 0 && submitAttempt && (
                    <p className="error-msg">
                        Must select at least one organization
                    </p>
                )}

                {successMsg && <p className="success-msg">{successMsg}</p>}
                {errorState && (
                    <p className="error-msg">
                        Failed to add user due to error: {errorMessage}
                    </p>
                )}
                <div className="form-control">
                    <label></label>
                    <button type="submit" className="admin-button">
                        Add User
                    </button>
                </div>
            </Form>
        </div>
    )
}

export default AdminAddUser

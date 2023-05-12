import React, { useState } from "react";
import { Form } from "react-bootstrap";
import { useForm } from "react-hook-form";

import "../components/css/Admin.css";
import "react-widgets/styles.css";
import EnvironmentVars from "../EnvironmentVars";

const ContactSupportMenu = () => {
    const [successMsg, setSuccessMsg] = useState("");
    const [errorState, setErrorState] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm();

    const sendPostData = async (json) => {
        
        try {
            const res = await fetch(EnvironmentVars.sendEmail, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("token"),
                },
                body: JSON.stringify(json),
            });

            const status = res.status;
            const data = await res.json();
            if (status === 200) {
                console.debug("Successfully sent email");
                setSuccessMsg("Successfully sent email");
                setErrorState(false);
                reset();
            }
            else if (status === 500) {
                console.error(data);
                setSuccessMsg("");
                setErrorState(true);
                setErrorMessage(data);
            }
        } catch (exception_var) {
            console.error(exception_var);
            setSuccessMsg("");
            setErrorState(true);
            setErrorMessage(exception_var);
        }      
    };

    const onSubmit = (data) => {
        sendPostData(data);
    };

    return (
        <div>
            <Form onSubmit={handleSubmit(onSubmit)}>
                <h5>Contact Support</h5>
                <Form.Group className="mb-3" controlId="email">
                    <Form.Label>Your Email</Form.Label>
                    <Form.Control
                        type="email"
                        placeholder="Enter your email"
                        {...register("email", {
                            required: "Email is required",
                        })}
                    />
                    {errors.email && (
                        <Form.Text className="text-danger">
                            {errors.email.message}
                        </Form.Text>
                    )}
                </Form.Group>
                <Form.Group className="mb-3" controlId="subject">
                    <Form.Label>Subject</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Enter your subject"
                        {...register("subject", {
                            required: "Subject is required",
                        })}
                    />
                    {errors.subject && (
                        <Form.Text className="text-danger">
                            {errors.subject.message}
                        </Form.Text>
                    )}
                </Form.Group>
                <Form.Group className="mb-3" controlId="message">
                    <Form.Label>Message</Form.Label>
                    <Form.Control
                        as="textarea"
                        rows={5}
                        placeholder="Enter your message"
                        {...register("message", {
                            required: "Message is required",
                        })}
                    />
                    {errors.message && (
                        <Form.Text className="text-danger">
                            {errors.message.message}
                        </Form.Text>
                    )}
                </Form.Group>

                {successMsg && <p className="success-msg">{successMsg}</p>}
                {errorState && (
                    <p className="error-msg">
                        Failed to send email due to error: {errorMessage}
                    </p>
                )}
                <div className="form-control">
                    <label></label>
                    <button type="submit" className="btn btn-primary">
                        Send Email
                    </button>
                </div>
            </Form>
        </div>
    );
}

export default ContactSupportMenu;
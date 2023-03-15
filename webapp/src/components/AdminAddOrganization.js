import React, { useState } from "react";
import { Form } from "react-bootstrap";
import { useForm } from "react-hook-form";
import EnvironmentVars from "../EnvironmentVars";

import "../components/css/Admin.css";
import "react-widgets/styles.css";

const AdminAddOrganization = (props) => {
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
    if (props.isLoginActive()) {
      props.setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminAddOrg, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: props.authLoginData["token"],
          },
          body: JSON.stringify(json),
        });

        const status = res.status;
        const data = await res.json();
        props.setLoading(false);
        if (status === 200) {
          setSuccessMsg("Organization Creation is successful.");
          reset();
          resetMsg();
          props.updateOrganizationData();
        } else if (status === 400) {
          setErrorMessage(data.message);
          setErrorState(true);
          resetMsg();
        } else if (status === 500) {
          setErrorMessage(data.message);
          setErrorState(true);
        }
      } catch (exception_var) {
        props.setLoading(false);
        setErrorState(true);
        setErrorMessage(String(exception_var));
        console.error(exception_var);
      }
    }
  };

  const onSubmit = (data) => {
    sendPostData(data);
  };

  function resetMsg() {
    setTimeout(() => setSuccessMsg(""), 5000);
  }

  return (
    <div>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Form.Group className="mb-3" controlId="name">
          <Form.Label>Organization Name</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter organization name"
            {...register("name", {
              required: "Please enter the organization name",
            })}
          />
          {errors.name && <p className="errorMsg">{errors.name.message}</p>}
        </Form.Group>

        {successMsg && <p className="success-msg">{successMsg}</p>}
        {errorState && (
          <p className="error-msg">
            Failed to add organization due to error: {errorMessage}
          </p>
        )}
        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">Add Organization</button>
        </div>
      </Form>
    </div>
  );
};

export default AdminAddOrganization;

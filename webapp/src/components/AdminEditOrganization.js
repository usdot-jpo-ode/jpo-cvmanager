import React, { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import { useForm } from "react-hook-form";
import EnvironmentVars from "../EnvironmentVars";

import "../components/css/Admin.css";
import "react-widgets/styles.css";

const AdminEditOrganization = (props) => {
  const [successMsg, setSuccessMsg] = useState("");
  const [errorState, setErrorState] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue
  } = useForm({
    defaultValues: {
      orig_name: "",
      name: ""
    },
  });

  const {
    selectedOrg
  } = props;

  const updateStates = (selectedOrgName) => {
    setValue("orig_name", selectedOrgName);
    setValue("name", selectedOrgName);
  };

  useEffect (() => {
    updateStates(selectedOrg);
  }, [selectedOrg])

  const sendPatchData = async (json) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminOrg, {
          method: "PATCH",
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
          setSuccessMsg("Changes were successfully applied!");
          resetMsg();
          props.updateOrganizationData(json.name);
          updateStates(json.name);
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

  const createJsonBody = (data) => {
    const json = {
      orig_name: props.selectedOrg,
      name: data.name,
      users_to_add: [],
      users_to_modify : [],
      users_to_remove: [],
      rsus_to_add: [],
      rsus_to_remove: []
    }
    return json;
  };

  const onSubmit = (data) => {
    const json = createJsonBody(data);
    sendPatchData(json);
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
            Failed to apply changes to organization due to error: {errorMessage}
          </p>
        )}
        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">Apply Changes</button>
        </div>
      </Form>
    </div>
  );
};

export default AdminEditOrganization;

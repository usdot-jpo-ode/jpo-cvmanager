import React, { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import { useForm } from "react-hook-form";
import { Multiselect,DropdownList } from "react-widgets";
import EnvironmentVars from "../EnvironmentVars";

import "../components/css/Admin.css";
import "react-widgets/styles.css";

const AdminEditUser = (props) => {
  const [successMsg, setSuccessMsg] = useState("");
  const [selectedOrganizationNames, setSelectedOrganizationNames] = useState([]);
  const [selectedOrganizations, setSelectedOrganizations] = useState([]);
  const [organizationNames, setOrganizationNames] = useState([]);
  const [availableRoles, setAvailableRoles] = useState([]);
  const [apiData, setApiData] = useState({});
  const [errorState, setErrorState] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [submitAttempt, setSubmitAttempt] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm({
    defaultValues: {
      orig_email: "",
      email: "",
      first_name: "",
      last_name: "",
      super_user: "",
      organizations_to_add: [],
      organizations_to_modify: [],
      organizations_to_remove: [],
    },
  });

  const {
    userData,
    isLoginActive,
    setLoading,
    authLoginData
  } = props;

  const updateStates = (data) => {
    if (Object.keys(data).length !== 0) {
      let orgData = [];
      for (let i = 0; i < data.allowed_selections.organizations.length; i++) {
        let organization = data.allowed_selections.organizations[i];
        let temp = { id: i, name: organization };
        orgData.push(temp);
      }
      setOrganizationNames(orgData);

      let roleData = [];
      for (let i = 0; i < data.allowed_selections.roles.length; i++) {
        let role = data.allowed_selections.roles[i];
        let temp = { role: role };
        roleData.push(temp);
      }
      setAvailableRoles(roleData);

      setValue('orig_email', data.user_data.email);
      setValue('email', data.user_data.email);
      setValue('first_name', data.user_data.first_name);
      setValue('last_name', data.user_data.last_name);
      setValue('super_user', data.user_data.super_user);

      let tempOrganizations = [];
      let tempOrganizationNames = [];

      for (var i = 0; i < data.user_data.organizations.length; i++) {
        const org = data.user_data.organizations[i];
        let tempOrg = { id: i, name: org.name, role: org.role };
        let tempName = {id: i, name: data.user_data.organizations[i].name}
        tempOrganizations.push(tempOrg);
        tempOrganizationNames.push(tempName);
      }

      setSelectedOrganizations(tempOrganizations)
      setSelectedOrganizationNames(tempOrganizationNames)

    }
  };

  const fetchEditUserApiData = async (email) => {
    if (isLoginActive) {
      setLoading(true);
      setErrorState(false);
  
      try {
        const res = await fetch(EnvironmentVars.adminUser + '?user_email=' + email, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: authLoginData["token"],
          },
        });
  
        const status = res.status;
        const data = await res.json();
  
        if (status === 200) {
          setApiData(data);
          updateStates(data);
        } else if (status === 500) {
          setErrorState(true);
        }
      } catch (exception_var) {
        setLoading(false);
        setErrorState(true);
        console.error(exception_var);
      }
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchEditUserApiData(userData.email);
  }, [userData]);

  const updateOrganizations = (values) => {
    let newOrganizations = [];
    for (const name of values) {
      if (selectedOrganizations.some(e => e.name === name.name)){
        var index = selectedOrganizations.findIndex(function(item, i){
          return item.name === name.name
        });
        newOrganizations.push(selectedOrganizations[index]);
      } else if (!selectedOrganizations.some(e => e.name === name.name)) {
        name.role = availableRoles[0].role;
        newOrganizations.push(name);
      }
    }
  
    setSelectedOrganizations(newOrganizations)
    setSelectedOrganizationNames(values)
  }

  const sendPatchData = async (json) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminUser, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: props.authLoginData["token"],
          },
          body: JSON.stringify(json),
        });
        const status = res.status;
        const data = await res.json();
        if (status === 200) {
          setSuccessMsg("Applied Changes Successfully!");
          props.updateUserData();
          resetMsg();
        } else if (status === 400) {
          setErrorMessage(data.message);
          setErrorState(true);
        } else if (status === 500) {
          setErrorMessage(data.message);
          setErrorState(true);
        }
      } catch (exception_var) {
        setErrorState(true);
        setErrorMessage(exception_var.message);
        console.error(exception_var);
      }
    }
    props.setLoading(false);
  };

  const organizationParser = (data, submitOrgs) => {
    let orgsToAdd = [];
    let orgsToModify = [];
    let orgsToRemove = [];

    for (const org of apiData.user_data.organizations){
      if (submitOrgs.some(e => e.name === org.name)){
        var index = submitOrgs.findIndex(function(item, i){
          return item.name === org.name;
        });
        if (submitOrgs[index].role !== org.role){
          const changedOrg = {name: submitOrgs[index].name, role: submitOrgs[index].role};
          orgsToModify.push(changedOrg);
        }
      } else {
        const removedOrg = {name: org.name, role: org.role};
        orgsToRemove.push(removedOrg);
      }
    }

    for (const org of submitOrgs){
      if (!apiData.user_data.organizations.some(e => e.name === org.name)){
        const newOrg = {name: org.name, role: org.role};
        orgsToAdd.push(newOrg);
      }
    }

    data.organizations_to_add = orgsToAdd;
    data.organizations_to_modify = orgsToModify;
    data.organizations_to_remove = orgsToRemove;
    return data;
  }


  const onSubmit = (data) => {
    if (selectedOrganizations.length !== 0) {
      setSubmitAttempt(false);
      let submitOrgs = selectedOrganizations;
      submitOrgs.forEach(elm => delete elm.id);
      data = organizationParser(data, submitOrgs);
      sendPatchData(data);
    } else {
      setSubmitAttempt(true);
    }
  };

  function resetMsg() {
    setTimeout(() => setSuccessMsg(""), 5000);
  }

  return (
    <div>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Form.Group className="mb-3" controlId="email">
          <Form.Label>Email</Form.Label>
          <Form.Control
            type="email"
            placeholder="Enter user email"
            {...register("email", {
              required: "Please enter user email",
              pattern: {
                value: /^[^@ ]+@[^@ ]+\.[^@ .]{2,}$/,
                message: "Please enter a valid email",
              },
            })}
          />
          {errors.email && <p className="errorMsg">{errors.email.message}</p>}
        </Form.Group>

        <Form.Group className="mb-3" controlId="first_name">
          <Form.Label>First Name</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter user's first name"
            {...register("first_name", {
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
            {...register("last_name", {
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
            {...register("super_user")}
          />
        </Form.Group>
        
        
        <Form.Group className="mb-3" controlId="organizations">
          <Form.Label>Organizations</Form.Label>
          <Multiselect
            className="form-multiselect"
            dataKey="name"
            textField="name"
            data={organizationNames}
            placeholder="Select organizations"
            value={selectedOrganizationNames}
            onChange={(value) => {
              updateOrganizations(value)
            }
            }
          />
        </Form.Group>

        {selectedOrganizations.length > 0 && (
          <Form.Group className="mb-3" controlId="roles">
            <Form.Label>Roles</Form.Label>
            <p className="spacer" />
            {selectedOrganizations.map((organization) => {              
              let role = {role: organization.role };

              return (
                <Form.Group className="mb-3" controlId={organization.id}>
                <Form.Label>{organization.name}</Form.Label>
                <DropdownList
                  className="form-dropdown"
                  dataKey="role"
                  textField="role"
                  data={availableRoles}
                  value={role}
                  onChange={(value) => {
                    role.role=value.role;
                    organization.role=value.role;
                  }}
                />
              </Form.Group>
              );
            })}
          </Form.Group>
        )}

        {selectedOrganizations.length === 0 && submitAttempt && (
          <p className="error-msg">Must select at least one organization</p>
        )}

        {successMsg && <p className="success-msg">{successMsg}</p>}
        {errorState && (
          <p className="error-msg">
            Failed to apply changes due to error: {errorMessage}
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

export default AdminEditUser;

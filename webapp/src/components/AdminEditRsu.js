import React, { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import { useForm } from "react-hook-form";
import { ErrorMessage } from "@hookform/error-message";
import { Multiselect, DropdownList } from "react-widgets";
import EnvironmentVars from "../EnvironmentVars";

import "../components/css/Admin.css";

const AdminEditRsu = (props) => {
  const [successMsg, setSuccessMsg] = useState("");
  const [apiData, setApiData] = useState({});
  const [errorState, setErrorState] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [primaryRoutes, setPrimaryRoutes] = useState([]);
  const [selectedRoute, setSelectedRoute] = useState("");
  const [otherRouteDisabled, setOtherRouteDisabled] = useState(true);
  const [rsuModels, setRsuModels] = useState([]);
  const [selectedModel, setSelectedModel] = useState("");
  const [sshCredentialGroups, setSshCredentialGroups] = useState([]);
  const [selectedSshGroup, setSelectedSshGroup] = useState("");
  const [snmpCredentialGroups, setSnmpCredentialGroups] = useState([]);
  const [selectedSnmpGroup, setSelectedSnmpGroup] = useState("");
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrganizations, setSelectedOrganizations] = useState([]);
  const [submitAttempt, setSubmitAttempt] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm({
    defaultValues: {
      orig_ip: "",
      ip: "",
      geo_position: {
        latitude: "",
        longitude: "",
      },
      milepost: "",
      primary_route: "",
      serial_number: "",
      model: "",
      scms_id: "",
      ssh_credential_group: "",
      snmp_credential_group: "",
    },
  });

  const {
    rsuData,
    isLoginActive,
    setLoading,
    authLoginData
  } = props;

  const updateStates = (apiData) => {
    if (Object.keys(apiData).length !== 0) {
      let allowedSelections = apiData.allowed_selections;
      let temp= [];

      temp= [];
      for (const val of allowedSelections.primary_routes){
        let name = {};
        name.name = val;
        temp.push(name);
      }
      setPrimaryRoutes(temp);

      temp= [];
      for (const val of allowedSelections.rsu_models){
        let name = {};
        name.name = val;
        temp.push(name);
      }
      setRsuModels(temp);

      temp= [];
      for (const val of allowedSelections.ssh_credential_groups){
        let name = {};
        name.name = val;
        temp.push(name);
      }
      setSshCredentialGroups(temp);

      temp= [];
      for (const val of allowedSelections.snmp_credential_groups){
        let name = {};
        name.name = val;
        temp.push(name);
      }
      setSnmpCredentialGroups(temp);

      temp= [];
      for (const val of allowedSelections.organizations){
        let name = {};
        name.name = val;
        temp.push(name);
      }
      setOrganizations(temp);

      setValue('orig_ip', apiData.rsu_data.ip);
      setValue('ip', apiData.rsu_data.ip);
      setValue('geo_position', apiData.rsu_data.geo_position);
      setValue('milepost', String(apiData.rsu_data.milepost));
      setValue('serial_number', apiData.rsu_data.serial_number);
      setValue('scms_id', apiData.rsu_data.scms_id);

      setSelectedRoute(apiData.rsu_data.primary_route);
      setSelectedModel(apiData.rsu_data.model);
      setSelectedSshGroup(apiData.rsu_data.ssh_credential_group);
      setSelectedSnmpGroup(apiData.rsu_data.snmp_credential_group);

      let tempOrganizations = [];
      for (var i = 0; i < apiData.rsu_data.organizations.length; i++) {
        let tempOrg = {};
        tempOrg.name = apiData.rsu_data.organizations[i];
        tempOrganizations[i]= tempOrg;
      }
      setSelectedOrganizations(tempOrganizations);

      setApiData(apiData);
    }
  };

  const fetchGetData = async (rsu_ip) => {
    if (isLoginActive) {
      setLoading(true);
      setErrorState(false);
      try {
        const res = await fetch(EnvironmentVars.adminRsu + '?rsu_ip=' + rsu_ip, {
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
    setLoading(false);
  };

  useEffect (() => {
    fetchGetData(rsuData.ip);
  }, []);

  useEffect(() => {
    if (selectedRoute === "Other") {
      setOtherRouteDisabled(false);
    } else {
      setOtherRouteDisabled(true);
    }
  }, [selectedRoute]);

  const checkForm = () => {
    if (selectedRoute === "") {
      return false;
    } else if (selectedModel === "") {
      return false;
    } else if (selectedSshGroup === "") {
      return false;
    } else if (selectedSnmpGroup === "") {
      return false;
    } else if (selectedOrganizations.length === 0) {
      return false;
    } else {
      return true;
    }
  };

  const updateJson = (data) => {
    let json = data;

    if (selectedRoute !== "Other") {
      json.primary_route = selectedRoute;
    }
    json.milepost = Number(json.milepost);
    json.model = selectedModel;
    json.ssh_credential_group = selectedSshGroup;
    json.snmp_credential_group = selectedSnmpGroup;

    let tempOrganizations = [];
    for (var i = 0; i < selectedOrganizations.length; i++) {
      tempOrganizations.push(selectedOrganizations[i].name);
    }

    let organizationsToAdd = [];
    let organizationsToRemove = [];
    for (const org of apiData.allowed_selections.organizations) {
      if (selectedOrganizations.some(e => e.name === org) && ! apiData.rsu_data.organizations.includes(org)){
        organizationsToAdd.push(org);
      }
      if (apiData.rsu_data.organizations.includes(org) && (selectedOrganizations.some(e => e.name === org) === false)){
        organizationsToRemove.push(org);
      }
    }

    json.organizations_to_add = organizationsToAdd;
    json.organizations_to_remove = organizationsToRemove;

    return json;
  };

  const sendPatchData = async (json) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminRsu + '?rsu_ip=' + json.ip, {
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
          setTimeout(() => setSuccessMsg(""), 3000);
          props.updateRsuData();
        } else if (status === 400) {
          setErrorMessage(data.message);
          setErrorState(true);
        } else if (status === 500) {
          setErrorMessage(data.message);
          setErrorState(true);
        }
      } catch (exception_var) {
        props.setLoading(false);
        setErrorState(true);
        setErrorMessage(exception_var.message);
        console.error(exception_var);
      }
    }
  };

  const onSubmit = (data) => {
    if (checkForm(data)) {
      setSubmitAttempt(false);
      data = updateJson(data);
      sendPatchData(data)
    } else {
      setSubmitAttempt(true);
    }
  };

  return (
    <div>
      { apiData &&
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Form.Group className="mb-3" controlId="ip">
          <Form.Label>RSU IP</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU IP"
            {...register("ip", {
              required: "Please enter the RSU's IP address",
              pattern: {
                value:
                  /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
                message: "Please enter a valid IP address",
              },
            })}
          />
          <ErrorMessage
            errors={errors}
            name="ip"
            render={({ message }) => <p className="errorMsg"> {message} </p>}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="geo_position.latitude">
          <Form.Label>Latitude</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU Latitude"
            {...register("geo_position.latitude", {
              required: "Please enter the RSU latitude",
              pattern: {
                value:
                  /^(\+|-)?(?:90(?:(?:\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,6})?))$/,
                message: "Please enter a valid latitude",
              },
            })}
          />
          <ErrorMessage
            errors={errors}
            name="geo_position.latitude"
            render={({ message }) => <p className="errorMsg"> {message} </p>}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="geo_position.longitude">
          <Form.Label>Longitude</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU Longitude"
            {...register("geo_position.longitude", {
              required: "Please enter the RSU longitude",
              pattern: {
                value:
                  /^(\+|-)?(?:180(?:(?:\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,6})?))$/,
                message: "Please enter a valid longitude",
              },
            })}
          />
          <ErrorMessage
            errors={errors}
            name="geo_position.longitude"
            render={({ message }) => <p className="errorMsg"> {message} </p>}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="milepost">
          <Form.Label>Milepost</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU Milepost"
            {...register("milepost", {
              required: "Please enter the RSU milepost",
              pattern: {
                value: /^\d*\.?\d*$/,
                message: "Please enter a valid milepost",
              },
            })}
          />
          <ErrorMessage
            errors={errors}
            name="milepost"
            render={({ message }) => <p className="errorMsg"> {message} </p>}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="primary_route">
          <Form.Label>Primary Route</Form.Label>
          <DropdownList
            className="form-dropdown"
            dataKey="name"
            textField="name"
            data={primaryRoutes}
            value={selectedRoute}
            onChange={(value) => {
              setSelectedRoute(value.name);
            }}
          />
          {selectedRoute === "" && submitAttempt && (
            <p className="error-msg">Must select a primary route</p>
          )}
          {(() => {
            if (selectedRoute === "Other") {
              return (
                <Form.Control
                  type="text"
                  placeholder="Enter Other Route"
                  disabled={otherRouteDisabled}
                  {...register("primary_route", {
                    required: "Please enter the other route",
                  })}
                />
              );
            }
          })()}
        </Form.Group>

        <Form.Group className="mb-3" controlId="serial_number">
          <Form.Label>Serial Number</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU Serial Number"
            {...register("serial_number", {
              required: "Please enter the RSU serial number",
            })}
          />
          {errors.serial_number && (
            <p className="errorMsg">{errors.serial_number.message}</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="model">
          <Form.Label>RSU Model</Form.Label>
          <DropdownList
            className="form-dropdown"
            dataKey="name"
            textField="name"
            data={rsuModels}
            value={selectedModel}
            onChange={(value) => {
              setSelectedModel(value.name);
            }}
          />
          {selectedModel === "" && submitAttempt && (
            <p className="error-msg">Must select a RSU model</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="scms_id">
          <Form.Label>SCMS ID</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter SCMS ID"
            {...register("scms_id", {
              required: "Please enter the SCMS ID",
            })}
          />
          {errors.scms_id && (
            <p className="errorMsg">{errors.scms_id.message}</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="ssh_credential_group">
          <Form.Label>SSH Credential Group</Form.Label>
          <DropdownList
            className="form-dropdown"
            dataKey="name"
            textField="name"
            data={sshCredentialGroups}
            value={selectedSshGroup}
            onChange={(value) => {
              setSelectedSshGroup(value.name);
            }}
          />
          {selectedSshGroup === "" && submitAttempt && (
            <p className="error-msg">Must select a SSH credential group</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="snmp_credential_group">
          <Form.Label>SNMP Credential Group</Form.Label>
          <DropdownList
            className="form-dropdown"
            dataKey="name"
            textField="name"
            data={snmpCredentialGroups}
            value={selectedSnmpGroup}
            onChange={(value) => {
              setSelectedSnmpGroup(value.name);
            }}
          />
          {selectedSnmpGroup === "" && submitAttempt && (
            <p className="error-msg">Must select a SNMP credential group</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="organizations">
          <Form.Label>Organization</Form.Label>
          <Multiselect
            className="form-dropdown"
            dataKey="name"
            textField="name"
            data={organizations}
            placeholder="Select organizations"
            value={selectedOrganizations}
            onChange={(value) => {
              setSelectedOrganizations(value);
            }}
          />
          {selectedOrganizations.length === 0 && submitAttempt && (
            <p className="error-msg">Must select an organization</p>
          )}
        </Form.Group>

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
    }
    </div>
  );
};

export default AdminEditRsu;

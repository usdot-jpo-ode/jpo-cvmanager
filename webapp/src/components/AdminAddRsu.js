import React, { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import { useForm } from "react-hook-form";
import { Multiselect, DropdownList } from "react-widgets";
import EnvironmentVars from "../EnvironmentVars";

import "../components/css/Admin.css";

const AdminAddRsu = (props) => {
  const [successMsg, setSuccessMsg] = useState("");
  const [apiData, setApiData] = useState({});
  const [errorState, setErrorState] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [primaryRoutes, setPrimaryRoutes] = useState([]);
  const [selectedRoute, setSelectedRoute] = useState("Select Route");
  const [otherRouteDisabled, setOtherRouteDisabled] = useState(true);
  const [rsuModels, setRsuModels] = useState([]);
  const [selectedModel, setSelectedModel] = useState("Select RSU Model");
  const [sshCredentialGroups, setSshCredentialGroups] = useState([]);
  const [selectedSshGroup, setSelectedSshGroup] = useState("Select SSH Group");
  const [snmpCredentialGroups, setSnmpCredentialGroups] = useState([]);
  const [selectedSnmpGroup, setSelectedSnmpGroup] = useState("Select SNMP Group");
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrganizations, setSelectedOrganizations] = useState([]);
  const [submitAttempt, setSubmitAttempt] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm();

  const {
    isLoginActive,
    setLoading,
    authLoginData
  } = props;

  const fetchGetData = async () => {
    if (isLoginActive()) {
      setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminAddRsu, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: authLoginData["token"],
          },
        });

        const status = res.status;
        const data = await res.json();

        if (status === 200) {
          const keyedData = updateApiJson(data);
          setApiData(keyedData);
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

  const updateApiJson = (apiJson) => {
    if (Object.keys(apiJson).length !== 0) {
      let keyedApiJson = {};

      let data = [];
      for (let i = 0; i < apiJson["primary_routes"].length; i++) {
        let value = apiJson["primary_routes"][i];
        let temp = {id:i, name: value };
        data.push(temp);
      }
      keyedApiJson.primary_routes = data;

      data = [];
      for (let i = 0; i < apiJson["rsu_models"].length; i++) {
        let value = apiJson["rsu_models"][i];
        let temp = {id:i, name: value };
        data.push(temp);
      }
      keyedApiJson.rsu_models = data;

      data = [];
      for (let i = 0; i < apiJson["ssh_credential_groups"].length; i++) {
        let value = apiJson["ssh_credential_groups"][i];
        let temp = {id:i, name: value };
        data.push(temp);
      }
      keyedApiJson.ssh_credential_groups = data;

      data = [];
      for (let i = 0; i < apiJson["snmp_credential_groups"].length; i++) {
        let value = apiJson["snmp_credential_groups"][i];
        let temp = {id:i, name: value };
        data.push(temp);
      }
      keyedApiJson.snmp_credential_groups = data;

      data = [];
      for (let i = 0; i < apiJson["organizations"].length; i++) {
        let value = apiJson["organizations"][i];
        let temp = {id:i, name: value };
        data.push(temp);
      }
      keyedApiJson.organizations = data;

      return keyedApiJson;
    }
  }

  useEffect (() => {
    fetchGetData();
  }, [])

  useEffect(() => {
    if (Object.keys(apiData).length !== 0 && successMsg === "") {
      setPrimaryRoutes(apiData.primary_routes);
      setRsuModels(apiData.rsu_models);
      setSshCredentialGroups(apiData.ssh_credential_groups);
      setSnmpCredentialGroups(apiData.snmp_credential_groups);
      setOrganizations(apiData.organizations);
    }
  }, [apiData]);

  useEffect(() => {
    if (selectedRoute === "Other") {
      setOtherRouteDisabled(false);
    } else {
      setOtherRouteDisabled(true);
    }
  }, [selectedRoute]);

  const checkForm = () => {
    if (selectedRoute === "Select Route") {
      return false;
    } else if (selectedModel === "Select RSU Model") {
      return false;
    } else if (selectedSshGroup === "Select SSH Group") {
      return false;
    } else if (selectedSnmpGroup === "Select SNMP Group") {
      return false;
    } else if (selectedOrganizations.length === 0) {
      return false;
    } else {
      return true;
    }
  };

  const updateJson = (data) => {
    let json = data;
    // creating geo_position object from latitudes and longitude
    json.geo_position = {
      latitude: Number(json.latitude),
      longitude: Number(json.longitude),
    };
    delete json.latitude;
    delete json.longitude;
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

    json.organizations = tempOrganizations;

    return json;
  };

  const sendPostData = async (json) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminAddRsu, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: props.authLoginData["token"],
          },
          body: JSON.stringify(json),
        });
        const status = res.status;
        const data = await res.json();
        if (status === 200) {
          setSuccessMsg("RSU Creation is successful.");
          resetForm();
          props.updateRsuData();
        } else if (status === 400) {
          setErrorMessage(data.message);
          setErrorState(true);
        } else if (status === 500) {
          setErrorMessage(data.message);
          setErrorState(true);
        }
      } catch (exception_var) {
        
        setErrorState(true);
        setErrorMessage(String(exception_var));
        console.error(exception_var);
      }
      props.setLoading(false);
    }
  };

  const onSubmit = (data) => {
    if (checkForm(data)) {
      setSubmitAttempt(false);
      data = updateJson(data);
      sendPostData(data);
    } else {
      setSubmitAttempt(true);
    }
  };

  function resetForm() {
    reset();
    setSelectedRoute("Select Route");
    setSelectedModel("Select RSU Model");
    setSelectedSshGroup("Select SSH Group");
    setSelectedSnmpGroup("Select SNMP Group");
    setSelectedOrganizations([]);
    setTimeout(() => setSuccessMsg(""), 5000);
  }

  return (
    <div>
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
          {errors.ip && <p className="errorMsg">{errors.ip.message}</p>}
        </Form.Group>

        <Form.Group className="mb-3" controlId="latitude">
          <Form.Label>Latitude</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU Latitude"
            {...register("latitude", {
              required: "Please enter the RSU latitude",
              pattern: {
                value:
                  /^(\+|-)?(?:90(?:(?:\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,6})?))$/,
                message: "Please enter a valid latitude",
              },
            })}
          />
          {errors.latitude && (
            <p className="errorMsg">{errors.latitude.message}</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="longitude">
          <Form.Label>Longitude</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU Longitude"
            {...register("longitude", {
              required: "Please enter the RSU longitude",
              pattern: {
                value:
                  /^(\+|-)?(?:180(?:(?:\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,6})?))$/,
                message: "Please enter a valid longitude",
              },
            })}
          />
          {errors.longitude && (
            <p className="errorMsg">{errors.longitude.message}</p>
          )}
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
                message: "Please enter a valid number",
              },
            })}
          />
          {errors.milepost && (
            <p className="errorMsg">{errors.milepost.message}</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="primary_route">
          <Form.Label>Primary Route</Form.Label>
          <DropdownList
            className="form-dropdown"
            dataKey="id"
            textField="name"
            defaultValue={primaryRoutes[0]}
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
            dataKey="id"
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
            dataKey="id"
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
            dataKey="id"
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
            dataKey="id"
            textField="name"
            placeholder="Select organizations"
            data={organizations}
            value={selectedOrganizations}
            onChange={(value) => {
              setSelectedOrganizations(value);
            }}
          />
          {selectedOrganizations.length === 0 && submitAttempt && (
            <p className="error-msg">Must select an organization</p>
          )}
        </Form.Group>

        {<p className="success-msg">{successMsg}</p>}

        {errorState && (
          <p className="error-msg">
            Failed to add rsu due to error: {errorMessage}
          </p>
        )}

        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">Add RSU</button>
        </div>
      </Form>
    </div>
  );
};

export default AdminAddRsu;

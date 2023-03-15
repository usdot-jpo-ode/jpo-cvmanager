import React, { useState, useEffect } from "react";
import AdminAddOrganization from "./AdminAddOrganization";
import AdminOrganizationTabRsu from "./AdminOrganizationTabRsu";
import AdminOrganizationTabUser from "./AdminOrganizationTabUser";
import AdminEditOrganization from "./AdminEditOrganization";
import AdminOrganizationDeleteMenu from "./AdminOrganizationDeleteMenu";
import EnvironmentVars from "../EnvironmentVars";
import { IoChevronBackCircleOutline, IoRefresh } from "react-icons/io5";
import { AiOutlinePlusCircle } from "react-icons/ai";
import Grid from '@mui/material/Grid';
import EditIcon from '@mui/icons-material/Edit';
import { DropdownList } from "react-widgets";
import { useSelector } from "react-redux";
import {
  selectLoading,
} from "../slices/rsuSlice";

import "../components/css/Admin.css";

const AdminOrganizationTab = (props) => {
  const { authLoginData, isLoginActive, setLoading } = props;
  const [activeDiv, setActiveDiv] = useState("organization_table");
  const [title, setTitle] = useState("Organizations");
  const [orgData, setOrgData] = useState([]);
  const [selectedOrg, setSelectedOrg] = useState([]);
  const [rsuTableData, setRsuTableData] = useState([]);
  const [userTableData, setUserTableData] = useState([]);
  const [errorState, setErrorState] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const loading = useSelector(selectLoading);

  let orgPatchJson = {
    orig_name: selectedOrg.name,
    name: selectedOrg.name,
    users_to_add: [],
    users_to_modify: [],
    users_to_remove: [],
    rsus_to_add: [],
    rsus_to_remove: []
  };

  const fetchOrg = async (orgName) => {
    if (isLoginActive) {
      setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(
          EnvironmentVars.adminOrg + "?org_name=" + orgName,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: authLoginData["token"],
            },
          }
        );

        const status = res.status;
        const data = await res.json();

        if (status === 200) {
          return data;
        } else if (status === 400) {
          console.error(data.message);
          setErrorState(true);
        } else if (status === 500) {
          console.error(data.message);
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

  const updateOrgData = async (specifiedOrg) => {
    const data = await fetchOrg("all");
    let tempData = [];
    let i = 0;
    for (const x in data?.org_data) {
      const temp = data?.org_data[x];
      temp.id = i;
      tempData.push(temp);
      i = i + 1;
    }
    setOrgData(tempData);
    if (specifiedOrg) {
      for (let i = 0; i < tempData.length; i++) {
        if (tempData[i].name === specifiedOrg) {
          setSelectedOrg(tempData[i]);
          break;
        }
      }
    } else {
      setSelectedOrg(tempData[0]);
    }
    setLoading(false);
  };

  useEffect(() => {
    updateOrgData();
  }, []);

  const updateTableData = async (orgName) => {
    const data = await fetchOrg(orgName).catch(_ => []);

    setRsuTableData(data?.org_data?.org_rsus);
    setUserTableData(data?.org_data?.org_users);

    setLoading(false);
  };

  useEffect(() => {
    updateTableData(selectedOrg.name);
    setLoading(false);
  }, [selectedOrg]);

  useEffect(() => {
    if (activeDiv === "organization_table") {
      setTitle("CV Manager Organizations");
    } else if (activeDiv === "edit_organization") {
      setTitle("Edit Organization");
    } else if (activeDiv === "add_organization") {
      setTitle("Add Organization");
    }
  }, [activeDiv]);

  const fetchPatchOrganization = async (json) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      setErrorState(false);

      try {
        const res = await fetch(EnvironmentVars.adminOrg,
          {
            method: "PATCH",
            headers: {
              "Content-Type": "application/json",
              Authorization: props.authLoginData["token"],
            },
            body: JSON.stringify(json),
          }
        );

        const status = res.status;
        const data = await res.json();

        props.setLoading(false);
        if (status === 200) {
          console.debug("PATCH successful ",json);
        } else if (status === 400) {
          console.error(data.message);
          setErrorState(true);
        } else if (status === 500) {
          console.error(data.message);
          setErrorState(true);
        }
      } catch (exception_var) {
        setErrorState(true);
        console.error(exception_var);
      }
    }
  }

  const refresh = () => {
    updateTableData(selectedOrg.name);
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeDiv !== "organization_table" && (
            <button
              key="org_table"
              className="admin_table_button"
              onClick={(value) => {
                setActiveDiv("organization_table");
              }}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeDiv === "organization_table" && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={(value) => {
                setActiveDiv("add_organization");
              }}
              title="Add Organization"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={(value) => {
                refresh();
              }}
              title="Refresh Organizations"
            >
              <IoRefresh size={20} />
            </button>
          ]}
        </h3>
      </div>

      {errorState && (
        <p className="error-msg">
          Failed to obtain data due to error: {errorMessage}
        </p>
      )}

      {activeDiv === "organization_table" && (
        <div>
          <Grid container>
            <Grid item xs={0}>
              <DropdownList
                style={{width:"250px"}}
                className="form-dropdown"
                dataKey="name"
                textField="name"
                data={orgData}
                value={selectedOrg}
                onChange={(value) => {
                  setSelectedOrg(value);
                }}
              />
            </Grid>
            <Grid item xs={0}>
              <button className="delete_button" onClick={(value) => {
                  setActiveDiv("edit_organization");
                }}
                title="Edit Organization"
                >
                <EditIcon size={20}/>
              </button>
            </Grid>
            <Grid item xs={0}>
              <AdminOrganizationDeleteMenu
                isLoginActive={props.isLoginActive}
                authLoginData={props.authLoginData}
                setLoading={props.setLoading}
                selectedOrg={selectedOrg.name}
                updateOrganizationData={updateOrgData}
              />
              
            </Grid>
          </Grid>
          
          <div className="scroll-div-org-tab">
            {activeDiv === "organization_table" && [
              <AdminOrganizationTabRsu
                isLoginActive={props.isLoginActive}
                authLoginData={props.authLoginData}
                selectedOrg={selectedOrg.name}
                setLoading={props.setLoading}
                loading={loading}
                orgPatchJson={orgPatchJson}
                fetchPatchOrganization={fetchPatchOrganization}
                updateTableData={updateTableData}
                tableData={rsuTableData}
              />,
              <AdminOrganizationTabUser
                isLoginActive={props.isLoginActive}
                authLoginData={props.authLoginData}
                selectedOrg={selectedOrg.name}
                setLoading={props.setLoading}
                loading={loading}
                orgPatchJson={orgPatchJson}
                fetchPatchOrganization={fetchPatchOrganization}
                updateTableData={updateTableData}
                tableData={userTableData}
              />
            ]}
          </div>
        </div>
      )}

      {activeDiv === "add_organization" && (
        <div className="scoll-div">
          <AdminAddOrganization
            authLoginData={props.authLoginData}
            isLoginActive={props.isLoginActive}
            setLoading={props.setLoading}
            updateOrganizationData={updateOrgData}
          />
        </div>
      )}

      {activeDiv === "edit_organization" && (
        <div className="scoll-div">
          <AdminEditOrganization
            authLoginData={props.authLoginData}
            isLoginActive={props.isLoginActive}
            setLoading={props.setLoading}
            selectedOrg={selectedOrg.name}
            updateOrganizationData={updateOrgData}
          />
        </div>
      )}
    </div>
  );
};

export default AdminOrganizationTab;

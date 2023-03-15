import React, { useState, useEffect } from "react";
import AdminTable from "./AdminTable";
import EnvironmentVars from "../EnvironmentVars";
import { AiOutlinePlusCircle } from "react-icons/ai";
import { ThemeProvider, createTheme } from "@mui/material";
import Accordion from "@mui/material/Accordion";
import AccordionSummary from "@mui/material/AccordionSummary";
import AccordionDetails from "@mui/material/AccordionDetails";
import Typography from "@mui/material/Typography";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { DropdownList, Multiselect } from "react-widgets";
import { confirmAlert } from "react-confirm-alert";
import { Options } from "./AdminDeletionOptions";

import "../components/css/Admin.css";

const AdminOrganizationTabUser = (props) => {
  const { selectedOrg, setLoading, isLoginActive, authLoginData } = props;
  const [userColumns] = useState([
    {
      title: "First Name",
      field: "first_name",
      editable: "never",
      id: 0,
      width: "23%",
    },
    {
      title: "Last Name",
      field: "last_name",
      editable: "never",
      id: 1,
      width: "23%",
    },
    { title: "Email", field: "email", editable: "never", id: 2, width: "24%" },
    {
      title: "Role",
      field: "role",
      id: 3,
      width: "23%",
      lookup: { user: "User", operator: "Operator", admin: "Admin" },
    },
  ]);
  let userActions = [
    {
      icon: "delete",
      tooltip: "Remove From Organization",
      position: "row",
      onClick: (event, rowData) => {
        const buttons = [
          {
            label: "Yes",
            onClick: () => userOnDelete(rowData),
          },
          {
            label: "No",
            onClick: () => {},
          },
        ];
        const alertOptions = Options(
          "Delete User",
          'Are you sure you want to delete "' +
            rowData.email +
            '" from ' +
            props.selectedOrg +
            " organization?",
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
    {
      tooltip: "Remove All Selected From Organization",
      icon: "delete",
      onClick: (event, rowData) => {
        const buttons = [
          {
            label: "Yes",
            onClick: () => userMultiDelete(rowData),
          },
          {
            label: "No",
            onClick: () => {},
          },
        ];
        const alertOptions = Options(
          "Delete Selected Users",
          "Are you sure you want to delete " +
            rowData.length +
            " users from " +
            props.selectedOrg +
            " organization?",
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
  ];
  let userTableEditable = {
    onBulkUpdate: (changes) =>
      new Promise((resolve, reject) => {
        userBulkEdit(changes);
        setTimeout(() => {
          resolve();
        }, 2000);
      }),
  };
  const [availableUserList, setAvailableUserList] = useState([]);
  const [selectedUserList, setSelectedUserList] = useState([]);
  const [availableRoles, setAvailableRoles] = useState([]);

  const fetchApiData = async () => {
    if (isLoginActive()) {
      setLoading(true);

      try {
        const res = await fetch(EnvironmentVars.adminAddUser, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: authLoginData["token"],
          },
        });

        const status = res.status;
        const data = await res.json();

        if (status === 200) {
          return data;
        } else if (status === 400) {
          console.error(data.message);
        } else if (status === 500) {
          console.error(data.message);
        }
      } catch (exception_var) {
        console.error(exception_var);
      }
    }
    setLoading(false);
  };

  const fetchUserData = async () => {
    if (isLoginActive()) {
      setLoading(true);

      try {
        const res = await fetch(EnvironmentVars.adminUser + "?user_email=all", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: authLoginData["token"],
          },
        });

        const status = res.status;
        const data = await res.json();

        if (status === 200) {
          return data;
        } else if (status === 400) {
          console.error(data.message);
        } else if (status === 500) {
          console.error(data.message);
        }
      } catch (exception_var) {
        console.error(exception_var);
      }
    }
    setLoading(false);
  };

  const updateAvailableRoles = async () => {
    let roleData = [];
    const apiData = await fetchApiData();
    for (let i = 0; i < apiData.roles.length; i++) {
      let role = {};
      role.role = apiData.roles[i];
      roleData.push(role);
    }
    setAvailableRoles(roleData);
  };

  const updateAvailableUserList = async (orgName) => {
    const userData = await fetchUserData().catch((_) => []);
    let availableUserList = [];
    let counter = 0;
    if (userData?.user_data) {
      for (const user of userData.user_data) {
        const userOrgs = user?.organizations;
        if (!userOrgs.some((e) => e.name === orgName)) {
          let tempValue = {};
          tempValue.id = counter;
          tempValue.email = user.email;
          tempValue.role = "user";
          availableUserList.push(tempValue);
          counter += 1;
        }
      }
    }
    setAvailableUserList(availableUserList);
  };

  useEffect(() => {
    updateAvailableRoles();
  }, []);

  useEffect(() => {
    setSelectedUserList([]);
    updateAvailableUserList(selectedOrg);
    setLoading(false);
  }, [selectedOrg]);

  const fetchGetUserData = async (user_email) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      try {
        const res = await fetch(
          EnvironmentVars.adminUser + "?user_email=" + user_email,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: props.authLoginData["token"],
            },
          }
        );

        const status = res.status;
        const data = await res.json();
        if (status === 200) {
          return data;
        } else if (status === 400) {
          console.error(data.message);
        } else if (status === 500) {
          console.error(data.message);
        }
      } catch (exception_var) {
        console.error(exception_var);
      }
    }
  };

  const userOnDelete = async (row) => {
    let promises = [];
    const userData = await fetchGetUserData(row.email);
    if (userData?.user_data?.organizations?.length > 1) {
      const userRole = { email: row.email, role: row.role };
      let patchJson = props.orgPatchJson;

      patchJson.users_to_remove = [userRole];
      promises.push(props.fetchPatchOrganization(patchJson));
    } else {
      alert(
        "Cannot remove User " +
          row.email +
          " from " +
          props.selectedOrg +
          " because they must belong to at least one organization."
      );
    }

    Promise.all(promises).then(() => {
      refresh();
    });
  };

  const userMultiDelete = async (rows) => {
    let promises = [];
    for (var row of rows) {
      const userData = await fetchGetUserData(row.email);
      if (userData?.user_data?.organizations?.length > 1) {
        const userRole = { email: row.email, role: row.role };
        let patchJson = props.orgPatchJson;

        patchJson.users_to_remove = [userRole];
        promises.push(props.fetchPatchOrganization(patchJson));
      } else {
        alert(
          "Cannot remove User " +
            row.email +
            " from " +
            props.selectedOrg +
            " because they must belong to at least one organization."
        );
      }
    }

    return Promise.all(promises).then(() => {
      refresh();
    });
  };

  const userMultiAdd = async (userList) => {
    let promises = [];
    for (const key in userList) {
      const row = userList[key];
      let patchJson = props.orgPatchJson;
      const userRole = { email: row?.email, role: row?.role };
      patchJson.users_to_add = [userRole];
      promises.push(props.fetchPatchOrganization(patchJson));
    }
    Promise.all(promises).then(() => {
      refresh();
    });
  };

  const userBulkEdit = async (json) => {
    let promises = [];
    const rows = Object.values(json);
    for (var row of rows) {
      let patchJson = props.orgPatchJson;
      const userRole = { email: row.newData.email, role: row.newData.role };
      patchJson.users_to_modify = [userRole];
      promises.push(props.fetchPatchOrganization(patchJson));
    }
    return Promise.all(promises).then(() => {
      refresh();
    });
  };

  const refresh = () => {
    props.updateTableData(props.selectedOrg);
    updateAvailableUserList(props.selectedOrg);
    setSelectedUserList([]);
  };

  const accordionTheme = createTheme({
    palette: {
      text: {
        primary: "#ffffff",
        secondary: "#ffffff",
        disabled: "#ffffff",
        hint: "#ffffff",
      },
      divider: "#333",
      background: {
        paper: "#0e2052",
      },
    },
  });

  const innerAccordionTheme = createTheme({
    palette: {
      text: {
        primary: "#fff",
        secondary: "#fff",
        disabled: "#fff",
        hint: "#fff",
      },
      divider: "#333",
      background: {
        paper: "#333",
      },
    },
  });

  return (
    <div>
      <ThemeProvider theme={accordionTheme}>
        <Accordion>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon className="expand" />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            <Typography style={{ fontSize: "18px" }}>
              {props.selectedOrg} Users
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            {props.loading === false && [
              <div className="accordion">
                <ThemeProvider theme={innerAccordionTheme}>
                  <Accordion>
                    <AccordionSummary
                      expandIcon={<ExpandMoreIcon className="expand" />}
                      aria-controls="panel1a-content"
                      id="panel1a-header"
                    >
                      <Typography>Add Users to {props.selectedOrg}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div className="spacer-large-user">
                        <Multiselect
                          className="org-multiselect"
                          dataKey="id"
                          textField="email"
                          placeholder="Click to add users"
                          data={availableUserList}
                          value={selectedUserList}
                          onChange={(value) => {
                            setSelectedUserList(value);
                          }}
                        />
                        <button
                          key="user_plus_button"
                          className="admin-button"
                          onClick={(value) => {
                            userMultiAdd(selectedUserList);
                          }}
                          title="Add Users To Organization"
                        >
                          <AiOutlinePlusCircle size={20} />
                        </button>
                      </div>
                      {selectedUserList.length > 0 && (
                        <p className="org-form-test">
                          <b>Please select a role for:</b>
                        </p>
                      )}
                      {selectedUserList.length > 0 && [
                        selectedUserList.map((user) => {
                          return (
                            <div>
                              <p>{user.email}</p>
                              <DropdownList
                                className="org-form-dropdown"
                                dataKey="role"
                                textField="role"
                                data={availableRoles}
                                value={user}
                                onChange={(value) => {
                                  user.role = value.role;
                                }}
                              />
                            </div>
                          );
                        }),
                      ]}
                    </AccordionDetails>
                  </Accordion>
                </ThemeProvider>
              </div>,
              <div>
                <AdminTable
                  title={"Modify User-Organization Assignment"}
                  data={props.tableData}
                  columns={userColumns}
                  actions={userActions}
                  editable={userTableEditable}
                />
              </div>,
            ]}
          </AccordionDetails>
        </Accordion>
      </ThemeProvider>
    </div>
  );
};

export default AdminOrganizationTabUser;

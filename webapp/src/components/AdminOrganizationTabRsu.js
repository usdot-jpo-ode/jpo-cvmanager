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
import { Multiselect } from "react-widgets";
import { confirmAlert } from "react-confirm-alert";
import { Options } from "./AdminDeletionOptions";

import "../components/css/Admin.css";

const AdminOrganizationTabRsu = (props) => {
  const { authLoginData, isLoginActive, setLoading, selectedOrg } = props;
  const [rsuColumns] = useState([
    { title: "IP Address", field: "ip", id: 0, width: "31%" },
    { title: "Primary Route", field: "primary_route", id: 1, width: "31%" },
    { title: "Milepost", field: "milepost", id: 2, width: "31%" },
  ]);
  let rsuActions = [
    {
      icon: "delete",
      tooltip: "Remove From Organization",
      position: "row",
      onClick: (event, rowData) => {
        const buttons = [
          {
            label: "Yes",
            onClick: () => rsuOnDelete(rowData),
          },
          {
            label: "No",
            onClick: () => {},
          },
        ];
        console.log(rowData);
        const alertOptions = Options(
          "Delete RSU",
          'Are you sure you want to delete "' +
            rowData.ip +
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
            onClick: () => rsuMultiDelete(rowData),
          },
          {
            label: "No",
            onClick: () => {},
          },
        ];
        const alertOptions = Options(
          "Delete Selected RSUs",
          "Are you sure you want to delete " +
            rowData.length +
            " RSUs from " +
            props.selectedOrg +
            " organization?",
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
  ];
  const [availableRsuList, setAvailableRsuList] = useState([]);
  const [selectedRsuList, setSelectedRsuList] = useState([]);

  const fetchRsuData = async () => {
    if (isLoginActive()) {
      setLoading(true);

      try {
        const res = await fetch(EnvironmentVars.adminRsu + "?rsu_ip=all", {
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

  const updateAvailableRsuList = async (orgName) => {
    const rsuData = await fetchRsuData().catch((_) => []);
    let availableRsuList = [];
    let counter = 0;
    if (rsuData?.rsu_data) {
      for (const rsu of rsuData.rsu_data) {
        const rsuOrgs = rsu?.organizations;
        if (!rsuOrgs.includes(orgName)) {
          let tempValue = {};
          tempValue.id = counter;
          tempValue.ip = rsu.ip;
          availableRsuList.push(tempValue);
          counter += 1;
        }
      }
    }
    setAvailableRsuList(availableRsuList);
  };

  useEffect(() => {
    setSelectedRsuList([]);
    updateAvailableRsuList(selectedOrg);
    setLoading(false);
  }, [selectedOrg]);

  const fetchGetRsuData = async (rsu_ip) => {
    if (props.isLoginActive()) {
      props.setLoading(true);
      try {
        const res = await fetch(
          EnvironmentVars.adminRsu + "?rsu_ip=" + rsu_ip,
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

  const rsuOnDelete = async (rsu) => {
    let promises = [];
    const rsuData = await fetchGetRsuData(rsu.ip);
    if (rsuData?.rsu_data?.organizations?.length > 1) {
      let patchJson = props.orgPatchJson;
      patchJson.rsus_to_remove = [rsu.ip];
      promises.push(props.fetchPatchOrganization(patchJson));
    } else {
      alert(
        "Cannot remove RSU " +
          rsu.ip +
          " from " +
          props.selectedOrg +
          " because it must belong to at least one organization."
      );
    }
    Promise.all(promises).then(() => {
      refresh();
    });
  };

  const rsuMultiDelete = async (rows) => {
    let promises = [];
    for (const row of rows) {
      const rsuData = await fetchGetRsuData(row.ip);
      if (rsuData?.rsu_data?.organizations?.length > 1) {
        let patchJson = props.orgPatchJson;
        patchJson.rsus_to_remove = [row.ip];
        promises.push(props.fetchPatchOrganization(patchJson));
      } else {
        alert(
          "Cannot remove RSU " +
            row.ip +
            " from " +
            props.selectedOrg +
            " because it must belong to at least one organization."
        );
      }
    }
    Promise.all(promises).then(() => {
      refresh();
    });
  };

  const rsuMultiAdd = async (rsuList) => {
    let promises = [];
    for (const row of rsuList) {
      let patchJson = props.orgPatchJson;
      patchJson.rsus_to_add = [row.ip];
      promises.push(props.fetchPatchOrganization(patchJson));
    }
    Promise.all(promises).then(() => {
      refresh();
    });
  };

  const refresh = () => {
    props.updateTableData(props.selectedOrg);
    updateAvailableRsuList(props.selectedOrg);
    setSelectedRsuList([]);
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
    <div className="accordion">
      <ThemeProvider theme={accordionTheme}>
        <Accordion className="accordion-content">
          <AccordionSummary
            expandIcon={<ExpandMoreIcon className="expand" />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            <Typography style={{ fontSize: "18px" }}>
              {props.selectedOrg} RSUs
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
                      <Typography>Add RSUs to {props.selectedOrg}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div className="spacer-large-rsu">
                        <Multiselect
                          className="org-multiselect"
                          dataKey="id"
                          textField="ip"
                          data={availableRsuList}
                          value={selectedRsuList}
                          placeholder="Click to add RSUs"
                          onChange={(value) => {
                            setSelectedRsuList(value);
                          }}
                        />

                        <button
                          key="rsu_plus_button"
                          className="admin-button"
                          onClick={(value) => {
                            rsuMultiAdd(selectedRsuList);
                          }}
                          title="Add RSUs To Organization"
                        >
                          <AiOutlinePlusCircle size={20} />
                        </button>
                      </div>
                    </AccordionDetails>
                  </Accordion>
                </ThemeProvider>
              </div>,
              <div>
                <AdminTable
                  title={"Modify RSU-Organization Assignment"}
                  data={props.tableData}
                  columns={rsuColumns}
                  actions={rsuActions}
                />
              </div>,
            ]}
          </AccordionDetails>
        </Accordion>
      </ThemeProvider>
    </div>
  );
};

export default AdminOrganizationTabRsu;

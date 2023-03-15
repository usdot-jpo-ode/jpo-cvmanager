import React, { useState, useEffect } from "react";
import AdminAddRsu from "./AdminAddRsu";
import AdminEditRsu from "./AdminEditRsu";
import AdminTable from "./AdminTable";
import EnvironmentVars from "../EnvironmentVars";
import { IoChevronBackCircleOutline, IoRefresh } from "react-icons/io5";
import { AiOutlinePlusCircle } from "react-icons/ai";
import { confirmAlert } from "react-confirm-alert";
import { Options } from "./AdminDeletionOptions";
import { useSelector } from "react-redux";
import { selectLoading } from "../slices/rsuSlice";

import "../components/css/Admin.css";

const AdminRsuTab = (props) => {
  const { authLoginData, isLoginActive, setLoading, updateRsuData } = props;
  const [activeDiv, setActiveDiv] = useState("rsu_table");
  const [tableData, setTableData] = useState([]);
  const [title, setTitle] = useState("RSUs");
  const [columns] = useState([
    { title: "Milepost", field: "milepost", id: 0 },
    { title: "IP Address", field: "ip", id: 1 },
    { title: "Primary Route", field: "primary_route", id: 2 },
    { title: "RSU Model", field: "model", id: 3 },
    { title: "Serial Number", field: "serial_number", id: 4 },
  ]);
  const loading = useSelector(selectLoading);

  let tableActions = [
    {
      icon: "delete",
      tooltip: "Delete RSU",
      position: "row",
      onClick: (event, rowData) => {
        const buttons = [
          {
            label: "Yes",
            onClick: () => onDelete(rowData),
          },
          {
            label: "No",
            onClick: () => {},
          },
        ];
        const alertOptions = Options(
          "Delete RSU",
          'Are you sure you want to delete "' + rowData.ip + '"?',
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
    {
      icon: "edit",
      tooltip: "Edit RSU",
      position: "row",
      onClick: (event, rowData) => onEdit(rowData),
    },
    {
      tooltip: "Remove All Selected From Organization",
      icon: "delete",
      onClick: (event, rowData) => {
        const buttons = [
          {
            label: "Yes",
            onClick: () => multiDelete(rowData),
          },
          {
            label: "No",
            onClick: () => {},
          },
        ];
        const alertOptions = Options(
          "Delete Selected RSUs",
          "Are you sure you want to delete " + rowData.length + " RSUs?",
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
  ];
  const [editRsuRowData, setEditRsuRowData] = useState({});

  const fetchTableData = async () => {
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

  const updateTableData = async () => {
    updateRsuData();
    const data = await fetchTableData();
    setTableData(data?.rsu_data);
    setLoading(false);
  };

  useEffect(() => {
    updateTableData();
  }, []);

  useEffect(() => {
    if (activeDiv === "rsu_table") {
      setTitle("CV Manager RSUs");
    } else if (activeDiv === "edit_rsu") {
      setTitle("Edit RSU");
    } else if (activeDiv === "add_rsu") {
      setTitle("Add RSU");
    }
  }, [activeDiv]);

  const fetchDeleteRsu = async (rsu_ip) => {
    if (props.isLoginActive()) {
      props.setLoading(true);

      try {
        const res = await fetch(
          EnvironmentVars.adminRsu + "?rsu_ip=" + rsu_ip,
          {
            method: "DELETE",
            headers: {
              "Content-Type": "application/json",
              Authorization: props.authLoginData["token"],
            },
          }
        );

        const status = res.status;
        if (status === 200) {
          console.debug("Successfully deleted RSU: " + rsu_ip);
        } else if (status === 500) {
        }
      } catch (exception_var) {
        props.setLoading(false);
        console.error(exception_var);
      }
    }
    props.setLoading(false);
  };

  const onEdit = (row) => {
    setEditRsuRowData(row);
    setActiveDiv("edit_rsu");
  };

  const onDelete = (row) => {
    fetchDeleteRsu(row.ip).then(() => updateTableData());
  };

  const multiDelete = (rows) => {
    let promises = [];
    for (const row of rows) {
      promises.push(fetchDeleteRsu(row.ip));
    }
    Promise.all(promises).then(() => updateTableData());
  };

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeDiv !== "rsu_table" && (
            <button
              key="rsu_table"
              className="admin_table_button"
              onClick={(value) => {
                setActiveDiv("rsu_table");
              }}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeDiv === "rsu_table" && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={(value) => {
                setActiveDiv("add_rsu");
              }}
              title="Add RSU"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={(value) => {
                updateTableData();
              }}
              title="Refresh RSUs"
            >
              <IoRefresh size={20} />
            </button>,
          ]}
        </h3>
      </div>
      {activeDiv === "rsu_table" && loading === false && (
        <div className="scroll-div-tab">
          <AdminTable
            title={""}
            data={tableData}
            columns={columns}
            actions={tableActions}
            onEdit={onEdit}
            onDelete={onDelete}
            multiDelete={multiDelete}
          />
        </div>
      )}

      {activeDiv === "add_rsu" && (
        <div className="scroll-div-tab">
          <AdminAddRsu
            authLoginData={props.authLoginData}
            isLoginActive={props.isLoginActive}
            setLoading={props.setLoading}
            updateRsuData={updateTableData}
          />
        </div>
      )}

      {activeDiv === "edit_rsu" && (
        <div className="scroll-div-tab">
          <AdminEditRsu
            rsuData={editRsuRowData}
            authLoginData={props.authLoginData}
            isLoginActive={props.isLoginActive}
            setLoading={props.setLoading}
            updateRsuData={updateTableData}
          />
        </div>
      )}
    </div>
  );
};

export default AdminRsuTab;

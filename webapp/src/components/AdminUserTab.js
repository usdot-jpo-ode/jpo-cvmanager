import React, { useState, useEffect } from "react";
import AdminAddUser from "./AdminAddUser";
import AdminEditUser from "./AdminEditUser";
import AdminTable from "./AdminTable";
import EnvironmentVars from "../EnvironmentVars";
import { IoChevronBackCircleOutline, IoRefresh } from "react-icons/io5";
import { AiOutlinePlusCircle } from "react-icons/ai";
import { confirmAlert } from "react-confirm-alert";
import { Options } from "./AdminDeletionOptions";
import { useSelector } from "react-redux";
import { selectLoading } from "../slices/rsuSlice";

import "../components/css/Admin.css";

const AdminUserTab = (props) => {
  const { authLoginData, isLoginActive, setLoading } = props;
  const [activeDiv, setActiveDiv] = useState("user_table");
  const [tableData, setTableData] = useState([]);
  const [title, setTitle] = useState("Users");
  const [columns] = useState([
    { title: "First Name", field: "first_name", id: 0 },
    { title: "Last Name", field: "last_name", id: 1 },
    { title: "Email", field: "email", id: 2 },
    {
      title: "Super User",
      field: "super_user",
      id: 3,
      render: (rowData) => (rowData.super_user ? "Yes" : "No"),
    },
  ]);
  const loading = useSelector(selectLoading);

  let tableActions = [
    {
      icon: "delete",
      tooltip: "Delete User",
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
          "Delete User",
          'Are you sure you want to delete "' + rowData.email + '"?',
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
    {
      icon: "edit",
      tooltip: "Edit User",
      position: "row",
      onClick: (event, rowData) => onEdit(rowData),
    },
    {
      tooltip: "Remove All Selected Users",
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
          "Delete Selected Users",
          "Are you sure you want to delete " + rowData.length + " users?",
          buttons
        );
        confirmAlert(alertOptions);
      },
    },
  ];
  const [editUserRowData, setEditUserRowData] = useState({});

  const fetchTableData = async () => {
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

  const updateTableData = async () => {
    const data = await fetchTableData();
    let tempData = [];
    let i = 0;
    for (const x in data?.user_data) {
      const temp = data.user_data[x];
      temp.id = i;
      tempData.push(temp);
      i = i + 1;
    }
    setTableData(tempData);
    setLoading(false);
  };

  useEffect(() => {
    updateTableData();
  }, []);

  useEffect(() => {
    if (activeDiv === "user_table") {
      setTitle("CV Manager Users");
    } else if (activeDiv === "edit_user") {
      setTitle("Edit User");
    } else if (activeDiv === "add_user") {
      setTitle("Add User");
    }
  }, [activeDiv]);

  const fetchDeleteUser = async (user_email) => {
    if (props.isLoginActive()) {
      props.setLoading(true);

      try {
        const res = await fetch(
          EnvironmentVars.adminUser + "?user_email=" + user_email,
          {
            method: "DELETE",
            headers: {
              "Content-Type": "application/json",
              Authorization: props.authLoginData["token"],
            },
          }
        );

        const status = res.status;
        const data = await res.json();

        props.setLoading(false);
        if (status === 200) {
          console.log("Successfully deleted User: " + user_email);
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

  const onEdit = (row) => {
    setEditUserRowData(row);
    setActiveDiv("edit_user");
  };

  const onDelete = (row) => {
    fetchDeleteUser(row.email).then(() => updateTableData());
  };

  const multiDelete = (rows) => {
    let promises = [];
    for (const row of rows) {
      promises.push(fetchDeleteUser(row.email));
    }
    Promise.all(promises).then(() => updateTableData());
  };

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeDiv !== "user_table" && (
            <button
              key="user_table"
              className="admin_table_button"
              onClick={(value) => {
                setActiveDiv("user_table");
              }}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeDiv === "user_table" && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={(value) => {
                setActiveDiv("add_user");
              }}
              title="Add User"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={(value) => {
                updateTableData();
              }}
              title="Refresh Users"
            >
              <IoRefresh size={20} />
            </button>,
          ]}
        </h3>
      </div>
      {activeDiv === "user_table" && loading === false && (
        <div className="scroll-div-tab">
          <AdminTable
            title={""}
            data={tableData}
            columns={columns}
            actions={tableActions}
          />
        </div>
      )}

      {activeDiv === "add_user" && (
        <div className="scroll-div-tab">
          <AdminAddUser
            authLoginData={props.authLoginData}
            isLoginActive={props.isLoginActive}
            setLoading={props.setLoading}
            updateUserData={updateTableData}
          />
        </div>
      )}

      {activeDiv === "edit_user" && (
        <div className="scroll-div-tab">
          <AdminEditUser
            userData={editUserRowData}
            authLoginData={props.authLoginData}
            isLoginActive={props.isLoginActive}
            setLoading={props.setLoading}
            updateUserData={updateTableData}
          />
        </div>
      )}
    </div>
  );
};

export default AdminUserTab;

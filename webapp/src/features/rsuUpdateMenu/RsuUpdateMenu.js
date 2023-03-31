import React from "react";
import { confirmAlert } from "react-confirm-alert";
import RsuUpdateItem from "../../components/RsuUpdateItem";
import "react-confirm-alert/src/react-confirm-alert.css";
import {
  selectChecked,
  selectOsUpdateAvailable,
  selectFwUpdateAvailable,

  // actions
  updateRsuData,
  performOSUpdate,
  performFWUpdate,
} from "./rsuUpdateMenuSlice";
import { useSelector, useDispatch } from "react-redux";

import "./rsuUpdateMenu.css";

const RsuUpdateMenu = (props) => {
  const dispatch = useDispatch();
  const checked = useSelector(selectChecked);
  const osUpdateAvailable = useSelector(selectOsUpdateAvailable);
  const fwUpdateAvailable = useSelector(selectFwUpdateAvailable);

  const verifyOS = {
    title: "RSU OS Update",
    message: "Are you sure you want to update the RSU's operating system?",
    buttons: [
      {
        label: "Yes",
        onClick: () => dispatch(performOSUpdate()),
      },
      {
        label: "No",
        onClick: () => {},
      },
    ],
    childrenElement: () => <div />,
    closeOnEscape: true,
    closeOnClickOutside: true,
    keyCodeForClose: [8, 32],
    willUnmount: () => {},
    afterClose: () => {},
    onClickOutside: () => {},
    onKeypressEscape: () => {},
  };

  const verifyFW = {
    title: "RSU Firmware Update",
    message: "Are you sure you want to update the RSU's firmware?",
    buttons: [
      {
        label: "Yes",
        onClick: () => dispatch(performFWUpdate()),
      },
      {
        label: "No",
        onClick: () => {},
      },
    ],
    childrenElement: () => <div />,
    closeOnEscape: true,
    closeOnClickOutside: true,
    keyCodeForClose: [8, 32],
    willUnmount: () => {},
    afterClose: () => {},
    onClickOutside: () => {},
    onKeypressEscape: () => {},
  };

  const handleUpdateOS = () => {
    confirmAlert(verifyOS);
  };

  const handleUpdateFW = () => {
    confirmAlert(verifyFW);
  };

  return (
    <div id="snmpdiv">
      <div id="updatediv">
        <h2 id="snmpheader">Administrator Update</h2>

        <button id="updatebtn" onClick={() => dispatch(updateRsuData(props.ipList))}>
          Check For Updates
        </button>

        {Object.entries(props.ipList).map((rsu) => (
          <RsuUpdateItem
            key={rsu[0]}
            ip={rsu[1]["properties"]["Ipv4Address"]}
            osUpdateAvailable={osUpdateAvailable}
            fwUpdateAvailable={fwUpdateAvailable}
            handleUpdateOS={handleUpdateOS}
            handleUpdateFW={handleUpdateFW}
          />
        ))}
      </div>
    </div>
  );
};

export default RsuUpdateMenu;

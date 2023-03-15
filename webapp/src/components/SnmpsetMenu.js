import React from "react";
import { useDispatch, useSelector } from "react-redux";

import {
  selectChangeSuccess,
  selectErrorState,
  selectDestIp,
  selectSnmpMsgType,
  selectSnmpFilterMsg,
  selectSnmpFilterErr,

  // Actions
  submitSnmpSet,
  filterSnmp,
  setDestIp,
  setMsgType,
} from "../slices/configSlice";

import { selectRsuIpv4, selectRsuManufacturer } from "../slices/rsuSlice";

import "./css/SnmpwalkMenu.css";

const SnmpsetMenu = () => {
  const dispatch = useDispatch();

  const changeSuccess = useSelector(selectChangeSuccess);
  const errorState = useSelector(selectErrorState);
  const destIp = useSelector(selectDestIp);
  const snmpMsgType = useSelector(selectSnmpMsgType);
  const snmpFilterMsg = useSelector(selectSnmpFilterMsg);
  const snmpFilterErr = useSelector(selectSnmpFilterErr);

  const rsuIp = useSelector(selectRsuIpv4);
  const rsuManufacturer = useSelector(selectRsuManufacturer);

  return (
    <div id="snmpdiv">
      <h2 id="snmpheader">Add Message Forwarding</h2>

      <form id="snmpform">
        <label id="snmplabel">
          <strong>Destination IP:</strong>
          <input
            id="snmpinput"
            type="text"
            value={destIp}
            onChange={(e) => dispatch(setDestIp(e.target.value))}
          />
        </label>
        <label id="snmplabel">
          <strong>Message Type:</strong>
          <select
            id="snmpdropdown"
            value={snmpMsgType}
            onChange={(e) => dispatch(setMsgType(e.target.value))}
          >
            <option value="bsm">BSM</option>
            <option value="spat">SPaT</option>
            <option value="map">MAP</option>
            <option value="srm">SRM</option>
            <option value="ssm">SSM</option>
          </select>
        </label>
      </form>

      <button id="refreshbtn" onClick={() => dispatch(submitSnmpSet([rsuIp]))}>
        Submit Config
      </button>

      {changeSuccess ? (
        <div>
          <p id="successtext">Successful write to RSU</p>
          <p id="infotext">
            Only message type and index is required for delete
          </p>
        </div>
      ) : (
        <p id="infotext">Only message type and index is required for delete</p>
      )}
      {errorState !== "" ? <p id="warningtext">{errorState}</p> : <div />}

      {rsuManufacturer === "Yunex" ? (
        <div>
          <p id="snmpfiltertext" marginTop="40px">
            Yunex RSUs use different SNMP tables for message TX and RX
            forwarding. <br /> BSM and SSM are on the RX table. MAP, SPaT and
            SRM are on the TX table. <br /> Start over from the 1 index for each
            table.
          </p>
        </div>
      ) : (
        <div />
      )}

      {rsuManufacturer === "Commsignia" ? (
        <div>
          <p id="snmpfiltertext" marginTop="40px">
            If you are configuring SPaT or MAP forwarding, apply the TX message{" "}
            <br /> filter after your configuration has been applied
          </p>
          <button id="refreshbtn" onClick={() => dispatch(filterSnmp([rsuIp]))}>
            Apply TX Filter
          </button>
          {snmpFilterMsg !== "" ? (
            <div>
              {snmpFilterErr === true ? (
                <p id="warningtext">{snmpFilterMsg}</p>
              ) : (
                <p id="successtext">{snmpFilterMsg}</p>
              )}
            </div>
          ) : (
            <div />
          )}
        </div>
      ) : (
        <div />
      )}
    </div>
  );
};

export default SnmpsetMenu;

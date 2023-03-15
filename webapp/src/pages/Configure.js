import React from "react";
import SnmpwalkMenu from "./../components/SnmpwalkMenu";
import SnmpsetMenu from "./../components/SnmpsetMenu";
import RsuRebootMenu from "./../components/RsuRebootMenu";
import { useSelector } from "react-redux";
import {
  selectRsuManufacturer,
  selectRsuIpv4,
  selectSelectedRsu,
} from "../slices/rsuSlice";
import { selectRole } from "../slices/userSlice";

const windowStyle = {
  height: "calc(100vh - 145px)",
  overflow: "auto",
  paddingBottom: "10px",
};

const headerStyle = {
  color: "white",
  fontFamily: "Arial, Helvetica, sans-serif",
  fontWeight: "550",
  margin: "15px 10px",
};

// Static object to define which manufacturers the configuration menu supports for each feature
const supported_manufacturers = {
  snmp: ["Kapsch", "Commsignia", "Yunex"],
  reboot: ["Kapsch", "Commsignia"],
};

const Configure = () => {
  const role = useSelector(selectRole);

  const selectedRsu = useSelector(selectSelectedRsu);
  const rsuManuacturer = useSelector(selectRsuManufacturer);
  const rsuIpv4 = useSelector(selectRsuIpv4);

  return (
    <div style={windowStyle}>
      {selectedRsu ? (
        <div>
          <h1 style={headerStyle}>{rsuIpv4 + " Configuration"}</h1>
          <div>
            {supported_manufacturers.snmp.includes(rsuManuacturer) && (
              <SnmpwalkMenu />
            )}
            {["admin", "operator"].includes(role) &&
              supported_manufacturers.snmp.includes(rsuManuacturer) && (
                <SnmpsetMenu />
              )}
            {["admin"].includes(role) &&
              supported_manufacturers.reboot.includes(rsuManuacturer) && (
                <RsuRebootMenu />
              )}
          </div>
        </div>
      ) : (
        <h2 style={headerStyle}>Select a RSU to configure on the Map tab</h2>
      )}
    </div>
  );
};

export default Configure;

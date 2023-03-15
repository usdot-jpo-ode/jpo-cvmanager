import React from "react";
import AdminFormManager from "../components/AdminFormManager.js";
import { Tab, Tabs, TabList, TabPanel } from "react-tabs";

import "../components/css/Admin.css";

function Admin(props) {
  return (
    <div id="admin">
      <h2 className="adminHeader">CV Manager Admin Interface</h2>
      <Tabs>
        <TabList>
          <Tab>
            <p>RSUs</p>
          </Tab>
          <Tab>
            <p>Users</p>
          </Tab>
          <Tab>
            <p>Organizations</p>
          </Tab>
        </TabList>

        <TabPanel>
          <div className="panel-content">
            <AdminFormManager
              activeForm={"add_rsu"}
              authLoginData={props.authLoginData}
              isLoginActive={props.isLoginActive}
              setLoading={props.setLoading}
              updateRsuData = {props.updateRsuData}
            />
          </div>
        </TabPanel>
        <TabPanel>
          <div className="panel-content">
            <AdminFormManager
              activeForm={"add_user"}
              authLoginData={props.authLoginData}
              isLoginActive={props.isLoginActive}
              setLoading={props.setLoading}
            />
          </div>
        </TabPanel>
        <TabPanel>
          <div className="panel-content">
            <AdminFormManager
              activeForm={"add_organization"}
              authLoginData={props.authLoginData}
              isLoginActive={props.isLoginActive}
              setLoading={props.setLoading}
            />
          </div>
        </TabPanel>
      </Tabs>
    </div>
  );
}

export default Admin;

import React, { Component } from "react";
import AdminRsuTab from "./AdminRsuTab";
import AdminUserTab from "./AdminUserTab";
import AdminOrganizationTab from "./AdminOrganizationTab";

import "../components/css/Admin.css";

class AdminFormManager extends Component {
    render() {
        return (
        <div className="scroll-div">
            {(() => {
            if (this.props.activeForm === "add_rsu") {
                return (
                <AdminRsuTab
                    authLoginData={this.props.authLoginData}
                    isLoginActive={this.props.isLoginActive}
                    setLoading={this.props.setLoading}
                    updateRsuData = {this.props.updateRsuData}
                />
                );
            } else if (this.props.activeForm === "add_user") {
                return (
                <AdminUserTab
                    authLoginData={this.props.authLoginData}
                    isLoginActive={this.props.isLoginActive}
                    setLoading={this.props.setLoading}
                />
                );
            } else if (this.props.activeForm === "add_organization") {
                return (
                <AdminOrganizationTab
                    authLoginData={this.props.authLoginData}
                    isLoginActive={this.props.isLoginActive}
                    setLoading={this.props.setLoading}
                />
                );
            }
            })()}
        </div>
        );
    }
}

export default AdminFormManager;
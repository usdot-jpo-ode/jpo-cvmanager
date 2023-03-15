import React, { Component } from 'react';
import EnvironmentVars from '../EnvironmentVars';
import { confirmAlert } from 'react-confirm-alert';
import DeleteIcon from '@mui/icons-material/Delete';
import 'react-confirm-alert/src/react-confirm-alert.css';
import {Options} from './AdminDeletionOptions';

import './css/Admin.css';

class AdminOrganizationDeleteMenu extends Component {
  constructor(props) {
    super(props);
    this.state = {
      changeSuccess: false
    }
  }

  deleteOrg = async () => {
    if (this.props.isLoginActive()) {
      this.props.setLoading(true);

      try {
        const res = await fetch(EnvironmentVars.adminOrg + '?org_name=' + this.props.selectedOrg, {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: this.props.authLoginData["token"],
          },
        });

        const status = res.status;
        const data = await res.json();
        if (status === 200) {
          console.debug("Successfully deleted Organization: " + this.props.selectedOrg);
          this.props.updateOrganizationData();
        } else if (status === 500) {
          console.error(data);
        }
      } catch (exception_var) {
        this.props.setLoading(false);
        console.error(exception_var);
      }
    }
    this.props.setLoading(false);
  }

  handleDelete = () => {
    const buttons = [
      {
        label: 'Yes',
        onClick: () => this.deleteOrg()
      },
      {
        label: 'No',
        onClick: () => {}
      }
    ]
    const alertOptions = Options(
      'Delete Organization', 
      'Are you sure you want to delete the "' + this.props.selectedOrg + '" organization?', 
      buttons);
    confirmAlert(alertOptions);
  }

  render() {
    return (
      <div>
        <button className="delete_button" onClick={this.handleDelete} title="Delete Organization"><DeleteIcon size={20}/></button>
      </div>
    )
  }
}

export default AdminOrganizationDeleteMenu;
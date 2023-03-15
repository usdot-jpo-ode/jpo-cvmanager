import React from "react";
import MaterialTable from "@material-table/core";
import { ThemeProvider, createTheme } from "@mui/material";

import "../components/css/Admin.css";

const AdminTable = (props) => {
  const defaultMaterialTheme = createTheme({
    palette: {
      common: {
        black: '#000000',
        white: '#ffffff',
      },
      primary: {
        main: '#d16d15',
        light: '#0e2052',
        contrastTextColor: '#0e2052'
      },
      secondary: {
        main: '#d16d15',
        light: '#0e2052',
        contrastTextColor: '#0e2052'
      },
      text: {
        primary: "#ffffff",
        secondary: "#ffffff",
        disabled: "#ffffff",
        hint: "#ffffff"
      },
      divider: '#333',
      background:{
        paper: "#333",
        default: "#1c1d1f"
      }
    },
    components: {
      MuiIcon: {
        styleOverrides: {
          root: {
            // Match 24px = 3 * 2 + 1.125 * 16
            color: "#d16d15"
          },
        },
      },
      TextField: {
        
      }
    },
    input: {
      color: "#11ff00",
    },
  });

  return (
    <div>
      <ThemeProvider theme={defaultMaterialTheme}>
        <MaterialTable
          actions={props.actions}
          columns={props.columns}
          data={props.data}
          title={props.title}
          editable = {props.editable}
          options={{
            selection: true,
            actionsColumnIndex: -1,
            tableLayout: "fixed",
            rowStyle: {
              overflowWrap: "break-word"
            },
          }}
          
        />
      </ThemeProvider>
    </div>
  );
};
export default AdminTable;

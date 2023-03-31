import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { selectToken } from "../../generalSlices/userSlice";
import EnvironmentVars from "../../EnvironmentVars";
import apiHelper from "../../apis/api-helper";

const initialState = {
  activeDiv: "organization_table",
  title: "Organizations",
  orgData: [],
  selectedOrg: [],
  rsuTableData: [],
  userTableData: [],
  errorState: false,
  errorMsg: "",
};

export const getOrgData = createAsyncThunk(
  "adminOrganizationTab/getOrgData",
  async (payload, { getState }) => {
    const { orgName, all, specifiedOrg } = payload;
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminOrg,
      token,
      query_params: { org_name: orgName },
    });

    switch (data.status) {
      case 200:
        return { success: true, message: "", data: data.body, all: all ?? false, specifiedOrg };
      case 400:
      case 500:
        return { success: false, message: data.message };
    }
    return data;
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const deleteOrg = createAsyncThunk(
  "adminOrganizationTab/deleteOrg",
  async (org, { getState, dispatch }) => {
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._deleteData({
      url: EnvironmentVars.adminOrg,
      token,
      query_params: { org_name: org },
    });

    switch (data.status) {
      case 200:
        console.debug("Successfully deleted Organization: " + org);
        dispatch(getOrgData({ orgName: "all", all: true }));
        return;
      case 400:
      case 500:
        console.error(data);
        return;
      default:
        return;
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const editOrg = createAsyncThunk(
  "adminEditUser/editOrg",
  async (json, { getState }) => {
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminOrg,
      token,
      body: JSON.stringify(json),
    });

    switch (data.status) {
      case 200:
        console.debug("PATCH successful ", json);
        return { success: true, message: "" };
      case 400:
      case 500:
        return { success: false, message: data.message };
      default:
        return { success: false, message: data.message };
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const adminOrganizationTabSlice = createSlice({
  name: "adminOrganizationTab",
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateTitle: (state) => {
      if (state.value.activeDiv === "organization_table") {
        state.value.title = "CV Manager Organizations";
      } else if (state.value.activeDiv === "edit_organization") {
        state.value.title = "Edit Organization";
      } else if (state.value.activeDiv === "add_organization") {
        state.value.title = "Add Organization";
      }
    },
    setActiveDiv: (state, action) => {
      state.value.activeDiv = action.payload;
    },
    setSelectedOrg: (state, action) => {
      state.value.selectedOrg = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getOrgData.pending, (state) => {
        state.loading = true;
      })
      .addCase(getOrgData.fulfilled, (state, action) => {
        state.loading = false;
        if (action.payload.success) {
          state.value.errorMsg = "";
          state.value.errorState = false;
          const data = action.payload.data;
          if (action.payload.all) {
            let tempData = [];
            let i = 0;
            for (const x in data?.org_data) {
              const temp = data?.org_data[x];
              temp.id = i;
              tempData.push(temp);
              i += 1;
            }
            state.value.orgData = tempData;
            if (action.payload.specifiedOrg) {
              for (let i = 0; i < tempData.length; i++) {
                if (tempData[i].name === action.payload.specifiedOrg) {
                  state.value.selectedOrg = tempData[i];
                  break;
                }
              }
            } else {
              state.value.selectedOrg = tempData[0];
            }
          } else {
            state.value.rsuTableData = data?.org_data?.org_rsus;
            state.value.userTableData = data?.org_data?.org_users;
          }
        } else {
          state.value.errorMsg = action.payload.message;
          state.value.errorState = true;
        }
        state.loading = false;
      })
      .addCase(getOrgData.rejected, (state) => {
        state.loading = false;
      })
      .addCase(editOrg.pending, (state) => {
        state.loading = true;
      })
      .addCase(editOrg.fulfilled, (state, action) => {
        state.loading = false;
        if (action.payload.success) {
          state.value.errorMsg = "";
          state.value.errorState = false;
        } else {
          state.value.errorMsg = action.payload.message;
          state.value.errorState = true;
        }
      })
      .addCase(editOrg.rejected, (state) => {
        state.loading = false;
      });
  },
});

export const { updateTitle, setActiveDiv, setSelectedOrg } = adminOrganizationTabSlice.actions;

export const selectLoading = (state) => state.adminOrganizationTab.loading;
export const selectActiveDiv = (state) => state.adminOrganizationTab.value.activeDiv;
export const selectTitle = (state) => state.adminOrganizationTab.value.title;
export const selectOrgData = (state) => state.adminOrganizationTab.value.orgData;
export const selectSelectedOrg = (state) => state.adminOrganizationTab.value.selectedOrg;
export const selectRsuTableData = (state) => state.adminOrganizationTab.value.rsuTableData;
export const selectUserTableData = (state) => state.adminOrganizationTab.value.userTableData;
export const selectErrorState = (state) => state.adminOrganizationTab.value.errorState;
export const selectErrorMsg = (state) => state.adminOrganizationTab.value.errorMsg;

export default adminOrganizationTabSlice.reducer;

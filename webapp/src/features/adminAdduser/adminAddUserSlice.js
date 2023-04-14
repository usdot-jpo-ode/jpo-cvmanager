import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { selectToken, selectOrganizationName } from "../../generalSlices/userSlice";
import EnvironmentVars from "../../EnvironmentVars";
import apiHelper from "../../apis/api-helper";
import { getAvailableUsers } from "../adminUserTab/adminUserTabSlice";

const initialState = {
  successMsg: "",
  selectedOrganizationNames: [],
  selectedOrganizations: [],
  organizationNames: [],
  availableRoles: [],
  apiData: {},
  errorState: false,
  errorMsg: "",
  submitAttempt: false,
};

export const getUserData = createAsyncThunk(
  "adminAddUser/getUserData",
  async (_, { getState }) => {
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminAddUser,
      token,
      additional_headers: { "Content-Type": "application/json" },
    });

    switch (data.status) {
      case 200:
        return { success: true, message: "", data: data.body };
      case 400:
      case 500:
        return { success: false, message: data.message };
      default:
        return;
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const createUser = createAsyncThunk(
  "adminAddUser/createUser",
  async (payload, { getState, dispatch }) => {
    const { json, reset } = payload;
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._postData({
      url: EnvironmentVars.adminAddUser,
      token,
      body: JSON.stringify(json),
    });

    switch (data.status) {
      case 200:
        dispatch(getAvailableUsers());
        dispatch(resetForm(reset));
        return { success: true, message: "User Creation is successful." };
      case 400:
      case 500:
        return { success: false, message: data.message };
      default:
        return { success: false, message: data.message };
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const resetForm = createAsyncThunk("adminAddUser/resetForm", async (reset, { dispatch }) => {
  reset();
  setTimeout(() => dispatch(adminAddUserSlice.actions.setSuccessMsg("")), 5000);
});

export const submitForm = createAsyncThunk(
  "adminAddUser/submitForm",
  async (payload, { getState, dispatch }) => {
    const { data, reset } = payload;
    const currentState = getState();
    const selectedOrganizations = selectSelectedOrganizations(currentState);
    if (selectedOrganizations.length !== 0) {
      const submitOrgs = [...selectedOrganizations].map((org) => ({ ...org }));
      submitOrgs.forEach((elm) => delete elm.id);
      const tempData = { ...data };
      tempData["organizations"] = submitOrgs;
      dispatch(createUser({ json: tempData, reset }));
      return false;
    } else {
      return true;
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const adminAddUserSlice = createSlice({
  name: "adminAddUser",
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateOrganizationNamesApiData: (state) => {
      if (Object.keys(state.value.apiData).length !== 0) {
        let orgData = [];
        state.value.apiData.organizations.forEach((organization, index) => orgData.push({ id: index, name: organization }));
        state.value.organizationNames = orgData;
      }
    },
    updateAvailableRolesApiData: (state) => {
      if (Object.keys(state.value.apiData).length !== 0) {
        let roleData = [];
        state.value.apiData.roles.forEach((role) => roleData.push({ role }));
        state.value.availableRoles = roleData;
      }
    },
    updateOrganizations: (state, action) => {
      let newOrganizations = [];
      for (const name of action.payload) {
        if (state.value.selectedOrganizations.some((e) => e.name === name.name)) {
          var index = state.value.selectedOrganizations.findIndex(function (item, i) {
            return item.name === name.name;
          });
          newOrganizations.push(state.value.selectedOrganizations[index]);
        } else if (!state.value.selectedOrganizations.some((e) => e.name === name.name)) {
          newOrganizations.push({ ...name, role: state.value.availableRoles[0].role });
        }
      }
      state.value.selectedOrganizations = newOrganizations;
      state.value.selectedOrganizationNames = action.payload;
    },
    setSuccessMsg: (state, action) => {
      state.value.successMsg = action.payload;
    },
    setSelectedRole: (state, action) => {
      const selectedOrganizations = [...state.value.selectedOrganizations];
      const { name, role } = action.payload;
      const index = selectedOrganizations.findIndex((org) => org.name === name);
      selectedOrganizations[index].role = role;
      state.value.selectedOrganizations = selectedOrganizations;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getUserData.pending, (state) => {
        state.loading = true;
      })
      .addCase(getUserData.fulfilled, (state, action) => {
        state.loading = false;
        if (action.payload.success) {
          state.value.apiData = action.payload.data;
          state.value.errorMsg = "";
          state.value.errorState = false;
        } else {
          state.value.errorMsg = action.payload.message;
          state.value.errorState = true;
        }
      })
      .addCase(getUserData.rejected, (state) => {
        state.loading = false;
      })
      .addCase(createUser.pending, (state) => {
        state.loading = true;
      })
      .addCase(createUser.fulfilled, (state, action) => {
        state.loading = false;
        if (action.payload.success) {
          state.value.errorMsg = "";
          state.value.errorState = false;
          state.value.successMsg = action.payload.message;
        } else {
          state.value.errorMsg = action.payload.message;
          state.value.errorState = true;
          state.value.successMsg = "";
        }
      })
      .addCase(createUser.rejected, (state) => {
        state.loading = false;
      })
      .addCase(resetForm.fulfilled, (state, action) => {
        state.value.selectedOrganizations = [];
        state.value.selectedOrganizationNames = [];
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload;
      });
  },
});

export const { updateOrganizationNamesApiData, updateAvailableRolesApiData, updateOrganizations, setSuccessMsg, setSelectedRole } = adminAddUserSlice.actions;

export const selectLoading = (state) => state.adminAddUser.loading;
export const selectSuccessMsg = (state) => state.adminAddUser.value.successMsg;
export const selectSelectedOrganizationNames = (state) => state.adminAddUser.value.selectedOrganizationNames;
export const selectSelectedOrganizations = (state) => state.adminAddUser.value.selectedOrganizations;
export const selectOrganizationNames = (state) => state.adminAddUser.value.organizationNames;
export const selectAvailableRoles = (state) => state.adminAddUser.value.availableRoles;
export const selectApiData = (state) => state.adminAddUser.value.apiData;
export const selectErrorState = (state) => state.adminAddUser.value.errorState;
export const selectErrorMsg = (state) => state.adminAddUser.value.errorMsg;
export const selectSubmitAttempt = (state) => state.adminAddUser.value.submitAttempt;

export default adminAddUserSlice.reducer;

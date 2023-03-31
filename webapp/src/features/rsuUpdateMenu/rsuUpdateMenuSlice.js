import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { selectToken } from "../../generalSlices/userSlice";
import EnvironmentVars from "../../EnvironmentVars";
import apiHelper from "../../apis/api-helper";

const initialState = {
  checked: false,
  osUpdateAvailable: [],
  fwUpdateAvailable: [],
};

export const updateRsuData = createAsyncThunk(
  "rsuUpdateMenu/updateRsuData",
  async (ipListProp, { getState }) => {
    const currentState = getState();
    const token = selectToken(currentState);

    let ipList = Object.entries(ipListProp).map((rsu) => {
      return rsu[1];
    });
    let osUpdateAvailableList = [];
    let fwUpdateAvailableList = [];
    for (let i = 0; i < ipListProp.length; i++) {
      try {
        const data = await apiHelper._postData({
          url: EnvironmentVars.rsuRestAuth,
          token,
          body: JSON.stringify({
            command: "checkforupdates",
            rsu_ip: ipList[i]["properties"]["Ipv4Address"],
            args: {},
          }),
        }).body;

        if (data.os === true) {
          osUpdateAvailableList.push(ipList[i].properties.Ipv4Address);
        }
        if (data.firmware === true) {
          fwUpdateAvailableList.push(ipList[i].properties.Ipv4Address);
        }
      } catch (error) {
        console.error(error);
      }
    }
    return { osUpdateAvailableList, fwUpdateAvailableList };
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const performOSUpdate = createAsyncThunk(
  "rsuUpdateMenu/performOSUpdate",
  async (ip, { getState }) => {
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._postData({
      url: EnvironmentVars.rsuRestAuth,
      token,
      body: JSON.stringify({
        command: "osupdate",
        rsu_ip: ip,
        args: {},
      }),
    });

    switch (data.status) {
      case 200:
        console.debug(data.body);
        return;
      default:
        console.error(data.message);
        return;
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const performFWUpdate = createAsyncThunk(
  "rsuUpdateMenu/performFWUpdate",
  async (ip, { getState }) => {
    const currentState = getState();
    const token = selectToken(currentState);

    const data = await apiHelper._postData({
      url: EnvironmentVars.rsuRestAuth,
      token,
      body: JSON.stringify({
        command: "fwupdate",
        rsu_ip: ip,
        args: {},
      }),
    });

    switch (data.status) {
      case 200:
        console.log(data.body);
        return;
      default:
        console.error(data.message);
        return;
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
);

export const rsuUpdateMenuSlice = createSlice({
  name: "rsuUpdateMenu",
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(updateRsuData.pending, (state) => {
        state.loading = true;
      })
      .addCase(updateRsuData.fulfilled, (state, action) => {
        state.loading = false;
        state.value.checked = true;
        state.value.osUpdateAvailable = [...state.value.osUpdateAvailable, action.payload.osUpdateAvailableList];
        state.value.fwUpdateAvailable = [...state.value.fwUpdateAvailable, action.payload.fwUpdateAvailableList];
      })
      .addCase(updateRsuData.rejected, (state) => {
        state.loading = false;
      });
  },
});

export const {} = rsuUpdateMenuSlice.actions;

export const selectLoading = (state) => state.rsuUpdateMenu.loading;
export const selectChecked = (state) => state.rsuUpdateMenu.value.checked;
export const selectOsUpdateAvailable = (state) => state.rsuUpdateMenu.value.osUpdateAvailable;
export const selectFwUpdateAvailable = (state) => state.rsuUpdateMenu.value.fwUpdateAvailable;

export default rsuUpdateMenuSlice.reducer;

import { configureStore } from "@reduxjs/toolkit";
import rsuReducer from "./slices/rsuSlice";
import userReducer from "./slices/userSlice";
import wzdxReducer from "./slices/wzdxSlice";
import configReducer from "./slices/configSlice";

const store = configureStore({
  reducer: {
    rsu: rsuReducer,
    user: userReducer,
    wzdx: wzdxReducer,
    config: configReducer,
  },
});

export default store;

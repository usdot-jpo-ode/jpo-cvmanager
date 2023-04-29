import React, { useEffect } from "react";
import { css } from "@emotion/react";
import RingLoader from "react-spinners/RingLoader";
import { GoogleOAuthProvider } from "@react-oauth/google";
import Header from "./components/Header";
import Menu from "./features/menu/Menu";
import Help from "./components/Help";
import Admin from "./pages/Admin";
import Grid from "@material-ui/core/Grid";
import Tabs from "./components/Tabs";
import Map from "./pages/Map";
import HeatMap from "./pages/HeatMap";
import WzdxMap from "./pages/WzdxMap";
import RsuMapView from "./pages/RsuMapView";
import BsmMap from "./pages/BsmMap";
import EnvironmentVars from "./EnvironmentVars";
import "./App.css";
import { useSelector, useDispatch } from "react-redux";
import {
  selectDisplayMap,

  // Actions
  getRsuData,
} from "./generalSlices/rsuSlice";
import { selectAuthLoginData, selectRole, selectLoadingGlobal } from "./generalSlices/userSlice";

const App = () => {
  const dispatch = useDispatch();

  const displayMap = useSelector(selectDisplayMap);
  const authLoginData = useSelector(selectAuthLoginData);
  const userRole = useSelector(selectRole);
  const loadingGlobal = useSelector(selectLoadingGlobal);

  useEffect(() => {
    // Refresh Data
    dispatch(getRsuData());
  }, [authLoginData, dispatch]);

  return (
    <GoogleOAuthProvider clientId={EnvironmentVars.GOOGLE_CLIENT_ID}>
      <div id="masterdiv">
        <Grid container id="content-grid" alignItems="center">
          <Header />
          {authLoginData ? (
            <Tabs>
              <div label="RSU Map">
                {displayMap ? null : <Menu />}
                {displayMap ? <RsuMapView auth={true} /> : <Map auth={true} />}
              </div>
              <div label="Heat Map">
                {displayMap ? null : <Menu />}
                <HeatMap auth={true} />
              </div>
              <div label="WZDx">
                <WzdxMap auth={true} />
              </div>
              <div label="BSM Map">
                <BsmMap auth={true} />
              </div>
              {userRole === "admin" && (
                <div label="Admin">
                  <Admin />
                </div>
              )}
              <div label="Help">
                <Help />
              </div>
            </Tabs>
          ) : (
            <div></div>
          )}
        </Grid>
        <RingLoader css={loadercss} size={200} color={"#13d48d"} loading={loadingGlobal} speedMultiplier={1} />
      </div>
    </GoogleOAuthProvider>
  );
};

const loadercss = css`
  display: block;
  margin: 0 auto;
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -125px;
  margin-left: -125px;
`;

export default App;

import React from "react";
import { render } from "@testing-library/react";
import Header from "./Header";
import { Provider } from "react-redux";
import { setupStore } from "../store";
import EnvironmentVars from "../EnvironmentVars";
import { GoogleOAuthProvider } from "@react-oauth/google";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <GoogleOAuthProvider clientId={EnvironmentVars.GOOGLE_CLIENT_ID}>
        <Header />
      </GoogleOAuthProvider>
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <GoogleOAuthProvider clientId={EnvironmentVars.GOOGLE_CLIENT_ID}>
          <Header />
        </GoogleOAuthProvider>
      </Provider>
    )
  ).toMatchSnapshot();
});

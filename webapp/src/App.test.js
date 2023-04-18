import React from "react";
import { render } from "@testing-library/react";
import App from "./App";
import { Provider } from "react-redux";
import { setupStore } from "./store";
import { replaceChaoticIds } from "./utils/test-utils";

it("should take a snapshot", () => {
  const initialState = {
    user: {
      value: {
        organization: {
          role: "admin",
        },
        authLoginData: {
          token: "token",
          expires_at: 1,
          data: {
            name: "name",
            email: "email",
            super_use: true,
          },
        },
      },
    },
  };
  const { container } = render(
    <Provider store={setupStore(initialState)}>
      <App />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

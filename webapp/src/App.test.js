import React from "react";
import { render } from "@testing-library/react";
import App from "./App";
import { Provider } from "react-redux";
import { setupStore } from "./store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <App />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <App />
      </Provider>
    )
  ).toMatchSnapshot();
});

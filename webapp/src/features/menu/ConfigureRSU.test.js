import React from "react";
import { render } from "@testing-library/react";
import ConfigureRSU from "./ConfigureRSU";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <ConfigureRSU />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <ConfigureRSU />
      </Provider>
    )
  ).toMatchSnapshot();
});

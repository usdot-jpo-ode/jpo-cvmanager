import React from "react";
import { render } from "@testing-library/react";
import SnmpsetMenu from "./SnmpsetMenu";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <SnmpsetMenu />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <SnmpsetMenu />
      </Provider>
    )
  ).toMatchSnapshot();
});

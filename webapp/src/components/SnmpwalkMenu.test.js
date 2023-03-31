import React from "react";
import { render } from "@testing-library/react";
import SnmpwalkMenu from "./SnmpwalkMenu";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <SnmpwalkMenu />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <SnmpwalkMenu />
      </Provider>
    )
  ).toMatchSnapshot();
});

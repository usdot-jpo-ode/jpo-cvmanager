import React from "react";
import { render } from "@testing-library/react";
import RsuRebootMenu from "./RsuRebootMenu";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <RsuRebootMenu />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <RsuRebootMenu />
      </Provider>
    )
  ).toMatchSnapshot();
});

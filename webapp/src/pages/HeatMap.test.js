import React from "react";
import { render } from "@testing-library/react";
import HeatMap from "./HeatMap";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <HeatMap />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <HeatMap />
      </Provider>
    )
  ).toMatchSnapshot();
});

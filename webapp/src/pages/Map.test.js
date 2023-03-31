import React from "react";
import { render } from "@testing-library/react";
import Map from "./Map";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <Map />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <Map />
      </Provider>
    )
  ).toMatchSnapshot();
});

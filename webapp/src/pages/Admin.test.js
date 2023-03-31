import React from "react";
import { render } from "@testing-library/react";
import Admin from "./Admin";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <Admin />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <Admin />
      </Provider>
    )
  ).toMatchSnapshot();
});

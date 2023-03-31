import React from "react";
import { render } from "@testing-library/react";
import DisplayCounts from "./DisplayCounts";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <DisplayCounts />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <DisplayCounts />
      </Provider>
    )
  ).toMatchSnapshot();
});

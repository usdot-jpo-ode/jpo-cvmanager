import React from "react";
import { render } from "@testing-library/react";
import Tabs from "./Tabs";
import { Provider } from "react-redux";
import { setupStore } from "../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <Tabs children={[{ props: {} }]} />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <Tabs children={[{ props: {} }]} />
      </Provider>
    )
  ).toMatchSnapshot();
});

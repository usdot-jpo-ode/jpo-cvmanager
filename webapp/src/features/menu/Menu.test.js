import React from "react";
import { render } from "@testing-library/react";
import Menu from "./Menu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <Menu />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <Menu />
      </Provider>
    )
  ).toMatchSnapshot();
});

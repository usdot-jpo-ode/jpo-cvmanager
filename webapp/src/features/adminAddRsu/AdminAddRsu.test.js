import React from "react";
import { render } from "@testing-library/react";
import AdminAddRsu from "./AdminAddRsu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminAddRsu />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminAddRsu />
      </Provider>
    )
  ).toMatchSnapshot();
});

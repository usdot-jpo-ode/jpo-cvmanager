import React from "react";
import { render } from "@testing-library/react";
import AdminOrganizationTabRsu from "./AdminOrganizationTabRsu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminOrganizationTabRsu />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminOrganizationTabRsu />
      </Provider>
    )
  ).toMatchSnapshot();
});

import React from "react";
import { render } from "@testing-library/react";
import AdminOrganizationTab from "./AdminOrganizationTab";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminOrganizationTab />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminOrganizationTab />
      </Provider>
    )
  ).toMatchSnapshot();
});

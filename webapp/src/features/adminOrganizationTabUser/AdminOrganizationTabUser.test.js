import React from "react";
import { render } from "@testing-library/react";
import AdminOrganizationTabUser from "./AdminOrganizationTabUser";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminOrganizationTabUser />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminOrganizationTabUser />
      </Provider>
    )
  ).toMatchSnapshot();
});

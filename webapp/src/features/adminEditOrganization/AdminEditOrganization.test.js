import React from "react";
import { render } from "@testing-library/react";
import AdminEditOrganization from "./AdminEditOrganization";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminEditOrganization />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminEditOrganization />
      </Provider>
    )
  ).toMatchSnapshot();
});

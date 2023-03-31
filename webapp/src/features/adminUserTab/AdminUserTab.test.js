import React from "react";
import { render } from "@testing-library/react";
import AdminUserTab from "./AdminUserTab";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminUserTab />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminUserTab />
      </Provider>
    )
  ).toMatchSnapshot();
});

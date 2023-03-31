import React from "react";
import { render } from "@testing-library/react";
import AdminAddUser from "./AdminAddUser";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminAddUser />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminAddUser />
      </Provider>
    )
  ).toMatchSnapshot();
});

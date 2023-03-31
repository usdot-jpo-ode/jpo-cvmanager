import React from "react";
import { render } from "@testing-library/react";
import AdminEditUser from "./AdminEditUser";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminEditUser userData={{}} />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminEditUser userData={{}} />
      </Provider>
    )
  ).toMatchSnapshot();
});

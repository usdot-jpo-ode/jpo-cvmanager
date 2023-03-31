import React from "react";
import { render } from "@testing-library/react";
import AdminEditRsu from "./AdminEditRsu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <AdminEditRsu rsuData={{}} />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <AdminEditRsu rsuData={{}} />
      </Provider>
    )
  ).toMatchSnapshot();
});

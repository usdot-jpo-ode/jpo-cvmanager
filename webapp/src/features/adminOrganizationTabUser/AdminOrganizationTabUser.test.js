import React from "react";
import { render } from "@testing-library/react";
import AdminOrganizationTabUser from "./AdminOrganizationTabUser";
import { Provider } from "react-redux";
import { setupStore } from "../../store";
import { replaceChaoticIds } from "../../utils/test-utils";

it("should take a snapshot", () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminOrganizationTabUser />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

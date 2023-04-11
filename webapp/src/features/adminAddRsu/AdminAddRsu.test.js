import React from "react";
import { render } from "@testing-library/react";
import AdminAddRsu from "./AdminAddRsu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";
import { replaceChaoticIds } from "../../utils/test-utils";

it("should take a snapshot", () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminAddRsu />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

import React from "react";
import { render } from "@testing-library/react";
import AdminFormManager from "./AdminFormManager";
import { replaceChaoticIds } from "../utils/test-utils";
import { setupStore } from "../store";
import { Provider } from "react-redux";

it("snapshot rsu", () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminFormManager activeForm={"add_rsu"} />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

it("snapshot user", () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminFormManager activeForm={"add_user"} />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

it("snapshot organization", () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminFormManager activeForm={"add_organization"} />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

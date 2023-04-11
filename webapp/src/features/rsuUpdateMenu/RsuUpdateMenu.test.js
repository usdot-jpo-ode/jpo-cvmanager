import React from "react";
import { render } from "@testing-library/react";
import RsuUpdateMenu from "./RsuUpdateMenu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";
import { replaceChaoticIds } from "../../utils/test-utils";

it("should take a snapshot", () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <RsuUpdateMenu ipList={[]} />
    </Provider>
  );

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

import React from "react";
import { render } from "@testing-library/react";
import RsuUpdateMenu from "./RsuUpdateMenu";
import { Provider } from "react-redux";
import { setupStore } from "../../store";

it("should take a snapshot", () => {
  const { asFragment } = render(
    <Provider store={setupStore({})}>
      <RsuUpdateMenu ipList={[]} />
    </Provider>
  );

  expect(
    asFragment(
      <Provider store={setupStore({})}>
        <RsuUpdateMenu ipList={[]} />
      </Provider>
    )
  ).toMatchSnapshot();
});

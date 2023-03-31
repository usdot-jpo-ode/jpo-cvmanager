import React from "react";
import { render } from "@testing-library/react";
import RsuUpdateItem from "./RsuUpdateItem";

it("should take a snapshot", () => {
  const { asFragment } = render(<RsuUpdateItem osUpdateAvailable={[]} fwUpdateAvailable={[]} />);

  expect(asFragment(<RsuUpdateItem osUpdateAvailable={[]} fwUpdateAvailable={[]} />)).toMatchSnapshot();
});

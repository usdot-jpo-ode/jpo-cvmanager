import React from "react";
import { render } from "@testing-library/react";
import Help from "./Help";

it("should take a snapshot", () => {
  const { asFragment } = render(<Help />);

  expect(asFragment(<Help />)).toMatchSnapshot();
});

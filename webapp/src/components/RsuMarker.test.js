import React from "react";
import { render } from "@testing-library/react";
import RsuMarker from "./RsuMarker";

it("should take a snapshot", () => {
  const { asFragment } = render(<RsuMarker />);

  expect(asFragment(<RsuMarker />)).toMatchSnapshot();
});

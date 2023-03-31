import React from "react";
import { render } from "@testing-library/react";
import SnmpwalkItem from "./SnmpwalkItem";

it("should take a snapshot", () => {
  const { asFragment } = render(<SnmpwalkItem content={{}} />);

  expect(asFragment(<SnmpwalkItem content={{}} />)).toMatchSnapshot();
});

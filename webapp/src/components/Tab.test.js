import React from "react";
import { render } from "@testing-library/react";
import Tab from "./Tab";

it("should take a snapshot", () => {
  const { asFragment } = render(<Tab onClick={() => {}} activeTab={""} label={""} />);

  expect(asFragment(<Tab onClick={() => {}} activeTab={""} label={""} />)).toMatchSnapshot();
});

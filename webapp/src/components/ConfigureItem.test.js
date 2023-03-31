import React from "react";
import { render } from "@testing-library/react";
import ConfigureItem from "./ConfigureItem";

it("should take a snapshot", () => {
  const { asFragment } = render(<ConfigureItem indexList={[]} />);

  expect(asFragment(<ConfigureItem indexList={[]} />)).toMatchSnapshot();
});

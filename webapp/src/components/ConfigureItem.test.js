import React from "react";
import { render } from "@testing-library/react";
import ConfigureItem from "./ConfigureItem";

it("should take a snapshot", () => {
  const { container } = render(<ConfigureItem indexList={[]} />);

  expect(container).toMatchSnapshot();
});

import React from "react";
import { render } from "@testing-library/react";
import AdminFormManager from "./AdminFormManager";

it("should take a snapshot", () => {
  const { asFragment } = render(<AdminFormManager />);

  expect(asFragment(<AdminFormManager />)).toMatchSnapshot();
});

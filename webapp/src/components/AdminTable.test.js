import React from "react";
import { render } from "@testing-library/react";
import AdminTable from "./AdminTable";

it("should take a snapshot", () => {
  const { asFragment } = render(<AdminTable />);

  expect(asFragment(<AdminTable />)).toMatchSnapshot();
});

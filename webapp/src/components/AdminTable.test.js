import React from "react";
import { render } from "@testing-library/react";
import AdminTable from "./AdminTable";
import { replaceChaoticIds } from "../utils/test-utils";

it("should take a snapshot", () => {
  const { container } = render(<AdminTable />);

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

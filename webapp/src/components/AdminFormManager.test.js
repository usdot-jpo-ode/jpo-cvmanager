import React from "react";
import { render } from "@testing-library/react";
import AdminFormManager from "./AdminFormManager";
import { replaceChaoticIds } from "../utils/test-utils";

it("should take a snapshot", () => {
  const { container } = render(<AdminFormManager />);

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

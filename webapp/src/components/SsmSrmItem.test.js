import React from "react";
import { render } from "@testing-library/react";
import SsmSrmItem from "./SsmSrmItem";
import { replaceChaoticIds } from "../utils/test-utils";

it("should take a snapshot", () => {
  const { container } = render(<SsmSrmItem elem={{}} setSelectedSrm={() => {}} />);

  expect(replaceChaoticIds(container)).toMatchSnapshot();
});

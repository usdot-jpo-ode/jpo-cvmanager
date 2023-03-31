import React from "react";
import { render } from "@testing-library/react";
import SsmSrmItem from "./SsmSrmItem";

it("should take a snapshot", () => {
  const { asFragment } = render(<SsmSrmItem elem={{}} setSelectedSrm={() => {}} />);

  expect(asFragment(<SsmSrmItem elem={{}} setSelectedSrm={() => {}} />)).toMatchSnapshot();
});

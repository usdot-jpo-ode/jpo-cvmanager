import React from "react";
import { render } from "@testing-library/react";
import AdminOrganizationDeleteMenu from "./AdminOrganizationDeleteMenu";

it("should take a snapshot", () => {
  const { asFragment } = render(<AdminOrganizationDeleteMenu />);

  expect(asFragment(<AdminOrganizationDeleteMenu />)).toMatchSnapshot();
});

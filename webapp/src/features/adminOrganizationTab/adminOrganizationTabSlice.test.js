import reducer from "./adminOrganizationTabSlice";

describe("admin organization tab reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        activeDiv: "organization_table",
        title: "Organizations",
        orgData: [],
        selectedOrg: [],
        rsuTableData: [],
        userTableData: [],
        errorState: false,
        errorMsg: "",
      },
    });
  });
});

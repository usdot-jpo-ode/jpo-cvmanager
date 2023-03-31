import reducer from "./adminOrganizationTabUserSlice";

describe("admin organization tab User reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        availableUserList: [],
        selectedUserList: [],
        availableRoles: [],
      },
    });
  });
});

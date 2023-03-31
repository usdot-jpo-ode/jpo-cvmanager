import reducer from "./adminOrganizationTabRsuSlice";

describe("admin organization tab RSU reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        availableRsuList: [],
        selectedRsuList: [],
      },
    });
  });
});

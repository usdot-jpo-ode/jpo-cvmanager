import reducer from "./menuSlice";

describe("menu reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        previousRequest: null,
        currentSort: null,
        sortedCountList: [],
        displayCounts: false,
        view: "buttons",
      },
    });
  });
});

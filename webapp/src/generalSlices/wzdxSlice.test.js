import reducer from "./wzdxSlice";

describe("wzdx reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: { type: "FeatureCollection", features: [] },
    });
  });
});

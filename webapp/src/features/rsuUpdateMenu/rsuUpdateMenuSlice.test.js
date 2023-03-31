import reducer from "./rsuUpdateMenuSlice";

describe("RSU update menu reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        checked: false,
        osUpdateAvailable: [],
        fwUpdateAvailable: [],
      },
    });
  });
});

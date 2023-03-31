import reducer from "./adminAddOrganizationSlice";

describe("admin add organization reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        successMsg: "",
        errorState: false,
        errorMsg: "",
      },
    });
  });
});

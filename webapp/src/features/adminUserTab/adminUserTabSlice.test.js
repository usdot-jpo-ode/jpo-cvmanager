import reducer from "./adminUserTabSlice";

describe("admin User tab reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        activeDiv: "user_table",
        tableData: [],
        title: "Users",
        editUserRowData: {},
      },
    });
  });
});

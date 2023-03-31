import reducer from "./adminRsuTabSlice";

describe("admin RSU tab reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        activeDiv: "rsu_table",
        tableData: [],
        title: "RSUs",
        columns: [
          { title: "Milepost", field: "milepost", id: 0 },
          { title: "IP Address", field: "ip", id: 1 },
          { title: "Primary Route", field: "primary_route", id: 2 },
          { title: "RSU Model", field: "model", id: 3 },
          { title: "Serial Number", field: "serial_number", id: 4 },
        ],
        editRsuRowData: {},
      },
    });
  });
});

import reducer from "./configSlice";

beforeEach(() => {
  fetchMock.doMock();
});

describe("config reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        msgFwdConfig: {},
        errorState: "",
        changeSuccess: false,
        rebootChangeSuccess: false,
        destIp: "",
        snmpMsgType: "bsm",
        snmpFilterMsg: "",
        snmpFilterErr: false,
        addPoint: false,
      },
    });
  });
});

// test refreshSnmpFwdConfig

// fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));

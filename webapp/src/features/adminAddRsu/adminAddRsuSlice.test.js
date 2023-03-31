import reducer from "./adminAddRsuSlice";

describe("admin add RSU reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      value: {
        successMsg: "",
        apiData: {},
        errorState: false,
        errorMsg: "",
        primaryRoutes: [],
        selectedRoute: "Select Route",
        otherRouteDisabled: true,
        rsuModels: [],
        selectedModel: "Select RSU Model",
        sshCredentialGroups: [],
        selectedSshGroup: "Select SSH Group",
        snmpCredentialGroups: [],
        selectedSnmpGroup: "Select SNMP Group",
        organizations: [],
        selectedOrganizations: [],
        submitAttempt: false,
      },
    });
  });
});

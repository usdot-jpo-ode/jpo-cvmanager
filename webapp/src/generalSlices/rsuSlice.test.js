import reducer from "./rsuSlice";
import { getRsuData, getRsuMapData, getBsmData, getIssScmsStatusData, getSrmSsmData, _getRsuInfo } from "./rsuSlice";
import CdotApi from "../apis/cdot-rsu-api";

describe("rsu reducer", () => {
  it("should handle initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual({
      loading: false,
      bsmLoading: false,
      requestOut: false,
      value: {
        selectedRsu: null,
        rsuData: [],
        rsuOnlineStatus: {},
        rsuCounts: {},
        countList: [],
        currentSort: "",
        startDate: "",
        endDate: "",
        messageLoading: false,
        warningMessage: false,
        msgType: "BSM",
        rsuMapData: {},
        mapList: [],
        mapDate: "",
        displayMap: false,
        bsmStart: "",
        bsmEnd: "",
        addPoint: false,
        bsmCoordinates: [],
        bsmData: [],
        bsmDateError: false,
        bsmFilter: false,
        bsmFilterStep: 30,
        bsmFilterOffset: 0,
        issScmsStatusData: {},
        ssmDisplay: false,
        srmSsmList: [],
        selectedSrm: [],
      },
    });
  });
});

describe("async thunks", () => {
  const initialState = {
    loading: null,
    bsmLoading: null,
    requestOut: null,
    value: {
      selectedRsu: null,
      rsuData: null,
      rsuOnlineStatus: null,
      rsuCounts: null,
      countList: null,
      currentSort: null,
      startDate: null,
      endDate: null,
      messageLoading: null,
      warningMessage: null,
      msgType: null,
      rsuMapData: null,
      mapList: null,
      mapDate: null,
      displayMap: null,
      bsmStart: null,
      bsmEnd: null,
      addPoint: null,
      bsmCoordinates: null,
      bsmData: null,
      bsmDateError: null,
      bsmFilter: null,
      bsmFilterStep: null,
      bsmFilterOffset: null,
      issScmsStatusData: null,
      ssmDisplay: null,
      srmSsmList: null,
      selectedSrm: null,
    },
  };

  beforeAll(() => {
    jest.mock("../apis/cdot-rsu-api.js");
  });

  afterAll(() => {
    jest.unmock("../apis/cdot-rsu-api.js");
  });

  describe("getRsuData", () => {
    it("returns and calls the api correctly", async () => {
      const dispatch = jest.fn();
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: "token" },
            organization: { name: "name" },
          },
        },
        rsu: {
          value: {
            rsuOnlineStatus: {},
            startDate: "",
            endDate: "",
          },
        },
      });
      const action = getRsuData();

      await action(dispatch, getState, undefined);
      expect(dispatch).toHaveBeenCalledTimes(5 + 2); // 5 for the 5 dispatched actions, 2 for the pending and fulfilled actions
    });

    it("Updates the state correctly pending", async () => {
      let loading = true;
      let rsuData = [];
      let rsuOnlineStatus = {};
      let rsuCounts = {};
      let countList = [];
      const state = reducer(initialState, {
        type: "rsu/getRsuData/pending",
      });
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, rsuData, rsuOnlineStatus, rsuCounts, countList },
      });
    });

    it("Updates the state correctly fulfilled", async () => {
      let loading = false;
      const state = reducer(initialState, {
        type: "rsu/getRsuData/fulfilled",
      });
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } });
    });

    it("Updates the state correctly rejected", async () => {
      let loading = false;
      const state = reducer(initialState, {
        type: "rsu/getRsuData/rejected",
      });
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } });
    });
  });

  describe("getRsuInfoOnly", () => {
    it("returns and calls the api correctly", async () => {
      const dispatch = jest.fn();
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: "token" },
            organization: { name: "name" },
          },
        },
        rsu: {
          value: {
            rsuOnlineStatus: {},
            startDate: "",
            endDate: "",
          },
        },
      });
      const action = getRsuData();

      const rsuList = ["rsu"];
      CdotApi.postRsuData = jest.fn().mockReturnValue({ rsuList });
      let resp = await action(dispatch, getState, undefined);
      expect(CdotApi.getRsuInfo).toHaveBeenCalledWith("token", "name");
      expect(resp.payload).toEqual(rsuList);
    });

    it("Updates the state correctly pending", async () => {
      let loading = true;
      const state = reducer(initialState, {
        type: "rsu/getRsuInfoOnly/pending",
      });
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      });
    });

    it("Updates the state correctly fulfilled", async () => {
      let loading = false;
      const state = reducer(initialState, {
        type: "rsu/getRsuInfoOnly/fulfilled",
      });
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } });
    });

    it("Updates the state correctly rejected", async () => {
      let loading = false;
      const state = reducer(initialState, {
        type: "rsu/getRsuInfoOnly/rejected",
      });
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } });
    });
  });
});

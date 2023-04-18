import CdotApi from "./cdot-rsu-api";
import EnvironmentVars from "../EnvironmentVars";
jest.mock("../EnvironmentVars", () => ({
  getBaseApiUrl: () => "REACT_APP_ENV",
}));

// const playSoundFileMock = jest.spyOn(EnvironmentVars.prototype, "getBaseApiUrl").mockImplementation(() => {
//   console.log("mocked function");
//   return "REACT_APP_ENV";
// });

beforeEach(() => {
  fetchMock.doMock();
  //   EnvironmentVars.mockClear();
  //   EnvironmentVars.getBaseApiUrl = jest.fn().mockReturnValue("REACT_APP_ENV");
  //   EnvironmentVars.mockImplementation(() => {
  //     return {
  //       getBaseApiUrl: () => "REACT_APP_ENV",
  //     };
  //   });
});

it("Test getRsuInfo", async () => {
  //   EnvironmentVars.getBaseApiUrl = jest.fn().mockReturnValue("REACT_APP_ENV");
  //   process.env.REACT_APP_GATEWAY_BASE_URL = "REACT_APP_ENV";
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));
  const actualResponse = await CdotApi.getRsuInfo("testToken", "testOrg");
  expect(actualResponse).toEqual(expectedResponse);

  console.log("ENDPOINT", EnvironmentVars.getBaseApiUrl());

  console.log("FETCH MOCK", fetchMock.mock.calls[0][1].headers);
  expect(fetchMock.mock.calls[0][0]).toBe("REACT_APP_ENV/rsuinfo");
  expect(fetchMock.mock.calls[0][1].method).toBe("GET");
  expect(fetchMock.mock.calls[0][1].headers).toBe({});
});

it("Test getRsuOnline", async () => {
  process.env.REACT_APP_ENV = "REACT_APP_ENV";

  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));

  const actualResponse = await CdotApi.getRsuOnline("testToken", "testOrg");

  expect(actualResponse).toEqual(expectedResponse);
});

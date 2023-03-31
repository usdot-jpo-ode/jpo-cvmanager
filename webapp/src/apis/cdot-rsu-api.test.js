import CdotApi from "./cdot-rsu-api";

beforeEach(() => {
  // if you have an existing `beforeEach` just add the following line to it
  fetchMock.doMock();
});

it("Test getRsuInfo", async () => {
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));

  const actualResponse = await CdotApi.getRsuInfo("testToken", "testOrg");

  expect(actualResponse).toEqual(expectedResponse);
});

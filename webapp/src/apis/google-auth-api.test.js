import GoogleAuthApi from "./google-auth-api";

beforeEach(() => {
  fetchMock.doMock();
});

it("Test Google Auth login", () => {
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));

  GoogleAuthApi.logIn("testToken").then((response) => {
    expect(response).toEqual(expectedResponse);
  });
});

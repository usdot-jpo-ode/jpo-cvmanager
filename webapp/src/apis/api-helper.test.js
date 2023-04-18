import ApiHelper from "./api-helper";

beforeEach(() => {
  fetchMock.doMock();
});

it("Test format query params", async () => {
  let queryParams = {};
  let response = ApiHelper.formatQueryParams(queryParams);
  expect(response).toEqual("");

  queryParams = { email: "jacob", password: "password" };
  response = ApiHelper.formatQueryParams(queryParams);
  expect(response).toEqual("?email=jacob&password=password");

  queryParams = { email: "", password: "" };
  response = ApiHelper.formatQueryParams(queryParams);
  expect(response).toEqual("");
});

it("Test fetch request", async () => {
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));
  const actualResponse = await ApiHelper._getData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse).toEqual(expectedResponse);
});

it("Test fetch request Error", async () => {
  fetchMock.mockRejectOnce(new Error("fake error message"));
  const actualResponse = await ApiHelper._getData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse).toEqual(null);
});

it("Test fetch with codes request", async () => {
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));
  let actualResponse = await ApiHelper._getDataWithCodes({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(expectedResponse);

  fetchMock.mockResponseOnce("NOT JSON");
  actualResponse = await ApiHelper._getDataWithCodes({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(undefined);
});

it("Test fetch with codes request Error", async () => {
  fetchMock.mockRejectOnce(new Error("fake error message"));
  const actualResponse = await ApiHelper._getDataWithCodes({ url: "https://test.com", token: "testToken" });
  expect(actualResponse).toEqual(null);
});

it("Test post request", async () => {
  let expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));
  let actualResponse = await ApiHelper._postData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(expectedResponse);

  fetchMock.mockResponseOnce("NOT JSON");
  actualResponse = await ApiHelper._postData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(undefined);
});

it("Test post request Error", async () => {
  fetchMock.mockRejectOnce(new Error("fake error message"));
  const actualResponse = await ApiHelper._postData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse).toEqual(null);
});

it("Test delete request", async () => {
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));
  let actualResponse = await ApiHelper._deleteData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(expectedResponse);

  fetchMock.mockResponseOnce("NOT JSON");
  actualResponse = await ApiHelper._deleteData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(undefined);
});

it("Test delete request Error", async () => {
  fetchMock.mockRejectOnce(new Error("fake error message"));
  const actualResponse = await ApiHelper._deleteData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse).toEqual(null);
});

it("Test patch request", async () => {
  const expectedResponse = { data: "Test JSON" };
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse));
  let actualResponse = await ApiHelper._patchData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(expectedResponse);

  fetchMock.mockResponseOnce("NOT JSON");
  actualResponse = await ApiHelper._patchData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse.body).toEqual(undefined);
});

it("Test patch request Error", async () => {
  fetchMock.mockRejectOnce(new Error("fake error message"));
  const actualResponse = await ApiHelper._patchData({ url: "https://test.com", token: "testToken" });
  expect(actualResponse).toEqual(null);
});

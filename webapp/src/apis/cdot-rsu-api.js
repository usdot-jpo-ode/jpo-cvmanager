import EnvironmentVars from "../EnvironmentVars";

class CdotApi {
  formatQueryParams(query_params) {
    if (!query_params) return;
    const params = [];
    for (const key in query_params) {
      if (query_params[key] !== "" && query_params[key] !== null) {
        params.push(`${key}=${query_params[key]}`);
      }
    }
    return !query_params ? "" : "?" + params.join("&");
  }

  // Helper Functions
  async _getData(url, token, url_ext, additional_headers = {}, onError) {
    console.debug("GETTING DATA FROM " + url);
    try {
      const resp = await fetch(url + url_ext, {
        method: "GET",
        headers: {
          ...additional_headers,
          Authorization: token,
        },
      });
      console.debug("GOT RESPONSE FROM", url + url_ext, resp);

      return await resp.json();
    } catch (err) {
      console.error("Error in _getData: " + err);
      onError(err);
      return null;
    }
  }

  async _postData(url, body, token, additional_headers = {}, onError) {
    console.debug("POSTING DATA TO " + url);
    try {
      const resp = await fetch(url, {
        method: "POST",
        body,
        headers: {
          ...additional_headers,
          Authorization: token,
          "Content-Type": "application/json",
        },
      });
      console.debug("GOT RESPONSE FROM", url, resp);

      return { body: await resp.json(), status: resp.status };
    } catch (err) {
      console.error("Error occurred in _postData", err);
      onError(err);
      return null;
    }
  }

  // External Methods
  getRsuInfo = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.rsuInfoEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );
  getRsuOnline = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.rsuOnlineEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );
  getRsuCounts = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.rsuCountsEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );
  getRsuGoogleAuth = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.googleAuthEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );
  getRsuCommand = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.rsuCommandEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );
  getRsuMapInfo = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.rsuMapInfoEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );
  getSsmSrmData = async (
    token,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.ssmSrmEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      {},
      onError
    );
  getIssScmsStatus = async (
    token,
    org,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.issScmsStatusEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      { Organization: org },
      onError
    );

  // WZDx
  getWzdxData = async (
    token,
    url_ext = "",
    query_params = {},
    onError = () => {}
  ) =>
    this._getData(
      EnvironmentVars.wzdxEndpoint + url_ext,
      token,
      this.formatQueryParams(query_params),
      {},
      onError
    );

  // POST
  postBsmData = async (token, body, url_ext, onError = () => {}) =>
    this._postData(
      EnvironmentVars.bsmDataEndpoint + url_ext,
      body,
      token,
      {},
      onError
    );

  // POST
  postRsuData = async (token, org, body, url_ext, onError = () => {}) =>{
    body = JSON.stringify(body);
    return await this._postData(
      EnvironmentVars.rsuCommandEndpoint + url_ext,
      body,
      token,
      { Organization: org },
      onError
    );
  }
}

export default new CdotApi();

import EnvironmentVars from "../EnvironmentVars";
import apiHelper from "./api-helper";

class CdotApi {
  // External Methods
  getRsuInfo = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuInfoEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });
  getRsuOnline = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuOnlineEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });
  getRsuCounts = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuCountsEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });
  getRsuGoogleAuth = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.googleAuthEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });
  getRsuCommand = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuCommandEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });
  getRsuMapInfo = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuMapInfoEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });
  getSsmSrmData = async (token, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.ssmSrmEndpoint + url_ext,
      token,
      query_params,
      onError,
    });
  getIssScmsStatus = async (token, org, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.issScmsStatusEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      onError,
    });

  // WZDx
  getWzdxData = async (token, url_ext = "", query_params = {}, onError = () => {}) =>
    apiHelper._getData({
      url: EnvironmentVars.wzdxEndpoint + url_ext,
      token,
      query_params,
      onError,
    });

  // POST
  postBsmData = async (token, body, url_ext, onError = () => {}) =>
    apiHelper._postData({ url: EnvironmentVars.bsmDataEndpoint + url_ext, body, token, onError });

  // POST
  postRsuData = async (token, org, body, url_ext, onError = () => {}) => {
    body = JSON.stringify(body);
    return await apiHelper._postData({
      url: EnvironmentVars.rsuCommandEndpoint + url_ext,
      body,
      token,
      additional_headers: { Organization: org },
      onError,
    });
  };
}

const cdotApiObject = new CdotApi();

export default cdotApiObject;

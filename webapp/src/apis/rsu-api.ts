import EnvironmentVars from '../EnvironmentVars'
import apiHelper from './api-helper'

class RsuApi {
  // External Methods
  getRsuInfo = async (token: string, org: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuInfoEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })
  getRsuOnline = async (token: string, org: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuOnlineEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })
  getRsuCounts = async (token: string, org: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuCountsEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })
  getRsuAuth = async (token: string, org: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.authEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })
  getRsuCommand = async (token: string, org: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuCommandEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })
  getRsuMapInfo = async (token: string, org: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.rsuMapInfoEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })
  getSsmSrmData = async (token: string, url_ext: string = '', query_params: Record<string, string> = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.ssmSrmEndpoint + url_ext,
      token,
      query_params,
    })
  getIssScmsStatus = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ) =>
    apiHelper._getData({
      url: EnvironmentVars.issScmsStatusEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
    })

  // WZDx
  getWzdxData = async (token: string, url_ext: string = '', query_params = {}) =>
    apiHelper._getData({
      url: EnvironmentVars.wzdxEndpoint + url_ext,
      token,
      query_params,
    })

  // POST
  postBsmData = async (token: string, body: Object, url_ext: string = '') =>
    apiHelper._postData({ url: EnvironmentVars.bsmDataEndpoint + url_ext, body, token })

  // POST
  postRsuData = async (token: string, org: string, body: Object, url_ext = '') => {
    body = JSON.stringify(body)
    return await apiHelper._postData({
      url: EnvironmentVars.rsuCommandEndpoint + url_ext,
      body,
      token,
      additional_headers: { Organization: org },
    })
  }

  // POST
  postRsuGeo = async (token: string, org: string, body: Object, url_ext: string) => {
    return await apiHelper._postData({
      url: EnvironmentVars.rsuGeoQueryEndpoint + url_ext,
      body,
      token,
      additional_headers: { Organization: org },
    })
  }

  // POST
  postContactSupport = async (json: Object) => {
    return await apiHelper._postData({
      url: EnvironmentVars.contactSupport,
      body: JSON.stringify(json),
    })
  }
}

const rsuApiObject = new RsuApi()

export default rsuApiObject

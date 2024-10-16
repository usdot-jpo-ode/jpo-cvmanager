import EnvironmentVars from '../EnvironmentVars'
import { WZDxWorkZoneFeed } from '../models/wzdx/WzdxWorkZoneFeed42'
import apiHelper from './api-helper'
import {
  ApiMsgRespWithCodes,
  GeoMsgDataPostBody,
  GetRsuCommandResp,
  GetRsuUserAuthResp,
  IssScmsStatus,
  RsuCommandPostBody,
  RsuCounts,
  RsuInfo,
  RsuMapInfo,
  RsuMapInfoIpList,
  RsuMsgFwdConfigs,
  RsuOnlineStatusRespMultiple,
  RsuOnlineStatusRespSingle,
  SsmSrmData,
} from './rsu-api-types'

class RsuApi {
  // External Methods
  getRsuInfo = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<RsuInfo> =>
    apiHelper._getData({
      url: EnvironmentVars.rsuInfoEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getRsuOnline = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<RsuOnlineStatusRespMultiple | RsuOnlineStatusRespSingle> =>
    apiHelper._getData({
      url: EnvironmentVars.rsuOnlineEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getRsuCounts = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<RsuCounts> =>
    apiHelper._getData({
      url: EnvironmentVars.rsuCountsEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getRsuMsgFwdConfigs = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<RsuMsgFwdConfigs> =>
    apiHelper._getData({
      url: EnvironmentVars.rsuMsgFwdQueryEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getRsuAuth = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<GetRsuUserAuthResp> =>
    apiHelper._getData({
      url: EnvironmentVars.authEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getRsuCommand = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<GetRsuCommandResp> =>
    apiHelper._getData({
      url: EnvironmentVars.rsuCommandEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getRsuMapInfo = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<RsuMapInfo | RsuMapInfoIpList> =>
    apiHelper._getData({
      url: EnvironmentVars.rsuMapInfoEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  getSsmSrmData = async (
    token: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<SsmSrmData> =>
    apiHelper._getData({
      url: EnvironmentVars.ssmSrmEndpoint + url_ext,
      token,
      query_params,
      tag: 'rsu',
    })
  getIssScmsStatus = async (
    token: string,
    org: string,
    url_ext: string = '',
    query_params: Record<string, string> = {}
  ): Promise<IssScmsStatus> =>
    apiHelper._getData({
      url: EnvironmentVars.issScmsStatusEndpoint + url_ext,
      token,
      query_params,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })

  // WZDx
  getWzdxData = async (token: string, url_ext: string = '', query_params = {}): Promise<WZDxWorkZoneFeed> =>
    apiHelper._getData({
      url: EnvironmentVars.wzdxEndpoint + url_ext,
      token,
      query_params,
    })

  // POST
  postGeoMsgData = async (token: string, body: Object, url_ext: string = ''): Promise<ApiMsgRespWithCodes<any>> =>
    apiHelper._postData({ url: EnvironmentVars.geoMsgDataEndpoint + url_ext, body, token, tag: 'rsu' })

  // POST
  postRsuData = async (
    token: string,
    org: string,
    body: RsuCommandPostBody,
    url_ext = ''
  ): Promise<ApiMsgRespWithCodes<any>> => {
    return await apiHelper._postData({
      url: EnvironmentVars.rsuCommandEndpoint + url_ext,
      body: JSON.stringify(body),
      token,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  }

  // POST
  postRsuGeo = async (token: string, org: string, body: Object, url_ext: string): Promise<ApiMsgRespWithCodes<any>> => {
    return await apiHelper._postData({
      url: EnvironmentVars.rsuGeoQueryEndpoint + url_ext,
      body,
      token,
      additional_headers: { Organization: org },
      tag: 'rsu',
    })
  }

  // POST
  postContactSupport = async (json: Object): Promise<ApiMsgRespWithCodes<any>> => {
    return await apiHelper._postData({
      url: EnvironmentVars.contactSupport,
      body: JSON.stringify(json),
      tag: 'rsu',
    })
  }
}

const rsuApiObject = new RsuApi()

export default rsuApiObject

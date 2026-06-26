class EnvironmentVars {
  static getBaseApiUrl() {
    return process.env.VITE_GATEWAY_BASE_URL?.replace(/\/$/, '') // remove trailing slash
  }

  static getMessageViewerTypes() {
    const VIEWER_MESSAGE_TYPES = process.env.VITE_VIEWER_MESSAGE_TYPES
    if (!VIEWER_MESSAGE_TYPES) {
      return ['BSM'] // default to BSM if not set
    }
    const messageTypes = VIEWER_MESSAGE_TYPES.split(',').map((item) => item.trim())
    return messageTypes
  }

  static getMapboxInitViewState() {
    const MAPBOX_INIT_LATITUDE = Number(process.env.VITE_MAPBOX_INIT_LATITUDE)
    const MAPBOX_INIT_LONGITUDE = Number(process.env.VITE_MAPBOX_INIT_LONGITUDE)
    const MAPBOX_INIT_ZOOM = Number(process.env.VITE_MAPBOX_INIT_ZOOM)

    const viewState = {
      latitude: MAPBOX_INIT_LATITUDE,
      longitude: MAPBOX_INIT_LONGITUDE,
      zoom: MAPBOX_INIT_ZOOM,
    }

    return viewState
  }

  static MAPBOX_TOKEN = process.env.VITE_MAPBOX_TOKEN
  static CVIZ_API_SERVER_URL = process.env.VITE_CVIZ_API_SERVER_URL?.replace(/\/$/, '') // remove trailing slash
  static CVIZ_API_WS_URL = process.env.VITE_CVIZ_API_WS_URL?.replace(/\/$/, '') // remove trailing slash
  static KEYCLOAK_HOST_URL = process.env.VITE_KEYCLOAK_URL
  static KEYCLOAK_REALM = process.env.VITE_KEYCLOAK_REALM
  static KEYCLOAK_CLIENT_ID = process.env.VITE_KEYCLOAK_CLIENT_ID
  static DOT_NAME = process.env.VITE_DOT_NAME
  static ENABLE_RSU_FEATURES = process.env.VITE_ENABLE_RSU_FEATURES !== 'false'
  static ENABLE_INTERSECTION_FEATURES = process.env.VITE_ENABLE_INTERSECTION_FEATURES !== 'false'
  static ENABLE_WZDX_FEATURES = process.env.VITE_ENABLE_WZDX_FEATURES !== 'false'
  static ENABLE_HAAS_FEATURES = process.env.VITE_ENABLE_HAAS_FEATURES !== 'false'
  static WEBAPP_THEME_LIGHT = process.env.VITE_WEBAPP_THEME_LIGHT
  static WEBAPP_THEME_DARK = process.env.VITE_WEBAPP_THEME_DARK

  static cvmanagerBaseEndpoint = `${this.getBaseApiUrl()}`
  static rsuInfoPath = '/devices/rsus/info'
  static rsuOnlineEndpoint = `${this.getBaseApiUrl()}/rsu-online-status`
  static rsuCountsEndpoint = `${this.getBaseApiUrl()}/rsucounts`
  static rsuCommandEndpoint = `${this.getBaseApiUrl()}/rsu-command`
  static rsuUpgradeEndpoint = `${this.CVIZ_API_SERVER_URL}/devices/rsus/upgrade`
  static wzdxEndpoint = `${this.getBaseApiUrl()}/wzdx-feed`
  static rsuGeoQueryEndpoint = `${this.getBaseApiUrl()}/rsu-config-geo-query`
  static rsuMsgFwdQueryEndpoint = `${this.getBaseApiUrl()}/rsu-msgfwd-query`
  static rsuMsgFwdFetchEndpoint = `${this.getBaseApiUrl()}/rsu-msgfwd-fetch`
  static geoMsgDataEndpoint = `${this.getBaseApiUrl()}/rsu-geo-msg-data`
  static ssmSrmEndpoint = `${this.getBaseApiUrl()}/rsu-ssm-srm-data`
  static adminNotification = `${this.getBaseApiUrl()}/admin-notification`
  static adminAddNotification = `${this.getBaseApiUrl()}/admin-new-notification`
  static adminAddOrg = `${this.getBaseApiUrl()}/admin-new-org`
  static adminOrg = `${this.getBaseApiUrl()}/admin-org`
  static adminOrgTimDeposit = `${this.getBaseApiUrl()}/admin-org-tim-deposit`
  static adminOrgSnmpMonitoring = `${this.getBaseApiUrl()}/admin-org-snmp-monitoring`
}

export default EnvironmentVars

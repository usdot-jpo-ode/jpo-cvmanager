class EnvironmentVars {
  static getBaseApiUrl() {
    switch (process.env.REACT_APP_ENV) {
      case 'dev':
        return process.env.REACT_APP_GATEWAY_BASE_URL_DEV
      case 'test':
        return process.env.REACT_APP_GATEWAY_BASE_URL_TEST
      case 'prod':
        return process.env.REACT_APP_GATEWAY_BASE_URL_PROD
      default:
        return process.env.REACT_APP_GATEWAY_BASE_URL
    }
  }

  static getClientId() {
    switch (process.env.REACT_APP_ENV) {
      case 'dev':
        return process.env.REACT_APP_GOOGLE_CLIENT_ID_DEV
      case 'test':
        return process.env.REACT_APP_GOOGLE_CLIENT_ID_TEST
      case 'prod':
        return process.env.REACT_APP_GOOGLE_CLIENT_ID_PROD
      default:
        return process.env.REACT_APP_GOOGLE_CLIENT_ID
    }
  }

  static MAPBOX_TOKEN = process.env.REACT_APP_MAPBOX_TOKEN
  static GOOGLE_CLIENT_ID = this.getClientId()

  static rsuInfoEndpoint = `${this.getBaseApiUrl()}/rsuinfo`
  static rsuOnlineEndpoint = `${this.getBaseApiUrl()}/rsu-online-status`
  static rsuCountsEndpoint = `${this.getBaseApiUrl()}/rsucounts`
  static rsuCommandEndpoint = `${this.getBaseApiUrl()}/rsu-command`
  static wzdxEndpoint = `${this.getBaseApiUrl()}/wzdx-feed`
  static rsuMapInfoEndpoint = `${this.getBaseApiUrl()}/rsu-map-info`
  static bsmDataEndpoint = `${this.getBaseApiUrl()}/rsu-bsm-data`
  static issScmsStatusEndpoint = `${this.getBaseApiUrl()}/iss-scms-status`
  static ssmSrmEndpoint = `${this.getBaseApiUrl()}/rsu-ssm-srm-data`
  static googleAuthEndpoint = `${this.getBaseApiUrl()}/rsu-google-auth`
  static adminAddRsu = `${this.getBaseApiUrl()}/admin-new-rsu`
  static adminRsu = `${this.getBaseApiUrl()}/admin-rsu`
  static adminAddUser = `${this.getBaseApiUrl()}/admin-new-user`
  static adminUser = `${this.getBaseApiUrl()}/admin-user`
  static adminAddOrg = `${this.getBaseApiUrl()}/admin-new-org`
  static adminOrg = `${this.getBaseApiUrl()}/admin-org`
}

export default EnvironmentVars

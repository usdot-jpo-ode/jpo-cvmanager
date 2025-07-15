import { evaluateFeatureFlags } from '../feature-flags'

class ApiHelper {
  formatQueryParams(query_params: Record<string, string>) {
    if (
      !query_params ||
      Object.keys(query_params).length === 0 ||
      Object.getPrototypeOf(query_params) !== Object.prototype
    )
      return ''
    const params = []
    for (const key in query_params) {
      if (query_params[key] !== '' && query_params[key] !== null) {
        params.push(`${key}=${query_params[key]}`)
      }
    }
    return !query_params || params.length === 0 ? '' : '?' + params.join('&')
  }

  // Helper Functions
  async _getData({
    url,
    token,
    query_params = {},
    url_ext = '',
    additional_headers = {},
    tag,
  }: {
    url: string
    token: string
    query_params?: Record<string, string>
    url_ext?: string
    additional_headers?: Record<string, string>
    tag?: FEATURE_KEY
  }) {
    if (!evaluateFeatureFlags(tag)) {
      console.debug(`Returning null because feature is disabled for tag ${tag} and url ${url}`)
      return null
    }
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'GET',
        headers: {
          ...additional_headers,
          Authorization: token,
        },
      })

      const response = await resp.json()
      return response
    } catch (err) {
      console.error('Error in _getData: ' + err)
      return null
    }
  }

  // Helper Functions
  async _getDataWithCodes({
    url,
    token,
    query_params = {},
    url_ext = '',
    additional_headers = {},
    tag,
  }: {
    url: string
    token: string
    query_params?: Record<string, string>
    url_ext?: string
    additional_headers?: Record<string, string>
    tag?: FEATURE_KEY
  }) {
    if (!evaluateFeatureFlags(tag)) {
      console.debug(`Returning null because feature is disabled for tag ${tag} and url ${url}`)
      return null
    }
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'GET',
        headers: {
          ...additional_headers,
          Authorization: token,
        },
      })

      let respBody = undefined
      try {
        respBody = await resp.json()
      } catch (err) {
        console.error('Error in _getDataWithCodes: ' + err)
      }
      return {
        body: respBody,
        status: resp.status,
        message: respBody?.message,
      }
    } catch (err) {
      console.error('Error in _getDataWithCodes: ' + err)
      return null
    }
  }

  async _postData({
    url,
    body,
    token,
    query_params = {},
    url_ext = '',
    additional_headers = {},
    tag,
  }: {
    url: string
    body: object | string
    token?: string
    query_params?: Record<string, string>
    url_ext?: string
    additional_headers?: Record<string, string>
    tag?: FEATURE_KEY
  }) {
    if (!evaluateFeatureFlags(tag)) {
      console.debug(`Returning null because feature is disabled for tag ${tag} and url ${url}`)
      return null
    }
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'POST',
        body: body as BodyInit,
        headers: {
          ...additional_headers,
          Authorization: token,
          'Content-Type': 'application/json',
        },
      })

      let respBody = undefined
      try {
        respBody = await resp.json()
      } catch (err) {
        console.error('Error in _postData: ' + err)
      }
      return {
        body: respBody,
        status: resp.status,
        message: respBody?.message,
      }
    } catch (err) {
      console.error('Error occurred in _postData', err)
      return null
    }
  }

  // Helper Functions
  async _deleteData({
    url,
    token,
    query_params = {},
    url_ext = '',
    additional_headers = {},
    tag,
  }: {
    url: string
    token: string
    query_params?: Record<string, string>
    url_ext?: string
    additional_headers?: Record<string, string>
    tag?: FEATURE_KEY
  }) {
    if (!evaluateFeatureFlags(tag)) {
      console.debug(`Returning null because feature is disabled for tag ${tag} and url ${url}`)
      return null
    }
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'DELETE',
        headers: {
          ...additional_headers,
          Authorization: token,
          'Content-Type': 'application/json',
        },
      })

      let respBody = undefined
      try {
        respBody = await resp.json()
      } catch (err) {
        console.error('Error in _deleteData: ' + err)
      }
      return {
        body: respBody,
        status: resp.status,
        message: respBody?.message,
      }
    } catch (err) {
      console.error('Error in _getDataWithCodes: ' + err)
      return null
    }
  }

  // Helper Functions
  async _patchData({
    url,
    token,
    body,
    query_params = {},
    url_ext = '',
    additional_headers = {},
    tag,
  }: {
    url: string
    token: string
    body: object
    query_params?: Record<string, string>
    url_ext?: string
    additional_headers?: Record<string, string>
    tag?: FEATURE_KEY
  }) {
    if (!evaluateFeatureFlags(tag)) {
      console.debug(`Returning null because feature is disabled for tag ${tag} and url ${url}`)
      return null
    }
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'PATCH',
        body: body as BodyInit,
        headers: {
          ...additional_headers,
          Authorization: token,
          'Content-Type': 'application/json',
        },
      })

      let respBody = undefined
      try {
        respBody = await resp.json()
      } catch (err) {
        console.error('Error in _patchData: ' + err)
      }
      return {
        body: respBody,
        status: resp.status,
        message: respBody?.message,
      }
    } catch (err) {
      console.error('Error in _getDataWithCodes: ' + err)
      return null
    }
  }
}

const apiHelper = new ApiHelper()
export default apiHelper

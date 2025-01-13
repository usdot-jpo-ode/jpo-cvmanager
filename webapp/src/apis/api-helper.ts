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
    console.debug('GETTING DATA FROM ' + url)
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'GET',
        headers: {
          ...additional_headers,
          Authorization: token,
        },
      })

      const response = await resp.json()
      console.debug(
        'GET REQUEST MADE TO ' +
          url +
          url_ext +
          ' WITH QUERY PARAMS ' +
          query_params +
          ' AND RESPONSE ' +
          JSON.stringify(response)
      )
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
    console.debug('GETTING DATA FROM ' + url)
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
      console.debug(
        'GET CODES REQUEST MADE TO ' +
          url +
          url_ext +
          ' WITH QUERY PARAMS ' +
          query_params +
          ' AND RESPONSE ' +
          JSON.stringify(respBody)
      )

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
    body: Object | string
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
    console.debug('POSTING DATA TO ' + url)
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
      console.debug(
        'POST REQUEST MADE TO ' +
          url +
          url_ext +
          ' WITH QUERY PARAMS ' +
          query_params +
          ' AND BODY ' +
          body +
          ' AND RESPONSE ' +
          JSON.stringify(respBody)
      )

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
    console.debug('DELETING DATA FROM ' + url)
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
      console.debug(
        'DELETE REQUEST MADE TO ' +
          url +
          url_ext +
          ' WITH QUERY PARAMS ' +
          query_params +
          ' AND RESPONSE ' +
          JSON.stringify(respBody)
      )

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
    body: Object
    query_params?: Record<string, string>
    url_ext?: string
    additional_headers?: Record<string, string>
    tag?: FEATURE_KEY
  }) {
    if (!evaluateFeatureFlags(tag)) {
      console.debug(`Returning null because feature is disabled for tag ${tag} and url ${url}`)
      return null
    }
    console.debug('PATCHING DATA FROM ' + url)
    try {
      console.debug('REQUEST BODY', body)
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

      console.debug(
        'PATCH REQUEST MADE TO ' +
          url +
          url_ext +
          ' WITH QUERY PARAMS ' +
          query_params +
          ' AND BODY ' +
          body +
          ' AND RESPONSE ' +
          JSON.stringify(respBody)
      )

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

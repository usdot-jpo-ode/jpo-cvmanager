class ApiHelper {
  formatQueryParams(query_params) {
    if (
      !query_params || // 👈 null and undefined check
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
  async _getData({ url, token, query_params = {}, url_ext = '', additional_headers = {} }) {
    console.debug('GETTING DATA FROM ' + url)
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'GET',
        headers: {
          ...additional_headers,
          Authorization: token,
        },
      })
      console.debug('GOT RESPONSE FROM', url + url_ext, resp)

      return await resp.json()
    } catch (err) {
      console.error('Error in _getData: ' + err)
      return null
    }
  }

  // Helper Functions
  async _getDataWithCodes({ url, token, query_params = {}, url_ext = '', additional_headers = {} }) {
    console.debug('GETTING DATA FROM ' + url)
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'GET',
        headers: {
          ...additional_headers,
          Authorization: token,
        },
      })
      console.debug('GOT RESPONSE FROM', url + url_ext, resp)

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

  async _postData({ url, body, token, query_params = {}, url_ext = '', additional_headers = {} }) {
    console.debug('POSTING DATA TO ' + url)
    try {
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'POST',
        body,
        headers: {
          ...additional_headers,
          Authorization: token,
          'Content-Type': 'application/json',
        },
      })
      console.debug('GOT RESPONSE FROM', url, resp)

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
  async _deleteData({ url, token, query_params = {}, url_ext = '', additional_headers = {} }) {
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
      console.debug('GOT RESPONSE FROM', url + url_ext, resp)

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
  async _patchData({ url, token, body, query_params = {}, url_ext = '', additional_headers = {} }) {
    console.debug('PATCHING DATA FROM ' + url)
    try {
      console.debug('REQUEST BODY', body)
      const resp = await fetch(url + this.formatQueryParams(query_params) + url_ext, {
        method: 'PATCH',
        body,
        headers: {
          ...additional_headers,
          Authorization: token,
          'Content-Type': 'application/json',
        },
      })
      console.debug('GOT RESPONSE FROM', url + url_ext, resp)

      let respBody = undefined
      console.log('PATCH RESPONSE 1', resp)
      try {
        respBody = await resp.json()
      } catch (err) {
        console.error('Error in _patchData: ' + err)
      }
      console.log('PATCH RESPONSE 2', resp)

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

export default new ApiHelper()

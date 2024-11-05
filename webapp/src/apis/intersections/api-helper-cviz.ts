import toast from 'react-hot-toast'
import EnvironmentVars from '../../EnvironmentVars'

class CvizApiHelper {
  formatQueryParams(query_params?: Record<string, any>): string {
    if (!query_params || Object.keys(query_params).length === 0) return ''
    return `?${new URLSearchParams(query_params).toString()}`
  }

  async invokeApi({
    path,
    basePath,
    method = 'GET',
    headers = {},
    queryParams,
    body,
    token,
    timeout,
    abortController,
    responseType = 'json',
    booleanResponse = false,
    toastOnFailure = true,
    toastOnSuccess = false,
    successMessage = 'Successfully completed request!',
    failureMessage = 'Request failed to complete',
  }: {
    path: string
    basePath?: string
    method?: string
    headers?: Record<string, string>
    queryParams?: Record<string, string>
    body?: Object
    token?: string
    timeout?: number
    abortController?: AbortController
    responseType?: string
    booleanResponse?: boolean
    toastOnFailure?: boolean
    toastOnSuccess?: boolean
    successMessage?: string
    failureMessage?: string
  }): Promise<any> {
    const url = (basePath ?? EnvironmentVars.CVIZ_API_SERVER_URL!) + path + this.formatQueryParams(queryParams)

    const localHeaders: HeadersInit = { ...headers }
    if (token) localHeaders['Authorization'] = `Bearer ${token}`
    if (method === 'POST' && body && !('Content-Type' in localHeaders)) {
      localHeaders['Content-Type'] = 'application/json'
    }

    let id: NodeJS.Timeout | undefined = undefined
    if (timeout) {
      if (!abortController) {
        abortController = new AbortController()
      }
      id = setTimeout(() => abortController?.abort(), timeout)
    }

    const options: RequestInit = {
      method: method,
      headers: localHeaders,
      body: body
        ? localHeaders['Content-Type'] === 'application/x-www-form-urlencoded'
          ? (body as string)
          : JSON.stringify(body)
        : undefined,
      mode: 'cors',
      signal: abortController?.signal,
    }

    console.debug('MAKING REQUEST TO ' + url + ' WITH OPTIONS', options)

    const resp = await fetch(url, options)
      .then((response) => {
        if (response.ok) {
          if (toastOnSuccess) toast.success(successMessage)
          if (booleanResponse) return true
        } else {
          console.error('Request failed with status code ' + response.status + ': ' + response.statusText)
          if (response.status === 401) {
            toast.error('Authentication failed, please sign in again')
            // signIn();
          } else if (response.status === 403) {
            toast.error('You are not authorized to perform this action.')
          } else if (toastOnFailure) toast.error(failureMessage + ', with status code ' + response.status)

          if (booleanResponse) return false
          return undefined
        }
        if (responseType === 'blob') {
          return response.blob()
        } else {
          const resp = response.json()
          resp.then((val) => console.debug('RESPONSE TO', url, val))
          return resp
        }
      })
      .catch((error: Error) => {
        const errorMessage = failureMessage ?? 'Fetch request failed'
        toast.error(errorMessage + '. Error: ' + error.message)
        console.error(error.message)
      })
    if (id) clearTimeout(id)
    return resp
  }
}

export const authApiHelper = new CvizApiHelper()

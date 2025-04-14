import { authApiHelper } from './api-helper-cviz'

class ConfigParamsApi {
  async getGeneralParameters(token: string, abortController?: AbortController): Promise<Config[]> {
    // return ConfigParamsGeneral;
    try {
      var response: PagedResponse<Config> = await authApiHelper.invokeApi({
        path: '/config/default/all',
        token: token,
        abortController,
        failureMessage: 'Failed to retrieve general parameters',
        tag: 'intersection',
      })
      return response?.content ?? ([] as Config[])
    } catch (exception_var) {
      console.error(exception_var)
      return []
    }
  }

  async getIntersectionParameters(
    token: string,
    intersectionId: number,
    roadRegulatorId: number,
    abortController?: AbortController
  ): Promise<IntersectionConfig[]> {
    // return configParamsIntersection;
    try {
      var response: PagedResponse<IntersectionConfig> = await authApiHelper.invokeApi({
        path: '/config/intersection/unique',
        token: token,
        abortController,
        queryParams: { intersection_id: intersectionId.toString(), road_regulator_id: roadRegulatorId.toString() },
        failureMessage: 'Failed to retrieve unique intersection parameters',
        tag: 'intersection',
      })
      return response?.content ?? ([] as IntersectionConfig[])
    } catch (exception_var) {
      console.error(exception_var)
      return []
    }
  }

  async getAllParameters(
    token: string,
    intersectionId: number,
    roadRegulatorId: number,
    abortController?: AbortController
  ): Promise<Config[]> {
    try {
      var response: PagedResponse<Config> = await authApiHelper.invokeApi({
        path: '/config/intersection/unique',
        token: token,
        abortController,
        queryParams: { intersection_id: intersectionId.toString(), road_regulator_id: roadRegulatorId.toString() },
        failureMessage: 'Failed to retrieve unique intersection parameters',
        tag: 'intersection',
      })
      return response?.content ?? ([] as IntersectionConfig[])
    } catch (exception_var) {
      console.error(exception_var)
      return []
    }
  }

  async getParameterGeneral(
    token: string,
    key: string,
    abortController?: AbortController
  ): Promise<Config | undefined> {
    try {
      var response = (
        (await authApiHelper.invokeApi({
          path: `/config/default/all`,
          token: token,
          abortController,
          failureMessage: `Failed to Retrieve Configuration Parameter ${key}`,
          tag: 'intersection',
        })) as PagedResponse<Config>
      )?.content
        .filter((c) => c.key === key)
        .at(-1)
      return response as Config
    } catch (exception_var) {
      console.error(exception_var)
      return undefined
    }
  }

  async getParameterIntersection(
    token: string,
    key: string,
    intersectionId: number,
    roadRegulatorId: number,
    abortController?: AbortController
  ): Promise<IntersectionConfig | undefined> {
    try {
      var response = (
        (await authApiHelper.invokeApi({
          path: `/config/intersection/all`,
          token: token,
          abortController,
          queryParams: { intersection_id: intersectionId.toString(), road_regulator_id: roadRegulatorId.toString() },
          toastOnFailure: false,
          tag: 'intersection',
          //   failureMessage: `Failed to Retrieve Configuration Parameter ${key}`,
        })) as PagedResponse<IntersectionConfig>
      )?.content
        .filter((c) => c.key === key && c.intersectionID !== null && c.intersectionID !== 0 && c.intersectionID !== -1)
        .at(-1)
      return response as IntersectionConfig
    } catch (exception_var) {
      console.error(exception_var)
      return undefined
    }
  }

  async getParameter(
    token: string,
    key: string,
    intersectionId: number,
    roadRegulatorId: number,
    abortController?: AbortController
  ): Promise<Config | undefined> {
    // try to get intersection parameter first, if not found, get general parameter
    var param: Config | undefined = undefined
    if (intersectionId !== -1) {
      var param: Config | undefined = await this.getParameterIntersection(
        token,
        key,
        intersectionId,
        roadRegulatorId,
        abortController
      )
    }
    if (param == undefined) {
      param = await this.getParameterGeneral(token, key, abortController)
    }
    return param
  }

  async updateDefaultParameter(
    token: string,
    name: string,
    param: Config,
    abortController?: AbortController
  ): Promise<Config | undefined> {
    try {
      var response: PagedResponse<Config> = await authApiHelper.invokeApi({
        path: '/config/default',
        token: token,
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: param,
        abortController,
        toastOnSuccess: true,
        successMessage: `Successfully Update Configuration Parameter ${name}`,
        failureMessage: `Failed to Update Configuration Parameter ${name}`,
        tag: 'intersection',
      })
      return response?.content?.[0] as Config
    } catch (exception_var) {
      console.error(exception_var)
      return undefined
    }
  }

  async updateIntersectionParameter(
    token: string,
    name: string,
    param: IntersectionConfig,
    abortController?: AbortController
  ): Promise<IntersectionConfig | undefined> {
    try {
      var response: PagedResponse<IntersectionConfig> = await authApiHelper.invokeApi({
        path: '/config/intersection',
        token: token,
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: param,
        abortController,
        toastOnSuccess: true,
        successMessage: `Successfully Update Intersection Configuration Parameter ${name}`,
        failureMessage: `Failed to Update Intersection Configuration Parameter ${name}`,
        tag: 'intersection',
      })
      return response?.content?.[0] as IntersectionConfig
    } catch (exception_var) {
      console.error(exception_var)
      return undefined
    }
  }

  async createIntersectionParameter(
    token: string,
    name: string,
    value: Config,
    intersectionId: number,
    roadRegulatorId: number,
    abortController?: AbortController
  ): Promise<Config | undefined> {
    const param: IntersectionConfig = {
      intersectionID: intersectionId,
      roadRegulatorID: roadRegulatorId,
      rsuID: 'rsu_1',
      ...value,
    }

    try {
      var response: PagedResponse<Config> = await authApiHelper.invokeApi({
        path: '/config/intersection/create/' + name,
        token: token,
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: param,
        abortController,
        toastOnSuccess: true,
        successMessage: `Successfully Created Intersection Configuration Parameter ${name}`,
        failureMessage: `Failed to Create Intersection Configuration Parameter ${name}`,
        tag: 'intersection',
      })
      return response?.content?.[0] as Config
    } catch (exception_var) {
      console.error(exception_var)
      return undefined
    }
  }

  async removeOverriddenParameter(
    token: string,
    name: string,
    config: IntersectionConfig,
    abortController?: AbortController
  ): Promise<Config | undefined> {
    try {
      var response: PagedResponse<Config> = await authApiHelper.invokeApi({
        path: '/config/intersection',
        token: token,
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: config,
        abortController,
        toastOnSuccess: true,
        successMessage: `Successfully Removed Intersection Configuration Parameter ${name}`,
        failureMessage: `Failed to Remove Intersection Configuration Parameter ${name}`,
        tag: 'intersection',
      })
      return response?.content?.[0] as Config
    } catch (exception_var) {
      console.error(exception_var)
      return undefined
    }
  }
}

export const configParamApi = new ConfigParamsApi()

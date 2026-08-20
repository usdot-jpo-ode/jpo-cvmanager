import { configureStore } from '@reduxjs/toolkit'
import rsuReducer from './generalSlices/rsuSlice'
import userReducer from './generalSlices/userSlice'
import wzdxReducer from './generalSlices/wzdxSlice'
import configReducer from './generalSlices/configSlice'
import intersectionReducer from './generalSlices/intersectionSlice'
import adminAddOrganizationReducer from './features/adminAddOrganization/adminAddOrganizationSlice'
import adminAddIntersectionReducer from './features/adminAddIntersection/adminAddIntersectionSlice'
import adminEditOrganizationReducer from './features/adminEditOrganization/adminEditOrganizationSlice'
import adminEditIntersectionReducer from './features/adminEditIntersection/adminEditIntersectionSlice'
import adminOrganizationTabReducer from './features/adminOrganizationTab/adminOrganizationTabSlice'
import adminOrganizationTabUserReducer from './features/adminOrganizationTabUser/adminOrganizationTabUserSlice'
import adminOrganizationTabRsuReducer from './features/adminOrganizationTabRsu/adminOrganizationTabRsuSlice'
import adminIntersectionTabReducer from './features/adminIntersectionTab/adminIntersectionTabSlice'
import menuReducer from './features/menu/menuSlice'
import asn1DecoderSlice from './features/intersections/decoder/asn1-decoder-slice'
import intersectionMapReducer from './features/intersections/map/map-slice'
import intersectionMapLayerStyleReducer from './features/intersections/map/map-layer-style-slice'
import dataSelectorReducer from './features/intersections/data-selector/dataSelectorSlice'
import { intersectionConfigSlice } from './features/api/intersectionConfigSlice'
import { intersectionMapApiSlice } from './features/api/intersectionMapApiSlice'
import { intersectionMapApiMiddleware } from './features/api/intersection-map-api-middleware'
import { emailApiSlice } from './features/api/emailApiSlice'
import { organizationApiSlice } from './features/api/organizationApiSlice'
import { rsuCountsApiSlice } from './features/api/rsuCountsApiSlice'
import { unsubscribeApiSlice } from './features/api/unsubscribeApiSlice'
import { subscriptionManagementApiSlice } from './features/api/subscriptionManagementApiSlice'
import { rsuApiSlice } from './features/api/rsuApiSlice'
import { scmsApiSlice } from './features/api/scmsApiSlice'
import { userApiSlice } from './features/api/userApiSlice'
import { adminIntersectionApiSlice } from './features/api/adminIntersectionApiSlice'
import mapSliceReducer from './pages/mapSlice'
import timeSyncReducer from './generalSlices/timeSyncSlice'
import haasSliceReducer from './generalSlices/haasAlertSlice'

export const setupStore = (preloadedState?: Partial<any>) => {
  return configureStore({
    reducer: {
      rsu: rsuReducer,
      user: userReducer,
      wzdx: wzdxReducer,
      config: configReducer,
      intersection: intersectionReducer,
      adminAddOrganization: adminAddOrganizationReducer,
      adminAddIntersection: adminAddIntersectionReducer,
      adminEditOrganization: adminEditOrganizationReducer,
      adminEditIntersection: adminEditIntersectionReducer,
      adminOrganizationTab: adminOrganizationTabReducer,
      adminOrganizationTabUser: adminOrganizationTabUserReducer,
      adminOrganizationTabRsu: adminOrganizationTabRsuReducer,
      adminIntersectionTab: adminIntersectionTabReducer,
      menu: menuReducer,
      intersectionMap: intersectionMapReducer,
      intersectionMapLayerStyle: intersectionMapLayerStyleReducer,
      dataSelector: dataSelectorReducer,
      map: mapSliceReducer,
      asn1Decoder: asn1DecoderSlice,
      timeSync: timeSyncReducer,
      haas: haasSliceReducer,
      [intersectionConfigSlice.reducerPath]: intersectionConfigSlice.reducer,
      [intersectionMapApiSlice.reducerPath]: intersectionMapApiSlice.reducer,
      [emailApiSlice.reducerPath]: emailApiSlice.reducer,
      [organizationApiSlice.reducerPath]: organizationApiSlice.reducer,
      [rsuCountsApiSlice.reducerPath]: rsuCountsApiSlice.reducer,
      [unsubscribeApiSlice.reducerPath]: unsubscribeApiSlice.reducer,
      [subscriptionManagementApiSlice.reducerPath]: subscriptionManagementApiSlice.reducer,
      [rsuApiSlice.reducerPath]: rsuApiSlice.reducer,
      [scmsApiSlice.reducerPath]: scmsApiSlice.reducer,
      [userApiSlice.reducerPath]: userApiSlice.reducer,
      [adminIntersectionApiSlice.reducerPath]: adminIntersectionApiSlice.reducer,
    },
    preloadedState,
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware({
        thunk: true,
        serializableCheck: false,
        immutableCheck: false,
      })
        .concat(intersectionConfigSlice.middleware)
        .concat(intersectionMapApiSlice.middleware)
        .concat(intersectionMapApiMiddleware.middleware)
        .concat(emailApiSlice.middleware)
        .concat(rsuCountsApiSlice.middleware)
        .concat(unsubscribeApiSlice.middleware)
        .concat(subscriptionManagementApiSlice.middleware)
        .concat(organizationApiSlice.middleware)
        .concat(rsuApiSlice.middleware)
        .concat(scmsApiSlice.middleware)
        .concat(userApiSlice.middleware)
        .concat(adminIntersectionApiSlice.middleware),
    devTools: true,
  })
}

type AppStore = ReturnType<typeof setupStore>
export type AppState = ReturnType<AppStore['getState']>

export type AppDispatch = ReturnType<typeof setupStore>['dispatch']

export type RootState = ReturnType<ReturnType<typeof setupStore>['getState']>

import { configureStore } from '@reduxjs/toolkit'
import rsuReducer from './generalSlices/rsuSlice'
import userReducer from './generalSlices/userSlice'
import wzdxReducer from './generalSlices/wzdxSlice'
import configReducer from './generalSlices/configSlice'
import intersectionReducer from './generalSlices/intersectionSlice'
import adminAddOrganizationReducer from './features/adminAddOrganization/adminAddOrganizationSlice'
import adminAddRsuReducer from './features/adminAddRsu/adminAddRsuSlice'
import adminAddIntersectionReducer from './features/adminAddIntersection/adminAddIntersectionSlice'
import adminAddUserReducer from './features/adminAddUser/adminAddUserSlice'
import adminEditOrganizationReducer from './features/adminEditOrganization/adminEditOrganizationSlice'
import adminEditIntersectionReducer from './features/adminEditIntersection/adminEditIntersectionSlice'
import adminEditUserReducer from './features/adminEditUser/adminEditUserSlice'
import adminOrganizationTabReducer from './features/adminOrganizationTab/adminOrganizationTabSlice'
import adminOrganizationTabUserReducer from './features/adminOrganizationTabUser/adminOrganizationTabUserSlice'
import adminOrganizationTabRsuReducer from './features/adminOrganizationTabRsu/adminOrganizationTabRsuSlice'
import adminOrganizationTabIntersectionReducer from './features/adminOrganizationTabIntersection/adminOrganizationTabIntersectionSlice'
import adminIntersectionTabReducer from './features/adminIntersectionTab/adminIntersectionTabSlice'
import adminUserTabReducer from './features/adminUserTab/adminUserTabSlice'
import adminNotificationTabReducer from './features/adminNotificationTab/adminNotificationTabSlice'
import adminAddNotificationReducer from './features/adminAddNotification/adminAddNotificationSlice'
import adminEditNotificationReducer from './features/adminEditNotification/adminEditNotificationSlice'
import menuReducer from './features/menu/menuSlice'
import asn1DecoderSlice from './features/intersections/decoder/asn1-decoder-slice'
import intersectionMapReducer from './features/intersections/map/map-slice'
import intersectionMapLayerStyleReducer from './features/intersections/map/map-layer-style-slice'
import dataSelectorReducer from './features/intersections/data-selector/dataSelectorSlice'
import { intersectionApiSlice } from './features/api/intersectionApiSlice'
import { organizationApiSlice } from './features/api/organizationApiSlice'
import { rsuCountsApiSlice } from './features/api/rsuCountsApiSlice'
import { rsuApiSlice } from './features/api/rsuApiSlice'
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
      adminAddRsu: adminAddRsuReducer,
      adminAddIntersection: adminAddIntersectionReducer,
      adminAddUser: adminAddUserReducer,
      adminEditOrganization: adminEditOrganizationReducer,
      adminEditIntersection: adminEditIntersectionReducer,
      adminEditUser: adminEditUserReducer,
      adminOrganizationTab: adminOrganizationTabReducer,
      adminOrganizationTabUser: adminOrganizationTabUserReducer,
      adminOrganizationTabRsu: adminOrganizationTabRsuReducer,
      adminOrganizationTabIntersection: adminOrganizationTabIntersectionReducer,
      adminIntersectionTab: adminIntersectionTabReducer,
      adminUserTab: adminUserTabReducer,
      adminNotificationTab: adminNotificationTabReducer,
      adminAddNotification: adminAddNotificationReducer,
      adminEditNotification: adminEditNotificationReducer,
      menu: menuReducer,
      intersectionMap: intersectionMapReducer,
      intersectionMapLayerStyle: intersectionMapLayerStyleReducer,
      dataSelector: dataSelectorReducer,
      map: mapSliceReducer,
      asn1Decoder: asn1DecoderSlice,
      timeSync: timeSyncReducer,
      haas: haasSliceReducer,
      [intersectionApiSlice.reducerPath]: intersectionApiSlice.reducer,
      [organizationApiSlice.reducerPath]: organizationApiSlice.reducer,
      [rsuCountsApiSlice.reducerPath]: rsuCountsApiSlice.reducer,
      [rsuApiSlice.reducerPath]: rsuApiSlice.reducer,
    },
    preloadedState,
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware({
        thunk: true,
        serializableCheck: false,
        immutableCheck: false,
      })
        .concat(intersectionApiSlice.middleware)
        .concat(organizationApiSlice.middleware)
        .concat(rsuCountsApiSlice.middleware)
        .concat(rsuApiSlice.middleware),
    devTools: true,
  })
}

type AppStore = ReturnType<typeof setupStore>
export type AppState = ReturnType<AppStore['getState']>

export type AppDispatch = ReturnType<typeof setupStore>['dispatch']

export type RootState = ReturnType<ReturnType<typeof setupStore>['getState']>

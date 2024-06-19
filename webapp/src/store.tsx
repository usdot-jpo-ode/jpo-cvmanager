import { configureStore } from '@reduxjs/toolkit'
import rsuReducer from './generalSlices/rsuSlice'
import userReducer from './generalSlices/userSlice'
import wzdxReducer from './generalSlices/wzdxSlice'
import configReducer from './generalSlices/configSlice'
import adminAddOrganizationReducer from './features/adminAddOrganization/adminAddOrganizationSlice'
import adminAddRsuReducer from './features/adminAddRsu/adminAddRsuSlice'
import adminAddUserReducer from './features/adminAdduser/adminAddUserSlice'
import adminEditOrganizationReducer from './features/adminEditOrganization/adminEditOrganizationSlice'
import adminEditRsuReducer from './features/adminEditRsu/adminEditRsuSlice'
import adminEditUserReducer from './features/adminEditUser/adminEditUserSlice'
import adminOrganizationTabReducer from './features/adminOrganizationTab/adminOrganizationTabSlice'
import adminOrganizationTabUserReducer from './features/adminOrganizationTabUser/adminOrganizationTabUserSlice'
import adminOrganizationTabRsuReducer from './features/adminOrganizationTabRsu/adminOrganizationTabRsuSlice'
import adminRsuTabReducer from './features/adminRsuTab/adminRsuTabSlice'
import adminUserTabReducer from './features/adminUserTab/adminUserTabSlice'
import adminNotificationTabReducer from './features/adminNotificationTab/adminNotificationTabSlice'
import adminAddNotificationReducer from './features/adminAddNotification/adminAddNotificationSlice'
import adminEditNotificationReducer from './features/adminEditNotification/adminEditNotificationSlice'
import menuReducer from './features/menu/menuSlice'

export const setupStore = (preloadedState: any) => {
  return configureStore({
    reducer: {
      rsu: rsuReducer,
      user: userReducer,
      wzdx: wzdxReducer,
      config: configReducer,
      adminAddOrganization: adminAddOrganizationReducer,
      adminAddRsu: adminAddRsuReducer,
      adminAddUser: adminAddUserReducer,
      adminEditOrganization: adminEditOrganizationReducer,
      adminEditRsu: adminEditRsuReducer,
      adminEditUser: adminEditUserReducer,
      adminOrganizationTab: adminOrganizationTabReducer,
      adminOrganizationTabUser: adminOrganizationTabUserReducer,
      adminOrganizationTabRsu: adminOrganizationTabRsuReducer,
      adminRsuTab: adminRsuTabReducer,
      adminUserTab: adminUserTabReducer,
      adminNotificationTab: adminNotificationTabReducer,
      adminAddNotification: adminAddNotificationReducer,
      adminEditNotification: adminEditNotificationReducer,
      menu: menuReducer,
    },
    preloadedState,
  })
}

const tempState = setupStore({}).getState

export type RootState = ReturnType<typeof tempState>

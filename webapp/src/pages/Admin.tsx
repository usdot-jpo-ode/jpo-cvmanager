import React, { useEffect } from 'react'
import AdminFormManager from '../components/AdminFormManager'
import { Tab, Tabs, TabList, TabPanel } from 'react-tabs'
import { useDispatch } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'

function Admin() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <div id="admin">
      <h2 className="adminHeader">CV Manager Admin Interface</h2>
      <Tabs>
        <TabList style={{ width: 'fit-content' }}>
          <Tab>
            <p>RSUs</p>
          </Tab>
          <Tab>
            <p>Users</p>
          </Tab>
          <Tab>
            <p>Organizations</p>
          </Tab>
        </TabList>

        <TabPanel>
          <div className="panel-content">
            <AdminFormManager activeForm={'add_rsu'} />
          </div>
        </TabPanel>
        <TabPanel>
          <div className="panel-content">
            <AdminFormManager activeForm={'add_user'} />
          </div>
        </TabPanel>
        <TabPanel>
          <div className="panel-content">
            <AdminFormManager activeForm={'add_organization'} />
          </div>
        </TabPanel>
      </Tabs>
    </div>
  )
}

export default Admin

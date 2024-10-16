import React from 'react'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'

import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import TextField from '@mui/material/TextField'
import EnvironmentVars from '../../EnvironmentVars'
import BounceLoader from 'react-spinners/BounceLoader'
import Select from 'react-select'
import {
  selectRequestOut,
  selectMsgType,
  selectCountList,
  selectStartDate,
  selectEndDate,
  selectWarningMessage,
  selectMessageLoading,
  updateMessageType,
} from '../../generalSlices/rsuSlice'
import { selectCurrentSort, selectSortedCountList, sortCountList, changeDate } from './menuSlice'

import '../../components/css/SnmpwalkMenu.css'
import { CountsListElement } from '../../models/Rsu'
import { MessageType } from '../../models/MessageTypes'
import { useAppDispatch, useAppSelector } from '../../hooks'

const messageTypeOptions = EnvironmentVars.getMessageTypes().map((type) => {
  return { value: type, label: type }
})

const DisplayCounts = () => {
  const dispatch = useAppDispatch()
  const countsMsgType = useAppSelector(selectMsgType)
  const startDate = useAppSelector(selectStartDate)
  const endDate = useAppSelector(selectEndDate)
  const requestOut = useAppSelector(selectRequestOut)
  const warning = useAppSelector(selectWarningMessage)
  const messageLoading = useAppSelector(selectMessageLoading)
  const countList = useAppSelector(selectCountList)
  const currentSort = useAppSelector(selectCurrentSort)
  const sortedCountList = useAppSelector(selectSortedCountList)

  const dateChanged = (e: Date, type: 'start' | 'end') => {
    if (!Number.isNaN(Date.parse(e.toString()))) {
      dispatch(changeDate(e, type, requestOut))
    }
  }

  const getWarningMessage = (warning: boolean) =>
    warning ? (
      <span className="warningMessage" role="alert">
        <p>Warning: time ranges greater than 24 hours may have longer load times.</p>
      </span>
    ) : (
      <span></span>
    )

  const sortBy = (key: string) => {
    dispatch(sortCountList(key, currentSort, countList))
  }

  const getTable = (messageLoading: boolean, sortedCountList: CountsListElement[]) =>
    messageLoading ? (
      <div>
        <div className="table">
          <div className="header">
            <div>RSU</div>
            <div>Road</div>
            <div>Count</div>
          </div>
        </div>
        <span className="bounceLoader">
          <BounceLoader loading={true} color={'#ffffff'}></BounceLoader>
        </span>
      </div>
    ) : (
      <div className="table">
        <div className="header">
          <div onClick={() => sortBy('rsu')}>RSU</div>
          <div onClick={() => sortBy('road')}>Road</div>
          <div onClick={() => sortBy('count')}>Count</div>
        </div>
        <div className="body">{formatRows(sortedCountList)}</div>
      </div>
    )
  const formatRows = (rows: CountsListElement[]) => rows.map((rowData) => <Row {...rowData} />)
  return (
    <div>
      <div id="container" className="sideBarOn">
        <h1 className="h1">{countsMsgType} Counts</h1>
        <div className="DateRangeContainer">
          <div style={{ marginBottom: '8px' }}>
            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DateTimePicker
                label="Select start date"
                value={dayjs(startDate)}
                maxDateTime={dayjs(endDate)}
                onChange={(e) => {
                  if (e === null) return
                  dateChanged(e.toDate(), 'start')
                }}
              />
            </LocalizationProvider>
          </div>
          <div>
            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DateTimePicker
                label="Select end date"
                value={dayjs(endDate)}
                minDateTime={dayjs(startDate)}
                maxDateTime={dayjs(new Date())}
                onChange={(e) => {
                  if (e === null) return
                  dateChanged(e.toDate(), 'end')
                }}
              />
            </LocalizationProvider>
          </div>
        </div>
        <Select
          options={messageTypeOptions}
          defaultValue={messageTypeOptions.filter((o) => o.label === countsMsgType)}
          placeholder="Select Message Type"
          className="selectContainer"
          onChange={(value) => dispatch(updateMessageType(value.value as MessageType))}
        />
        {getWarningMessage(warning)}
        {getTable(messageLoading, sortedCountList)}
      </div>
    </div>
  )
}
const Row = ({ rsu, road, count }: { rsu: string; road: string; count: number }) => (
  <div className="row">
    <div>{rsu}</div>
    <div>{road}</div>
    <div>{count}</div>
  </div>
)
export default DisplayCounts

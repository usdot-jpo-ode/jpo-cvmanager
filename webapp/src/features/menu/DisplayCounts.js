import React from 'react'
import { useSelector, useDispatch } from 'react-redux'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import TextField from '@mui/material/TextField'
import { MessageTypes } from '../../constants'
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
import { selectPreviousRequest, selectCurrentSort, selectSortedCountList, sortCountList, changeDate } from './menuSlice'

import '../../components/css/SnmpwalkMenu.css'

const messageTypeOptions = MessageTypes.map((type) => {
  return { value: type, label: type }
})
const DisplayCounts = (props) => {
  const dispatch = useDispatch()
  const msgType = useSelector(selectMsgType)
  const startDate = useSelector(selectStartDate)
  const endDate = useSelector(selectEndDate)
  const requestOut = useSelector(selectRequestOut)
  const previousRequest = useSelector(selectPreviousRequest)
  const warning = useSelector(selectWarningMessage)
  const messageLoading = useSelector(selectMessageLoading)
  const countList = useSelector(selectCountList)
  const currentSort = useSelector(selectCurrentSort)
  const sortedCountList = useSelector(selectSortedCountList)

  const dateChanged = (e, type) => {
    dispatch(changeDate(e, type, requestOut, previousRequest))
  }

  const getWarningMessage = (warning) =>
    warning ? (
      <span className="warningMessage">
        <p>Warning: time ranges greater than 24 hours may have longer load times.</p>
      </span>
    ) : (
      <span></span>
    )

  const sortBy = (key) => {
    dispatch(sortCountList(key, currentSort, countList))
  }

  const getTable = (messageLoading, sortedCountList) =>
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
  const formatRows = (rows) => rows.map((rowData) => <Row {...rowData} />)
  return (
    <div>
      <div id="container" className="sideBarOn">
        <h1 className="h1">{msgType} Counts</h1>
        <div className="DateRangeContainer">
          <div>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DateTimePicker
                label="Select start date"
                value={dayjs(startDate)}
                maxDateTime={dayjs(endDate)}
                onChange={(e) => {
                  dateChanged(e.toDate(), 'start')
                }}
                renderInput={(params) => <TextField {...params} />}
              />
            </LocalizationProvider>
          </div>
          <div>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DateTimePicker
                label="Select end date"
                value={dayjs(endDate)}
                minDateTime={dayjs(startDate)}
                maxDateTime={dayjs(new Date())}
                onChange={(e) => {
                  dateChanged(e.toDate(), 'end')
                }}
                renderInput={(params) => <TextField {...params} />}
              />
            </LocalizationProvider>
          </div>
        </div>
        <Select
          options={messageTypeOptions}
          defaultValue={messageTypeOptions.filter((o) => o.label === msgType)}
          placeholder="Select Message Type"
          className="selectContainer"
          onChange={(value) => dispatch(updateMessageType(value.value))}
        />
        {getWarningMessage(warning)}
        {getTable(messageLoading, sortedCountList)}
      </div>
    </div>
  )
}
const Row = ({ rsu, road, count }) => (
  <div className="row">
    <div>{rsu}</div>
    <div>{road}</div>
    <div>{count}</div>
  </div>
)
export default DisplayCounts

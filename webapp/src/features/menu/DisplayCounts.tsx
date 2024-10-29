import React from 'react'
import { useSelector, useDispatch } from 'react-redux'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'

import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import TextField from '@mui/material/TextField'
import EnvironmentVars from '../../EnvironmentVars'
import BounceLoader from 'react-spinners/BounceLoader'
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
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { CountsListElement } from '../../models/Rsu'
import { MessageType } from '../../models/MessageTypes'
import { MenuItem, Select, Typography, useTheme } from '@mui/material'

const messageTypeOptions = EnvironmentVars.getMessageTypes().map((type) => {
  return { value: type, label: type }
})

const DisplayCounts = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const countsMsgType = useSelector(selectMsgType)
  const startDate = useSelector(selectStartDate)
  const endDate = useSelector(selectEndDate)
  const requestOut = useSelector(selectRequestOut)
  const warning = useSelector(selectWarningMessage)
  const messageLoading = useSelector(selectMessageLoading)
  const countList = useSelector(selectCountList)
  const currentSort = useSelector(selectCurrentSort)
  const sortedCountList = useSelector(selectSortedCountList)

  const dateChanged = (e: Date, type: 'start' | 'end') => {
    if (!Number.isNaN(Date.parse(e.toString()))) {
      dispatch(changeDate(e, type, requestOut))
    }
  }

  const getWarningMessage = (warning: boolean) =>
    warning ? (
      <Typography
        component="span"
        role="alert"
        sx={{ backgroundColor: theme.palette.error.main, display: 'flex', justifyContent: 'center' }}
      >
        Warning: time ranges greater than 24 hours may have longer load times.
      </Typography>
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
          <BounceLoader loading={true} color={theme.palette.text.primary}></BounceLoader>
        </span>
      </div>
    ) : (
      <div className="table">
        <div className="header">
          <div onClick={() => sortBy('rsu')} style={{ border: `1px solid ${theme.palette.text.primary}` }}>
            RSU
          </div>
          <div onClick={() => sortBy('road')} style={{ border: `1px solid ${theme.palette.text.primary}` }}>
            Road
          </div>
          <div onClick={() => sortBy('count')} style={{ border: `1px solid ${theme.palette.text.primary}` }}>
            Count
          </div>
        </div>
        <div className="body">{formatRows(sortedCountList)}</div>
      </div>
    )
  const formatRows = (rows: CountsListElement[]) => rows.map((rowData) => <Row {...rowData} />)
  return (
    <div>
      <div id="container" className="sideBarOn">
        <h1 className="h1">{countsMsgType} Counts</h1>
        <div
          className="DateRangeContainer"
          style={{ border: `2px solid ${theme.palette.text.primary}`, borderRadius: 15 }}
        >
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
          placeholder="Select Message Type"
          value={countsMsgType}
          onChange={(event) => dispatch(updateMessageType(event.target.value as MessageType))}
          sx={{
            width: '90%',
            textAlign: 'center',
            marginLeft: 2.5,
            marginRight: 'auto',
            position: 'relative',
            zIndex: 1000,
          }}
        >
          {messageTypeOptions.map((option) => {
            return (
              <MenuItem value={option.value} key={option.value}>
                {option.label}
              </MenuItem>
            )
          })}
        </Select>
        {getWarningMessage(warning)}
        {getTable(messageLoading, sortedCountList)}
      </div>
    </div>
  )
}
const Row = ({ rsu, road, count }: { rsu: string; road: string; count: number }) => {
  const theme = useTheme()
  return (
    <div className="row">
      <div style={{ border: `1px solid ${theme.palette.text.primary}` }}>{rsu}</div>
      <div style={{ border: `1px solid ${theme.palette.text.primary}` }}>{road}</div>
      <div style={{ border: `1px solid ${theme.palette.text.primary}` }}>{count}</div>
    </div>
  )
}
export default DisplayCounts

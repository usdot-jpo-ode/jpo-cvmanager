import React from 'react'
import { useSelector, useDispatch } from 'react-redux'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'

import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
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
import {
  selectCurrentSort,
  selectSortedCountList,
  sortCountList,
  changeDate,
  toggleMapMenuSelection,
} from './menuSlice'

import '../../components/css/SnmpwalkMenu.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { CountsListElement } from '../../models/Rsu'
import { MessageType } from '../../models/MessageTypes'
import { Box, FormControl, InputLabel, MenuItem, Paper, Select, Stack, Typography, useTheme } from '@mui/material'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

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
    ) : null

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
          <div onClick={() => sortBy('rsu')} style={{ borderBottom: `1px solid ${theme.palette.text.primary}` }}>
            RSU
          </div>
          <div onClick={() => sortBy('road')} style={{ borderBottom: `1px solid ${theme.palette.text.primary}` }}>
            Road
          </div>
          <div onClick={() => sortBy('count')} style={{ borderBottom: `1px solid ${theme.palette.text.primary}` }}>
            Count
          </div>
        </div>
        <div className="body">{formatRows(sortedCountList)}</div>
      </div>
    )
  const formatRows = (rows: CountsListElement[]) => {
    if (rows.length === 0) {
      return (
        <div className="row">
          <div
            style={{
              gridColumn: '1 / span 3',
              textAlign: 'center',
            }}
          >
            <Typography>No data found for the selected range</Typography>
          </div>
        </div>
      )
    }
    return rows.map((rowData) => <Row {...rowData} />)
  }
  return (
    <Paper sx={{ pb: 1, pl: 1, pr: 1 }}>
      <SideBarHeader
        onClick={() => dispatch(toggleMapMenuSelection('Display Message Counts'))}
        title="Message Counts"
      />
      <Stack direction="column" spacing={2}>
        <Box sx={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DateTimePicker
              sx={{ width: '90%' }}
              label="Select start date"
              value={dayjs(startDate)}
              maxDateTime={dayjs(endDate)}
              onChange={(e) => {
                if (e === null) return
                dateChanged(e.toDate(), 'start')
              }}
            />
          </LocalizationProvider>
        </Box>
        <Box sx={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DateTimePicker
              sx={{ width: '90%' }}
              label="Select end date"
              value={dayjs(endDate)}
              minDateTime={dayjs(startDate)}
              maxDateTime={dayjs(endDate)}
              onChange={(e) => {
                if (e === null) return
                dateChanged(e.toDate(), 'end')
              }}
            />
          </LocalizationProvider>
        </Box>
        <Box sx={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
          <FormControl sx={{ width: '90%' }}>
            <InputLabel htmlFor="counts-msg-dropdown">Message Type</InputLabel>
            <Select
              label="Message Type"
              id="counts-msg-dropdown"
              value={countsMsgType}
              onChange={(event) => dispatch(updateMessageType(event.target.value as MessageType))}
              sx={{
                textAlign: 'left',
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
          </FormControl>
        </Box>
        {getWarningMessage(warning)}
        {getTable(messageLoading, sortedCountList)}
      </Stack>
    </Paper>
  )
}
const Row = ({ rsu, road, count }: { rsu: string; road: string; count: number }) => {
  const theme = useTheme()
  return (
    <div className="row">
      <div style={{ borderBottom: `1px solid ${theme.palette.text.primary}` }}>{rsu}</div>
      <div style={{ borderBottom: `1px solid ${theme.palette.text.primary}` }}>{road}</div>
      <div style={{ borderBottom: `1px solid ${theme.palette.text.primary}` }}>{count}</div>
    </div>
  )
}
export default DisplayCounts

import React, { useMemo } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'

import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import EnvironmentVars from '../../EnvironmentVars'
import BounceLoader from 'react-spinners/BounceLoader'
import { selectWarningMessage, selectMessageLoading } from '../../generalSlices/rsuSlice'
import { toggleMapMenuSelection } from './menuSlice'

import '../../components/css/SnmpwalkMenu.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { CountsListElement } from '../../models/Rsu'
import { MessageType } from '../../models/MessageTypes'
import { Box, FormControl, InputLabel, MenuItem, Paper, Select, Stack, Typography, useTheme } from '@mui/material'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import { useGetRsuCountsQuery } from '../api/rsuCountsApiSlice'
import { selectOrganizationName } from '../../generalSlices/userSlice'

const messageTypeOptions = EnvironmentVars.getMessageTypes().map((type) => {
  return { value: type, label: type }
})

const DisplayCounts = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const warning = useSelector(selectWarningMessage)
  const messageLoading = useSelector(selectMessageLoading)
  const organization = useSelector(selectOrganizationName)

  const [countsMsgType, setCountsMsgType] = React.useState<MessageType>('BSM')
  const [startDate, setStartDate] = React.useState<Date>(new Date())
  const [endDate, setEndDate] = React.useState<Date>(new Date())
  const [currentSort, setCurrentSort] = React.useState<string | null>(null)

  const { data: rsuCounts } = useGetRsuCountsQuery({ organization, startDate, endDate })

  const countList = useMemo(() => {
    return Object.entries(rsuCounts).map(([key, value]) => {
      return {
        key: key,
        rsu: key,
        road: value.road,
        count: value.messageTypeCounts?.[countsMsgType] || 0,
      }
    })
  }, [rsuCounts, countsMsgType])

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

  const sortedCountList = useMemo(() => {
    if (!currentSort) return countList

    const key = currentSort.replace('__desc', '')
    const isDescending = currentSort.includes('__desc')

    return [...countList].sort((a, b) => {
      const aVal = a[key]
      const bVal = b[key]

      if (aVal < bVal) return isDescending ? 1 : -1
      if (aVal > bVal) return isDescending ? -1 : 1
      return 0
    })
  }, [currentSort, countList])

  const sortBy = (key: string) => {
    // Default to ascending. If re-pressed (already sorting by this key), switch to descending.
    if (key === currentSort) {
      setCurrentSort(key + '__desc')
    } else {
      setCurrentSort(key)
    }
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
                if (e && !Number.isNaN(Date.parse(e.toString()))) {
                  setStartDate(e.toDate())
                }
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
                if (e && !Number.isNaN(Date.parse(e.toString()))) {
                  setEndDate(e.toDate())
                }
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
              onChange={(event) => setCountsMsgType(event.target.value as MessageType)}
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

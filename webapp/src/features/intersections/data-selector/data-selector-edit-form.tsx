import React, { useState } from 'react'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import dayjs from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'

import {
  Button,
  Card,
  CardActions,
  CardContent,
  Divider,
  Grid2,
  TextField,
  InputLabel,
  MenuItem,
  Select,
  InputAdornment,
  FormControl,
} from '@mui/material'
import { FormikCheckboxList } from './formik-checkbox-list'
import { selectDataSelectorForm, setDataSelectorForm } from './dataSelectorSlice'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { selectIntersections } from '../../../generalSlices/intersectionSlice'

interface Item {
  label: string
  value: string
}

// TODO: Add processing_time_period event type when supported by the API
const EVENT_TYPES: Item[] = [
  { label: 'All', value: 'All' },
  { label: 'ConnectionOfTravelEvent', value: 'connection_of_travel' },
  { label: 'IntersectionReferenceAlignmentEvent', value: 'intersection_reference_alignment' },
  { label: 'LaneDirectionOfTravelEvent', value: 'lane_direction_of_travel' },
  { label: 'SignalGroupAlignmentEvent', value: 'signal_group_alignment' },
  { label: 'SignalStateConflictEvent', value: 'signal_state_conflict' },
  { label: 'SignalStateEvent', value: 'signal_state' },
  { label: 'SignalStateStopEvent', value: 'signal_state_stop' },
  { label: 'TimeChangeDetailsEvent', value: 'time_change_details' },
  { label: 'MapMinimumDataEvent', value: 'map_minimum_data' },
  { label: 'SpatMinimumDataEvent', value: 'spat_minimum_data' },
  { label: 'MapBroadcastRateEvent', value: 'map_broadcast_rate' },
  { label: 'SpatBroadcastRateEvent', value: 'spat_broadcast_rate' },
]

const ASSESSMENT_TYPES: Item[] = [
  { label: 'All', value: 'All' },
  { label: 'SignalStateEventAssessment', value: 'signal_state_event_assessment' },
  { label: 'StopLineStopAssessment', value: 'signal_state_assessment' },
  { label: 'LaneDirectionOfTravelAssessment', value: 'lane_direction_of_travel' },
  { label: 'ConnectionOfTravelAssessment', value: 'connection_of_travel' },
]

export const DataSelectorEditForm = (props: {
  onQuery: (query: any) => void
  onVisualize: (query: any) => void
  dbIntersectionId: number | undefined
}) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const { onQuery, onVisualize, dbIntersectionId, ...other } = props
  const [visualize, setVisualize] = useState(false)

  const dataSelectorForm = useSelector(selectDataSelectorForm)
  const intersectionIds = useSelector(selectIntersections)

  const formik = useFormik({
    initialValues: {
      ...dataSelectorForm,
      intersectionId: dataSelectorForm.intersectionId ?? dbIntersectionId,
    },
    validationSchema: Yup.object({}),
    onSubmit: async (values, helpers) => {
      const endTime = dayjs(values.startDate).add(values.timeRange, values.timeUnit).toDate()
      try {
        if (visualize) {
          onVisualize({
            intersectionId: values.intersectionId,
            startDate: values.startDate,
            endTime: endTime,
            eventTypes: values.eventTypes.map((e) => e.value).filter((e) => e !== 'All'),
          })
        } else {
          dispatch(setDataSelectorForm(values))
          helpers.setStatus({ success: true })
          helpers.setSubmitting(false)
          onQuery({
            type: values.type,
            intersectionId: values.intersectionId,
            startDate: values.startDate,
            endTime: endTime,
            eventTypes: values.eventTypes.map((e) => e.value).filter((e) => e !== 'All'),
            assessmentTypes: values.assessmentTypes.map((e) => e.value).filter((e) => e !== 'All'),
            bsmVehicleId: values.bsmVehicleId,
          })
        }
      } catch (err) {
        console.error(err)
        toast.error('Something went wrong!')
        helpers.setStatus({ success: false })
        helpers.setErrors({ submit: err.message })
        helpers.setSubmitting(false)
      }
    },
  })

  const onTypeChange = (newType) => {
    formik.setFieldValue('eventTypes', [] as Item[])
    formik.setFieldValue('assessmentTypes', [] as Item[])
  }

  const getTypeSpecificFilters = (type) => {
    switch (type) {
      case 'bsm':
        return (
          <>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.bsmVehicleId && formik.errors.bsmVehicleId)}
                fullWidth
                helperText={formik.touched.bsmVehicleId && formik.errors.bsmVehicleId}
                label="Vehicle ID"
                name="bsmVehicleId"
                onChange={formik.handleChange}
                value={formik.values.bsmVehicleId}
              />
            </Grid2>
          </>
        )
      case 'events':
        return (
          <>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <InputLabel variant="standard" htmlFor="uncontrolled-native">
                Event Type
              </InputLabel>
              <FormikCheckboxList
                values={EVENT_TYPES}
                selectedValues={formik.values.eventTypes}
                setValues={(val) => formik.setFieldValue('eventTypes', val)}
              />
            </Grid2>
          </>
        )
      case 'assessments':
        return (
          <>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <InputLabel variant="standard" htmlFor="uncontrolled-native">
                Assessment Type
              </InputLabel>
              <FormikCheckboxList
                values={ASSESSMENT_TYPES}
                selectedValues={formik.values.assessmentTypes}
                setValues={(val) => formik.setFieldValue('assessmentTypes', val)}
              />
            </Grid2>
          </>
        )
      default:
        return <></>
    }
  }

  return (
    <form onSubmit={formik.handleSubmit} {...other}>
      <Card>
        {/* <CardHeader title="Edit Configuration Parameter" /> */}
        <Divider />
        <CardContent>
          <Grid2 container spacing={3}>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <FormControl fullWidth error={Boolean(formik.touched.intersectionId && formik.errors.intersectionId)}>
                <InputLabel id="intersectionId-label">Intersection ID</InputLabel>
                <Select
                  labelId="intersectionId-label"
                  value={formik.values.intersectionId}
                  label="Intersection ID"
                  name="intersectionId"
                  onChange={(e) => {
                    formik.setFieldValue('intersectionId', Number(e.target.value))
                  }}
                  onBlur={formik.handleBlur}
                >
                  {intersectionIds?.map((intersectionId) => (
                    <MenuItem value={intersectionId.intersectionID} key={intersectionId.intersectionID}>
                      {intersectionId}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <Select
                error={Boolean(formik.touched.type && formik.errors.type)}
                value={formik.values.type}
                label="Type"
                onChange={(e) => {
                  onTypeChange(e.target.value)
                  formik.setFieldValue('type', e.target.value)
                }}
                onBlur={formik.handleBlur}
              >
                <MenuItem value={'events'}>Events</MenuItem>
                <MenuItem value={'assessments'}>Assessments</MenuItem>
              </Select>
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  value={dayjs(formik.values.startDate)}
                  onChange={(e) => formik.setFieldValue('startDate', e?.toDate(), true)}
                />
              </LocalizationProvider>
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <TextField
                helperText={formik.touched.timeRange && formik.errors.timeRange}
                label="Time Range"
                name="timeRange"
                type="number"
                onBlur={formik.handleBlur}
                onChange={formik.handleChange}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <Select
                          error={Boolean(formik.touched.timeUnit && formik.errors.timeUnit)}
                          value={formik.values.timeUnit}
                          label="Unit"
                          onChange={(e) => {
                            formik.setFieldValue('timeUnit', e.target.value)
                          }}
                          onBlur={formik.handleBlur}
                        >
                          <MenuItem value={'minutes'}>minutes</MenuItem>
                          <MenuItem value={'hours'}>hours</MenuItem>
                          <MenuItem value={'days'}>days</MenuItem>
                        </Select>
                      </InputAdornment>
                    ),
                  },
                }}
                value={formik.values.timeRange}
              />
            </Grid2>
            {getTypeSpecificFilters(formik.values.type)}
          </Grid2>
        </CardContent>
        <CardActions
          sx={{
            flexWrap: 'wrap',
            m: -1,
          }}
        >
          <Button
            disabled={formik.isSubmitting}
            type="submit"
            sx={{ m: 1 }}
            variant="contained"
            onClick={() => setVisualize(false)}
          >
            Query Data
          </Button>
          <Button
            disabled={formik.isSubmitting && formik.values.type === 'events'}
            type="submit"
            sx={{ m: 1 }}
            variant="contained"
            onClick={() => setVisualize(true)}
          >
            View Counts
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

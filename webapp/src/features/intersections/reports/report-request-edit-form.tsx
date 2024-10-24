import React from 'react'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { LocalizationProvider } from '@mui/x-date-pickers'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { Button, Card, CardActions, CardContent, Divider, Grid2, TextField } from '@mui/material'

type Props = {
  onGenerateReport: ({
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
  }: {
    intersectionId?: number
    roadRegulatorId?: number
    startTime: Date
    endTime: Date
  }) => void
  dbIntersectionId?: number
}

// TODO: Consider adding a road regulator dropdown
export const ReportRequestEditForm = (props: Props) => {
  const { onGenerateReport, dbIntersectionId } = props
  const formik = useFormik({
    initialValues: {
      startDate: new Date(Date.now() - 86400000), //yesterday
      endDate: new Date(),
      intersectionId: dbIntersectionId,
      roadRegulatorId: -1,
      submit: null,
    },
    validationSchema: Yup.object({
      startDate: Yup.date().required('Start date is required'),
      endDate: Yup.date()
        .required('End date is required')
        .min(Yup.ref('startDate'), 'end date must be after start date'),
      intersectionId: Yup.string().required('Intersection ID is required'),
    }),
    onSubmit: async (values, helpers) => {
      try {
        helpers.setStatus({ success: true })
        helpers.setSubmitting(false)
        onGenerateReport({
          intersectionId: values.intersectionId,
          roadRegulatorId: values.roadRegulatorId,
          startTime: values.startDate,
          endTime: values.endDate,
        })
      } catch (err) {
        console.error(err)
        toast.error('Something went wrong!')
        helpers.setStatus({ success: false })
        helpers.setErrors({ submit: err.message })
        helpers.setSubmitting(false)
      }
    },
  })

  return (
    <form onSubmit={formik.handleSubmit}>
      <Card>
        <Divider />
        <CardContent>
          <Grid2 container spacing={3}>
            <Grid2 size={{ md: 6, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.intersectionId && formik.errors.intersectionId)}
                fullWidth
                label="Intersection ID"
                name="intersectionId"
                onChange={formik.handleChange}
                value={formik.values.intersectionId}
              />
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <LocalizationProvider dateAdapter={AdapterDateFns}>
                <DateTimePicker
                  value={formik.values.startDate}
                  onChange={(e) => formik.setFieldValue('startDate', e as Date | null, true)}
                  disableFuture
                />
              </LocalizationProvider>
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <LocalizationProvider dateAdapter={AdapterDateFns}>
                <DateTimePicker
                  value={formik.values.endDate}
                  onChange={(e) => formik.setFieldValue('endDate', e as Date | null, true)}
                  disableFuture
                />
              </LocalizationProvider>
            </Grid2>
          </Grid2>
        </CardContent>
        <CardActions
          sx={{
            flexWrap: 'wrap',
            m: -1,
          }}
        >
          <Button disabled={formik.isSubmitting} type="submit" sx={{ m: 1 }} variant="contained">
            Generate Performance Report
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

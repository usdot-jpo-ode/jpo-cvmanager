import React from 'react'
import toast from 'react-hot-toast'
import * as Yup from 'yup'
import { useFormik } from 'formik'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { Button, Card, CardActions, CardContent, Divider, Grid2, TextField } from '@mui/material'
import dayjs from 'dayjs'

type Props = {
  onGenerateReport: ({
    intersectionId,
    startTime,
    endTime,
  }: {
    intersectionId?: number
    startTime: Date
    endTime: Date
  }) => void
  dbIntersectionId?: number
}

const DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000

// TODO: Consider adding a road regulator dropdown
export const ReportRequestEditForm = (props: Props) => {
  const { onGenerateReport, dbIntersectionId } = props
  const formik = useFormik({
    initialValues: {
      startDate: new Date(Date.now() - DAY_IN_MILLISECONDS), //yesterday
      endDate: new Date(),
      intersectionId: dbIntersectionId,
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
            <Grid2 size={{ md: 4, xs: 12 }}>
              <TextField
                error={Boolean(formik.touched.intersectionId && formik.errors.intersectionId)}
                fullWidth
                label="Intersection ID"
                name="intersectionId"
                onChange={formik.handleChange}
                value={formik.values.intersectionId}
                helperText={formik.touched.intersectionId && formik.errors.intersectionId}
              />
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  value={dayjs(formik.values.startDate)}
                  label="Start Date"
                  onChange={(e) => formik.setFieldValue('startDate', e?.toDate() as Date | null, true)}
                  disableFuture
                  slotProps={{
                    textField: {
                      variant: 'outlined',
                      fullWidth: true,
                      error: Boolean(formik.touched.startDate && formik.errors.startDate),
                      helperText: formik.touched.startDate && formik.errors.startDate,
                    },
                  }}
                ></DateTimePicker>
              </LocalizationProvider>
            </Grid2>
            <Grid2 size={{ md: 4, xs: 12 }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  value={dayjs(formik.values.endDate)}
                  label="End Date"
                  onChange={(e) => formik.setFieldValue('endDate', e?.toDate() as Date | null, true)}
                  disableFuture
                  slotProps={{
                    textField: {
                      variant: 'outlined',
                      fullWidth: true,
                      error: Boolean(formik.touched.endDate && formik.errors.endDate),
                      helperText: formik.touched.endDate && formik.errors.endDate,
                    },
                  }}
                ></DateTimePicker>
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
          <Button
            disabled={formik.isSubmitting}
            type="submit"
            sx={{ m: 1 }}
            variant="contained"
            className="capital-case museo-slab"
          >
            Generate Performance Report
          </Button>
        </CardActions>
      </Card>
    </form>
  )
}

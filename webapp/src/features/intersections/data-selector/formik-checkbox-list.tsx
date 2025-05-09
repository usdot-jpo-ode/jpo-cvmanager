import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox, Grid2, Typography, useTheme } from '@mui/material'

export const FormikCheckboxList = (props) => {
  const { values, selectedValues, setValues } = props
  const theme = useTheme()

  return values.map((eventType) => (
    <Grid2 size={4} sx={{ height: 'fit-content' }}>
      <div key={eventType.label} style={{ display: 'flex', alignItems: 'center' }}>
        <Checkbox
          sx={{
            color: theme.palette.custom.rowActionIcon,
          }}
          style={{ marginRight: 8 }}
          checked={selectedValues.indexOf(eventType) > -1}
          onChange={(e) => {
            const newEventTypes = [...selectedValues]
            // if value is All, check or uncheck all
            if (eventType.label === 'All') {
              if (e.target.checked) {
                newEventTypes.push(...values)
              } else {
                newEventTypes.splice(0, newEventTypes.length)
              }
            } else {
              // if value is not All, uncheck All
              const index = newEventTypes.findIndex((val) => val.label === 'All')
              if (index > -1) {
                newEventTypes.splice(index, 1)
              }

              if (e.target.checked) {
                newEventTypes.push(eventType)
              } else {
                newEventTypes.splice(newEventTypes.indexOf(eventType), 1)
              }
            }
            setValues(newEventTypes)
          }}
        />
        <Typography color={theme.palette.text.secondary}>{eventType.label}</Typography>
      </div>
    </Grid2>
  ))
}

FormikCheckboxList.propTypes = {
  values: PropTypes.array.isRequired,
  selectedValues: PropTypes.array.isRequired,
  setValues: PropTypes.func.isRequired,
}

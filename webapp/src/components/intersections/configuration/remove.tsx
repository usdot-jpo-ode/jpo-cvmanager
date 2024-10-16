import React, { useEffect } from 'react'
import { Box, CircularProgress, Container, Typography, useTheme } from '@mui/material'
import { ConfigParamRemoveForm } from '../../../features/intersections/configuration/configuration-remove-form'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useParams } from 'react-router-dom'
import { useAppSelector } from '../../../hooks'
import {
  filterParameter,
  useLazyGetGeneralParametersQuery,
  useLazyGetIntersectionParametersQuery,
} from '../../../features/api/intersectionConfigParamApiSlice'

const ConfigParamRemove = () => {
  const theme = useTheme() // Access the current theme
  const intersectionId = useAppSelector(selectSelectedIntersectionId)

  const [triggerIntersection, { data: intersectionParameters, isFetching: isFetchingIntersection }] =
    useLazyGetIntersectionParametersQuery()
  const [triggerGeneral, { data: generalParameters, isFetching: isFetchingGeneral }] =
    useLazyGetGeneralParametersQuery()

  const { key } = useParams<{ key: string }>()

  useEffect(() => {
    if (intersectionId) {
      triggerIntersection(intersectionId)
    }
  }, [intersectionId, triggerIntersection])

  useEffect(() => {
    triggerGeneral(undefined)
  }, [triggerGeneral])

  console.log(theme)

  const parameter = filterParameter(key, intersectionParameters, generalParameters)

  if (!parameter) {
    return (
      <>
        <Box
          component="main"
          sx={{
            backgroundColor: 'background.default',
            flexGrow: 1,
            py: 8,
          }}
        >
          <Container maxWidth="md">
            <Box
              sx={{
                alignItems: 'center',
                display: 'flex',
                overflow: 'hidden',
              }}
            >
              {isFetchingIntersection || isFetchingGeneral ? (
                <div>
                  <CircularProgress />

                  <Typography noWrap variant="h4">
                    Loading {key}
                  </Typography>
                </div>
              ) : (
                <div>
                  <Typography noWrap variant="h4">
                    Unable to find parameter {key}
                  </Typography>
                </div>
              )}
            </Box>
          </Container>
        </Box>
      </>
    )
  } else {
    return (
      <>
        <Box
          component="main"
          sx={{
            backgroundColor: 'background.default',
            flexGrow: 1,
            py: 8,
          }}
        >
          <Container maxWidth="md">
            <Box
              sx={{
                alignItems: 'center',
                display: 'flex',
                overflow: 'hidden',
              }}
            >
              <div>
                <Typography variant="h5">{parameter.key}</Typography>
              </div>
            </Box>
            <Box mt={3}>
              <ConfigParamRemoveForm parameter={parameter} defaultParameter={parameter} />
            </Box>
          </Container>
        </Box>
      </>
    )
  }
}

export default ConfigParamRemove

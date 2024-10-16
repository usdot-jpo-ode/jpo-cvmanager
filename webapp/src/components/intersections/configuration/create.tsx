import React, { useEffect } from 'react'
import { Box, CircularProgress, Container, Typography } from '@mui/material'
import { ConfigParamCreateForm } from '../../../features/intersections/configuration/configuration-create-form'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useParams } from 'react-router-dom'
import { useAppSelector } from '../../../hooks'
import {
  filterParameter,
  useLazyGetGeneralParametersQuery,
  useLazyGetIntersectionParametersQuery,
} from '../../../features/api/intersectionConfigParamApiSlice'

const ConfigParamCreate = () => {
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

                  <Typography noWrap variant="h5">
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
                <Typography noWrap variant="h4">
                  {parameter.category}/{parameter.key}
                </Typography>
              </div>
            </Box>
            <Box mt={3}>
              <ConfigParamCreateForm parameter={parameter} />
            </Box>
          </Container>
        </Box>
      </>
    )
  }
}

export default ConfigParamCreate

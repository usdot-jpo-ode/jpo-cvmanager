import React from 'react'
import { Box, Container, Typography } from '@mui/material'
import { ConfigParamEditForm } from '../../../features/intersections/configuration/configuration-edit-form'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useParams } from 'react-router-dom'
import { useAppSelector } from '../../../hooks'
import { selectParameter } from '../../../features/api/intersectionConfigParamApiSlice'

const ConfigParamEdit = () => {
  const intersectionId = useAppSelector(selectSelectedIntersectionId)

  const { key } = useParams<{ key: string }>()

  const parameter = useAppSelector(selectParameter(key, intersectionId))

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
              <div>
                <Typography variant="h6">Unable to find parameter {key}</Typography>
              </div>
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
          <Container maxWidth="lg">
            <Box
              sx={{
                alignItems: 'center',
                display: 'flex',
                overflow: 'hidden',
              }}
            >
              <Typography noWrap variant="h5">
                {parameter.key}
              </Typography>
            </Box>
            <Box mt={3}>
              <ConfigParamEditForm parameter={parameter} />
            </Box>
          </Container>
        </Box>
      </>
    )
  }
}

export default ConfigParamEdit

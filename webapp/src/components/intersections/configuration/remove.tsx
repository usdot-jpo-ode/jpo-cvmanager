import React from 'react'
import { Box, Container, Typography } from '@mui/material'
import { ConfigParamRemoveForm } from '../../../features/intersections/configuration/configuration-remove-form'
import { useParams } from 'react-router-dom'
import { useAppSelector } from '../../../hooks'
import { selectParameter } from '../../../features/api/intersectionApiSlice'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'

const ConfigParamRemove = () => {
  const intersectionId = useAppSelector(selectSelectedIntersectionId)

  const { key } = useParams<{ key: string }>()

  const parameter = useAppSelector(selectParameter(key))
  const formattedKey = parameter?.key.replace(/\./g, '.\u200B') // Replace periods with period and zero-width space, to help with line breaks

  if (!parameter || intersectionId === -1) {
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
              <div>
                <Typography variant="h6">
                  Unable to find parameter:
                  <br />
                  {formattedKey}
                  <br />
                  Do you have the right intersection ID selected?
                </Typography>
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
          <Container maxWidth="md">
            <Box
              sx={{
                alignItems: 'center',
                display: 'flex',
                overflow: 'hidden',
              }}
            >
              <div>
                <Typography variant="h5">{formattedKey}</Typography>
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

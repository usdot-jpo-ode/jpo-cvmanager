import React from 'react'
import { Box, Container, Typography } from '@mui/material'
import { ConfigParamEditForm } from '../../../features/intersections/configuration/configuration-edit-form'
import { useParams } from 'react-router-dom'
import { useAppSelector } from '../../../hooks'
import { selectParameter } from '../../../features/api/intersectionApiSlice'

const ConfigParamEdit = () => {
  const { key } = useParams<{ key: string }>()

  const parameter = useAppSelector(selectParameter(key))
  const formattedKey = parameter?.key.replace(/\./g, '.\u200B') // Replace periods with period and zero-width space, to help with line breaks

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
                <Typography variant="h6">
                  Unable to find parameter:
                  <br />
                  {formattedKey}
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
          <Container maxWidth="lg">
            <Box
              sx={{
                alignItems: 'center',
                display: 'flex',
                overflow: 'hidden',
              }}
            >
              <Typography variant="h5">{formattedKey}</Typography>
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

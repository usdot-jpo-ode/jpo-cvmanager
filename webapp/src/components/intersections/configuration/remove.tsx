import React, { useState, useCallback, useEffect } from 'react'
import { Box, Container, Typography } from '@mui/material'
import { configParamApi } from '../../../apis/intersections/configuration-param-api'
import { ConfigParamRemoveForm } from '../../../features/intersections/configuration/configuration-remove-form'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { selectToken } from '../../../generalSlices/userSlice'
import { useSelector } from 'react-redux'
import { useParams } from 'react-router-dom'

const ConfigParamRemove = () => {
  const [parameter, setParameter] = useState<Config | undefined>(undefined)
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)
  const token = useSelector(selectToken)

  const { key } = useParams<{ key: string }>()

  const getParameter = async (key: string) => {
    try {
      const data = await configParamApi.getParameter(token, key, intersectionId, roadRegulatorId)

      setParameter(data)
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => {
    getParameter(key as string)
  }, [intersectionId])

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
                <Typography noWrap variant="h4">
                  Unable to find parameter {key}
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
                <Typography noWrap variant="h4">
                  {parameter.category}/{parameter.key}
                </Typography>
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

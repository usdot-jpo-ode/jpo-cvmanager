import React, { useEffect, useState } from 'react'
import { Box, Container, Grid2, Typography } from '@mui/material'
import DecoderApi from '../../apis/intersections/decoder-api'
import { DecoderTables } from '../../features/intersections/decoder/decoder-tables'
import { v4 as uuidv4 } from 'uuid'
import MapTab, { getTimestamp } from '../../features/intersections/map/map-component'
import { selectToken } from '../../generalSlices/userSlice'
import { useAppSelector } from '../../hooks'

const DecoderPage = () => {
  const token = useAppSelector(selectToken)

  const [data, setData] = useState({} as { [id: string]: DecoderDataEntry })
  const [selectedMapMessage, setSelectedMapMessage] = useState(
    undefined as undefined | { id: string; intersectionId: number; rsuIp: string }
  )
  const [selectedBsms, setSelectedBsms] = useState([] as string[])

  useEffect(() => {
    const freshData = [] as DecoderDataEntry[]
    for (let i = 0; i < 3; i++) {
      const id = uuidv4()
      if (i % 3 == 2) {
        // bsm
        setSelectedBsms((prevBsms) => [...prevBsms, id])
      }
      freshData.push({
        id: id,
        type: i % 3 == 0 ? 'MAP' : i % 3 == 1 ? 'SPAT' : 'BSM',
        status: 'NOT_STARTED',
        text: '',
        selected: false,
        isGreyedOut: false,
        decodedResponse: undefined,
      })
    }
    setData(freshData.reduce((acc, entry) => ({ ...acc, [entry.id]: entry }), {}))
  }, [])

  const submitDecoderRequest = (data: string, type: DECODER_MESSAGE_TYPE) => {
    return DecoderApi.submitDecodeRequest({
      token: token,
      data,
      type,
    })
  }

  const onTextChanged = (id: string, text: string, type: DECODER_MESSAGE_TYPE) => {
    setData((prevData) => {
      submitDecoderRequest(text, type)?.then((response) => {
        if (type == 'BSM') {
          setSelectedBsms((prevBsms) => [...prevBsms, id])
        }
        setData((prevData) => {
          return {
            ...prevData,
            [id]: {
              ...prevData[id],
              decodedResponse: response,
              timestamp: getTimestampFromType(type, response),
              status: text == '' ? 'NOT_STARTED' : response == undefined ? 'ERROR' : 'COMPLETED',
            },
          }
        })
      })
      let newEntry = {}
      if (prevData[id].text != undefined) {
        let newId = uuidv4()
        newEntry[newId] = {
          id: newId,
          type: type,
          status: 'NOT_STARTED',
          text: '',
          selected: false,
          isGreyedOut: false,
          decodedResponse: undefined,
        }
      }
      return {
        ...prevData,
        ...newEntry,
        [id]: {
          id: id,
          type: type,
          status: 'IN_PROGRESS',
          selected: false,
          isGreyedOut: false,
          text: text,
          decodedResponse: undefined,
        },
      }
    })
  }

  const onItemDeleted = (id: string) => {
    if (data[id]?.text != '') {
      setData((prevData) => {
        delete prevData[id]
        return { ...prevData }
      })
    }
  }

  const onItemSelected = (id: string) => {
    const type = data[id].type
    switch (type) {
      case 'MAP':
        const intersectionId = data[id]?.decodedResponse?.processedMap?.properties?.intersectionId
        const rsuIp = data[id]?.decodedResponse?.processedMap?.properties?.originIp
        if (intersectionId) {
          setSelectedMapMessage({ id, intersectionId, rsuIp: rsuIp! })
        }
        return
      case 'BSM':
        setSelectedBsms((prevBsms) => {
          if (prevBsms.includes(id)) {
            return prevBsms.filter((bsmId) => bsmId !== id)
          } else {
            return [...prevBsms, id]
          }
        })
        return
    }
  }

  const getTimestampFromType = (type: DECODER_MESSAGE_TYPE, decodedResponse: DecoderApiResponseGeneric | undefined) => {
    switch (type) {
      case 'MAP':
        return getTimestamp(decodedResponse?.processedMap?.properties.odeReceivedAt)
      case 'SPAT':
        return getTimestamp(decodedResponse?.processedSpat?.utcTimeStamp)
      case 'BSM':
        return getTimestamp(decodedResponse?.bsm?.metadata.odeReceivedAt)
    }
  }

  const onFileUploaded = (contents: string[], type: DECODER_MESSAGE_TYPE) => {
    setData((prevData) => {
      const textToIds: { [text: string]: string } = {}
      contents.forEach((text) => {
        const id = uuidv4()
        textToIds[text] = id
        submitDecoderRequest(text, type)?.then((response) => {
          if (type == 'BSM') {
            setSelectedBsms((prevBsms) => [...prevBsms, id])
          }
          setData((prevData) => {
            return {
              ...prevData,
              [id]: {
                ...prevData[id],
                decodedResponse: response,
                timestamp: getTimestampFromType(type, response),
                status: text == '' ? 'NOT_STARTED' : response == undefined ? 'ERROR' : 'COMPLETED',
              },
            }
          })
        })
      })
      let newEntries = {}
      contents.forEach((text) => {
        newEntries[textToIds[text]] = {
          id: textToIds[text],
          type: type,
          status: 'IN_PROGRESS',
          text: text,
          timestamp: undefined,
          selected: false,
          isGreyedOut: false,
          decodedResponse: undefined,
        }
      })
      return {
        ...prevData,
        ...newEntries,
      }
    })
  }

  const getIntersectionId = (decodedResponse: DecoderApiResponseGeneric | undefined) => {
    if (!decodedResponse) {
      return undefined
    }

    switch (decodedResponse.type) {
      case 'MAP':
        const mapPayload = decodedResponse.processedMap
        return mapPayload?.properties?.intersectionId
      case 'SPAT':
        const spatPayload = decodedResponse.processedSpat
        return spatPayload?.intersectionId
      case 'BSM':
        const bsmPayload = decodedResponse.bsm
        return bsmPayload?.metadata.originIp
    }
  }

  const isGreyedOut = (intersectionId: number | string | undefined) => {
    return selectedMapMessage?.intersectionId === undefined || intersectionId !== selectedMapMessage?.intersectionId
  }

  const isGreyedOutIp = (rsuIp: string | undefined) => {
    return (selectedMapMessage?.rsuIp === undefined || rsuIp !== selectedMapMessage?.rsuIp) && rsuIp != ''
  }

  return (
    <>
      <Box
        component="main"
        sx={{
          backgroundColor: 'background.default',
          flexGrow: 1,
          py: 4,
        }}
      >
        <Container maxWidth={false} style={{ minWidth: '1000px' }}>
          <Box
            sx={{
              alignItems: 'center',
              display: 'flex',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              m: -1,
            }}
          >
            <Grid2 container justifyContent="space-between" spacing={3}>
              <Grid2>
                <Typography sx={{ m: 1 }} variant="h4" color="text.secondary">
                  ASN.1 Decoder
                </Typography>
              </Grid2>
            </Grid2>
          </Box>
          <Box
            sx={{
              alignItems: 'center',
              display: 'flex',
              overflow: 'hidden',
              height: '50vh',
            }}
          >
            <MapTab
              sourceData={{
                map: Object.values(data)
                  .filter((v) => v.type === 'MAP' && v.status == 'COMPLETED' && v.id == selectedMapMessage?.id)
                  .map((v) => v.decodedResponse?.processedMap!),
                spat: Object.values(data)
                  .filter(
                    (v) =>
                      v.type === 'SPAT' && v.status == 'COMPLETED' && !isGreyedOut(getIntersectionId(v.decodedResponse))
                  )
                  .map((v) => v.decodedResponse?.processedSpat!),
                bsm: Object.values(data)
                  .filter((v) => v.type === 'BSM' && v.status == 'COMPLETED' && v.selected)
                  .map((v) => v.decodedResponse?.bsm!),
              }}
              sourceDataType={'exact'}
              intersectionId={-1}
              roadRegulatorId={-1}
            />
          </Box>
          <Grid2 container justifyContent="space-between" spacing={3}>
            <Grid2>
              <Typography sx={{ m: 1 }} variant="h6" color="white">
                1. Upload data, either by uploading individual files or pasting the data directly into the text box.
                <br />
                2. Select an uploaded MAP message to view the decoded data. SPAT data is filtered by intersection ID.
                <br />
                3. Select BSM messages to view the decoded data. All selected BSM data is shown.
              </Typography>
            </Grid2>
          </Grid2>
        </Container>
        <Container sx={{ mt: 1, alignItems: 'center', display: 'flex' }}>
          <DecoderTables
            contents={Object.values(data)}
            selectedIntersectionId={selectedMapMessage?.intersectionId}
            selectedMapMessageId={selectedMapMessage?.id}
            selectedRsuIp={selectedMapMessage?.rsuIp}
            onItemSelected={onItemSelected}
            onTextChanged={onTextChanged}
            onItemDeleted={onItemDeleted}
            onFileUploaded={onFileUploaded}
            selectedBsms={selectedBsms}
            setSelectedBsms={setSelectedBsms}
          />
        </Container>
      </Box>
    </>
  )
}

export default DecoderPage

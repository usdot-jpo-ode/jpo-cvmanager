import React, { useEffect, useState } from 'react'
import { Box, Container, Grid, Typography } from '@mui/material'
import DecoderApi from '../../apis/intersections/decoder-api'
import { DecoderTables } from '../../features/intersections/decoder/decoder-tables'
import { v4 as uuidv4 } from 'uuid'
import MapTab, { getTimestamp } from '../../features/intersections/map/map-component'
import { selectToken } from '../../generalSlices/userSlice'
import { useDispatch, useSelector } from 'react-redux'
import { centerMapOnPoint } from '../../features/intersections/map/map-slice'
import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import { RootState } from '../../store'

const DecoderPage = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const token = useSelector(selectToken)

  const [data, setData] = useState({} as { [id: string]: DecoderDataEntry })
  const [selectedMapMessage, setSelectedMapMessage] = useState(
    undefined as undefined | { id: string; intersectionId: number; rsuIp: string }
  )
  const [selectedBsms, setSelectedBsms] = useState([] as string[])
  const [currentBsms, setCurrentBsms] = useState([] as OdeBsmData[])

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
      token,
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
              status: text == '' ? 'NOT_STARTED' : response?.decodeErrors !== '' ? 'ERROR' : 'COMPLETED',
            },
          }
        })
      })
      prevData = {
        ...prevData,
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
      let newEntry = {}
      if (
        prevData[id].text != undefined &&
        Object.values(prevData).find((v) => v.type == type && v.text == '') == undefined
      ) {
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
        ...newEntry,
        ...prevData,
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
      // TODO: Batch requests to setData
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

  const getIntersectionId = (decodedResponse: DecoderApiResponseGeneric | undefined): number | undefined => {
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
      default:
        return undefined
    }
  }

  const isGreyedOut = (intersectionId: number | undefined) => {
    return selectedMapMessage?.intersectionId === undefined || intersectionId !== selectedMapMessage?.intersectionId
  }

  const isGreyedOutIp = (rsuIp: string | undefined) => {
    return (selectedMapMessage?.rsuIp === undefined || rsuIp !== selectedMapMessage?.rsuIp) && rsuIp != ''
  }

  useEffect(() => {
    const newBsmData = Object.values(data)
      .filter((v) => v.type === 'BSM' && v.status === 'COMPLETED' && selectedBsms.includes(v.id))
      .map((v) => v.decodedResponse?.bsm)
    setCurrentBsms(newBsmData.filter((v) => v !== undefined) as OdeBsmData[])
  }, [data, selectedBsms])

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
            <Grid container justifyContent="space-between" spacing={3}>
              <Grid item>
                <Typography sx={{ m: 1 }} variant="h4" color="text.secondary">
                  ASN.1 Decoder
                </Typography>
              </Grid>
            </Grid>
          </Box>
          <Box
            sx={{
              alignItems: 'center',
              display: 'flex',
              overflow: 'hidden',
              height: '80vh',
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
                bsm: currentBsms,
              }}
              sourceDataType={'exact'}
              timeFilterBsms={false}
              intersectionId={-1}
              roadRegulatorId={-1}
            />
          </Box>
          <Grid container justifyContent="space-between" spacing={3}>
            <Grid item>
              <Typography sx={{ m: 1 }} variant="h6" color="white">
                1. Upload data, either by uploading individual files or pasting the data directly into the text box.
                <br />
                2. Select an uploaded MAP message to view the decoded data. SPAT data is filtered by intersection ID.
                <br />
                3. Select BSM messages to view the decoded data. All selected BSM data is shown.
              </Typography>
            </Grid>
          </Grid>
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
            centerMapOnLocation={(lat: number, long: number) => {
              dispatch(centerMapOnPoint({ latitude: lat, longitude: long }))
            }}
          />
        </Container>
      </Box>
    </>
  )
}

export default DecoderPage

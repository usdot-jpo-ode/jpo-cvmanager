import React from 'react'
import PerfectScrollbar from 'react-perfect-scrollbar'
import {
  Box,
  Card,
  Checkbox,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material'
import { DecoderEntry } from './decoder-entry'
import DownloadIcon from '@mui/icons-material/Download'

type DecoderTableProps = {
  contents: DecoderDataEntry[]
  selectedIntersectionId: number | undefined
  selectedMapMessageId: string | undefined
  selectedRsuIp: string | undefined
  selectedBsms: string[]
  setSelectedBsms: (bsms: string[]) => void
  onItemSelected: (id: string) => void
  onTextChanged: (id: string, messageText: string, type: DECODER_MESSAGE_TYPE) => void
  onItemDeleted: (id: string) => void
  onFileUploaded: (contents: string[], type: DECODER_MESSAGE_TYPE) => void
  centerMapOnLocation: (lat: number, long: number) => void
}

export const DecoderTables = (props: DecoderTableProps) => {
  const {
    contents,
    selectedIntersectionId,
    selectedMapMessageId,
    selectedRsuIp,
    selectedBsms,
    setSelectedBsms,
    onItemSelected,
    onTextChanged,
    onItemDeleted,
  } = props

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
      default:
        return undefined
    }
  }

  const isGreyedOut = (intersectionId: number | undefined) => {
    return selectedIntersectionId === undefined || intersectionId !== selectedIntersectionId
  }

  const dataFileUploaded = (event, type: DECODER_MESSAGE_TYPE) => {
    const file = event.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = function (evt) {
        try {
          // Split the file content by new lines and remove empty strings
          const contents = evt.target?.result as string
          const lines = contents.split('\n').filter((line) => line.trim() !== '')
          props.onFileUploaded(lines, type)
          // Now lines is an array of strings from the file
        } catch (e) {
          console.error('Error reading uploaded decoder file', e)
        }
      }
      reader.readAsText(file)
    }
  }

  const handleDownloadClick = (type: DECODER_MESSAGE_TYPE) => {
    let files = contents.filter((v) => v.type === type && v.decodedResponse != undefined).map((v) => v.decodedResponse)
    if (files.length > 0) {
      downloadJsonFile(files, `${type}_decoded_${new Date().toISOString()}.json`)
    }
  }

  const downloadJsonFile = (contents: any, name: string, alreadyStringified = false) => {
    const element = document.createElement('a')
    const file = new Blob([alreadyStringified ? contents : JSON.stringify(contents)], {
      type: 'text/plain',
    })
    element.href = URL.createObjectURL(file)
    element.download = name
    document.body.appendChild(element) // Required for this to work in FireFox
    element.click()
  }

  const toggleBsmSelection = () => {
    if (selectedBsms.length == contents.filter((v) => v.type === 'BSM').length) {
      setSelectedBsms([])
    } else {
      setSelectedBsms(contents.filter((v) => v.type === 'BSM').map((v) => v.id))
    }
  }

  return (
    <Card>
      <PerfectScrollbar>
        <Box display="flex" justifyContent="space-between" sx={{ minWidth: 1050 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell style={{ position: 'relative', height: '60px' }}>
                    MAP Messages
                    <input
                      type="file"
                      style={{ position: 'absolute', right: 10, top: 18, width: 200 }}
                      onChange={(event) => dataFileUploaded(event, 'MAP')}
                      title="Your custom description here"
                    />
                    <IconButton
                      aria-label="download"
                      onClick={() => handleDownloadClick('MAP')}
                      style={{ position: 'absolute', right: 0, top: 5 }}
                    >
                      <DownloadIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {contents
                  .filter((v) => v.type === 'MAP')
                  .map((entry) => {
                    return (
                      <DecoderEntry
                        id={entry.id}
                        status={entry.status}
                        type={entry.type}
                        text={entry.text}
                        isGreyedOut={entry.id !== selectedMapMessageId}
                        decodedResponse={entry.decodedResponse}
                        selected={entry.id == selectedMapMessageId}
                        timestamp={entry.timestamp}
                        onSelected={onItemSelected}
                        onTextChanged={(id, text) => onTextChanged(id, text, 'MAP')}
                        onDeleted={onItemDeleted}
                        centerMapOnLocation={props.centerMapOnLocation}
                      />
                    )
                  })}
              </TableBody>
            </Table>
          </TableContainer>

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell style={{ position: 'relative', height: '60px' }}>
                    SPAT Messages
                    <input
                      type="file"
                      style={{ position: 'absolute', right: 10, top: 18, width: 200 }}
                      onChange={(event) => dataFileUploaded(event, 'SPAT')}
                      title="Your custom description here"
                    />
                    <IconButton
                      aria-label="download"
                      onClick={() => handleDownloadClick('SPAT')}
                      style={{ position: 'absolute', right: 0, top: 5 }}
                    >
                      <DownloadIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {contents
                  .filter((v) => v.type === 'SPAT')
                  .map((entry) => {
                    return (
                      <DecoderEntry
                        id={entry.id}
                        status={entry.status}
                        type={entry.type}
                        text={entry.text}
                        isGreyedOut={isGreyedOut(getIntersectionId(entry.decodedResponse))}
                        decodedResponse={entry.decodedResponse}
                        selected={false}
                        timestamp={entry.timestamp}
                        onSelected={onItemSelected}
                        onTextChanged={(id, text) => onTextChanged(id, text, 'SPAT')}
                        onDeleted={onItemDeleted}
                        centerMapOnLocation={props.centerMapOnLocation}
                      />
                    )
                  })}
              </TableBody>
            </Table>
          </TableContainer>

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell style={{ position: 'relative', height: '60px' }}>
                    <Checkbox
                      checked={selectedBsms.length == contents.filter((v) => v.type === 'BSM').length}
                      onChange={toggleBsmSelection}
                      sx={{ m: 0, p: 0 }}
                    />
                    BSM Messages
                    <input
                      type="file"
                      style={{ position: 'absolute', right: 10, top: 18, width: 200 }}
                      onChange={(event) => dataFileUploaded(event, 'BSM')}
                      title="Your custom description here"
                    />
                    <IconButton
                      aria-label="download"
                      onClick={() => handleDownloadClick('BSM')}
                      style={{ position: 'absolute', right: 0, top: 5 }}
                    >
                      <DownloadIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {contents
                  .filter((v) => v.type === 'BSM')
                  .map((entry) => {
                    return (
                      <DecoderEntry
                        id={entry.id}
                        status={entry.status}
                        type={entry.type}
                        text={entry.text}
                        isGreyedOut={!selectedBsms.includes(entry.id)}
                        decodedResponse={entry.decodedResponse}
                        selected={selectedBsms.includes(entry.id)}
                        timestamp={entry.timestamp}
                        onSelected={onItemSelected}
                        onTextChanged={(id, text) => onTextChanged(id, text, 'BSM')}
                        onDeleted={onItemDeleted}
                        centerMapOnLocation={props.centerMapOnLocation}
                      />
                    )
                  })}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </PerfectScrollbar>
    </Card>
  )
}

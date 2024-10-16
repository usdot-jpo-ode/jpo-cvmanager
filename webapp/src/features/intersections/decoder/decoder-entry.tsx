import React from 'react'
import { format } from 'date-fns'
import { Box, Checkbox, IconButton, TableCell, TableRow, TextField, Typography } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import DownloadIcon from '@mui/icons-material/Download'
import CircularProgress from '@mui/material/CircularProgress'

type DecoderEntryProps = {
  onSelected: (id: string) => void
  onTextChanged: (id: string, messageText: string) => void
  onDeleted: (id: string) => void
}

export const DecoderEntry = (props: DecoderDataEntry & DecoderEntryProps) => {
  const {
    id,
    status,
    selected,
    text,
    type,
    isGreyedOut,
    decodedResponse,
    timestamp,
    onSelected,
    onTextChanged,
    onDeleted,
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
      case 'BSM':
        const bsmPayload = decodedResponse.bsm
        return bsmPayload?.metadata.originIp
    }
  }

  const handleCheckboxChange = () => {
    onSelected(id)
  }

  const handleTextChange = (event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    onTextChanged(id, event.target.value)
  }

  const handleDeleteClick = () => {
    onDeleted(id)
  }

  const handleDownloadClick = () => {
    let contents = '{}'
    switch (type) {
      case 'MAP':
        contents = JSON.stringify(decodedResponse?.processedMap ?? {})
        break
      case 'SPAT':
        contents = JSON.stringify(decodedResponse?.processedSpat ?? {})
        break
      case 'BSM':
        contents = JSON.stringify(decodedResponse?.bsm ?? {})
        break
    }
    if (contents !== '{}') {
      const key = getIntersectionId(decodedResponse)
      downloadJsonFile(contents, `${type}_${key}_decoded_${new Date().toISOString()}.json`)
    }
  }

  const downloadJsonFile = (contents: any, name: string) => {
    const element = document.createElement('a')
    const file = new Blob([JSON.stringify(contents)], {
      type: 'text/plain',
    })
    element.href = URL.createObjectURL(file)
    element.download = name
    document.body.appendChild(element) // Required for this to work in FireFox
    element.click()
  }

  const getCellColor = () => {
    switch (status) {
      case 'NOT_STARTED':
        return 'white'
      case 'IN_PROGRESS':
        return 'yellow'
      case 'COMPLETED':
        if (isGreyedOut) {
          return '#51634e'
        } else {
          return '#448b3b'
        }
      case 'ERROR':
        return 'red'
      default:
        return 'white'
    }
  }

  const getIconColor = () => {
    switch (status) {
      case 'NOT_STARTED':
        return '#555555'
      default:
        return '#ffffff'
    }
  }

  return (
    <TableRow>
      <TableCell style={{ backgroundColor: getCellColor(), border: '1px solid white' }}>
        <div style={{ display: 'flex' }}>
          {(type == 'MAP' || type == 'BSM') && text != '' && (
            <Checkbox
              checked={selected}
              onChange={handleCheckboxChange}
              sx={{ m: 0, p: 0, mr: 1, ml: 1 }}
              style={{ color: getIconColor() }}
            />
          )}
          {timestamp && (
            <Typography variant="subtitle1" color="white">
              {format(new Date(timestamp), 'yyyy-MM-dd HH:mm:ss')}
            </Typography>
          )}
        </div>
        <br></br>
        <TextField value={text} placeholder="Paste data here" onChange={handleTextChange} sx={{ width: 160 }} />
        <IconButton aria-label="delete" onClick={handleDeleteClick} style={{ color: getIconColor() }}>
          <DeleteIcon />
        </IconButton>
        <IconButton aria-label="download" onClick={handleDownloadClick} style={{ color: getIconColor() }}>
          <DownloadIcon />
        </IconButton>
        {status === 'IN_PROGRESS' && <CircularProgress />}
        <Box>
          <TextField
            value={'Errors: ' + decodedResponse?.decodeErrors == '' ? 'None' : decodedResponse?.decodeErrors}
            InputProps={{ readOnly: true }}
            fullWidth
          />
        </Box>
      </TableCell>
    </TableRow>
  )
}

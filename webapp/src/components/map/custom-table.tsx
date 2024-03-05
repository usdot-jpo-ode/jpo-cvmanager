import React from 'react'
import {
  Paper,
  Box,
  IconButton,
  Typography,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableBody,
  TableCell,
} from '@mui/material'

export const CustomTable = (props) => {
  const { headers, data }: { headers: string[]; data: string[][] } = props
  let rowKey = 0
  let cellKey = 0

  return (
    <TableContainer component={Paper} sx={{ pt: 0, pb: 0, px: 4 }}>
      <Table stickyHeader size="small">
        <TableHead>
          <TableRow>
            {headers.map((head) => (
              <TableCell key={head}>{head}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {data.map((row) => (
            <TableRow hover key={++rowKey}>
              {row.map((cell) => (
                <TableCell component="th" scope="row" key={++cellKey}>
                  <>{cell}</>
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

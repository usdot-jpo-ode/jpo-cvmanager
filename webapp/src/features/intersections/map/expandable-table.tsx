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
  Collapse,
  useTheme,
} from '@mui/material'
import React, { useState } from 'react'
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp'

export const ExpandableTable = (props) => {
  const { headers, data }: { headers: string[]; data: string[][]; details: string[] } = props
  const [expandedRows, setExpandedRows] = useState<number[]>([])

  const theme = useTheme()

  const handleExpandOne = (rowKey: number) => {
    if (!expandedRows.includes(rowKey)) {
      setExpandedRows((prevExpanded: number[]) => [...prevExpanded, rowKey])
    } else {
      setExpandedRows((prevExpanded: number[]) => prevExpanded.filter((key: number) => key !== rowKey))
    }
  }

  return (
    <TableContainer component={Paper} elevation={0} sx={{ pt: 0, pb: 0, px: 4 }}>
      <Table
        stickyHeader
        size="small"
        sx={{
          '& .MuiTableRow-head, .MuiTableCell-head': {
            backgroundColor: theme.palette.background.paper,
          },
        }}
      >
        <TableHead>
          <TableRow>
            {headers.map((head) => (
              <TableCell
                className="capital-case"
                sx={{ fontSize: '16px !important', textTransform: 'capitalize !important' }}
                key={head}
              >
                {head}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {data.map((row, rowKey) => {
            const isRowExpanded = [...expandedRows].indexOf(rowKey) !== -1
            return (
              <>
                <TableRow hover key={rowKey}>
                  <TableCell padding="checkbox">
                    <IconButton aria-label="expand row" size="small" onClick={() => handleExpandOne(rowKey)}>
                      {isRowExpanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                    </IconButton>
                  </TableCell>
                  {row.map((cell, cellKey) => (
                    <TableCell component="th" scope="row" key={++cellKey}>
                      <>{cell}</>
                    </TableCell>
                  ))}
                </TableRow>
                <TableRow>
                  <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
                    <Collapse in={isRowExpanded} timeout="auto" unmountOnExit>
                      <Box sx={{ margin: 1 }}>
                        <Typography color="textPrimary" variant="body1">
                          {props.details[rowKey]}
                        </Typography>
                      </Box>
                    </Collapse>
                  </TableCell>
                </TableRow>
              </>
            )
          })}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

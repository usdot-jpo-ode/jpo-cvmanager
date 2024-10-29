// import { SvgIconComponent } from '@mui/icons-material'
// import { IconProps } from '@mui/material'

// import {
//   Box,
//   Button,
//   Card,
//   Container,
//   Divider,
//   Grid2,
//   InputAdornment,
//   Stack,
//   Tab,
//   Tabs,
//   TextField,
//   TextFieldProps,
//   Typography,
//   CardHeader,
// } from '@mui/material'
// import React, { useEffect, useState, useRef } from 'react'

// export interface Action<RowData extends object> {
//   disabled?: boolean
//   icon: string | (() => React.ReactNode) | SvgIconComponent
//   tooltip?: string
//   onClick: (event: any, data: RowData | RowData[]) => void
//   iconProps?: IconProps
// }

// export interface GenericTableProps {
//   actions: Action<any>[]
//   headers: string[]
//   data: any[]
//   title: string
//   searchableFields?: string[]
// }

// const applyFilters = (
//   parameters: {}[],
//   filter: { query: string; tab: string },
//   searchableFields?: string[],
//   tabField?: string
// ) =>
//   parameters.filter((parameter) => {
//     if (filter.query) {
//       let queryMatched = false
//       if (searchableFields) {
//         searchableFields.forEach((property) => {
//           if (parameter[property].toLowerCase().includes(filter.query.toLowerCase())) {
//             queryMatched = true
//           }
//         })
//       } else {
//         // search all fields
//         for (const property in parameter) {
//           if (parameter[property].toLowerCase().includes(filter.query.toLowerCase())) {
//             queryMatched = true
//           }
//         }
//       }

//       if (!queryMatched) {
//         return false
//       }
//     }
//     if (tabField) {
//       return parameter[tabField] == filter.tab
//     } else {
//       return true
//     }
//   })

// const applyPagination = (parameters, page, rowsPerPage) =>
//   parameters.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

// export const NotificationsTable = (props: GenericTableProps) => {
//   const { actions, headers, data, title, searchableFields } = props
//   const queryRef = useRef<TextFieldProps>(null)
//   const [currentTab, setCurrentTab] = useState('all')
//   const [page, setPage] = useState(0)
//   const [rowsPerPage, setRowsPerPage] = useState(10)
//   const [filter, setFilter] = useState({
//     query: '',
//     tab: currentTab,
//   })

//   useEffect(() => {
//     updateDescription()
//   }, [currentTab])

//   const handleTabsChange = (event, value) => {
//     const updatedFilter = { ...filter, tab: value }
//     setCurrentTab(value)
//     setFilter(updatedFilter)
//     setPage(0)
//     setCurrentTab(value)
//   }

//   const handleQueryChange = (event) => {
//     event.preventDefault()
//     setFilter((prevState) => ({
//       ...prevState,
//       query: queryRef.current?.value as string,
//     }))
//   }

//   const handlePageChange = (event, newPage) => {
//     setPage(newPage)
//   }

//   const handleRowsPerPageChange = (event) => {
//     setRowsPerPage(parseInt(event.target.value, 10))
//   }

//   const updateDescription = () => {
//     for (let i = 0; i < tabs.length; i++) {
//       if (tabs[i].value === currentTab) {
//         setCurrentDescription(tabs[i].description)
//       }
//     }
//   }

//   // Usually query is done on backend with indexing solutions
//   const filteredNotifications = applyFilters(notifications, filter)
//   const paginatedNotifications = applyPagination(filteredNotifications, page, rowsPerPage)

//   return (
//     <NotificationsTableResults
//       customers={paginatedNotifications}
//       allTabNotifications={notifications}
//       notificationsCount={filteredNotifications.length}
//       selectedNotifications={acceptedNotifications}
//       onSelectedItemsChanged={setAcceptedNotifications}
//       expandedNotifications={expandedNotifications}
//       onExpandedItemsChanged={setExpandedNotifications}
//       onPageChange={handlePageChange}
//       onRowsPerPageChange={handleRowsPerPageChange}
//       rowsPerPage={rowsPerPage}
//       page={page}
//     />
//   )
// }

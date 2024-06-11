import { useState } from "react";
import PerfectScrollbar from "react-perfect-scrollbar";
import PropTypes from "prop-types";
import { format } from "date-fns";
import NextLink from "next/link";
import {
  Avatar,
  Box,
  Card,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
  IconButton,
  CardHeader,
} from "@mui/material";
import { getInitials } from "../../utils/get-initials";
import React from "react";
import { v4 as uuid } from "uuid";
import { PencilAlt as PencilAltIcon } from "../../icons/pencil-alt";
import MapRoundedIcon from "@mui/icons-material/MapRounded";
import { ConstructionOutlined } from "@mui/icons-material";

export const AssessmentListResults = ({
  customers,
  allTabNotifications,
  notificationsCount,
  selectedNotifications,
  onSelectedItemsChanged,
  onPageChange,
  onRowsPerPageChange,
  page,
  rowsPerPage,
}) => {
  const handleSelectAll = (event) => {
    let newSelectedCustomerIds: uuid[] = [];

    if (event.target.checked) {
      newSelectedCustomerIds = allTabNotifications.map((customer) => customer.id);
    } else {
      newSelectedCustomerIds = [];
    }

    onSelectedItemsChanged(newSelectedCustomerIds);
  };

  const handleSelectOne = (event, notificationId) => {
    if (!selectedNotifications.includes(notificationId)) {
      onSelectedItemsChanged((prevSelected) => [...prevSelected, notificationId]);
    } else {
      onSelectedItemsChanged((prevSelected) => prevSelected.filter((id) => id !== notificationId));
    }
  };

  return (
    <Card>
      <PerfectScrollbar>
        <Box sx={{ minWidth: 1050 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={selectedNotifications.length === notificationsCount}
                    color="primary"
                    indeterminate={
                      selectedNotifications.length > 0 && selectedNotifications.length < notificationsCount
                    }
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell>Notification Type</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Message</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {customers.map((customer) => {
                const isNotificationSelected = [...selectedNotifications].indexOf(customer.id) !== -1;

                return (
                  <TableRow
                    hover
                    key={customer.id}
                    selected={[...selectedNotifications].indexOf(customer.message) !== -1}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={isNotificationSelected}
                        onChange={(event) => handleSelectOne(event, customer.id)}
                        value="true"
                      />
                    </TableCell>
                    <TableCell>
                      <Box
                        sx={{
                          alignItems: "center",
                          display: "flex",
                        }}
                      >
                        <Typography color="textPrimary" variant="body1">
                          {customer.notificationType}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{format(customer.notificationGeneratedAt, "MM/dd/yyyy")}</TableCell>
                    <TableCell>{customer.notificationText}</TableCell>
                    <TableCell align="right">
                      <NextLink href={`/map/notification/${customer.id}`} passHref>
                        <IconButton component="a">
                          <MapRoundedIcon fontSize="medium" />
                        </IconButton>
                      </NextLink>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Box>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={notificationsCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  );
};

AssessmentListResults.propTypes = {
  customers: PropTypes.array.isRequired,
  onSelectedItemsChanged: PropTypes.func,
};

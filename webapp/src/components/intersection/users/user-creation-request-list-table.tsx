import PerfectScrollbar from "react-perfect-scrollbar";
import PropTypes from "prop-types";
import NextLink from "next/link";
import {
  Box,
  Card,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
} from "@mui/material";
import React from "react";
import AddIcon from "@mui/icons-material/Add";

export const UserCreationRequestListTable = (props) => {
  const { users, parametersCount, onPageChange, onRowsPerPageChange, page, rowsPerPage } = props;

  const generalDefaultRow = (user: User) => {
    return (
      <TableRow hover key={user.id}>
        <TableCell>{user.email}</TableCell>
        <TableCell>{user.first_name}</TableCell>
        <TableCell>{user.last_name}</TableCell>
        <TableCell>{user.role}</TableCell>
        <TableCell align="right">
          <NextLink href={`/users/create/${user.email}/${user.role}/${user.first_name}/${user.last_name}`} passHref>
            <IconButton component="a">
              <AddIcon fontSize="small" />
            </IconButton>
          </NextLink>
        </TableCell>
      </TableRow>
    );
  };

  return (
    <Card>
      <PerfectScrollbar>
        <Box sx={{ minWidth: 1050 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Email</TableCell>
                <TableCell>First Name</TableCell>
                <TableCell>Last Name</TableCell>
                <TableCell>Role</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(users as User[]).map((user) => {
                return generalDefaultRow(user);
              })}
            </TableBody>
          </Table>
        </Box>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={parametersCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  );
};

UserCreationRequestListTable.propTypes = {
  users: PropTypes.array.isRequired,
  parametersCount: PropTypes.number.isRequired,
  onPageChange: PropTypes.func.isRequired,
  onRowsPerPageChange: PropTypes.func,
  page: PropTypes.number.isRequired,
  rowsPerPage: PropTypes.number.isRequired,
};

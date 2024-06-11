import PerfectScrollbar from "react-perfect-scrollbar";
import PropTypes from "prop-types";
import { format } from "date-fns";
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
  Typography,
} from "@mui/material";
import React from "react";
import MapRoundedIcon from "@mui/icons-material/MapRounded";

export const AssessmentListResults = ({
  assessments,
  assessmentsCount,
  onPageChange,
  onRowsPerPageChange,
  page,
  rowsPerPage,
}) => {
  const getAssessmentDescription = (assessment: Assessment) => {
    return JSON.stringify(assessment).replace(/,/g, ", ");
  };

  return (
    <Card>
      <PerfectScrollbar>
        <Box sx={{ minWidth: 1050, overflowX: "scroll" }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Assessment Type</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Open Map</TableCell>
                <TableCell>Message</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {assessments.map((assessment: Assessment, i: number) => {
                return (
                  <TableRow hover key={i}>
                    <TableCell>
                      <Box
                        sx={{
                          alignItems: "center",
                          display: "flex",
                        }}
                      >
                        <Typography color="textPrimary" variant="body1">
                          {assessment.assessmentType}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{format(assessment.assessmentGeneratedAt, "MM/dd/yyyy HH:mm:ss")}</TableCell>
                    <TableCell align="right">
                      <NextLink href={`/map/${assessment.intersectionID}/${assessment.assessmentGeneratedAt}`} passHref>
                        <IconButton component="a">
                          <MapRoundedIcon fontSize="medium" />
                        </IconButton>
                      </NextLink>
                    </TableCell>
                    <TableCell>{getAssessmentDescription(assessment)}</TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Box>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={assessmentsCount}
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
  assessments: PropTypes.array.isRequired,
  onSelectedItemsChanged: PropTypes.func,
};

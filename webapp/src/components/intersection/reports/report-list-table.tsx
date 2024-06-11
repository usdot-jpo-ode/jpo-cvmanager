import { Fragment } from "react";
import NextLink from "next/link";
import { format } from "date-fns";
import PropTypes from "prop-types";
import {
  Avatar,
  Box,
  Button,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TablePagination,
  TableRow,
  Typography,
} from "@mui/material";
import PerfectScrollbar from "react-perfect-scrollbar";
import { ArrowDownward } from "@mui/icons-material";
import { useSession } from "next-auth/react";
import toast from "react-hot-toast";
import ReportsApi, { ReportMetadata } from "../../apis/reports-api";

interface ReportRowProps {
  report: ReportMetadata;
}

const ReportRow = (props: ReportRowProps) => {
  const { data: session } = useSession();
  const { report } = props;

  const downloadReport = async (reportName: string) => {
    if (!session?.accessToken) {
      console.error("Did not attempt to download report. Access token:", session?.accessToken);
      return;
    }
    const promise = ReportsApi.downloadReport({ token: session?.accessToken, reportName });
    toast.promise(promise, {
      loading: `Downloading Performance Report ${reportName}`,
      success: `Successfully Downloaded Performance Report ${reportName}`,
      error: `Error Downloading Performance Report ${reportName}`,
    });
    const report = await promise;
    const name = `Performance Report ${reportName}.pdf`;
    if (report !== undefined) {
      downloadPdf(report, name);
    }
  };

  const downloadPdf = (contents: Blob, name: string) => {
    const url = window.URL.createObjectURL(contents);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", name); //or any other extension
    document.body.appendChild(link);
    link.click();
  };

  return (
    <TableRow
      key={report.reportName}
      sx={{
        boxShadow: 1,
        transition: (theme) =>
          theme.transitions.create("box-shadow", {
            easing: theme.transitions.easing.easeOut,
          }),
        "&:hover": {
          boxShadow: 8,
        },
        "& > td": {
          backgroundColor: "background.paper",
          borderBottom: 0,
        },
      }}
    >
      <TableCell width="25%">
        <NextLink href="/dashboard/logs/1" passHref>
          <Box
            component="a"
            sx={{
              alignItems: "center",
              display: "inline-flex",
              textDecoration: "none",
              whiteSpace: "nowrap",
            }}
          >
            <Box sx={{ ml: 2 }}>
              <Typography color="textSecondary" variant="subtitle2">
                {report.reportName}
              </Typography>
            </Box>
          </Box>
        </NextLink>
      </TableCell>
      <TableCell>
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Typography variant="subtitle2">Report Duration</Typography>
          <Typography color="textSecondary" variant="body2">
            {report.reportStartTime && format(new Date(report.reportStartTime), "MMM dd, h:mm:ss a")} -{" "}
            {report.reportStopTime && format(new Date(report.reportStopTime), "MMM dd, h:mm:ss a")}
          </Typography>
        </Box>
      </TableCell>
      <TableCell>
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Typography variant="subtitle2">Generated At</Typography>
          <Typography color="textSecondary" variant="body2">
            {report.reportGeneratedAt && format(new Date(report.reportGeneratedAt), "MMM dd, h:mm:ss a")}
          </Typography>
        </Box>
      </TableCell>
      <TableCell align="right">
        <Button
          endIcon={<ArrowDownward fontSize="small" />}
          onClick={() => {
            downloadReport(report.reportName);
          }}
        >
          Download
        </Button>
      </TableCell>
    </TableRow>
  );
};

interface ReportListTableProps {
  group: boolean;
  reports: ReportMetadata[];
  reportsCount: number;
  onPageChange: (event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => void;
  onRowsPerPageChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  page: number;
  rowsPerPage: number;
}

export const ReportListTable = (props: ReportListTableProps) => {
  const { group, reports, reportsCount, onPageChange, onRowsPerPageChange, page, rowsPerPage, ...other } = props;

  return (
    <div {...other}>
      <PerfectScrollbar>
        <Table
          sx={{
            borderCollapse: "separate",
            borderSpacing: (theme) => `0 ${theme.spacing(3)}`,
            minWidth: 600,
            marginTop: (theme) => `-${theme.spacing(3)}`,
            p: "1px",
          }}
        >
          {
            <TableBody>
              {reports.map((report: ReportMetadata) => (
                <ReportRow report={report} />
              ))}
            </TableBody>
          }
        </Table>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={reportsCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </div>
  );
};

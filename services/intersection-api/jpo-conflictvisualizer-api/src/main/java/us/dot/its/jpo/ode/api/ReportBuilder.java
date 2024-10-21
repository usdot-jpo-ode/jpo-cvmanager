package us.dot.its.jpo.ode.api;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.AxesChartStyler.TextAlignment;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;
import com.itextpdf.text.BaseColor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessmentGroup;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.models.ChartData;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;

public class ReportBuilder {

    private Document document;
    private PdfWriter writer;
    DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter secondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:SS");

    // Creates a new PDF report builder to add components to.
    public ReportBuilder(OutputStream stream) {
        document = new Document();
        try {
            writer = PdfWriter.getInstance(document, stream);

            document.open();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    // Writes PDF to File System if Output Stream, allows getting PDF as ByteStream
    // for ByteOutputStreams
    public void write() {
        document.close();

    }

    public void addTitlePage(String reportTitle, long startTime, long endTime) {

        String startTimeString = Instant.ofEpochMilli(startTime).toString();
        String endTimeString = Instant.ofEpochMilli(endTime).toString();

        try {
            Font f = new Font(FontFamily.TIMES_ROMAN, 42.0f, Font.BOLD, BaseColor.BLACK);
            Paragraph p = new Paragraph(reportTitle, f);
            p.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(p);

            Paragraph dates = new Paragraph(startTimeString + "     -     " + endTimeString);
            dates.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(dates);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addTitle(String title) {
        try {
            document.newPage();
            Font f = new Font(FontFamily.TIMES_ROMAN, 36.0f, Font.BOLD, BaseColor.BLACK);
            Paragraph p = new Paragraph(title, f);
            document.add(p);

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addPageBreak() {
        document.newPage();
    }

    public void addMapBroadcastRate(List<IDCount> data) {

        List<Date> times = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        int timeDeltaSeconds = 3600; // seconds per hour

        Date lastDate = null;
        for (IDCount elem : data) {
            try {
                Date newDate = sdf.parse(elem.getId());

                if (lastDate != null) {

                    // If the difference between records is greater than 1 hour. Insert fake records to zero the delta
                    if (newDate.toInstant().toEpochMilli() - lastDate.toInstant().toEpochMilli() > timeDeltaSeconds * 1000 ) {
                        times.add(Date.from(lastDate.toInstant().plusSeconds(timeDeltaSeconds)));
                        values.add(0.0);
                        times.add(Date.from(newDate.toInstant().minusSeconds(timeDeltaSeconds)));
                        values.add(0.0);
                    }
                }

                times.add(newDate);
                values.add((double) elem.getCount());

                lastDate = newDate;

            } catch (ParseException e) {

            }
        }

        int width = (int) (document.getPageSize().getWidth() * 0.9);

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(400).title("MAP Broadcast Rate").xAxisTitle("Date")
                .yAxisTitle("Average Broadcast Rate (msg/sec)").build();

        if(times.size() > 0 && values.size() > 0){
            XYSeries series = chart.addSeries("MAP Broadcast Rate", times, values);
            series.setSmooth(true);
            series.setMarker(SeriesMarkers.NONE);

            ArrayList<Date> markerDates = new ArrayList<>();
            markerDates.add(times.get(0));
            markerDates.add(times.get(times.size() - 1));

            ArrayList<Double> bottomMarkerValues = new ArrayList<>();
            bottomMarkerValues.add(9.0);
            bottomMarkerValues.add(9.0);

            ArrayList<Double> topMarkerValues = new ArrayList<>();
            topMarkerValues.add(11.0);
            topMarkerValues.add(11.0);

            addMarkerLine(chart, markerDates, bottomMarkerValues);
            addMarkerLine(chart, markerDates, topMarkerValues);
        }
        

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

        try {
            document.add(Image.getInstance(chartImage, null));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

    }

    public void addSpatBroadcastRate(List<IDCount> data) {

        List<Date> times = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        int timeDeltaSeconds = 3600; // seconds per hour

        Date lastDate = null;
        for (IDCount elem : data) {
            try {
                Date newDate = sdf.parse(elem.getId());
                if (lastDate != null) {
                    if (newDate.toInstant().toEpochMilli() - lastDate.toInstant().toEpochMilli() > timeDeltaSeconds * 1000) {
                        times.add(Date.from(lastDate.toInstant().plusSeconds(timeDeltaSeconds)));
                        values.add(0.0);
                        times.add(Date.from(newDate.toInstant().minusSeconds(timeDeltaSeconds)));
                        values.add(0.0);
                    }
                }
                times.add(newDate);
                values.add((double) elem.getCount());
                lastDate = newDate;
            } catch (ParseException e) {

            }
        }

        int width = (int) (document.getPageSize().getWidth() * 0.9);

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(400).title("SPaT Broadcast Rate").xAxisTitle("Date")
                .yAxisTitle("Broadcast Rate (msg/second)").build();

        if(times.size() > 0 && values.size() > 0){
            XYSeries series = chart.addSeries("SPaT Broadcast Rate", times, values);
            series.setSmooth(true);
            series.setMarker(SeriesMarkers.NONE);

            ArrayList<Date> markerDates = new ArrayList<>();
            markerDates.add(times.get(0));
            markerDates.add(times.get(times.size() - 1));

            ArrayList<Double> bottomMarkerValues = new ArrayList<>();
            bottomMarkerValues.add(9.0);
            bottomMarkerValues.add(9.0);

            ArrayList<Double> topMarkerValues = new ArrayList<>();
            topMarkerValues.add(11.0);
            topMarkerValues.add(11.0);

            addMarkerLine(chart, markerDates, bottomMarkerValues);
            addMarkerLine(chart, markerDates, topMarkerValues);
        }
        

        

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

        try {
            document.add(Image.getInstance(chartImage, null));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

    }

    public void addSpatBroadcastRateDistribution(List<IDCount> data, Long startTime, Long endTime){

        // Calculate how many time intervals had no messages recorded
        long totalIntervals = (endTime - startTime) / 10;
        long countedIntervals = 0;
        for(IDCount elem : data){
            countedIntervals += elem.getCount();
        }
        long zeroIntervals = totalIntervals - countedIntervals;

        // Add Intervals with no data to the chart
        if(data.size() > 0 && data.get(0).getId().equals("0")){
            data.get(0).setCount(data.get(0).getCount() + zeroIntervals);
        } else{
            IDCount count = new IDCount();
            count.setId("0");
            count.setCount(zeroIntervals);
            data.add(count); 
        }

        // Fill in Missing Intervals with Zeros and Rename ranges
        List<IDCount> output = new ArrayList<>();
        for(int i =0; i <200; i+=10){
            IDCount count = new IDCount();
            count.setId(i + " - " + (i+9));
            output.add(count);
        }

        IDCount count = new IDCount();
        count.setId("> 200");
        output.add(count);
        

        for(IDCount elem : data){
            int index = Integer.parseInt(elem.getId()) / 10;
            output.get(index).setCount(elem.getCount());
            System.out.println(elem);
        }

        // Convert to Chart Data and generate graph
        ChartData chartData = ChartData.fromIDCountList(output);
        try {
            document.add(getBarGraph(chartData, "SPaT Broadcast Rate Distribution", "Messages Per 10 Seconds", "Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addMapBroadcastRateDistribution(List<IDCount> data, Long startTime, Long endTime){

        // Calculate how many time intervals had no messages recorded
        long totalIntervals = (endTime - startTime) / 10;
        long countedIntervals = 0;
        for(IDCount elem : data){
            countedIntervals += elem.getCount();
        }
        long zeroIntervals = totalIntervals - countedIntervals;

        // Add Intervals with no data to the chart
        if(data.size() > 0 && data.get(0).getId().equals("0")){
            data.get(0).setCount(data.get(0).getCount() + zeroIntervals);
        } else{
            IDCount count = new IDCount();
            count.setId("0");
            count.setCount(zeroIntervals);
            data.add(count); 
        }

        // Fill in Missing Intervals with Zeros and Rename ranges
        List<IDCount> output = new ArrayList<>();
        for(int i =0; i <20; i++){
            IDCount count = new IDCount();
            count.setId(""+i);
            output.add(count);
        }

        IDCount count = new IDCount();
        count.setId("> 20");
        output.add(count);
        

        for(IDCount elem : data){
            int index = Integer.parseInt(elem.getId());
            output.get(index).setCount(elem.getCount());
            System.out.println(elem);
        }

        // Convert to Chart Data and generate graph
        ChartData chartData = ChartData.fromIDCountList(output);
        try {
            document.add(getBarGraph(chartData, "MAP Broadcast Rate Distribution", "Messages Per 10 Seconds", "Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addMapMinimumDataEventErrors(List<MapMinimumDataEvent> events){
        List<String> missingElements = new ArrayList<>();
        if(events.size()> 0){
            MapMinimumDataEvent event = events.get(0);
            missingElements = event.getMissingDataElements();
        }

        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Font rowFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);


        PdfPTable table = new PdfPTable(1);
        // table.addCell(new PdfPCell(new Paragraph("Map Missing data Elements")));
        table.addCell(new PdfPCell(new Paragraph("Latest MAP Missing Data Elements", boldFont)));

        for(String missingElement: missingElements){
            if(!missingElement.contains("metadata")){
                int splitIndex = missingElement.indexOf(": is missing");

                if (splitIndex >= 0) {
                    // table.addCell(missingElement.substring(2, splitIndex).trim());
                    table.addCell(new PdfPCell(new Paragraph(missingElement.substring(2, splitIndex).trim(), rowFont)));
                }

                
            }
            
        }

        try {
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addSpatMinimumDataEventErrors(List<SpatMinimumDataEvent> events){
        List<String> missingElements = new ArrayList<>();
        if(events.size()> 0){
            SpatMinimumDataEvent event = events.get(0);
            missingElements = event.getMissingDataElements();
        }

        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Font rowFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);


        PdfPTable table = new PdfPTable(1);
        // table.addCell(new PdfPCell(new Paragraph("Map Missing data Elements")));
        table.addCell(new PdfPCell(new Paragraph("SPaT Missing Data Elements", boldFont)));

        for(String missingElement: missingElements){
            if(!missingElement.contains("metadata")){
                int splitIndex = missingElement.indexOf(": is missing");

                if (splitIndex >= 0) {
                    // table.addCell(missingElement.substring(2, splitIndex).trim());
                    table.addCell(new PdfPCell(new Paragraph(missingElement.substring(2, splitIndex).trim(), rowFont)));
                }

                
            }
            
        }

        try {
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addMarkerLine(XYChart chart, ArrayList<Date> startEndDate, ArrayList<Double> startEndValue) {
        if(startEndDate.size() > 2 && startEndValue.size() > 2){
            XYSeries series = chart.addSeries("MAP Minimum Marker" + startEndValue.hashCode(), startEndDate, startEndValue);
            series.setSmooth(true);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineWidth(0.125f);
            series.setLineColor(Color.BLACK);
            series.setShowInLegend(false);
        }
        
    }

    public void addSignalStateEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "Stop Line Passage Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateStopEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "Stop Line Stop Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addLaneDirectionOfTravelEvent(ChartData data) {
        try {
            document.add(getBarGraph(data, "Lane Direction of Travel Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addConnectionOfTravelEvent(ChartData data) {
        try {
            document.add(getBarGraph(data, "Connection of Travel Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateConflictEvent(ChartData data) {
        try {
            document.add(getBarGraph(data, "Signal State Conflict Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSpatTimeChangeDetailsEvent(ChartData data) {
        try {
            document.add(getBarGraph(data, "Time Change Details Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addLaneDirectionOfTravelMedianDistanceDistribution(ChartData data) {
        try {
            document.add(getBarGraph(data, "Distance from Centerline Distribution (ft)", "Feet", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addLaneDirectionOfTravelMedianHeadingDistribution(ChartData data) {
        try {
            document.add(getBarGraph(data, "Heading Error Distribution (deg)", "Degrees", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addIntersectionReferenceAlignmentEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "Intersection Reference Alignment Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addMapMinimumDataEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "MAP Minimum Data Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addSpatMinimumDataEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "SPaT Minimum Data Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addMapBroadcastRateEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "MAP Broadcast Rate Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addSpatBroadcastRateEvents(ChartData data) {
        try {
            document.add(getBarGraph(data, "SPaT Broadcast Rate Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    

    public Image getLineGraph(ChartData data, String title, String xAxisLabel, String yAxislabel) {
        int width = (int) (document.getPageSize().getWidth() * 0.9);

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(400).title("Test Report").xAxisTitle("X")
                .yAxisTitle("Y").build();

        if(data.getLabels().size() > 0 && data.getValues().size() > 0){
            XYSeries series = chart.addSeries("Fake Data", data.getLabels(), data.getValues());
        }
        

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);
        

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

        try {
            return Image.getInstance(chartImage, null);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Image getBarGraph(ChartData data, String title, String xAxisLabel, String yAxislabel) {
        int width = (int) (document.getPageSize().getWidth() * 0.9);

        CategoryChart chart = new CategoryChartBuilder()
                .width(width)
                .height(300)
                .title(title)
                .xAxisTitle(xAxisLabel)
                .yAxisTitle(yAxislabel)
                .build();

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);

        chart.getStyler().setLabelsVisible(false);
        chart.getStyler().setPlotGridLinesVisible(false);
        chart.getStyler().setXAxisMaxLabelCount(31);
        chart.getStyler().setXAxisLabelAlignmentVertical(TextAlignment.Centre);
        chart.getStyler().setXAxisLabelRotation(90);
        
        if(data.getLabels().size() > 0 && data.getValues().size() > 0){
            CategorySeries series = chart.addSeries("series", data.getLabels(), data.getValues());
            series.setFillColor(Color.BLUE); 
        }
        else{
            double[] fakeLabels = {0};
            double[] fakeData = {0};

            CategorySeries series = chart.addSeries("series", fakeLabels, fakeData);
            series.setFillColor(Color.BLUE);
        }

        

        

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

        try {
            return Image.getInstance(chartImage, null);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void addHeadingOverTime(List<LaneDirectionOfTravelAssessment> assessments) {
        // int width = (int) (document.getPageSize().getWidth() * 0.9);
        // Map<String, ArrayList<Double>> distancesFromCenterline = new HashMap<>();
        // Map<String, ArrayList<Date>> timestamps = new HashMap<>();

        // for (LaneDirectionOfTravelAssessment assessment : assessments) {
        //     for (LaneDirectionOfTravelAssessmentGroup group : assessment.getLaneDirectionOfTravelAssessmentGroup()) {
        //         String hash = "Lane: " + group.getLaneID() + " Segment: " + group.getSegmentID();
        //         if (distancesFromCenterline.containsKey(hash)) {
        //             distancesFromCenterline.get(hash).add((double)Math.round(group.getMedianHeading() - group.getExpectedHeading()));
        //             timestamps.get(hash).add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
        //         } else {
        //             ArrayList<Double> distances = new ArrayList<>();
        //             distances.add((double)Math.round(group.getMedianHeading() - group.getExpectedHeading()));
        //             distancesFromCenterline.put(hash, distances);

        //             ArrayList<Date> times = new ArrayList<>();
        //             times.add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
        //             timestamps.put(hash, times);
        //         }
        //     }
        // }

        // XYChart chart = new XYChartBuilder().width(width).height(400).title("Vehicle Heading Error Delta")
        //         .xAxisTitle("Time")
        //         .yAxisTitle("Heading Delta (Degrees)").build();

        // if (assessments.size() > 0) {
        //     Date minDate = Date.from(Instant.ofEpochMilli(assessments.get(0).getTimestamp()));
        //     Date maxDate = Date.from(Instant.ofEpochMilli(assessments.get(assessments.size() - 1).getTimestamp()));
        //     for (String key : distancesFromCenterline.keySet()) {
        //         ArrayList<Double> distances = distancesFromCenterline.get(key);
        //         ArrayList<Date> times = timestamps.get(key);

        //         distances.add(0, distances.get(0));
        //         times.add(0, minDate);

        //         distances.add(distances.size(), distances.get(distances.size() - 1));
        //         times.add(maxDate);

        //         if(times.size() > 0 && distances.size() > 0){
        //             XYSeries series = chart.addSeries(key, times, distances);
        //             series.setSmooth(true);
        //             series.setMarker(SeriesMarkers.NONE);
        //         }
        //     }
        // }

        // chart.getStyler().setShowWithinAreaPoint(false);
        // chart.getStyler().setChartBackgroundColor(Color.WHITE);
        // chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        // chart.getStyler().setLegendVisible(true);
        // chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
        // chart.getStyler().setLegendLayout(LegendLayout.Vertical);
        // chart.getStyler().setLegendFont(new java.awt.Font("Times New Roman", java.awt.Font.PLAIN, 6));
        

        // chart.getStyler().setPlotGridLinesVisible(false);
        // chart.getStyler().setXAxisMaxLabelCount(31);
        // chart.getStyler().setXAxisLabelAlignmentVertical(TextAlignment.Centre);
        // chart.getStyler().setXAxisLabelRotation(90);
        

        // BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

        // try {
        //     document.add(Image.getInstance(chartImage, null));
        // } catch (IOException | DocumentException e) {
        //     e.printStackTrace();
        // }
        int width = (int) (document.getPageSize().getWidth() * 0.9);

        Map<Integer, Map<Integer, ArrayList<Double>>> headingsByLane = new HashMap<>();
        Map<Integer, Map<Integer, ArrayList<Date>>> timestamps = new HashMap<>();




        for (LaneDirectionOfTravelAssessment assessment : assessments) {
            for (LaneDirectionOfTravelAssessmentGroup group : assessment.getLaneDirectionOfTravelAssessmentGroup()) {
                if (headingsByLane.containsKey(group.getLaneID())) {

                    if(headingsByLane.get(group.getLaneID()).containsKey(group.getSegmentID())){
                        headingsByLane.get(group.getLaneID()).get(group.getSegmentID()).add((double)Math.round(group.getMedianHeading() - group.getExpectedHeading()));
                        timestamps.get(group.getLaneID()).get(group.getSegmentID()).add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
                    }
                    else{
                        ArrayList<Double> headings = new ArrayList<>();
                        headings.add((double)Math.round(group.getMedianHeading() - group.getExpectedHeading()));
                        headingsByLane.get(group.getLaneID()).put(group.getSegmentID(), headings);

                        ArrayList<Date> times = new ArrayList<>();
                        times.add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
                        timestamps.get(group.getLaneID()).put(group.getSegmentID(), times);

                    }

                    
                } else {
                    ArrayList<Double> headings = new ArrayList<>();
                    headings.add((double)Math.round(group.getMedianHeading() - group.getExpectedHeading()));
                    headingsByLane.put(group.getLaneID(), new HashMap<Integer,ArrayList<Double>>());
                    headingsByLane.get(group.getLaneID()).put(group.getSegmentID(), headings);

                    ArrayList<Date> times = new ArrayList<>();
                    times.add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
                    timestamps.put(group.getLaneID(), new HashMap<Integer,ArrayList<Date>>());
                    timestamps.get(group.getLaneID()).put(group.getSegmentID(), times);
                }
            }
        }

        

        if (assessments.size() > 0) {
            Date minDate = Date.from(Instant.ofEpochMilli(assessments.get(0).getTimestamp()));
            Date maxDate = Date.from(Instant.ofEpochMilli(assessments.get(assessments.size() - 1).getTimestamp()));

            ArrayList<Integer> keys = new ArrayList<>(headingsByLane.keySet());
            Collections.sort(keys);

            for (Integer key : keys) {

                XYChart chart = new XYChartBuilder().width(width).height(300).title("Vehicle Heading Error Delta")
                 .xAxisTitle("Time")
                 .yAxisTitle("Heading Delta (Degrees)").build();


                

                ArrayList<Integer> segments = new ArrayList<>(headingsByLane.get(key).keySet());
                Collections.sort(segments);
                for (Integer segment : segments) {
                    ArrayList<Double> headings = headingsByLane.get(key).get(segment);
                    ArrayList<Date> times = timestamps.get(key).get(segment);
                    headings.add(0, headings.get(0));
                    times.add(0, minDate);

                    headings.add(headings.size(), headings.get(headings.size() - 1));
                    times.add(maxDate);

                    if(times.size() > 0 && headings.size() > 0){
                        
                        XYSeries series = chart.addSeries("Segment: " + segment , times, headings);
                        series.setSmooth(true);
                        series.setMarker(SeriesMarkers.NONE);
                    }
                }

                chart.getStyler().setShowWithinAreaPoint(false);
                chart.getStyler().setChartBackgroundColor(Color.WHITE);
                chart.getStyler().setPlotBackgroundColor(Color.WHITE);
                chart.getStyler().setLegendVisible(true);
                chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
                chart.getStyler().setLegendLayout(LegendLayout.Vertical);
                chart.getStyler().setLegendFont(new java.awt.Font("Times New Roman", java.awt.Font.PLAIN, 6));

                chart.getStyler().setPlotGridLinesVisible(false);
                chart.getStyler().setXAxisMaxLabelCount(31);
                chart.getStyler().setXAxisLabelAlignmentVertical(TextAlignment.Centre);
                chart.getStyler().setXAxisLabelRotation(90);

                BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

                try {
                    document.add(Image.getInstance(chartImage, null));
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addDistanceFromCenterlineOverTime(List<LaneDirectionOfTravelAssessment> assessments) {
        int width = (int) (document.getPageSize().getWidth() * 0.9);

        Map<Integer, Map<Integer, ArrayList<Double>>> distancesFromCenterline = new HashMap<>();
        Map<Integer, Map<Integer, ArrayList<Date>>> timestamps = new HashMap<>();




        for (LaneDirectionOfTravelAssessment assessment : assessments) {
            for (LaneDirectionOfTravelAssessmentGroup group : assessment.getLaneDirectionOfTravelAssessmentGroup()) {
                if (distancesFromCenterline.containsKey(group.getLaneID())) {

                    if(distancesFromCenterline.get(group.getLaneID()).containsKey(group.getSegmentID())){
                        distancesFromCenterline.get(group.getLaneID()).get(group.getSegmentID()).add((double)Math.round(group.getMedianCenterlineDistance()));
                        timestamps.get(group.getLaneID()).get(group.getSegmentID()).add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
                    }
                    else{
                        ArrayList<Double> distances = new ArrayList<>();
                        distances.add((double)Math.round(group.getMedianCenterlineDistance()));
                        distancesFromCenterline.get(group.getLaneID()).put(group.getSegmentID(), distances);

                        ArrayList<Date> times = new ArrayList<>();
                        times.add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
                        timestamps.get(group.getLaneID()).put(group.getSegmentID(), times);

                    }

                    
                } else {
                    ArrayList<Double> distances = new ArrayList<>();
                    distances.add((double)Math.round(group.getMedianCenterlineDistance()));
                    distancesFromCenterline.put(group.getLaneID(), new HashMap<Integer,ArrayList<Double>>());
                    distancesFromCenterline.get(group.getLaneID()).put(group.getSegmentID(), distances);

                    ArrayList<Date> times = new ArrayList<>();
                    times.add(Date.from(Instant.ofEpochMilli(assessment.getTimestamp())));
                    timestamps.put(group.getLaneID(), new HashMap<Integer,ArrayList<Date>>());
                    timestamps.get(group.getLaneID()).put(group.getSegmentID(), times);
                }
            }
        }

        

        if (assessments.size() > 0) {
            Date minDate = Date.from(Instant.ofEpochMilli(assessments.get(0).getTimestamp()));
            Date maxDate = Date.from(Instant.ofEpochMilli(assessments.get(assessments.size() - 1).getTimestamp()));

            ArrayList<Integer> keys = new ArrayList<>(distancesFromCenterline.keySet());
            Collections.sort(keys);

            for (Integer key : keys) {

                XYChart chart = new XYChartBuilder().width(width).height(300).title("Distance From Centerline Lane: " + key)
                .xAxisTitle("Time")
                .yAxisTitle("Distance from Centerline (cm)").build();


                

                ArrayList<Integer> segments = new ArrayList<>(distancesFromCenterline.get(key).keySet());
                Collections.sort(segments);
                for (Integer segment : segments) {
                    ArrayList<Double> distances = distancesFromCenterline.get(key).get(segment);
                    ArrayList<Date> times = timestamps.get(key).get(segment);
                    distances.add(0, distances.get(0));
                    times.add(0, minDate);

                    distances.add(distances.size(), distances.get(distances.size() - 1));
                    times.add(maxDate);

                    if(times.size() > 0 && distances.size() > 0){
                        
                        XYSeries series = chart.addSeries("Segment: " + segment , times, distances);
                        series.setSmooth(true);
                        series.setMarker(SeriesMarkers.NONE);
                    }
                }

                chart.getStyler().setShowWithinAreaPoint(false);
                chart.getStyler().setChartBackgroundColor(Color.WHITE);
                chart.getStyler().setPlotBackgroundColor(Color.WHITE);
                chart.getStyler().setLegendVisible(true);
                chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
                chart.getStyler().setLegendLayout(LegendLayout.Vertical);
                chart.getStyler().setLegendFont(new java.awt.Font("Times New Roman", java.awt.Font.PLAIN, 6));

                chart.getStyler().setPlotGridLinesVisible(false);
                chart.getStyler().setXAxisMaxLabelCount(31);
                chart.getStyler().setXAxisLabelAlignmentVertical(TextAlignment.Centre);
                chart.getStyler().setXAxisLabelRotation(90);

                BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

                try {
                    document.add(Image.getInstance(chartImage, null));
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addLaneConnectionOfTravelMap(List<LaneConnectionCount> laneConnectionCounts) {

        Set<Integer> ingressLanes = new HashSet<>();
        Set<Integer> egressLanes = new HashSet<>();
        Map<String, Integer> laneLookup = new HashMap<>();

        for (LaneConnectionCount count : laneConnectionCounts) {
            ingressLanes.add(count.getIngressLaneID());
            egressLanes.add(count.getEgressLaneID());
            laneLookup.put(count.getIngressLaneID() + "_" + count.getEgressLaneID(), count.getCount());
        }

        Integer[] ingressLaneLabels = new Integer[ingressLanes.size()];
        ingressLaneLabels = ingressLanes.toArray(ingressLaneLabels);

        Integer[] egressLaneLabels = new Integer[egressLanes.size()];
        egressLaneLabels = egressLanes.toArray(egressLaneLabels);

        Arrays.sort(ingressLaneLabels);
        Arrays.sort(egressLaneLabels);

        int[][] pairMappings = new int[ingressLaneLabels.length][egressLaneLabels.length];

        for (int i = 0; i < ingressLaneLabels.length; i++) {
            for (int j = 0; j < egressLaneLabels.length; j++) {
                int ingressLane = ingressLaneLabels[i];
                int egressLane = egressLaneLabels[j];
                String hash = ingressLane + "_" + egressLane;
                if (laneLookup.containsKey(hash)) {
                    pairMappings[i][j] = laneLookup.get(hash);
                }

            }
        }

        int[] ingressLaneLabelsInt = Arrays.stream(ingressLaneLabels).mapToInt(Integer::intValue).toArray();
        int[] egressLaneLabelsInt = Arrays.stream(egressLaneLabels).mapToInt(Integer::intValue).toArray();

        int width = (int) (document.getPageSize().getWidth() * 0.9);

        HeatMapChart chart = new HeatMapChartBuilder().width(width).height(600).title("Ingress Egress Lane Pairings")
                .xAxisTitle("Ingress Lane ID").yAxisTitle("Egress Lane ID").build();

        chart.getStyler().setPlotContentSize(1);
        chart.getStyler().setShowValue(true);

        if(ingressLaneLabels.length > 0 && egressLaneLabels.length > 0 ){
            chart.addSeries("Ingress, Egress Lane Pairings", ingressLaneLabelsInt, egressLaneLabelsInt, pairMappings);
        }else{
            int[] fakeIngressLanes = {0};
            int[] fakeEgressLanes = {0};
            int[][] fakePairs = {{0}};
            chart.addSeries("Ingress, Egress Lane Pairings", fakeIngressLanes, fakeEgressLanes, fakePairs);
        }
        

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);

        chart.getStyler().setPlotGridLinesVisible(false);

        // Color[] rangeColors = {Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW,
        // Color.ORANGE, Color.RED};
        // chart.getStyler().setRangeColors(rangeColors);

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);
        Image iTextImage;
        try {
            iTextImage = Image.getInstance(chartImage, null);
            document.add(iTextImage);
        } catch (IOException | DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getZonedDateTimeDayString(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(dayFormatter);
    }

    public String getZonedSecondsString(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(secondsFormatter);
    }

    public ZonedDateTime utcMillisToDay(long utcMillis) {
        ZonedDateTime day = ZonedDateTime.ofInstant(Instant.ofEpochMilli(utcMillis), ZoneOffset.UTC);

        ZonedDateTime dayStart = ZonedDateTime.of(day.getYear(), day.getMonthValue(), day.getDayOfMonth(), 0, 0, 0, 0,
                ZoneOffset.UTC);
        return dayStart;
    }

    public ZonedDateTime utcSecondsToDay(long utcMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(utcMillis / 1000), ZoneOffset.UTC);
    }

    public String utcMillisToDayString(long utcMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(utcMillis / 1000), ZoneOffset.UTC).format(dayFormatter);
    }

    public List<String> getDayStringsInRange(long startTimeMillis, long endTimeMillis) {
        ZonedDateTime startDayTime = utcMillisToDay(startTimeMillis);
        ZonedDateTime endDayTime = utcMillisToDay(endTimeMillis);

        List<String> dateRange = new ArrayList<>();

        int daysAdded = 0;

        while (startDayTime.plusDays(daysAdded).compareTo(endDayTime) <= 0) {
            dateRange.add(getZonedDateTimeDayString(startDayTime.plusDays(daysAdded)));
            daysAdded += 1;
        }

        return dateRange;
    }

    public List<Long> getSecondsStringInRange(long startTimeMillis, long endTimeMillis) {
        long time = startTimeMillis / 1000;
        long endTime = endTimeMillis / 1000;
        List<Long> secondRange = new ArrayList<>();

        while (time < endTime) {
            secondRange.add(time);
            time += 3600;
        }

        return secondRange;
    }

    public double roundPrec(double input, int precision){
        double scaler = Math.pow(10.0, (double)precision);
        return Math.round(input * scaler) / scaler;
    }

}

package us.dot.its.jpo.ode.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.AxesChartStyler.TextAlignment;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.geom.Point;
import com.itextpdf.text.BadElementException;

// import org.jfree.chart.ChartFactory;
// import org.jfree.chart.JFreeChart;
// import org.jfree.chart.axis.CategoryAxis;
// import org.jfree.chart.axis.NumberAxis;
// import org.jfree.chart.plot.CategoryPlot;
// import org.jfree.chart.plot.PlotOrientation;
// import org.jfree.chart.renderer.category.LineAndShapeRenderer;
// import org.jfree.data.category.DefaultCategoryDataset;
// import org.thymeleaf.TemplateEngine;
// import org.thymeleaf.context.Context;
// import org.thymeleaf.templatemode.TemplateMode;
// import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
// import org.xhtmlrenderer.pdf.ITextRenderer;

// import com.itextpdf.text.Document;

// import com.itextpdf.text.PageSize;
// import com.itextpdf.text.Paragraph;
// import com.itextpdf.text.pdf.PdfContentByte;
// import com.itextpdf.text.pdf.PdfTemplate;
// import com.itextpdf.text.pdf.PdfWriter;
// import com.lowagie.text.DocumentException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.ChartData;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;

import java.io.FileNotFoundException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class ReportBuilder {

    private Document document;
    private PdfWriter writer;
    private int width = 400;
    private int height = 400;
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

    public void addMapBroadcastRate(List<IDCount> mapBroadcastRateCounts) {

        try {
            document.newPage();
            document.add(new Paragraph("Map Message Broadcast Rate Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int) document.getPageSize().getWidth();
            int height = (int) 400;

            PdfTemplate template = contentByte.createTemplate(width, height);
            Graphics2D graphics2d = template.createGraphics(width, height,
                    new DefaultFontMapper());
            Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
                    height);

            generateLineChart(
                    getIDCountAsDataset(mapBroadcastRateCounts, "second"),
                    "Map Message Broadcast Rate",
                    "Time",
                    "Message Count").draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
            contentByte.addTemplate(template, startCoordX, startCoordY);

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSpatBroadcastRate(ChartData data) {

        try {
            document.newPage();
            document.add(new Paragraph("Spat Message Broadcast Rate Report"));
            getLineGraph(data, "SPaT Message Broadcast Rate", "Time", "Message Count");
            // PdfContentByte contentByte = writer.getDirectContent();

            // int width = (int) document.getPageSize().getWidth();
            // int height = (int) 400;

            // PdfTemplate template = contentByte.createTemplate(width, height);
            // Graphics2D graphics2d = template.createGraphics(width, height,
            // new DefaultFontMapper());
            // Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
            // height);

            // generateLineChart(
            // getIDCountAsDataset(spatBroadcastRateCounts, "second"),
            // "SPaT Message Broadcast Rate",
            // "Time",
            // "Message Count").draw(graphics2d, rectangle2d);

            // graphics2d.dispose();

            // double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            // double startCoordY = getVerticalCenterpoint() - (height / 2.0);
            // contentByte.addTemplate(template, startCoordX, startCoordY);

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateEvents(ChartData data) {
        try {
            document.newPage();
            document.add(new Paragraph("Signal State Passage Event Report"));
            document.add(getBarGraph(data, "Signal State Passage Events Per Day", "Day", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateStopEvents(ChartData data) {

        try {
            document.newPage();
            document.add(new Paragraph("Signal State Stop Events Report"));
            document.add(getBarGraph(data, "Signal State Stop Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addLaneDirectionOfTravelEvent(ChartData data) {

        try {
            document.newPage();
            document.add(new Paragraph("Lane Direction of Travel Report"));
            document.add(getBarGraph(data, "Lane Direction of Travel Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addConnectionOfTravelEvent(ChartData data) {

        try {
            document.newPage();
            document.add(new Paragraph("Connection of Travel Event Report"));
            document.add(getBarGraph(data, "Connection of Travel Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateConflictEvent(ChartData data) {

        try {
            document.newPage();
            document.add(new Paragraph("Signal State Conflict Events Event Report"));
            document.add(getBarGraph(data, "Signal State Conflict Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSpatTimeChangeDetailsEvent(ChartData data) {

        try {
            document.newPage();
            document.add(new Paragraph("Time Change Details Event Report"));
            document.add(getBarGraph(data, "Time Change Details Events Per Day", "Day", "Event Count"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addLaneDirectionOfTravelMedianDistanceDistribution(ChartData data){
        try {
            document.newPage();
            document.add(new Paragraph("Lane Direction of Travel Report"));
            document.add(getBarGraph(data, "Distance from Centerline Distribution (ft)", "Feet", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addLaneDirectionOfTravelMedianHeadingDistribution(ChartData data){
        try {
            document.newPage();
            document.add(new Paragraph("Lane Direction of Travel Report"));
            document.add(getBarGraph(data, "Heading Error Distribution (deg)", "Degrees", "Event Count"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    // public void addTestImage() {
    // // double[] xData = new double[] { 0.0, 1.0, 2.0 };
    // // double[] yData = new double[] { 2.0, 1.0, 0.0 };

    // // int width = (int)(document.getPageSize().getWidth() * 0.9);
    // // // Create Chart
    // // // XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)",
    // // xData, yData);
    // // XYChart chart = new XYChartBuilder().width(width).height(400).title("Test
    // // Report").xAxisTitle("X").yAxisTitle("Y").build();
    // // XYSeries series = chart.addSeries("Fake Data", xData, yData);

    // // // Show it
    // // // new SwingWrapper(chart).displayChart();

    // // // Save it
    // // // BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);

    // // // or save it in high-res
    // // // BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI",
    // // BitmapFormat.PNG, 300);
    // // // new SwingWrapper(chart).displayChart();
    // // chart.getStyler().setShowWithinAreaPoint(false);
    // // chart.getStyler().setChartBackgroundColor(Color.WHITE);
    // // chart.getStyler().setPlotBackgroundColor(Color.WHITE);
    // // chart.getStyler().setLegendVisible(false);
    // System.out.println(0);
    // // Create Chart
    // XYChart chart = new XYChartBuilder().width(width).height(400).title("Test
    // Report").xAxisTitle("X").yAxisTitle("Y").build();

    // // Customize Chart
    // // chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
    // // chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
    // // chart.getStyler().setZoomEnabled(true);
    // //
    // chart.getStyler().setZoomResetButtomPosition(Styler.CardinalPosition.InsideS);
    // // chart.getStyler().setZoomResetByDoubleClick(false);
    // // chart.getStyler().setZoomResetByButton(true);
    // // chart.getStyler().setZoomSelectionColor(new Color(0, 0, 192, 128));

    // // Series
    // Random random = new Random();
    // System.out.println(1);

    // // generate data
    // List<Date> xData1 = new ArrayList<>();
    // List<Double> yData1 = new ArrayList<>();
    // List<Date> xData2 = new ArrayList<>();
    // List<Double> yData2 = new ArrayList<>();

    // System.out.println(2);
    // DateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
    // sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    // Date date = null;
    // for (int i = 1; i <= 14; i++) {

    // try {
    // date = sdf.parse("23:45:31." + (100 * i + random.nextInt(20)));
    // } catch (ParseException e) {
    // e.printStackTrace();
    // }
    // xData1.add(date);
    // xData2.add(date);
    // yData1.add(Math.random() * i);
    // yData2.add(Math.random() * i * 100);
    // }
    // System.out.println(3);
    // XYSeries series = chart.addSeries("series 1", xData1, yData1);
    // series.setMarker(SeriesMarkers.NONE);
    // chart.addSeries("series 2", xData2,
    // yData2).setMarker(SeriesMarkers.NONE).setYAxisGroup(1);
    // System.out.println(4 +" " + chart);
    // BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);
    // System.out.println(4.5);
    // Image iTextImage;
    // System.out.println(5);
    // try {
    // iTextImage = Image.getInstance(chartImage, null);
    // document.newPage();
    // System.out.println(6);
    // document.add(new Paragraph("Time Change Details Event Report"));
    // document.add(iTextImage);
    // System.out.println(7);
    // } catch (IOException | DocumentException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // }

    public void addTestBarChart() {
        int width = (int) (document.getPageSize().getWidth() * 0.9);

        CategoryChart chart = new CategoryChartBuilder()
                .width(width)
                .height(400)
                .title("Test Bar Chart")
                .xAxisTitle("Score")
                .yAxisTitle("Number")
                .build();

        chart.addSeries("test 1", Arrays.asList("hi", "1", "2", "3", "4"), Arrays.asList(4, 5, 9, 6, 5));

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);

        chart.getStyler().setLabelsVisible(false);
        chart.getStyler().setPlotGridLinesVisible(false);

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);
        Image iTextImage;
        try {
            iTextImage = Image.getInstance(chartImage, null);
            document.newPage();
            document.add(new Paragraph("Time Change Details Event Report"));
            document.add(iTextImage);
        } catch (IOException | DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Image getLineGraph(ChartData data, String title, String xAxisLabel, String yAxislabel) {
        int width = (int) (document.getPageSize().getWidth() * 0.9);

        // double[] xData = new double[] { 0.0, 1.0, 2.0 };
        // double[] yData = new double[] { 2.0, 1.0, 0.0 };

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(400).title("Test Report").xAxisTitle("X")
                .yAxisTitle("Y").build();
        XYSeries series = chart.addSeries("Fake Data", data.getLabels(), data.getValues());

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
                .height(400)
                .title(title)
                .xAxisTitle(xAxisLabel)
                .yAxisTitle(yAxislabel)
                .build();

        chart.addSeries("series", data.getLabels(), data.getValues());

        chart.getStyler().setShowWithinAreaPoint(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendVisible(false);

        chart.getStyler().setLabelsVisible(false);
        chart.getStyler().setPlotGridLinesVisible(false);
        chart.getStyler().setXAxisMaxLabelCount(31);
        chart.getStyler().setXAxisLabelAlignmentVertical(TextAlignment.Centre);
        chart.getStyler().setXAxisLabelRotation(90);

        BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);

        try {
            return Image.getInstance(chartImage, null);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void addLaneConnectionOfTravelMap(List<LaneConnectionCount> laneConnectionCounts){

        Map<Integer, Integer> ingressLanes = new HashMap<>();
        Map<Integer, Integer> egressLanes = new HashMap<>();
        int ingressIndex = 0;
        int egressIndex = 0;



        for(LaneConnectionCount count: laneConnectionCounts){
                // ingressLanes.add(count.getIngressLaneID());
                // egressLanes.add(count.getEgressLaneID());
            int ingressLane = count.getIngressLaneID();
            int egressLane = count.getEgressLaneID();


            if(!ingressLanes.containsKey(ingressLane)){
                ingressLanes.put(ingressLane, ingressIndex);
                ingressIndex +=1;
            }

            if(!egressLanes.containsKey(egressLane)){
                egressLanes.put(egressLane, egressIndex);
                egressIndex +=1;
            }
        }

        int[][] pairMappings = new int[ingressLanes.size()][egressLanes.size()];
        int[] ingressLaneLabels = new int[ingressLanes.size()];
        int[] egressLaneLabels = new int[egressLanes.size()];

        for(LaneConnectionCount count: laneConnectionCounts){
            pairMappings[ingressLanes.get(count.getIngressLaneID())][egressLanes.get(count.getEgressLaneID())] = count.getCount();
            ingressLaneLabels[ingressLanes.get(count.getIngressLaneID())] = count.getIngressLaneID();
            egressLaneLabels[egressLanes.get(count.getEgressLaneID())] = count.getEgressLaneID();
        }


        


        

        int width = (int) (document.getPageSize().getWidth() * 0.9);

        HeatMapChart chart = new HeatMapChartBuilder().width(width).height(600).title("Ingress Egress Lane Pairings")
                .build();

        chart.getStyler().setPlotContentSize(1);
        chart.getStyler().setShowValue(true);

        chart.addSeries("Ingress, Egress Lane Pairings", ingressLaneLabels, egressLaneLabels, pairMappings);

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
            document.newPage();
            document.add(new Paragraph("Ingress Egress Lane Pairings"));
            document.add(iTextImage);
        } catch (IOException | DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // public void addTestHeatmap() {
    //     int width = (int) (document.getPageSize().getWidth() * 0.9);

    //     HeatMapChart chart = new HeatMapChartBuilder().width(width).height(600).title(getClass().getSimpleName())
    //             .build();

    //     chart.getStyler().setPlotContentSize(1);
    //     chart.getStyler().setShowValue(true);

    //     int[] xData = { 1, 2, 3, 4 };
    //     int[] yData = { 1, 2, 3 };
    //     int[][] heatData = new int[xData.length][yData.length];
    //     Random random = new Random();
    //     for (int i = 0; i < xData.length; i++) {
    //         for (int j = 0; j < yData.length; j++) {
    //             heatData[i][j] = random.nextInt(1000);
    //         }
    //     }
    //     chart.addSeries("Basic HeatMap", xData, yData, heatData);

    //     chart.getStyler().setShowWithinAreaPoint(false);
    //     chart.getStyler().setChartBackgroundColor(Color.WHITE);
    //     chart.getStyler().setPlotBackgroundColor(Color.WHITE);
    //     chart.getStyler().setLegendVisible(false);

    //     chart.getStyler().setPlotGridLinesVisible(false);

    //     // Color[] rangeColors = {Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW,
    //     // Color.ORANGE, Color.RED};
    //     // chart.getStyler().setRangeColors(rangeColors);

    //     BufferedImage chartImage = BitmapEncoder.getBufferedImage(chart);
    //     Image iTextImage;
    //     try {
    //         iTextImage = Image.getInstance(chartImage, null);
    //         document.newPage();
    //         document.add(new Paragraph("Time Change Details Event Report"));
    //         document.add(iTextImage);
    //     } catch (IOException | DocumentException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }

    // }

    public static String parseThymeleafTemplate() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("to", "Conflict Monitor Report");

        // System.out.println(chartHTML);

        // BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        // writer.write(html);
        // writer.close();
        double[] xData = new double[] { 0.0, 1.0, 2.0 };
        double[] yData = new double[] { 2.0, 1.0, 0.0 };

        // Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

        // Show it
        // new SwingWrapper(chart).displayChart();

        // Save it
        // BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);

        // or save it in high-res
        // BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI",
        // BitmapFormat.PNG, 300);
        BufferedImage image = BitmapEncoder.getBufferedImage(chart);

        return templateEngine.process("report_template", context);
    }

    public static void generatePdfFromHtml(String html) {
        try {
            // String outputFolder = System.getProperty("user.home") + File.separator +
            // "thymeleaf.pdf";
            // OutputStream outputStream = new FileOutputStream(outputFolder);
            // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStream outputStream = new FileOutputStream("test.pdf");

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
            // return outputStream.toByteArray();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (com.lowagie.text.DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private double getHorizontalCenterpoint() {
        return document.getPageSize().getWidth() / 2.0;
    }

    private double getVerticalCenterpoint() {
        return document.getPageSize().getHeight() / 2.0;
    }

    public JFreeChart generatePieChart() {
        DefaultPieDataset dataSet = new DefaultPieDataset();
        dataSet.setValue("China", 19.64);
        dataSet.setValue("India", 17.3);
        dataSet.setValue("United States", 4.54);
        dataSet.setValue("Indonesia", 3.4);
        dataSet.setValue("Brazil", 2.83);
        dataSet.setValue("Pakistan", 2.48);
        dataSet.setValue("Bangladesh", 2.38);

        JFreeChart chart = ChartFactory.createPieChart(
                "World Population by countries", dataSet, true, true, false);

        return chart;
    }

    // public JFreeChart generateBarChart() {
    // DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
    // dataSet.setValue(791, "Population", "1750 AD");
    // dataSet.setValue(978, "Population", "1800 AD");
    // dataSet.setValue(1262, "Population", "1850 AD");
    // dataSet.setValue(1650, "Population", "1900 AD");
    // dataSet.setValue(2519, "Population", "1950 AD");
    // dataSet.setValue(6070, "Population", "2000 AD");

    // JFreeChart chart = ChartFactory.createBarChart(
    // "World Population growth", "Year", "Population in millions",
    // dataSet, PlotOrientation.VERTICAL, false, true, false);

    // return chart;
    // }

    public DefaultCategoryDataset getIDCountAsDataset(List<IDCount> idCounts, String rowKey) {

        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        for (IDCount count : idCounts) {
            dataSet.setValue(count.getCount(), rowKey, count.getId());
        }

        return dataSet;
    }

    public JFreeChart generateLineChart(DefaultCategoryDataset dataSet, String title, String xAxisLabel,
            String yAxisLabel) {
        return ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataSet, PlotOrientation.VERTICAL, false,
                false, false);
    }

    public JFreeChart generateBarChart(DefaultCategoryDataset dataSet, String title, String xAxisLabel,
            String yAxisLabel) {
        return ChartFactory.createBarChart(title, xAxisLabel, yAxisLabel, dataSet, PlotOrientation.VERTICAL, false,
                true, false);
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
            time += 1;
            System.out.println(endTime - time);
        }

        return secondRange;
    }

}

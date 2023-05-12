package us.dot.its.jpo.ode.api;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.geom.Point;

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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IDCount;

import java.io.FileNotFoundException;

public class ReportBuilder {

    private Document document;
    private PdfWriter writer;
    private int width = 400;
    private int height = 400;

    // Creates a new PDF report builder to add components to.
    public ReportBuilder(OutputStream stream){
        document = new Document();
        try {
            writer = PdfWriter.getInstance(document, stream);
            document.open();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    // Writes PDF to File System if Output Stream, allows getting PDF as ByteStream for ByteOutputStreams
    public void write(){
        document.close();

    }

    public void addMapBroadcastRate(List<IDCount> mapBroadcastRateCounts){
        
        try {
            document.newPage();
            document.add(new Paragraph("Map Message Broadcast Rate Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int)document.getPageSize().getWidth();
            int height = (int)400;
            

			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);            

            generateLineChart(
                getIDCountAsDataset(mapBroadcastRateCounts, "second"),
                "Map Message Broadcast Rate",
                "Time",
                "Message Count"
            ).draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
			contentByte.addTemplate(template, startCoordX, startCoordY);


        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSpatBroadcastRate(List<IDCount> spatBroadcastRateCounts){
        
        try {
            document.newPage();
            document.add(new Paragraph("Spat Message Broadcast Rate Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int)document.getPageSize().getWidth();
            int height = (int)400;
            

			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);            

            generateLineChart(
                getIDCountAsDataset(spatBroadcastRateCounts, "second"),
                "SPaT Message Broadcast Rate",
                "Time",
                "Message Count"
            ).draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
			contentByte.addTemplate(template, startCoordX, startCoordY);


        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateEvents(List<IDCount> counts){
        
        try {
            document.newPage();
            document.add(new Paragraph("Signal State Passage Event Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int)document.getPageSize().getWidth();
            int height = (int)400;
            

			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);            

            generateBarChart(
                getIDCountAsDataset(counts, "day"),
                "Signal State Passage Events Per Day",
                "Time",
                "Event Count"
            ).draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
			contentByte.addTemplate(template, startCoordX, startCoordY);


        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addSignalStateStopEvents(List<IDCount> counts){
        
        try {
            document.newPage();
            document.add(new Paragraph("Signal State Stop Event Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int)document.getPageSize().getWidth();
            int height = (int)400;
            

			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);            

            generateBarChart(
                getIDCountAsDataset(counts, "day"),
                "Signal State Stop Events Per Day",
                "Time",
                "Event Count"
            ).draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
			contentByte.addTemplate(template, startCoordX, startCoordY);


        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addLaneDirectionOfTravelEvent(List<IDCount> counts){
        
        try {
            document.newPage();
            document.add(new Paragraph("Lane Direction of Travel Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int)document.getPageSize().getWidth();
            int height = (int)400;
            

			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);            

            generateBarChart(
                getIDCountAsDataset(counts, "day"),
                "Lane Direction of Travel Events Per Day",
                "Time",
                "Event Count"
            ).draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
			contentByte.addTemplate(template, startCoordX, startCoordY);


        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    public void addConnectionOfTravelEvent(List<IDCount> counts){
        
        try {
            document.newPage();
            document.add(new Paragraph("Connection of Travel Event Report"));
            PdfContentByte contentByte = writer.getDirectContent();

            int width = (int)document.getPageSize().getWidth();
            int height = (int)400;
            

			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);            

            generateBarChart(
                getIDCountAsDataset(counts, "day"),
                "Connection of Travel Events Per Day",
                "Time",
                "Event Count"
            ).draw(graphics2d, rectangle2d);

            graphics2d.dispose();

            double startCoordX = getHorizontalCenterpoint() - (width / 2.0);
            double startCoordY = getVerticalCenterpoint() - (height / 2.0);
			contentByte.addTemplate(template, startCoordX, startCoordY);


        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }



    

    private double getHorizontalCenterpoint(){
        return document.getPageSize().getWidth() / 2.0;
    }

    private double getVerticalCenterpoint(){
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
	// 	DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
	// 	dataSet.setValue(791, "Population", "1750 AD");
	// 	dataSet.setValue(978, "Population", "1800 AD");
	// 	dataSet.setValue(1262, "Population", "1850 AD");
	// 	dataSet.setValue(1650, "Population", "1900 AD");
	// 	dataSet.setValue(2519, "Population", "1950 AD");
	// 	dataSet.setValue(6070, "Population", "2000 AD");

	// 	JFreeChart chart = ChartFactory.createBarChart(
	// 			"World Population growth", "Year", "Population in millions",
	// 			dataSet, PlotOrientation.VERTICAL, false, true, false);

	// 	return chart;
	// }

    public DefaultCategoryDataset getIDCountAsDataset(List<IDCount> idCounts, String rowKey){
        
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        for(IDCount count: idCounts){
            dataSet.setValue(count.getCount(), rowKey, count.getId());
        }

        return dataSet;
    }


    public JFreeChart generateLineChart(DefaultCategoryDataset dataSet, String title, String xAxisLabel, String yAxisLabel){
        return ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataSet, PlotOrientation.VERTICAL, false, false, false);
    }

    public JFreeChart generateBarChart(DefaultCategoryDataset dataSet, String title, String xAxisLabel, String yAxisLabel){
        return ChartFactory.createBarChart(title, xAxisLabel, yAxisLabel, dataSet, PlotOrientation.VERTICAL, false, true, false);
    }
}

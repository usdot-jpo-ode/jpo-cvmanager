package us.dot.its.jpo.ode.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

public class ReportBuilder {

    public String parseThymeleafTemplate() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
    
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    
        Context context = new Context();
        context.setVariable("to", "Conflict Monitor Report");
    
        return templateEngine.process("report_template", context);
    }

    public byte[] generatePdfFromHtml(String html) {
        try {
            // String outputFolder = System.getProperty("user.home") + File.separator + "thymeleaf.pdf";
            // OutputStream outputStream = new FileOutputStream(outputFolder);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
            return outputStream.toByteArray();

        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    
        
    }
    
}

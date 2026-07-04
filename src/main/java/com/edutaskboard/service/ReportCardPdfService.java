package com.edutaskboard.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.edutaskboard.model.ReportCard;
import com.edutaskboard.model.SubjectResult;
import com.edutaskboard.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportCardPdfService {

    @Autowired private TemplateEngine templateEngine;
    @Autowired private ResultService resultService;

    /** Renders the printable HTML report card, then converts it to PDF bytes. */
    public byte[] generatePdf(User student, com.edutaskboard.model.SchoolClass schoolClass) {
        ReportCard reportCard = resultService.reportCardFor(student, schoolClass).orElse(null);
        List<SubjectResult> results = resultService.subjectResultsFor(student, schoolClass);

        Context ctx = new Context();
        ctx.setVariable("student", student);
        ctx.setVariable("schoolClass", schoolClass);
        ctx.setVariable("reportCard", reportCard);
        ctx.setVariable("results", results);

        String html = templateEngine.process("pdf/report-card-pdf", ctx);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report card PDF: " + e.getMessage(), e);
        }
    }
}

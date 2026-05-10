package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for generating and downloading PDF reports.
 */
@RestController
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates a hardcoded sample Leadership Compass report as a downloadable PDF.
     *
     * @return the generated PDF file as a downloadable response
     */
    @GetMapping("/api/reports/dummy")
    public ResponseEntity<byte[]> downloadDummyReport() {
        byte[] pdfBytes = pdfService.generateDummyReportPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leadership-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
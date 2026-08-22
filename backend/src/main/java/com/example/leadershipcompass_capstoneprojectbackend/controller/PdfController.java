package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.service.PdfService;
import com.example.leadershipcompass_capstoneprojectbackend.service.SurveyService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {

    private final PdfService pdfService;
    private final SurveyService surveyService;

    public PdfController(PdfService pdfService, SurveyService surveyService) {
        this.pdfService = pdfService;
        this.surveyService = surveyService;
    }

    @GetMapping("/api/reports/dummy")
    public ResponseEntity<byte[]> downloadDummyReport() {
        byte[] pdfBytes = pdfService.generateDummyReportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leadership-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/api/reports/{resultId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long resultId) {
        SurveyResult result = surveyService.getResultById(resultId);
        byte[] pdfBytes = pdfService.generateReportPdf(result);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leadership-report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }
}
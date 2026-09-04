package com.amtpilot.process.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.process.service.ProcessService;

@RestController
@RequestMapping("/api/v1/processes")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> getProcesses(
            @RequestParam(defaultValue = "Dortmund") String city,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE)
            String traceId) {

        List<ProcessResponse> processes =
                processService.getProcessesByCity(city);

        return ResponseEntity.ok(
                ApiResponse.success(processes, traceId));
    }
}
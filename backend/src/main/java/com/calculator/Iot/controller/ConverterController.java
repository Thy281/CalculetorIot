package com.calculator.Iot.controller;

import com.calculator.Iot.dto.ConverterRequest;
import com.calculator.Iot.dto.ConverterResult;
import com.calculator.Iot.service.NumberBaseConverterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/converter")
@RequiredArgsConstructor
public class ConverterController {

    private final NumberBaseConverterService converterService;

    @PostMapping("/convert")
    public ResponseEntity<ConverterResult> convert(
            @RequestBody ConverterRequest request,
            HttpServletRequest httpRequest) {

        System.out.println("====== CONVERSION REQUEST ======");
        System.out.println("Origin IP : " + httpRequest.getRemoteAddr());
        System.out.println("Payload   : " + request);
        System.out.println("=================================");

        String convertedValue = converterService.convertDirect(
            request.value(),
            request.fromBase(),
            request.toBase()
        );

        ConverterResult result = new ConverterResult(
            request.value(),
            request.fromBase(),
            convertedValue,
            request.toBase()
        );

        return ResponseEntity.ok(result);
    }
}

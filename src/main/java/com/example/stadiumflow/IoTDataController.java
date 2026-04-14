package com.example.stadiumflow;

import com.example.stadiumflow.service.IoTService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/iot")
@CrossOrigin(origins = "*")
public class IoTDataController {

    private final IoTService ioTService;

    public IoTDataController(IoTService ioTService) {
        this.ioTService = ioTService;
    }

    @GetMapping("/stream")
    public SseEmitter streamIoTData() {
        return ioTService.registerClient();
    }
}

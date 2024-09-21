package com.example.ping.controller;

import com.example.ping.pojo.ResponseResult;
import com.example.ping.service.PingService;
import com.example.ping.service.impl.PingServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class PingController {

    @Resource
    private PingService pingService;

    @GetMapping("/ping")
    public Mono<ResponseResult> ping() {
        Mono<ResponseResult> responseResult = pingService.ping("Hello");
        log.info("ping response result: {}", responseResult);
        return responseResult;
    }


}

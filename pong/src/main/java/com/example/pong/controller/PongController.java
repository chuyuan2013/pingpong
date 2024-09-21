package com.example.pong.controller;

import com.example.pong.pojo.ResponseResult;
import com.example.pong.service.PongService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class PongController {
    @Resource
    private PongService pongService;

    @GetMapping("/pong")
    public Mono<ResponseResult> pong(String params) {
        log.info("pong request params: {}", params);
        ResponseResult responseResult = pongService.allowRequest(params);
        log.info("pong response result: {}", responseResult);
        return Mono.just(responseResult);
    }

}

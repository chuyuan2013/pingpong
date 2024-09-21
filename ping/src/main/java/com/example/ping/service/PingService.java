package com.example.ping.service;

import com.example.ping.pojo.ResponseResult;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface PingService {

    Mono<ResponseResult> ping(String str);
}

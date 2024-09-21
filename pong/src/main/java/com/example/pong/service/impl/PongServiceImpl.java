package com.example.pong.service.impl;

import com.example.pong.config.RateLimiter;
import com.example.pong.pojo.ResponseResult;
import com.example.pong.service.PongService;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PongServiceImpl implements PongService {

    @Resource
    private RateLimiter rateLimiter;

    @Override
    public ResponseResult allowRequest(String params) {
        ResponseResult responseResult = new ResponseResult();

        //1, check params
        if(StringUtil.isNullOrEmpty(params)){
            responseResult.setCode(HttpStatus.BAD_REQUEST.value());
            responseResult.setMessage("error");
            responseResult.setData("parameters are not allowed to be empty");
            return responseResult;
        }else if(!"Hello".equalsIgnoreCase(params)){
            responseResult.setCode(HttpStatus.BAD_REQUEST.value());
            responseResult.setMessage("fail");
            responseResult.setData("parameter error");
            return responseResult;
        }

        //2,check rate limit and response
        if(rateLimiter.allowRequest()){
            responseResult.setCode(HttpStatus.OK.value());
            responseResult.setMessage("success");
            responseResult.setData("World");
        }else{
            responseResult.setCode(HttpStatus.TOO_MANY_REQUESTS.value());
            responseResult.setMessage("fail");
            responseResult.setData("Request send & Pong throttled it");
        }
        return responseResult;
    }
}

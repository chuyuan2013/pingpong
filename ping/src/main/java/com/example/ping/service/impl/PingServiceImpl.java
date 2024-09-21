package com.example.ping.service.impl;

import com.example.ping.pojo.ResponseResult;
import com.example.ping.service.PingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class PingServiceImpl implements PingService {
    @Resource
    private WebClient webClient;

    @Value("${server.lock.path}")
    String lockFilePath;

    @Value("${server.pong.url}?params=hello")
    private String url;

    @Value("${spring.application.maxRequestsPerSecond}")
    private static int maxRequestsPerSecond = 2;
    private static final AtomicInteger requestCount = new AtomicInteger(0); // 请求计数

    /**
     * 发送 ping 请求，并返回响应
     * @return 响应字符串
     */
    @Override
    public Mono<ResponseResult> ping(String str) {
        ResponseResult responseResult = new ResponseResult();
        if (requestCount.get() >= maxRequestsPerSecond) {
            log.info("Request not send as being \"rate limited\".");
            responseResult.setCode(HttpStatus.TOO_MANY_REQUESTS.value());
            responseResult.setMessage("fail");
            responseResult.setData("Request not send as being \"rate limited\".");
        }else{
            FileLock lock = getLock();
            if(lock != null){
                try {
                    requestCount.incrementAndGet();
                    Mono<ResponseResult> response = webClient.get().uri(url).retrieve().bodyToMono(ResponseResult.class);
                    requestCount.decrementAndGet();
                    return response;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try{
                        lock.release();
                    }catch (Exception e){
                        log.info(e.getMessage());
                    }
                }
            }else{
                responseResult = new ResponseResult();
                responseResult.setCode(HttpStatus.FORBIDDEN.value());
                responseResult.setMessage("forbidden");
                responseResult.setData("lock file error");
            }
        }
        return Mono.just(responseResult);

    }


    /**
     * 1,检查文件是否存在，不存在则创建
     * 2,获取文件锁
     */
    public FileLock getLock() {
        if (lockFilePath == null) {
            throw new IllegalArgumentException("lockFilePath cannot be null");
        }

        File file = new File(lockFilePath);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileChannel channel = null;
        FileLock lock = null;
        try {
            channel = FileChannel.open(new File(lockFilePath).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
            lock = channel.lock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try{
                channel.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return lock;
    }

    /**
     * 定时重置请求计数
     */
    @Scheduled(fixedRate = 1000)
    private void RequestCountReset() {
        if(requestCount.get() >= maxRequestsPerSecond){
            requestCount.set(0);
        }
    }
}

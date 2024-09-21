package com.example.ping.service.impl

import com.example.ping.controller.PingController
import com.example.ping.pojo.ResponseResult
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import spock.lang.Specification

@SpringBootTest
class PingServiceImplTest extends Specification{

    PingServiceImpl pingService
    WebClient webClient = Mock(WebClient)
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
    WebClient.ResponseSpec responseSpec = Mock(WebClient.ResponseSpec)

    def lockFilePath = "test.lock"
    def maxRequestsPerSecond = 5
    PingController pingController

    def setup() {
        pingController = new PingController()
        pingService = new PingServiceImpl()
        pingService.webClient = webClient
        pingService.lockFilePath = lockFilePath
        pingService.maxRequestsPerSecond = maxRequestsPerSecond

        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri(_) >> requestHeadersUriSpec
        requestHeadersUriSpec.retrieve() >> responseSpec
    }

    def "should return rate limited response when request count exceeds maxRequestsPerSecond"() {
        given:
        pingService.requestCount.set(maxRequestsPerSecond)

        when:
        def result = pingService.ping("Hello").block()

        then:
        result.code == HttpStatus.TOO_MANY_REQUESTS.value()
        result.message == "fail"
        result.data == "Request not send as being \"rate limited\"."
    }

    def "should successfully send ping request and return response"() {
        given:
        pingService.requestCount.set(0)
        def responseResult = new ResponseResult(code: HttpStatus.OK.value(), message: "success", data: "pong")
        responseSpec.bodyToMono(ResponseResult.class) >> Mono.just(responseResult)

        when:
        def result = pingService.ping("Hello").block()

        then:
        result.code == HttpStatus.OK.value()
        result.message == "success"
        result.data == "pong"
    }

    def "should return forbidden response when lock file error occurs"() {
        given:
        pingService.lockFilePath = lockFilePath

        def pingServiceSpy = Spy(PingServiceImpl) {
            getLock() >> null // 模拟成功获取锁
        }

        when:
        def result = pingServiceSpy.ping("Hello").block()
        println("值："+result)
        then:
        result.code == HttpStatus.FORBIDDEN.value()
        result.message == "forbidden"
        result.data == "lock file error"
    }

    def "should reset request count periodically"() {
        given:
        pingService.requestCount.set(maxRequestsPerSecond)

        when:
        pingService.RequestCountReset()

        then:
        pingService.requestCount.get() == 0
    }

    def "should return too many requests when limit exceeded"() {
        given:
        pingService.metaClass.getLock = { -> Mock(FileLock) } // 模拟获取文件锁
        pingService.maxRequestsPerSecond = 1 // 设置最大请求数为 1
        pingService.requestCount.set(1) // 手动设置请求计数

        when:
        def result = pingService.ping("Hello").block()

        then:
        result != null
        result.code == HttpStatus.TOO_MANY_REQUESTS.value()
        result.message == "fail"
        result.data == "Request not send as being \"rate limited\"."
    }
}

package com.example.pong.service.impl

import com.example.pong.config.RateLimiter
import com.example.pong.pojo.ResponseResult
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
class PongServiceImplTest extends Specification {

    @Subject
    PongServiceImpl pongServiceImpl

    RateLimiter rateLimiter = Mock(RateLimiter)

    def setup() {
        pongServiceImpl = new PongServiceImpl()
        pongServiceImpl.rateLimiter = rateLimiter
    }

    def "should return success response when valid parameters and within rate limit"() {
        given:
        rateLimiter.allowRequest() >> true

        when:
        ResponseResult result = pongServiceImpl.allowRequest("Hello")

        then:
        result.code == HttpStatus.OK.value()
        result.message == "success"
        result.data == "World"
    }

    def "should return bad request when parameters are empty"() {
        when:
        ResponseResult result = pongServiceImpl.allowRequest("")

        then:
        result.code == HttpStatus.BAD_REQUEST.value()
        result.message == "error"
        result.data == "parameters are not allowed to be empty"
    }

    def "should return bad request when parameters are incorrect"() {
        when:
        ResponseResult result = pongServiceImpl.allowRequest("Goodbye")

        then:
        result.code == HttpStatus.BAD_REQUEST.value()
        result.message == "fail"
        result.data == "parameter error"
    }

    def "should return too many requests response when rate limit exceeded"() {
        given:
        rateLimiter.allowRequest() >> false

        when:
        ResponseResult result = pongServiceImpl.allowRequest("Hello")

        then:
        result.code == HttpStatus.TOO_MANY_REQUESTS.value()
        result.message == "fail"
        result.data == "Request send & Pong throttled it"
    }
}

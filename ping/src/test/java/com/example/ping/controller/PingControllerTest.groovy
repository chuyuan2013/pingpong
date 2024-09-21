package com.example.ping.controller

import com.example.ping.pojo.ResponseResult
import com.example.ping.service.PingService
import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import spock.lang.*

@SpringBootTest
class PingControllerTest extends Specification {

    PingController pingController
    PingService pingService = Mock(PingService)

    def setup() {
        pingController = new PingController()
        pingController.pingService = pingService
    }

    def "should return response from ping service"() {
        given:
        def responseResult = new ResponseResult(code: HttpStatus.OK.value(), message: "success", data: "Hello")
        pingService.ping("Hello") >> Mono.just(responseResult)

        when:
        def result = pingController.ping().block()

        then:
        result.code == HttpStatus.OK.value()
        result.message == "success"
        result.data == "Hello"
    }
}
package com.example.pong.controller

import com.example.pong.pojo.ResponseResult
import com.example.pong.service.PongService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
class PongControllerTest extends Specification{

    PongService pongService = Mock();

    @Subject
    PongController pongController = new PongController(pongService: pongService)

    def "should return success response when params are Hello"() {
        given:
        ResponseResult responseResult = new ResponseResult(code: 200, message: "success", data: "World")
        pongService.allowRequest("Hello") >> responseResult

        when:
        Mono<ResponseEntity<ResponseResult>> response = pongController.pong("Hello")

        println("返回的数据："+response.block().data)

        then:
        response.block().code == HttpStatus.OK.value()
        response.block().message == "success"
        response.block().data == "World"

    }


}

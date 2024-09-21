package com.example.ping.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回结果类
 */
@Data
public class ResponseResult implements Serializable {
    /**
     * 状态码
     */
    private Integer code;
    /**
     * 返回信息
     */
    private String message;
    /**
     * 返回数据
      */
    private Object data;

}

package com.kjq.common;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Message {
    private String username;
    private Integer sex;
    private String content;
}

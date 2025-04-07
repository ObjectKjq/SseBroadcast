package com.kjq.controller;

import cn.hutool.json.JSONUtil;
import com.kjq.common.Message;
import com.kjq.server.SseServer;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class BroadcastController {

    @Resource
    private SseServer sseServer;

    @PostMapping("/broadcast")
    public void broadcastMessage(@RequestBody Message message) {
        if (ObjectUtils.isNotEmpty(message)) {
            sseServer.broadcast(JSONUtil.toJsonStr(message));
        }
    }

}

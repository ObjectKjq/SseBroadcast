package com.kjq.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SseServer {
    // 保存所有连接的客户端
    private static final List<SseEmitter> emitters = new Vector<>();

    /**
     * 客户端订阅 SSE
     */
    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        /**
         * complete()	正常结束连接，触发 onCompletion
         * completeWithError(Throwable ex)	异常结束连接，先触发 onError，再触发 onCompletion
         * send(Object data)	发送事件数据，失败会触发 onError
         */
        SseEmitter emitter = new SseEmitter(0L); // 0表示不超时

        // 添加 emitter 到列表
        emitters.add(emitter);
        log.info("客户端连接成功，当前连接数：{}", emitters.size());
        // 退出回调函数
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("客户端断开连接，当前连接数：{}", emitters.size());
        });
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError(ex -> emitter.completeWithError(ex));

        return emitter;
    }

    /**
     * 广播消息
     * @param message
     */
    public void broadcast(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

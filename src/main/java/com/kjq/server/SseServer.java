package com.kjq.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Iterator;
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
        emitter.onTimeout(() -> {
            emitter.complete(); // 关闭
            log.warn("连接超时关闭");
        });
        emitter.onError(ex -> {
            emitter.completeWithError(ex); // 异常关闭
            log.error("连接异常关闭: {}", ex.getMessage());
        });

        return emitter;
    }

    /**
     * 广播消息
     *
     * @param message
     */
    public void broadcast(String message) {
        Iterator<SseEmitter> iterator = emitters.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(message);
            } catch (IOException e) {
                iterator.remove(); // 确保失效连接被移除
            }
        }
    }

    /**
     * 发送心跳检测客户端是否断开连接，30s一次
     */
    @Scheduled(fixedDelay = 30_000L)
    public void sseHeartbeat() {
        Iterator<SseEmitter> iterator = emitters.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            // 发送心跳事件
            try {
                // 发送心跳事件（空消息，仅作为保活机制）
                emitter.send(SseEmitter.event()
                        .name("heartbeat")  // 事件类型设为 "heartbeat"
                        .data(""));          // 空数据
            } catch (IOException e) {
                // 发送失败可能是由于客户端断开连接，移除该 emitter
                iterator.remove(); // 确保失效连接被移除
            }
        }
    }
}

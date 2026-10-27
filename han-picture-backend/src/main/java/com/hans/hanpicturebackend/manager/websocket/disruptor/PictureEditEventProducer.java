package com.hans.hanpicturebackend.manager.websocket.disruptor;

import com.hans.hanpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hans.hanpicturebackend.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 生产者
 */
@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        // 获取可以生成的位置
        long next = ringBuffer.next();
        // 下面try finally是为了保证获取到位置后，一定要发布，sequence不会卡住
        try {
            // 获取事件对象
            PictureEditEvent pictureEditEvent = ringBuffer.get(next);
            // 设置数据
            pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
            pictureEditEvent.setSession(session);
            pictureEditEvent.setUser(user);
            pictureEditEvent.setPictureId(pictureId);
        } finally {
            // 发布事件
            ringBuffer.publish(next);
        }
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void close() {
        pictureEditEventDisruptor.shutdown();
    }
}

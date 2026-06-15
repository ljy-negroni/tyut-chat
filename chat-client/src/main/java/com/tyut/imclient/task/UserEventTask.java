package com.tyut.imclient.task;

import com.tyut.imclient.listener.EventListenerMulticaster;
import com.tyut.imcommon.contant.IMRedisKey;
import com.tyut.imcommon.model.IMUserEvent;
import com.tyut.imcommon.mq.RedisMQConsumer;
import com.tyut.imcommon.mq.RedisMQListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_USER_EVENT_QUEUE, batchSize = 100)
public class UserEventTask extends RedisMQConsumer<IMUserEvent> {

    private final EventListenerMulticaster listenerMulticaster;
    @Override
    public void onMessage(List<IMUserEvent> events) {
        listenerMulticaster.multicast(events);
    }
}

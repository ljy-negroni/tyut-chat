package com.tyut.imclient.task;

import com.tyut.imclient.listener.MessageListenerMulticaster;
import com.tyut.imcommon.contant.IMRedisKey;
import com.tyut.imcommon.enums.IMListenerType;
import com.tyut.imcommon.model.IMBatchSendResult;
import com.tyut.imcommon.mq.RedisMQListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_RESULT_PRIVATE_QUEUE, batchSize = 100)
public class PrivateMessageResultResultTask extends AbstractMessageResultTask<IMBatchSendResult> {

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void onMessage(List<IMBatchSendResult> batchResults) {
        listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, batchResults);
    }

}

package com.tyut.imserver.task;

import com.tyut.imcommon.contant.IMRedisKey;
import com.tyut.imcommon.enums.IMCmdType;
import com.tyut.imcommon.model.IMRecvInfo;
import com.tyut.imcommon.mq.RedisMQListener;
import com.tyut.imserver.netty.processor.AbstractMessageProcessor;
import com.tyut.imserver.netty.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_MESSAGE_GROUP_QUEUE, batchSize = 100, period = 10)
public class PullGroupMessageTask extends AbstractPullMessageTask<IMRecvInfo> {

    @Override
    public void onMessage(IMRecvInfo recvInfo) {
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.GROUP_MESSAGE);
        processor.process(recvInfo);
    }

}

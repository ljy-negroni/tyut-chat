package com.tyut.imserver.task;

import com.tyut.imcommon.contant.IMRedisKey;
import com.tyut.imcommon.enums.IMCmdType;
import com.tyut.imcommon.model.IMForceLogoutInfo;
import com.tyut.imcommon.mq.RedisMQListener;
import com.tyut.imserver.netty.processor.AbstractMessageProcessor;
import com.tyut.imserver.netty.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_USER_FORCE_LOGOUT_QUEUE)
public class PullForceLogoutTask extends AbstractPullMessageTask<IMForceLogoutInfo> {

    @Override
    public void onMessage(IMForceLogoutInfo logoutInfo) {
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.FORCE_LOGOUT);
        processor.process(logoutInfo);
    }

}

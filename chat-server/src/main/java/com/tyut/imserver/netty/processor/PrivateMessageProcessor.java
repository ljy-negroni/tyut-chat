package com.tyut.imserver.netty.processor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.tyut.imcommon.contant.IMRedisKey;
import com.tyut.imcommon.enums.IMCmdType;
import com.tyut.imcommon.enums.IMSendCode;
import com.tyut.imcommon.model.IMBatchSendResult;
import com.tyut.imcommon.model.IMRecvInfo;
import com.tyut.imcommon.model.IMSendInfo;
import com.tyut.imcommon.model.IMUserInfo;
import com.tyut.imcommon.mq.RedisMQTemplate;
import com.tyut.imcommon.util.ThreadPoolExecutorFactory;
import com.tyut.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisMQTemplate redisMQTemplate;
    private final ScheduledThreadPoolExecutor EXECUTOR = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo sender = recvInfo.getSender();
        List<IMUserInfo> sucessReceivers = new ArrayList<>();
        List<IMUserInfo> noChannelReceivers = new ArrayList<>();
        List<IMUserInfo> errorReceivers = new ArrayList<>();
        for (IMUserInfo receiver : recvInfo.getReceivers()) {
            log.info("接收到私聊消息，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(),
                    recvInfo.getData());
            try {
                ChannelHandlerContext channelCtx =
                        UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
                if (!Objects.isNull(channelCtx)) {
                    // 推送消息到用户
                    IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                    sendInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    channelCtx.channel().writeAndFlush(sendInfo);
                    sucessReceivers.add(receiver);
                } else {
                    noChannelReceivers.add(receiver);
                    log.error("未找到channel，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(),
                            recvInfo.getData());
                }
            } catch (Exception e) {
                // 消息推送失败确认
                errorReceivers.add(receiver);
                log.error("发送异常，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData(),
                        e);
            }
        }
        // 批量回复推送结果
        sendResult(recvInfo, sucessReceivers, IMSendCode.SUCCESS);
        sendResult(recvInfo, noChannelReceivers, IMSendCode.NOT_FIND_CHANNEL);
        sendResult(recvInfo, errorReceivers, IMSendCode.UNKONW_ERROR);
    }

    private void sendResult(IMRecvInfo recvInfo, List<IMUserInfo> receivers, IMSendCode sendCode) {
        if (recvInfo.getSendResult() && CollectionUtil.isNotEmpty(receivers)) {
            EXECUTOR.execute(()->{
                IMBatchSendResult<Object> result = new IMBatchSendResult<>();
                result.setSender(recvInfo.getSender());
                result.setReceivers(receivers);
                result.setCode(sendCode.code());
                result.setData(recvInfo.getData());
                // 推送到结果队列
                String key = StrUtil.join(":", IMRedisKey.IM_RESULT_PRIVATE_QUEUE, recvInfo.getServiceName());
                redisMQTemplate.opsForList().rightPush(key, result);
            });
        }
    }
}

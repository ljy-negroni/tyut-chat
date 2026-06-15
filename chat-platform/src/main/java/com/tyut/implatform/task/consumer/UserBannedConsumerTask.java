package com.tyut.implatform.task.consumer;

import com.tyut.imclient.IMClient;
import com.tyut.imcommon.model.IMSystemMessage;
import com.tyut.imcommon.mq.RedisMQConsumer;
import com.tyut.imcommon.mq.RedisMQListener;
import com.tyut.implatform.contant.RedisKey;
import com.tyut.implatform.dto.UserBanDTO;
import com.tyut.implatform.enums.MessageType;
import com.tyut.implatform.vo.SystemMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author: Blue
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = RedisKey.IM_QUEUE_USER_BANNED)
public class UserBannedConsumerTask extends RedisMQConsumer<UserBanDTO> {

    private final IMClient imClient;
    @Override
    public void onMessage(UserBanDTO dto) {
        log.info("用户被封禁处理,userId:{},原因:{}",dto.getId(),dto.getReason());
        // 推送消息将用户赶下线
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.USER_BANNED.code());
        msgInfo.setContent(dto.getReason());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(dto.getId()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendSystemMessage(sendMessage);
    }
}

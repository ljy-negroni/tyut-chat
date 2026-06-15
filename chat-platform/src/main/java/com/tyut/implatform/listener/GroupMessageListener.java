package com.tyut.implatform.listener;

import com.tyut.imclient.annotation.IMListener;
import com.tyut.imclient.listener.MessageListener;
import com.tyut.imcommon.enums.IMListenerType;
import com.tyut.imcommon.enums.IMSendCode;
import com.tyut.imcommon.model.IMSendResult;
import com.tyut.implatform.vo.GroupMessageVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Slf4j
@IMListener(type = IMListenerType.GROUP_MESSAGE)
@AllArgsConstructor
public class GroupMessageListener implements MessageListener<GroupMessageVO> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(List<IMSendResult<GroupMessageVO>> results) {
        for(IMSendResult<GroupMessageVO> result:results){
            GroupMessageVO messageInfo = result.getData();
            if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
                log.info("消息送达，消息id:{}，发送者:{},接收者:{},终端:{}", messageInfo.getId(), result.getSender().getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
            }
        }
    }

}

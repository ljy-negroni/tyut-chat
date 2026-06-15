package com.tyut.implatform.task.consumer;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tyut.imclient.IMClient;
import com.tyut.imcommon.enums.IMTerminalType;
import com.tyut.imcommon.model.IMGroupMessage;
import com.tyut.imcommon.model.IMUserInfo;
import com.tyut.imcommon.mq.RedisMQConsumer;
import com.tyut.imcommon.mq.RedisMQListener;
import com.tyut.implatform.contant.Constant;
import com.tyut.implatform.contant.RedisKey;
import com.tyut.implatform.dto.GroupBanDTO;
import com.tyut.implatform.entity.GroupMessage;
import com.tyut.implatform.enums.MessageStatus;
import com.tyut.implatform.enums.MessageType;
import com.tyut.implatform.service.GroupMemberService;
import com.tyut.implatform.service.GroupMessageService;
import com.tyut.implatform.util.BeanUtils;
import com.tyut.implatform.vo.GroupMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author: Blue
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = RedisKey.IM_QUEUE_GROUP_BANNED)
public class GroupBannedConsumerTask extends RedisMQConsumer<GroupBanDTO> {

    private final IMClient imClient;

    private final GroupMessageService groupMessageService;

    private final GroupMemberService groupMemberService;

    @Override
    public void onMessage(GroupBanDTO dto) {
        log.info("群聊被封禁处理,群id:{},原因:{}", dto.getId(), dto.getReason());
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(dto.getId());
        // 保存消息
        GroupMessage message = new GroupMessage();
        message.setLocalId(IdWorker.getIdStr());
        message.setGroupId(dto.getId());
        message.setContent("本群聊已被管理员封禁,原因:" + dto.getReason());
        message.setSendId(Constant.SYS_USER_ID);
        message.setSendTime(new Date());
        message.setStatus(MessageStatus.PENDING.code());
        message.setSendNickName("系统管理员");
        message.setType(MessageType.TIP_TEXT.code());
        groupMessageService.saveMessage(message);
        // 推送提示语到群聊中
        GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(Constant.SYS_USER_ID, IMTerminalType.PC.code()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
    }
}

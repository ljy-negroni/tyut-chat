package com.tyut.implatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tyut.imclient.IMClient;
import com.tyut.imcommon.enums.IMTerminalType;
import com.tyut.imcommon.model.IMBatchPrivateMessage;
import com.tyut.imcommon.model.IMPrivateMessage;
import com.tyut.imcommon.model.IMUserInfo;
import com.tyut.implatform.annotation.RedisLock;
import com.tyut.implatform.contant.RedisKey;
import com.tyut.implatform.dto.FriendDndDTO;
import com.tyut.implatform.entity.Friend;
import com.tyut.implatform.entity.PrivateMessage;
import com.tyut.implatform.entity.User;
import com.tyut.implatform.enums.MessageStatus;
import com.tyut.implatform.enums.MessageType;
import com.tyut.implatform.exception.GlobalException;
import com.tyut.implatform.mapper.FriendMapper;
import com.tyut.implatform.mapper.UserMapper;
import com.tyut.implatform.service.FriendService;
import com.tyut.implatform.service.PrivateMessageService;
import com.tyut.implatform.session.SessionContext;
import com.tyut.implatform.session.UserSession;
import com.tyut.implatform.util.BeanUtils;
import com.tyut.implatform.util.ConvUtil;
import com.tyut.implatform.vo.FriendVO;
import com.tyut.implatform.vo.PrivateMessageVO;
import com.tyut.implatform.vo.UserOnlineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisKey.IM_CACHE_FRIEND)
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    private final UserMapper userMapper;
    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    @Lazy
    @Autowired
    private PrivateMessageService privateMessageService;

    @Override
    public List<Friend> findAllFriends() {
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        return this.list(wrapper);
    }

    @Override
    public List<Friend> findByFriendIds(List<Long> friendIds) {
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.in(Friend::getFriendId, friendIds);
        wrapper.eq(Friend::getDeleted, false);
        return this.list(wrapper);
    }

    @Override
    public List<FriendVO> findFriends(Long version) {
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.gt(version > 0, Friend::getVersion, version);
        List<Friend> friends = this.list(wrapper);
        return friends.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<Long> findFriendIds() {
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getDeleted, false);
        wrapper.select(Friend::getFriendId);
        List<Friend> friends = this.list(wrapper);
        return friends.stream().map(Friend::getFriendId).collect(Collectors.toList());
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_FRIEND_ADD, key = "#userId+':'+#friendId")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addFriend(Long userId, Long friendId) {
        if (friendId.equals(userId)) {
            throw new GlobalException("不允许添加自己为好友");
        }
        // 互相绑定好友关系
        FriendServiceImpl proxy = (FriendServiceImpl)AopContext.currentProxy();
        proxy.bindFriend(userId, friendId);
        proxy.bindFriend(friendId, userId);
        // 推送添加好友提示
        sendAddTipMessage(userId, friendId);
        log.info("添加好友，用户id:{},好友id:{}", userId, friendId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFriend(Long friendId) {
        Long userId = SessionContext.getSession().getUserId();
        // 互相解除好友关系，走代理清理缓存
        FriendServiceImpl proxy = (FriendServiceImpl)AopContext.currentProxy();
        proxy.unbindFriend(userId, friendId);
        proxy.unbindFriend(friendId, userId);
        // 推送解除好友提示
        sendDelTipMessage(friendId);
        // 模拟对方向我推送在线状态
        IMTerminalType.codes().forEach(terminal -> sendOnlineStatus(friendId, userId, terminal));
        log.info("删除好友，用户id:{},好友id:{}", userId, friendId);
    }


    @Override
    public void sendOnlineStatus(Long userId, Integer terminal) {
        List<Long> fids = loadAllFriendIds(userId);
        UserOnlineVO vo = new UserOnlineVO();
        vo.setUserId(userId);
        vo.setTerminal(terminal);
        vo.setOnline(imClient.isOnline(userId, IMTerminalType.fromCode(terminal)));
        // 广播给所有好友
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(userId);
        msgInfo.setType(MessageType.FRIEND_ONLINE.code());
        msgInfo.setContent(JSON.toJSONString(vo));
        IMBatchPrivateMessage<PrivateMessageVO> sendMessage = new IMBatchPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(userId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvIds(fids);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendBatchPrivateMessage((sendMessage));
    }

    @Override
    public void sendOnlineStatus(Long userId, Long friendId, Integer terminal) {
        UserOnlineVO vo = new UserOnlineVO();
        vo.setUserId(userId);
        vo.setTerminal(terminal);
        vo.setOnline(imClient.isOnline(userId, IMTerminalType.fromCode(terminal)));
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(userId);
        msgInfo.setRecvId(friendId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_ONLINE.code());
        msgInfo.setContent(JSON.toJSONString(vo));
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(userId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(friendId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Cacheable(key = "#userId1+':'+#userId2")
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId1);
        wrapper.eq(Friend::getFriendId, userId2);
        wrapper.eq(Friend::getDeleted, false);
        return this.exists(wrapper);
    }

    /**
     * 单向绑定好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void bindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            friend = new Friend();
        }
        friend.setVersion(getNextVersion());
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        User friendInfo = userMapper.selectById(friendId);
        friend.setFriendHeadImage(friendInfo.getHeadImageThumb());
        friend.setFriendNickName(friendInfo.getNickName());
        friend.setDeleted(false);
        this.saveOrUpdate(friend);
        // 推送好友变化信息s
        sendAddFriendMessage(userId, friendId, friend);
    }

    @Override
    public void setDnd(FriendDndDTO dto) {
        UserSession session = SessionContext.getSession();
        LambdaUpdateWrapper<Friend> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.eq(Friend::getFriendId, dto.getFriendId());
        wrapper.set(Friend::getIsDnd, dto.getIsDnd());
        wrapper.set(Friend::getVersion, getNextVersion());
        this.update(wrapper);
        // 推送同步消息
        sendSyncDndMessage(dto.getFriendId(), dto.getIsDnd());
    }

    /**
     * 单向解除好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void unbindFriend(Long userId, Long friendId) {
        // 逻辑删除
        LambdaUpdateWrapper<Friend> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getFriendId, friendId);
        wrapper.set(Friend::getDeleted, true);
        wrapper.set(Friend::getVersion, getNextVersion());
        this.update(wrapper);
        // 推送好友变化信息
        sendDelFriendMessage(userId, friendId);
    }

    @Override
    public FriendVO findFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.eq(Friend::getFriendId, friendId);
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            throw new GlobalException("对方不是您的好友");
        }
        return convert(friend);
    }

    @Override
    public List<UserOnlineVO> findOnlineTerminals() {
        // 查询好友id列表
        List<Long> ids = findFriendIds();
        // 查询在线的好友终端
        Map<Long, List<IMTerminalType>> onlineMap = imClient.getOnlineTerminal(ids);
        // 返回vo
        List<UserOnlineVO> vos = new ArrayList<>();
        onlineMap.forEach((userId, terminals) -> terminals.forEach(terminal -> {
            UserOnlineVO vo = new UserOnlineVO();
            vo.setUserId(userId);
            vo.setTerminal(terminal.code());
            vo.setOnline(true);
            vos.add(vo);
        }));
        return vos;
    }

    @Override
    public Long getNextVersion() {
        String key = StrUtil.join(":", RedisKey.IM_FRIEND_MAX_VERSION);
        if (redisTemplate.hasKey(key)) {
            return redisTemplate.opsForValue().increment(key);
        } else {
            LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
            wrapper.orderByDesc(Friend::getVersion);
            wrapper.last("limit 1");
            Friend friend = this.getOne(wrapper);
            Long version = Objects.isNull(friend) ? 1 : friend.getVersion() + 1;
            redisTemplate.opsForValue().set(key, version);
            return version;
        }
    }

    List<Long> loadAllFriendIds(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getDeleted, false);
        wrapper.select(Friend::getFriendId);
        List<Friend> friends = this.list(wrapper);
        return friends.stream().map(Friend::getFriendId).collect(Collectors.toList());
    }

    private FriendVO convert(Friend f) {
        FriendVO vo = new FriendVO();
        vo.setId(f.getFriendId());
        vo.setHeadImage(f.getFriendHeadImage());
        vo.setNickName(f.getFriendNickName());
        vo.setDeleted(f.getDeleted());
        vo.setIsDnd(f.getIsDnd());
        vo.setVersion(f.getVersion());
        return vo;
    }

    void sendAddFriendMessage(Long userId, Long friendId, Friend friend) {
        // 推送好友状态信息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(friendId);
        msgInfo.setRecvId(userId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_NEW.code());
        FriendVO vo = convert(friend);
        msgInfo.setContent(JSON.toJSONString(vo));
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(userId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendDelFriendMessage(Long userId, Long friendId) {
        // 推送好友状态信息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(friendId);
        msgInfo.setRecvId(userId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_DEL.code());
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(userId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendAddTipMessage(Long userId, Long friendId) {
        UserSession session = SessionContext.getSession();
        PrivateMessage message = new PrivateMessage();
        message.setSendId(session.getUserId());
        message.setRecvId(friendId);
        message.setConvKey(ConvUtil.buildConvKey(message.getSendId(), message.getRecvId()));
        message.setContent("你们已成为好友，现在可以开始聊天了");
        message.setSendTime(new Date());
        message.setStatus(MessageStatus.PENDING.code());
        message.setType(MessageType.TIP_TEXT.code());
        privateMessageService.saveMessage(message);
        // 推给对方
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(message, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 推给自己
        sendMessage.setRecvId(session.getUserId());
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendDelTipMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        // 推送好友状态信息
        PrivateMessage message = new PrivateMessage();
        message.setSendId(session.getUserId());
        message.setRecvId(friendId);
        message.setConvKey(ConvUtil.buildConvKey(message.getSendId(), message.getRecvId()));
        message.setSendTime(new Date());
        message.setType(MessageType.TIP_TEXT.code());
        message.setStatus(MessageStatus.PENDING.code());
        message.setContent("你们的好友关系已被解除");
        privateMessageService.saveMessage(message);
        // 推送
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(message, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(friendId);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendSyncDndMessage(Long friendId, Boolean isDnd) {
        // 同步免打扰状态到其他终端
        UserSession session = SessionContext.getSession();
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRecvId(friendId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_DND.code());
        msgInfo.setContent(isDnd.toString());
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(true);
        imClient.sendPrivateMessage(sendMessage);
    }

}

package com.tyut.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tyut.implatform.dto.FeedCommentDTO;
import com.tyut.implatform.dto.FeedPageDTO;
import com.tyut.implatform.dto.FeedPublishDTO;
import com.tyut.implatform.entity.Feed;
import com.tyut.implatform.entity.FeedComment;
import com.tyut.implatform.entity.FeedLike;
import com.tyut.implatform.exception.GlobalException;
import com.tyut.implatform.mapper.FeedCommentMapper;
import com.tyut.implatform.mapper.FeedLikeMapper;
import com.tyut.implatform.mapper.FeedMapper;
import com.tyut.implatform.service.FeedService;
import com.tyut.implatform.service.FriendService;
import com.tyut.implatform.service.UserService;
import com.tyut.implatform.session.SessionContext;
import com.tyut.implatform.session.UserSession;
import com.tyut.implatform.util.BeanUtils;
import com.tyut.implatform.vo.FeedCommentVO;
import com.tyut.implatform.vo.FeedVO;
import com.tyut.implatform.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl extends ServiceImpl<FeedMapper, Feed> implements FeedService {

    private final FeedLikeMapper feedLikeMapper;
    private final FeedCommentMapper feedCommentMapper;
    private final FriendService friendService;
    private final UserService userService;

    @Override
    public FeedVO publish(FeedPublishDTO dto) {
        UserSession session = SessionContext.getSession();
        Feed feed = new Feed();
        feed.setUserId(session.getUserId());
        feed.setContent(dto.getContent());
        if (CollectionUtil.isNotEmpty(dto.getImages())) {
            feed.setImages(JSON.toJSONString(dto.getImages()));
        }
        feed.setCreatedTime(new Date());
        this.save(feed);
        return buildFeedVO(feed);
    }

    @Override
    public void deleteFeed(Long feedId) {
        UserSession session = SessionContext.getSession();
        Feed feed = this.getById(feedId);
        if (feed == null) {
            throw new GlobalException("动态不存在");
        }
        if (!feed.getUserId().equals(session.getUserId())) {
            throw new GlobalException("只能删除自己的动态");
        }
        this.removeById(feedId);
        feedLikeMapper.delete(Wrappers.lambdaQuery(FeedLike.class).eq(FeedLike::getFeedId, feedId));
        feedCommentMapper.delete(Wrappers.lambdaQuery(FeedComment.class).eq(FeedComment::getFeedId, feedId));
    }

    @Override
    public List<FeedVO> timeline(FeedPageDTO dto) {
        UserSession session = SessionContext.getSession();
        List<Long> friendIds = friendService.findFriendIds();
        List<Long> visibleUserIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(friendIds)) {
            visibleUserIds.addAll(friendIds);
        }
        visibleUserIds.add(session.getUserId());

        LambdaQueryWrapper<Feed> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Feed::getUserId, visibleUserIds);
        wrapper.orderByDesc(Feed::getCreatedTime);
        Page<Feed> page = this.page(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        return page.getRecords().stream().map(this::buildFeedVO).collect(Collectors.toList());
    }

    @Override
    public List<FeedVO> myFeeds(Long userId, FeedPageDTO dto) {
        LambdaQueryWrapper<Feed> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Feed::getUserId, userId);
        wrapper.orderByDesc(Feed::getCreatedTime);
        Page<Feed> page = this.page(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        return page.getRecords().stream().map(this::buildFeedVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean toggleLike(Long feedId) {
        UserSession session = SessionContext.getSession();
        Feed feed = this.getById(feedId);
        if (feed == null) {
            throw new GlobalException("动态不存在");
        }
        LambdaQueryWrapper<FeedLike> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FeedLike::getFeedId, feedId);
        wrapper.eq(FeedLike::getUserId, session.getUserId());
        FeedLike existing = feedLikeMapper.selectOne(wrapper);
        if (existing != null) {
            feedLikeMapper.deleteById(existing.getId());
            return false;
        } else {
            FeedLike like = new FeedLike();
            like.setFeedId(feedId);
            like.setUserId(session.getUserId());
            like.setCreatedTime(new Date());
            feedLikeMapper.insert(like);
            return true;
        }
    }

    @Override
    public FeedCommentVO comment(FeedCommentDTO dto) {
        UserSession session = SessionContext.getSession();
        Feed feed = this.getById(dto.getFeedId());
        if (feed == null) {
            throw new GlobalException("动态不存在");
        }
        FeedComment comment = new FeedComment();
        comment.setFeedId(dto.getFeedId());
        comment.setUserId(session.getUserId());
        comment.setContent(dto.getContent());
        comment.setCreatedTime(new Date());
        feedCommentMapper.insert(comment);

        FeedCommentVO vo = new FeedCommentVO();
        vo.setId(comment.getId());
        vo.setUserId(session.getUserId());
        vo.setNickName(session.getNickName());
        vo.setHeadImage(session.getHeadImage());
        vo.setContent(comment.getContent());
        vo.setCreatedTime(comment.getCreatedTime());
        return vo;
    }

    @Override
    public void deleteComment(Long commentId) {
        UserSession session = SessionContext.getSession();
        FeedComment comment = feedCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new GlobalException("评论不存在");
        }
        Feed feed = this.getById(comment.getFeedId());
        if (!comment.getUserId().equals(session.getUserId())
                && (feed == null || !feed.getUserId().equals(session.getUserId()))) {
            throw new GlobalException("无权删除此评论");
        }
        feedCommentMapper.deleteById(commentId);
    }

    private FeedVO buildFeedVO(Feed feed) {
        UserSession session = SessionContext.getSession();
        FeedVO vo = BeanUtils.copyProperties(feed, FeedVO.class);
        if (feed.getImages() != null && !feed.getImages().isEmpty()) {
            vo.setImages(JSON.parseArray(feed.getImages(), String.class));
        }
        UserVO user = userService.findUserById(feed.getUserId());
        if (user != null) {
            vo.setNickName(user.getNickName());
            vo.setHeadImage(user.getHeadImageThumb());
        }
        Long likeCount = feedLikeMapper.selectCount(
                Wrappers.lambdaQuery(FeedLike.class).eq(FeedLike::getFeedId, feed.getId()));
        vo.setLikeCount(likeCount);
        Long likedCount = feedLikeMapper.selectCount(
                Wrappers.lambdaQuery(FeedLike.class)
                        .eq(FeedLike::getFeedId, feed.getId())
                        .eq(FeedLike::getUserId, session.getUserId()));
        vo.setLiked(likedCount > 0);
        List<FeedLike> recentLikes = feedLikeMapper.selectList(
                Wrappers.lambdaQuery(FeedLike.class)
                        .eq(FeedLike::getFeedId, feed.getId())
                        .orderByDesc(FeedLike::getCreatedTime)
                        .last("limit 10"));
        List<String> likeNames = new ArrayList<>();
        for (FeedLike like : recentLikes) {
            UserVO likeUser = userService.findUserById(like.getUserId());
            if (likeUser != null) {
                likeNames.add(likeUser.getNickName());
            }
        }
        vo.setLikeUserNames(likeNames);
        Long commentCount = feedCommentMapper.selectCount(
                Wrappers.lambdaQuery(FeedComment.class).eq(FeedComment::getFeedId, feed.getId()));
        vo.setCommentCount(commentCount);
        List<FeedComment> recentComments = feedCommentMapper.selectList(
                Wrappers.lambdaQuery(FeedComment.class)
                        .eq(FeedComment::getFeedId, feed.getId())
                        .orderByAsc(FeedComment::getCreatedTime)
                        .last("limit 5"));
        List<FeedCommentVO> commentVOs = new ArrayList<>();
        for (FeedComment c : recentComments) {
            FeedCommentVO cvo = new FeedCommentVO();
            cvo.setId(c.getId());
            cvo.setUserId(c.getUserId());
            cvo.setContent(c.getContent());
            cvo.setCreatedTime(c.getCreatedTime());
            UserVO commentUser = userService.findUserById(c.getUserId());
            if (commentUser != null) {
                cvo.setNickName(commentUser.getNickName());
                cvo.setHeadImage(commentUser.getHeadImageThumb());
            }
            commentVOs.add(cvo);
        }
        vo.setComments(commentVOs);
        return vo;
    }
}

package com.tyut.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tyut.implatform.dto.FeedCommentDTO;
import com.tyut.implatform.dto.FeedPageDTO;
import com.tyut.implatform.dto.FeedPublishDTO;
import com.tyut.implatform.entity.Feed;
import com.tyut.implatform.vo.FeedCommentVO;
import com.tyut.implatform.vo.FeedVO;

import java.util.List;

public interface FeedService extends IService<Feed> {

    FeedVO publish(FeedPublishDTO dto);

    void deleteFeed(Long feedId);

    List<FeedVO> timeline(FeedPageDTO dto);

    List<FeedVO> myFeeds(Long userId, FeedPageDTO dto);

    Boolean toggleLike(Long feedId);

    FeedCommentVO comment(FeedCommentDTO dto);

    void deleteComment(Long commentId);
}

package com.tyut.implatform.controller;

import com.tyut.implatform.dto.FeedCommentDTO;
import com.tyut.implatform.dto.FeedPageDTO;
import com.tyut.implatform.dto.FeedPublishDTO;
import com.tyut.implatform.result.Result;
import com.tyut.implatform.result.ResultUtils;
import com.tyut.implatform.service.FeedService;
import com.tyut.implatform.vo.FeedCommentVO;
import com.tyut.implatform.vo.FeedVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "朋友圈")
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @PostMapping("/publish")
    @Operation(summary = "发布动态")
    public Result<FeedVO> publish(@Valid @RequestBody FeedPublishDTO dto) {
        return ResultUtils.success(feedService.publish(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除动态")
    public Result deleteFeed(@NotNull(message = "动态id不能为空") @PathVariable Long id) {
        feedService.deleteFeed(id);
        return ResultUtils.success();
    }

    @GetMapping("/timeline")
    @Operation(summary = "好友动态时间线")
    public Result<List<FeedVO>> timeline(@Valid FeedPageDTO dto) {
        return ResultUtils.success(feedService.timeline(dto));
    }

    @GetMapping("/my")
    @Operation(summary = "查看某用户的动态")
    public Result<List<FeedVO>> myFeeds(@RequestParam Long userId, @Valid FeedPageDTO dto) {
        return ResultUtils.success(feedService.myFeeds(userId, dto));
    }

    @PostMapping("/like")
    @Operation(summary = "点赞/取消点赞")
    public Result<Boolean> toggleLike(@RequestParam Long feedId) {
        return ResultUtils.success(feedService.toggleLike(feedId));
    }

    @PostMapping("/comment")
    @Operation(summary = "评论动态")
    public Result<FeedCommentVO> comment(@Valid @RequestBody FeedCommentDTO dto) {
        return ResultUtils.success(feedService.comment(dto));
    }

    @DeleteMapping("/comment/{id}")
    @Operation(summary = "删除评论")
    public Result deleteComment(@NotNull(message = "评论id不能为空") @PathVariable Long id) {
        feedService.deleteComment(id);
        return ResultUtils.success();
    }
}

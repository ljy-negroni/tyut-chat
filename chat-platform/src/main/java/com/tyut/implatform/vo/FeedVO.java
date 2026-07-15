package com.tyut.implatform.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FeedVO {

    private Long id;

    private Long userId;

    private String nickName;

    private String headImage;

    private String content;

    private List<String> images;

    private Date createdTime;

    private Boolean liked;

    private Long likeCount;

    private List<String> likeUserNames;

    private Long commentCount;

    private List<FeedCommentVO> comments;
}

package com.tyut.implatform.vo;

import lombok.Data;

import java.util.Date;

@Data
public class FeedCommentVO {

    private Long id;

    private Long userId;

    private String nickName;

    private String headImage;

    private String content;

    private Date createdTime;
}

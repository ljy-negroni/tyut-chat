package com.tyut.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("im_feed_comment")
public class FeedComment {

    private Long id;

    private Long feedId;

    private Long userId;

    private String content;

    private Date createdTime;
}

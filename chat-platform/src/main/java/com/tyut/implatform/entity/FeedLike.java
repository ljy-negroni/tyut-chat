package com.tyut.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("im_feed_like")
public class FeedLike {

    private Long id;

    private Long feedId;

    private Long userId;

    private Date createdTime;
}

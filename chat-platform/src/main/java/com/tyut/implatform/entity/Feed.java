package com.tyut.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("im_feed")
public class Feed {

    private Long id;

    private Long userId;

    private String content;

    private String images;

    private Date createdTime;
}

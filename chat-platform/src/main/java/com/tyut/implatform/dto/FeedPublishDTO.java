package com.tyut.implatform.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class FeedPublishDTO {

    @Size(max = 2000, message = "动态内容不能超过2000字")
    private String content;

    private List<String> images;
}

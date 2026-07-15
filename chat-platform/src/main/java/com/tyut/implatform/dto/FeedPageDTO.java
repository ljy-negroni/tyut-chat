package com.tyut.implatform.dto;

import lombok.Data;

@Data
public class FeedPageDTO {

    private Integer page = 1;

    private Integer size = 10;
}

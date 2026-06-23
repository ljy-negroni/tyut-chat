package com.tyut.implatform.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tyut.implatform.result.Result;
import com.tyut.implatform.result.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "验证码")
@RestController
@RequiredArgsConstructor
public class CaptchaController {

    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TTL = 5; // 分钟

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "返回验证码图片base64和唯一key")
    public Result<Map<String, String>> captcha() {
        // 生成线段干扰的验证码图，宽120高42，4位数字
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 42, 4, 20);
        String code = lineCaptcha.getCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        // 存入 Redis，5 分钟过期
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code, CAPTCHA_TTL, TimeUnit.MINUTES);
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", lineCaptcha.getImageBase64Data());
        return ResultUtils.success(result);
    }
}

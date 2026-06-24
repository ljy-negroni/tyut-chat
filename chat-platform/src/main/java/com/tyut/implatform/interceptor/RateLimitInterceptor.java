package com.tyut.implatform.interceptor;

import com.alibaba.fastjson.JSON;
import com.tyut.implatform.result.Result;
import com.tyut.implatform.result.ResultUtils;
import com.tyut.implatform.enums.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.List;

/**
 * IP 限流拦截器 — 基于 Redis 滑动窗口
 * 防止单 IP 大规模请求攻击
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    /** 每个时间窗口内最大请求数 */
    private static final int MAX_REQUESTS = 30;
    /** 时间窗口（秒） */
    private static final int WINDOW_SECONDS = 1;

    /** 白名单路径（不限制） */
    private static final List<String> WHITE_LIST = List.of(
        "/captcha", "/login", "/register", "/logout", "/refreshToken",
        "/swagger", "/v3/api-docs", "/doc.html", "/favicon.ico",
        "/error", "/actuator"
    );

    /**
     * Redis Lua 脚本：滑动窗口计数
     * KEYS[1] = 限流 key
     * ARGV[1] = 窗口大小（秒）
     * ARGV[2] = 最大请求数
     * 返回 1 表示通过，0 表示超限
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
        RATE_LIMIT_SCRIPT.setScriptText(
            "local key = KEYS[1] " +
            "local window = tonumber(ARGV[1]) " +
            "local max_req = tonumber(ARGV[2]) " +
            "local now = redis.call('TIME') " +
            "local now_ms = tonumber(now[1]) * 1000 + math.floor(tonumber(now[2]) / 1000) " +
            "local window_start = now_ms - window * 1000 " +
            "redis.call('ZREMRANGEBYSCORE', key, 0, window_start) " +
            "local count = redis.call('ZCARD', key) " +
            "if count < max_req then " +
            "  redis.call('ZADD', key, now_ms, now_ms .. '-' .. count) " +
            "  redis.call('EXPIRE', key, window + 1) " +
            "  return 1 " +
            "else " +
            "  return 0 " +
            "end"
        );
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单放行
        for (String white : WHITE_LIST) {
            if (path.startsWith(white)) {
                return true;
            }
        }

        // 静态资源放行
        if (path.contains(".")) {
            return true;
        }

        String ip = getClientIp(request);
        String key = "rate_limit:" + ip;

        Long allowed = redisTemplate.execute(
            RATE_LIMIT_SCRIPT,
            Collections.singletonList(key),
            String.valueOf(WINDOW_SECONDS),
            String.valueOf(MAX_REQUESTS)
        );

        if (allowed != null && allowed == 0) {
            log.warn("限流触发 - IP: {}, 路径: {}", ip, path);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            Result<?> result = ResultUtils.error(ResultCode.PROGRAM_ERROR, "请求过于频繁，请稍后重试");
            response.getWriter().write(JSON.toJSONString(result));
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

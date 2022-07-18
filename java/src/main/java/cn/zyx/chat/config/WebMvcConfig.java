package cn.zyx.chat.config;

import cn.zyx.chat.common.interceptor.JwtInterceptor;
import cn.zyx.chat.common.resolver.AccountHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2020-11-30 0:55
 * @email jieni_cao@foxmail.com
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AccountHandlerMethodArgumentResolver accountHandlerMethodArgumentResolver;

    /**
     * 添加jwt拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求，通过判断是否有 @JwtToken 注解 决定是否需要登录
                .addPathPatterns("/app/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(accountHandlerMethodArgumentResolver);
    }

}

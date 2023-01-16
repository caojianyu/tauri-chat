package cn.zyx.chat.common.interceptor;

import cn.zyx.chat.common.annotation.JwtToken;
import cn.zyx.chat.common.constant.SysConstants;
import cn.zyx.chat.common.exception.RRException;
import cn.zyx.chat.utils.DesUtil;
import cn.zyx.chat.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2020-11-10 23:01
 * @email jieni_cao@foxmail.com
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) {
        // 从http请求头中取出token
        String token = httpServletRequest.getHeader(SysConstants.AUTHORIZATION);
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();

        // 检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(JwtToken.class)) {
            JwtToken jwtToken = method.getAnnotation(JwtToken.class);
            if (jwtToken.required()) {
                // 执行认证
                if (token == null) {
                    throw new RRException("请登录");
                }
                if (jwtUtil.isTokenExpired(jwtUtil.verify(token).getExpiresAt())) {
                    throw new RRException("身份信息已过期，请重新登录");
                }
            }

            if (token != null) {
                // 获取token中的accountId加密串
                String accountId = jwtUtil.getAccountByToken(token);
                // 解密
                accountId = DesUtil.getDecryptString(accountId);
                httpServletRequest.setAttribute(SysConstants.ACCOUNT_KEY, accountId);
            }
        }

        return true;
    }
}

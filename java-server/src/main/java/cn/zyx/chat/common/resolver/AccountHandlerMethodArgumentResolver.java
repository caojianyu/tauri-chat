package cn.zyx.chat.common.resolver;

import cn.zyx.chat.common.annotation.Account;
import cn.zyx.chat.common.constant.SysConstants;
import cn.zyx.chat.entity.UcAccount;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2020-11-11 0:19
 * @email jieni_cao@foxmail.com
 */
@Component
public class AccountHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(UcAccount.class) && parameter.hasParameterAnnotation(Account.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) throws Exception {
        //获取用户
        Object object = request.getAttribute(SysConstants.ACCOUNT_KEY, RequestAttributes.SCOPE_REQUEST);
        if (object == null) {
            return null;
        }
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String accountStr = valueOperations.get(object);
        //获取用户信息
        UcAccount account = JSONObject.parseObject(accountStr, UcAccount.class);
        return account;
    }
}

package cn.zyx.chat.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2020-11-11 0:19
 * @email jieni_cao@foxmail.com
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Account {
}

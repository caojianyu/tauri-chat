package cn.zyx.chat.common.exception;

import cn.zyx.chat.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2020-11-10 23:46
 * @email jieni_cao@foxmail.com
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(RRException.class)
    public R handleException(RRException e) {
        String msg = e.getMessage();
        int code = e.getCode();
        if (msg == null || msg.equals("")) {
            msg = "服务器出错";
        }
        if(code == 100){
            return R.error(code,msg);
        }
        return R.error(msg);
    }
}

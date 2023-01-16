package cn.zyx.chat.utils;

import cn.zyx.chat.common.exception.RRException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2020-11-11 22:24
 * @email jieni_cao@foxmail.com
 */
@ConfigurationProperties(prefix = "zyx.jwt")
@Component
public class JwtUtil {

    private String secret;
    private long expire;

    /**
     * 生成jwt token
     */
    public String generateToken(long accountId) {
        String token = null;
        //HMAC
        try {
            Date date = new Date(System.currentTimeMillis() + expire * 1000);
            //  des 加密
            String uid = DesUtil.getEncryptString(String.valueOf(accountId));
            Algorithm algorithm = Algorithm.HMAC256(secret);
            token = JWT.create()
                    .withIssuer(uid)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            //Invalid Signing configuration / Couldn't convert Claims.
        }
        return token;
    }

    /**
     * 根据token获取账户
     *
     * @param token
     * @return
     */
    public String getAccountByToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getIssuer();
        } catch (JWTDecodeException exception) {
            //Invalid token
        }
        return null;
    }

    public DecodedJWT verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
//                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            throw new RRException("token无效，请重新获取", 100);
        }
    }

    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

}

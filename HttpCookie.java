import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @ClassName HttpCookie
 * @Author 一熹
 * @Date 2023/5/18 11:37
 * @Version 1.8
 **/
// 构造者模式
public class HttpCookie {
    public static void main(String[] args) {
        HttpCookie httpCookie = new CookieBuilder()
                .setKey("detail")
                .setValue("username:origami,password:abc123")
                .setPath("/")
                .setExpireTime(360L * 24 * 60 * 60 * 1000)
                .build();
        String time = httpCookie.toString();
        System.out.println(time);
    }

    @Override
    public String toString() {
        String cookieValue = "";
        if (!Objects.equals(key, null) && !Objects.equals(value, null)) {
            cookieValue += (key + "=" + value + "; ");
        }
        if (!Objects.equals(path, null)) {
            cookieValue += "Path=" + path + "; ";
        }
        if (!Objects.equals(expireTime, null)) {
            cookieValue += "Expires=" + expireTime + "; ";
        }
        return cookieValue;
    }

    private String key;
    private String value;
    private String path;
    private String expireTime;
    private static final byte[] keys = {14, -56, -99, -119, 87, 35, 64, 26, 93, -75, -98, 81, -8, -119, 103, 81, 14, -56, -99, -119, 87, 35, 64, 26};
    private static final SymmetricCrypto aes = SecureUtil.aes(keys);

    //注意无参构造器私有，避免外界使用构造器创建User对象
    private HttpCookie() {
    }

    public static String encryptCookie(String rawCookieValue) {
        return aes.encryptHex(rawCookieValue);
    }

    public static String decryptCookie(String encryptedCookieValue) {
        return aes.decryptStr(encryptedCookieValue);
    }

    public static class CookieBuilder {

        String key;
        String value;
        String path;
        String expireTime;

        public CookieBuilder() {
        }

        public CookieBuilder setKey(String key) {
            this.key = key;
            return this;
        }

        public CookieBuilder setValue(String value) {
            this.value = value;
            return this;
        }

        public CookieBuilder setPath(String path) {
            this.path = path;
            return this;
        }

        public CookieBuilder setExpireTime(long million) {
            this.expireTime = getExpireTime(million);
            return this;
        }

        private String getExpireTime(long millis) {
            // 创建一个Instant对象
            Instant instant = Instant.ofEpochMilli(System.currentTimeMillis() + millis);
            // 创建一个ZonedDateTime对象
            ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);
            // 创建一个DateTimeFormatter对象
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            return formatter.format(zonedDateTime);
        }

        public HttpCookie build() {
            HttpCookie httpCookie = new HttpCookie();
            httpCookie.key = key;
            httpCookie.value = value;
            httpCookie.path = path;
            httpCookie.expireTime = expireTime;
            return httpCookie;
        }
    }
}
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
/**
 * @ClassName test
 * @Author ׂ»לה
 * @Date 2023/5/18 11:56
 * @Version 1.8
 **/
public class test {
    static SymmetricCrypto aes = SecureUtil.aes();
    public static void main(String[] args) {
        testCookie();
    }

    public static void testCookie() {
        String cookieValue = "Hello World!";
        String encryptCookie = HttpCookie.encryptCookie(cookieValue);
        String decryptCookie = HttpCookie.decryptCookie("encryptCookie");
        System.out.println("encryptCookie = " + encryptCookie);
        System.out.println("decryptCookie = " + decryptCookie);
    }
}

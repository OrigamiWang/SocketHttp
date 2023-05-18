package webroot;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @ClassName Util
 * @Author ׂ»לה
 * @Date 2023/5/18 16:19
 * @Version 1.8
 **/
public class FileUtil {

    public static HtmlFile getFileWithoutCookie(FileInputStream fis) throws IOException {
        int len;
        HtmlFile file = new HtmlFile();
        int bufSize = 1024;
        StringBuilder tempFileBuf = new StringBuilder();
        long fileLen = 0;

        byte[] buffer = new byte[bufSize];
        while ((len = fis.read(buffer)) != -1) {
            String bufStr = new String(buffer, 0, len, StandardCharsets.UTF_8);
            fileLen += bufStr.getBytes(StandardCharsets.UTF_8).length;
            tempFileBuf.append(bufStr);
        }
        file.setFileBuf(tempFileBuf);
        file.setFileLen(fileLen);
        return file;
    }

    public static HtmlFile getFileWithCookie(FileInputStream fis, Map<String, String> cookieMap, boolean hasCookie) throws IOException {
        int len;
        HtmlFile file = new HtmlFile();
        int bufSize = 1024;
        StringBuilder tempFileBuf = new StringBuilder();
        long fileLen = 0;

        byte[] buffer = new byte[bufSize];
        String username = cookieMap.getOrDefault("username", "");
        String password = cookieMap.getOrDefault("password", "");
        String[] formatStr = {"{{username}}", "{{password}}"};
        while ((len = fis.read(buffer)) != -1) {
            String bufStr = new String(buffer, 0, len, StandardCharsets.UTF_8);
            if (hasCookie) {
                System.out.println("username = " + username);
                System.out.println("password = " + password);
                for (String str : formatStr) {
                    if (bufStr.contains(str)) {
                        bufStr = bufStr.replace(str, cookieMap.getOrDefault(extractFormatStr(str), ""));
                    }
                }
            }
            fileLen += bufStr.getBytes(StandardCharsets.UTF_8).length;
            tempFileBuf.append(bufStr);

        }
        file.setFileBuf(tempFileBuf);
        file.setFileLen(fileLen);
        return file;
    }

    private static String extractFormatStr(String formatStr) {
        return formatStr.substring(2, formatStr.length() - 2).trim();
    }


}

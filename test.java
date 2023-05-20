import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * @ClassName test
 * @Author 一熹
 * @Date 2023/5/18 11:56
 * @Version 1.8
 **/
public class test {
    public static void main(String[] args) {
        String[] lineArr = new String[]{"<script", "\n", "\n",
                "        type=\"text/javascript\"",
                "        src=\"./login.js\">", "</script>"};
        String line = "";
        int idx = 0;
        StringBuilder sb;
        while (true) {
            if (idx >= lineArr.length) {
                break;
            }
            line = lineArr[idx++];
            if (line.contains("<script")) {
                sb = new StringBuilder();
                sb.append(line);
                System.out.println(line);
                while (true) {
                    if (idx >= lineArr.length || line.contains("</script>")) {
                        break;
                    } else {
                        line = lineArr[idx++];
                        System.out.println(line);
                        // 去除line的空格, \r, \n
                        String replacedLine = line
                                .replace(" ", "")
                                .replace("\n", "");
                        sb.append(replacedLine);
                    }
                }
                // 从sb中获取src
                String processedLine = sb.toString();
                if (processedLine.contains("src=\"")) {
                    String temp = processedLine.substring(processedLine.indexOf("src=\"") + 5);
                    String requestPath = temp.substring(0, temp.indexOf("\""));
                    System.out.println("requestPath = " + requestPath);
                }
            } else {
                System.out.println(line);
            }
        }
    }

}

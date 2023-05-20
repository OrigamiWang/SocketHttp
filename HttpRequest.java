import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName HttpRequest
 * @Author 一熹
 * @Date 2023/5/18 14:54
 * @Version 1.8
 **/
public class HttpRequest {
    public static void main(String[] args) {
        HttpRequest request = new HttpRequestBuilder()
                .setRequestMethod("GET")
                .setRequestPath("/login.html")
                .setHost("127.0.0.1:8001")
                .build();
        System.out.println(request.toString());
    }

    @Override
    public String toString() {
        String response = "";
        String statusLine = requestMethod + " " + requestPath + " " + "HTTP/1.1" + CRLF;
        response += statusLine;
        String accept = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9," +
                "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7" + CRLF;
        response += accept;
        String acceptEncoding = "Accept-Encoding: gzip, deflate, br" + CRLF;
        response += acceptEncoding;
        String acceptLanguage = "Accept-Language: zh-CN,zh;q=0.9" + CRLF;
        response += acceptLanguage;
        String cacheControl = "Cache-Control: no-cache" + CRLF;
        response += cacheControl;
        String connection = "Connection: keep-alive" + CRLF;
        response += connection;
        // cookie
        if (!Objects.equals(cookie, null)) {
            response += (cookie + CRLF);
        }
        response += ("Host: " + host + CRLF);
        String userAgent = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36" + CRLF;
        response += userAgent;
        return response + CRLF;
    }

    private static final String CRLF = "\r\n";
    private String requestMethod;
    private String requestPath;
    private String cookie;
    private String host;

    private HttpRequest() {
    }

    public static class HttpRequestBuilder {
        private String requestMethod;
        private String requestPath;
        private String cookie;
        private String host;
        private static final Map<String, String> mimeMap = new HashMap<>();

        public HttpRequestBuilder() {
            //初始化
            mimeMap.put("html", "text/html");
            mimeMap.put("txt", "text/plain");
            mimeMap.put("xml", "text/xml");
            mimeMap.put("jpg", "image/jpeg");
            mimeMap.put("css", "text/css");
            mimeMap.put("png", "image/apng");
            mimeMap.put("js", "application/x-javascript");
        }

        public static String getContentType(String filePath) {
            // 用文件后缀来判断文件类型
            String fileSuffix = filePath.substring(filePath.lastIndexOf('.') + 1);
            System.out.println("fileSuffix = " + fileSuffix);
            return mimeMap.get(fileSuffix);
        }

        public HttpRequestBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public HttpRequestBuilder setRequestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public HttpRequestBuilder setRequestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }


        public HttpRequestBuilder setCookie(String cookie) {
            this.cookie = cookie;
            return this;
        }


        public HttpRequest build() {
            HttpRequest request = new HttpRequest();
            request.requestPath = requestPath;
            request.requestMethod = requestMethod;
            request.cookie = cookie;
            request.host = host;
            return request;
        }


    }
}

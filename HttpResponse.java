import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName HttpResponse
 * @Author 一熹
 * @Date 2023/5/18 14:56
 * @Version 1.8
 **/
public class HttpResponse {
    public static void main(String[] args) {
        HttpResponse response = new HttpResponse.HttpResponseBuilder()
                .setStatusLine(200)
                .setContentLength(0)
                .setContentType("html")
                .build();
        System.out.println(response.toString());
    }

    private static final String CRLF = "\r\n";
    private String statusLine;
    private String contentLength;
    private String setCookie;
    private String contentType;


    private HttpResponse() {
    }

    @Override
    public String toString() {
        String response = "";
        if (!Objects.equals(statusLine, null)) {
            response += statusLine + CRLF;
        }
        if (!Objects.equals(contentType, null)) {
            response += contentType + CRLF;
        }
        if (!Objects.equals(contentLength, null)) {
            response += contentLength + CRLF;
        }
        if (!Objects.equals(setCookie, null)) {
            response += setCookie + CRLF;
        }
        response += CRLF;
        return response;
    }

    public static class HttpResponseBuilder {
        private String statusLine;
        private String contentLength;
        private String setCookie;
        private String contentType;
        private static final Map<String, String> mimeMap = new HashMap<>();

        public HttpResponseBuilder() {
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

        public HttpResponseBuilder setStatusLine(long statusCode) {
            String statusLine = "";
            if (statusCode == 200) {
                statusLine = "HTTP/1.1 200 OK";
            } else if (statusCode == 404) {
                statusLine = "HTTP/1.1 404 Not Found";
            }
            this.statusLine = statusLine;
            return this;
        }

        public HttpResponseBuilder setContentLength(long contentLength) {
            this.contentLength = "Content-Length: " + contentLength;
            return this;
        }

        public HttpResponseBuilder setSetCookie(String setCookie) {
            this.setCookie = "Set-Cookie: " + setCookie;
            return this;
        }

        public HttpResponseBuilder setContentType(String filePath) {
            //通过文件后缀获取mime类型
            this.contentType = "Content-type: " + mimeMap.get(getSuffix(filePath)) + "; charset=utf-8";
            return this;
        }

        private String getSuffix(String filePath) {
            return filePath.substring(filePath.lastIndexOf('.') + 1);
        }

        public HttpResponse build() {
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.statusLine = statusLine;
            httpResponse.contentLength = contentLength;
            httpResponse.setCookie = setCookie;
            httpResponse.contentType = contentType;
            return httpResponse;
        }

    }
}

import webroot.FileUtil;
import webroot.HtmlFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName HttpServer
 * @Author 一熹
 * @Date 2023/5/17 8:24
 * @Version 1.8
 **/
public class HttpServer {
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        System.out.println("线程池...");
        int port = 8001;
        ServerSocket welcomeSocket;
        try {
            welcomeSocket = new ServerSocket(port);
            System.out.println("HttpServer running on port: " + welcomeSocket.getLocalPort());
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(300);
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("new connection accepted..." + connectionSocket.getInetAddress() + " : " + connectionSocket.getPort());
                try {
                    HttpRequestHandler req = new HttpRequestHandler(connectionSocket);
                    fixedThreadPool.execute(req);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class HttpRequestHandler implements Runnable {
    private static final String CRLF = "\r\n";
    private final Socket socket;
    private final BufferedInputStream bis;
    private final OutputStream bos;
    private static final String fileRoot = "Project3_Socket\\src\\main\\java\\webroot";
    private static final String uploadFileDir = "Project3_Socket\\src\\main\\java\\webroot\\upload_file";

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public HttpRequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.bis = new BufferedInputStream(socket.getInputStream());
        this.bos = new BufferedOutputStream(socket.getOutputStream());
    }


    public void processRequest() throws IOException {
        // 实现通过BufferedInputStream一行一行的读取，每一行最后都有\r\n
        // 只读取第一行获得req method

        String headLine = readReqHeaderLine(bis);
        System.out.println("---- > " + headLine);
        String reqMethod = headLine.split(" ")[0];
        System.out.println("reqMethod = " + reqMethod);
        //这里将请求头全部解析，目的是找到是否存在cookie;cookie不存在，则为""
        switch (reqMethod) {
            case "GET":
                doGet(headLine);
                break;
            case "POST":
                doPost(headLine);
                break;
            case "HEAD":
                doHead();
                break;
            default:
                System.out.println("不支持的请求头");
        }
        try {
            bos.close();
            bis.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> arrToMap(String[] arr) {
        Map<String, String> map = new HashMap<>();
        for (String a : arr) {
            String[] kv = a.split(":");
            map.put(kv[0], kv[1]);
        }
        return map;
    }

    public void doGet(String firstHeadLine) throws IOException {

        // get html
        String pathVariable = firstHeadLine.split(" ")[1];
        String filePath = fileRoot + pathVariable;
        File file = new File(filePath);
        System.out.println("filePath = " + filePath);
        // 文件不存在直接返回404
        if (!file.exists()) {
            String notFoundHtml = fileRoot + "/404.html";
            HtmlFile html404 = FileUtil.getFileWithoutCookie(new FileInputStream(notFoundHtml));
            HttpResponse response = new HttpResponse.HttpResponseBuilder()
                    .setStatusLine(404)
                    .setContentType("html")
                    .setContentLength(html404.getFileLen())
                    .build();
            bos.write(response.toString().getBytes());
            bos.write(html404.getFileBuf().toString().getBytes());
            return;
        }
        // 获取cookie map
        Map<String, String> cookieMap = getCookieMap();
        rtn200(filePath, cookieMap);

    }


    public Map<String, String> getCookieMap() throws IOException {
        Map<String, String> cookieMap = new HashMap<>();
        //get cookie
        while (true) {
            String headerLine = readReqHeaderLine(bis);
            if (headerLine.contains("Cookie")) {
                String encryptedCookieValue = headerLine
                        .substring(getIndex(headerLine, "Cookie: "), headerLine.length() - 2);
                //cookieStr = "username:origami,password:abc123"
                // 借助hutool工具包通过aes解密cookie
                String decryptedCookieValue = HttpCookie.decryptCookie(encryptedCookieValue);
                String cookieStr = decryptedCookieValue.substring(getIndex(decryptedCookieValue, "detail="));

                String[] kvArr = cookieStr.split(";")[0].split(",");

                cookieMap = arrToMap(kvArr);
                break;
            }
            if (headerLine.equals(CRLF)) {
                break;
            }
        }
        return cookieMap;
    }

    public void doPost(String head) throws IOException {
        // 解析head，判断该请求是上传文件，还是表单提交
        System.out.println("head = " + head);
        String pathVariable = head.split(" ")[1];
        // 上传文件: /upload/文件路径
        // 表单提交: /save?username=xxx&password=xxx
        String prefix = getPrefix(pathVariable);
        switch (prefix) {
            case "upload":
                doUpload();
                break;
            case "save":
                doSave(pathVariable);
                break;
            default:
        }

    }

    // 解析头，返回路径中的操作(upload / save)
    public String getPrefix(String head) {
        int left = 1;
        int right = head.length();
        for (int i = 1; i < head.length(); i++) {
            if (head.charAt(i) == '/' || head.charAt(i) == '?') {
                right = i;
                break;
            }
        }
        return head.substring(left, right);
    }

    //表单提交
    public void doSave(String pathVariable) throws IOException {
        System.out.println("执行set-cookie操作...");
        pathVariable = pathVariable.substring(getIndex(pathVariable, "?"));
        String[] kv = pathVariable.split("&");
        Map<String, String> userMap = new HashMap<>();
        for (String s : kv) {
            String[] usernamePassword = s.split("=");
            userMap.put(usernamePassword[0], usernamePassword[1]);
        }
        String username = userMap.get("username");
        String password = userMap.get("password");
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        // 将username和password存入cookie通过response将其存入Set-Cookie中返回
        // 构造cookie
        HttpCookie httpCookie = new HttpCookie.CookieBuilder()
                .setKey("detail")
                .setValue("username:" + username + ",password:" + password)
                .setPath("/")
                .setExpireTime(360L * 24 * 60 * 60 * 1000)
                .build();
        // 借助hutool工具包通过aes加密cookie
        String encryptedCookieValue = HttpCookie.encryptCookie(httpCookie.toString());
        HttpResponse response = new HttpResponse.HttpResponseBuilder()
                .setStatusLine(200)
                .setSetCookie(encryptedCookieValue)
                .build();
        bos.write(response.toString().getBytes(StandardCharsets.UTF_8));
        bos.flush();
    }

    // 上传文件
    public void doUpload() throws IOException {
        // String boundary = "";
        long contentLength = 0;
        // 找到boundary，文件的开头
        String fileName = "";
        boolean flag = false;
        while (bis.available() > 0) {
            String headerLine = readReqHeaderLine(bis);
            System.out.println(headerLine);
            //     boundary用于区分req head 和 req body， 但我觉得不如直接判断CRLF来的好用
            //     if (headerLine.contains("boundary")) {
            //         boundary = headerLine.substring(headerLine.indexOf("boundary=") + "boundary=".length());
            //         continue;
            //     }
            if (headerLine.contains("Content-Length")) {
                contentLength = Long.parseLong(headerLine.substring(getIndex(headerLine, "Content-Length: "), headerLine.length() - 2));
                continue;
            }
            if (headerLine.contains("filename")) {
                fileName = headerLine.substring(getIndex(headerLine, "filename=\""), headerLine.length() - 3);
                flag = true;
                continue;
            }
            //     if (headerLine.equals("--" + boundary)) {
            //         continue;
            //     }
            if (headerLine.equals(CRLF) && flag) {
                // 文件开始
                uploadFile(contentLength, fileName);
                break;
            }
        }

    }

    public void uploadFile(long contentLength, String fileName) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(uploadFileDir + "/" + fileName));
        int len = 1024;
        byte[] buffer = new byte[len];
        while (contentLength >= len) {
            contentLength -= len;
            bis.read(buffer);
            bos.write(buffer, 0, len);
        }
        bis.read(buffer, 0, (int) contentLength);
        bos.write(buffer, 0, (int) contentLength);
        bos.flush();

    }

    public int getIndex(String headerLine, String content) {
        return headerLine.indexOf(content) + content.length();
    }

    public void doHead() throws IOException {
        HttpResponse headResponse = new HttpResponse.HttpResponseBuilder()
                .setStatusLine(200)
                .setContentLength(0)
                .setContentType("html")
                .build();
        bos.write(headResponse.toString().getBytes(StandardCharsets.UTF_8));
        bos.flush();
    }

    public String readReqHeaderLine(BufferedInputStream bis) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[1];
        while (true) {
            bis.read(buf);
            String bufStr = new String(buf, 0, 1);
            sb.append(bufStr);
            if (bufStr.equals("\r")) {
                // 读取\n
                bis.read(buf);
                sb.append("\n");
                break;
            }
        }
        return sb.toString();
    }

    public void rtn200(String filePath, Map<String, String> cookieMap) throws IOException {
        boolean hasCookie = false;
        FileInputStream fis;

        String contentType = new HttpResponse.HttpResponseBuilder().getContentType(filePath);
        if (contentType.equals("text/html")) {
            hasCookie = true;
        }
        fis = new FileInputStream(filePath);
        // 由于cookie的值会传给html从而改变html的内容，即html页面的长度无法确定。因此先通过getFile确定渲染后的html的长度，并将内容存入fileBuilder
        // 但是这样操作会影响图片，因此图片单独处理


        if (!contentType.equals("image/apng") && !contentType.equals("image/jpeg")) {
            // 如果文件不是png图片
            HtmlFile cookieFile = FileUtil.getFileWithCookie(fis, cookieMap, hasCookie);
            HttpResponse response = new HttpResponse.HttpResponseBuilder()
                    .setStatusLine(200)
                    .setContentType(filePath)
                    .setContentLength(cookieFile.getFileLen())
                    .build();

            bos.write(response.toString().getBytes(StandardCharsets.UTF_8));
            writeFile(cookieFile.getFileBuf());
        } else {
            // 如果文件是png图片
            HttpResponse response = new HttpResponse.HttpResponseBuilder()
                    .setStatusLine(200)
                    .setContentType(filePath)
                    .setContentLength(fis.available())
                    .build();
            bos.write(response.toString().getBytes(StandardCharsets.UTF_8));
            writeImg(fis);
        }

        bos.flush();
    }

    public void writeImg(FileInputStream fis) throws IOException {
        int sumLen = fis.available();
        int bufLen = 1024;
        byte[] buffer = new byte[bufLen];
        while (bufLen <= sumLen) {
            sumLen -= bufLen;
            fis.read(buffer, 0, bufLen);
            bos.write(buffer, 0, bufLen);
        }
        fis.read(buffer, 0, sumLen);
        bos.write(buffer);
        bos.flush();
    }

    public void writeFile(StringBuilder fileBuilder) throws IOException {
        byte[] fileByte = fileBuilder.toString().getBytes(StandardCharsets.UTF_8);
        bos.write(fileByte);
    }

}

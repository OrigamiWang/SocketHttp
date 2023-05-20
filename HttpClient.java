import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName HttpClient
 * @Author 一熹
 * @Date 2023/5/19 11:02
 * @Version 1.8
 **/
public class HttpClient {
    public static void main(String[] args) throws IOException {
        int clientNum = 1;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(300);
        for (int i = 0; i < clientNum; i++) {
            HttpClientHandler client = new HttpClientHandler();
            client.init("127.0.0.1", 8001, "GET", "./login.html");
            fixedThreadPool.execute(client);
        }
        // 关闭线程池，否则会阻塞
        fixedThreadPool.shutdown();
    }
}

class HttpClientHandler implements Runnable {

    private String ip;
    private int port;
    private String requestPath;
    private String requestMethod;

    public void init(String ip, int port, String requestMethod, String requestPath) {
        this.ip = ip;
        this.port = port;
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
    }


    @Override
    public void run() {
        try {
            generateRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateRequest() throws IOException {
        // 整体步骤：先对html发出请求，再查看html中引用的css、js、img等，再对每个分别发送请求
        System.out.println("http client...");
        Socket socket = new Socket(ip, port);
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        // html
        HttpRequest request = constructRequest(ip, port, requestMethod, requestPath);
        String reqHead = request.toString();
        out.println(reqHead);
        List<String> requestPathList = processHtml(br);
        System.out.println(requestPathList);
        br.close();
        out.close();
        socket.close();
        requestPathList.forEach(requestPath -> {
            System.out.println(requestPath);
            try {
                Socket socket2 = new Socket(ip, port);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
                PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
                HttpRequest request2 = constructRequest(ip, port, requestMethod, requestPath);
                String reqHead2 = request2.toString();
                out2.println(reqHead2);
                getResponse(br2);

                br2.close();
                out2.close();
                socket2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

    public HttpRequest constructRequest(String ip, int port, String requestMethod, String requestPath) {
        return new HttpRequest.HttpRequestBuilder()
                .setRequestMethod(requestMethod)
                .setRequestPath(requestPath)
                .setHost(ip + ":" + port)
                .build();
    }

    public void getResponse(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

    public List<String> processHtml(BufferedReader br) throws IOException {
        List<String> requestPathList = new ArrayList<>();
        String line;
        StringBuilder sb;
        while ((line = br.readLine()) != null) {
            if (line.contains("<script")) {
                sb = new StringBuilder();
                sb.append(line);
                System.out.println(line);
                while (true) {
                    if (line.contains("</script>")) {
                        break;
                    } else {
                        line = br.readLine();
                        if (line == null) {
                            break;
                        }
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
                    requestPathList.add(requestPath);
                }
            } else if (line.contains("<link")) {
                sb = new StringBuilder();
                sb.append(line);
                System.out.println(line);
                while (true) {
                    if (line.contains(">")) {
                        break;
                    } else {
                        line = br.readLine();
                        if (line == null) {
                            break;
                        }
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
                if (processedLine.contains("href=\"")) {
                    String temp = processedLine.substring(processedLine.indexOf("href=\"") + 6);
                    String requestPath = temp.substring(0, temp.indexOf("\""));
                    requestPathList.add(requestPath);
                }
            } else {
                System.out.println(line);
            }
        }
        return requestPathList;
    }
}

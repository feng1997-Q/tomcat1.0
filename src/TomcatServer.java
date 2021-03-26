import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.StringTokenizer;

public class TomcatServer {

    private final static int PORT = 8888;

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(PORT);//根据端口号启动一个serverSocket
            ServletHandler servletHandler=new ServletHandler(server);
            servletHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private static class ServletHandler extends Thread{
        ServerSocket server=null;
        public ServletHandler(ServerSocket server){
            this.server=server;
        }


        @Override
        public void run() {
            while (true) {
                try {
                    Socket client = null;
                    client = server.accept();//ServerSocket阻塞等待客户端请求数据
                    if (client != null) {
                        try {
                            System.out.println("接收到一个客户端的请求");

                            //根据客户端的Socket对象获取输入流对象。
                            //封装字节流到字符流
                            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                            String line = reader.readLine();

                            //拆分http请求路径，取http需要请求的资源完整路径
                            String resource = line.substring(line.indexOf('/'),line.lastIndexOf('/') - 5);

                            resource = URLDecoder.decode(resource, "UTF-8");

                            //获取到这次请求的方法类型，比如get或post请求
                            String method = new StringTokenizer(line).nextElement().toString();

                            //继续循环读取浏览器客户端发出的一行一行的数据
                            while ((line = reader.readLine()) != null) {
                                if (line.equals("")) {//当line等于空行的时候标志Header消息结束
                                    break;
                                }
                            }

                            //如果是POST的请求，直接打印POST提交上来的数据
                            if ("post".equals(method.toLowerCase())) {
                                System.out.println("the post request body is: "
                                        + reader.readLine());
                            }else if("get".equals(method.toLowerCase())){
                                //判断是get类型的http请求处理
                                //根据http请求的资源后缀名来确定返回数据
                                String s = resource.substring(resource.indexOf("/")+1,resource.indexOf("?"));
                                int a = Integer.parseInt(resource.substring(resource.indexOf("?") + 1, resource.indexOf("&")).substring(2));
                                int b = Integer.parseInt(resource.substring(resource.lastIndexOf("&")+1).substring(2));
                                if (s.equals("add")) {
                                    PrintStream writer = new PrintStream(client.getOutputStream(), true);
                                    writer.println("HTTP/1.0 200 OK");// 返回应答消息,并结束应答
                                    writer.println("Content-Type:text/html;charset=utf-8");
                                    writer.println();

                                    int sum = a+b;
                                    writer.println(sum);

                                    writer.println();
                                    writer.close();
                                    closeSocket(client);
                                    continue;
                                }else if (s.equals("mult")) {
                                    PrintStream writer = new PrintStream(client.getOutputStream(), true);
                                    writer.println("HTTP/1.0 200 OK");// 返回应答消息,并结束应答
                                    writer.println("Content-Type:text/html;charset=utf-8");
                                    writer.println();

                                    int sum = a*b;
                                    writer.println(sum);

                                    writer.println();
                                    writer.close();
                                    closeSocket(client);
                                    continue;
                                }
                            }



                        } catch (Exception e) {
                            System.out.println("HTTP服务器错误:"
                                    + e.getLocalizedMessage());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void closeSocket(Socket socket) {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println(socket + "离开了HTTP服务器");
        }

    }

}

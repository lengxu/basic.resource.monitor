package cn.uyun.serviet.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wuhan
 * @date 2019-06-03
 */

public class ResourceServiceImpl {
    public static void main(String[] args) {
        System.out.println(checkServerOnline("10.40.132.21"));
    }

    private static final int port = 22;

    public static boolean checkServerOnline(String ip){
        return pingCmd(ip, 5, 1) && telnetCmd(ip);
    }

    public static boolean pingCmd(String ip, int num, int timeOut){
        long startTime = System.currentTimeMillis();
        boolean result = false;
        String command = "ping " + ip + " -n " + num + " -w " + timeOut;
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            String line;
            int checkResult = 0;
            while((line = in.readLine()) != null){
                checkResult = getCheckResult(line);
            }
            result = (checkResult == num);
        }catch (Exception e){
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("耗时：" + (endTime - startTime));
        return result;
    }

    private static int getCheckResult(String line) {
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",    Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            return 1;
        }
        return 0;
    }


    public static boolean telnetCmd(String ip){
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port), 10);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(socket.isConnected()){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}

package section01;

import java.io.*;
import java.net.*;

public class QuizClient {
    private static String host = "localhost";
    private static int port = 1234;

    public static void main(String[] args) {
        loadServerInfo();

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("서버에 연결되었습니다. 퀴즈를 시작합니다!");

            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse);

                if (serverResponse.startsWith("문제:")) {
                    System.out.print("답변: ");
                    String answer = console.readLine();
                    if (answer != null) {
                        out.println(answer);
                    } else {
                        System.out.println("답변을 입력할 수 없습니다. 연결이 종료되었습니다.");
                        break;
                    }
                }
            }
            System.out.println("퀴즈가 종료되었습니다.");
        } catch (UnknownHostException e) {
            System.out.println("알 수 없는 호스트: " + host);
        } catch (IOException e) {
            System.out.println("입출력 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private static void loadServerInfo() {
        File configFile = new File("server_info.txt");
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line = reader.readLine();
                if (line != null && line.contains(":")) {
                    String[] config = line.split(":");
                    if (config.length == 2) {
                        host = config[0].trim();
                        port = Integer.parseInt(config[1].trim());
                        System.out.println("서버 정보: " + host + ":" + port);
                    } else {
                        System.out.println("server_info.txt 파일 형식 오류: IP:포트 형식이어야 합니다. 기본 설정 사용: localhost:1234");
                        host = "localhost";
                        port = 1234;
                    }
                } else {
                    System.out.println("server_info.txt 파일 내용 오류: IP:포트 형식이어야 합니다. 기본 설정 사용: localhost:1234");
                    host = "localhost";
                    port = 1234;
                }
            } catch (IOException e) {
                System.out.println("server_info.txt 파일을 읽는 중 오류가 발생했습니다. 기본 설정 사용: localhost:1234");
                host = "localhost";
                port = 1234;
            } catch (NumberFormatException e) {
                System.out.println("포트 번호 형식이 잘못되었습니다. 기본 포트를 사용합니다: 1234");
                host = "localhost";
                port = 1234;
            }
        } else {
            System.out.println("server_info.txt 파일을 찾을 수 없습니다. 기본 설정 사용: localhost:1234");
            host = "localhost";
            port = 1234;
        }
    }
}

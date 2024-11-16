package section01;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizServer {
    private static final List<Question> questions = new ArrayList<>();
    private static String host = "localhost";
    private static int port = 1234;
    private static final int MAX_CLIENTS = 10;

    public static void main(String[] args) {
        loadServerInfo();
        loadQuestions();
        ExecutorService clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host))) {
            System.out.println("퀴즈 서버가 " + host + ":" + port + "에서 실행 중입니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientThreadPool.submit(new ClientHandler(clientSocket, questions));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientThreadPool.shutdown();
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
                    }
                } else {
                    System.out.println("server_info.txt 파일 내용 오류: IP:포트 형식이어야 합니다. 기본 설정 사용: localhost:1234");
                }
            } catch (IOException e) {
                System.out.println("server_info.txt 파일을 읽는 중 오류가 발생했습니다. 기본 설정 사용: localhost:1234");
            } catch (NumberFormatException e) {
                System.out.println("포트 번호 형식이 잘못되었습니다. 기본 포트를 사용합니다: 1234");
            }
        } else {
            System.out.println("server_info.txt 파일을 찾을 수 없습니다. 기본 설정 사용: localhost:1234");
        }
    }

    private static void loadQuestions() {
        questions.add(new Question("한국의 수도는 어디인가?", "서울"));
        questions.add(new Question("3 + 5는 얼마인가?", "8"));
        questions.add(new Question("지구에서 가장 큰 대양은 무엇인가?", "태평양"));
        questions.add(new Question("10 - 4는 얼마인가?", "6"));
        questions.add(new Question("5 * 3는 얼마인가?", "15"));
    }
}

class Question {
    private final String question;
    private final String answer;

    public Question(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final List<Question> questions;

    public ClientHandler(Socket socket, List<Question> questions) {
        this.clientSocket = socket;
        this.questions = questions;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            int score = 0;
            for (Question question : questions) {
                out.println("문제: " + question.getQuestion());
                String response = in.readLine();
                if (response != null && response.equalsIgnoreCase(question.getAnswer())) {
                    out.println("정답입니다!");
                    score += 20;
                } else {
                    out.println("오답입니다. 정답은 " + question.getAnswer() + "입니다.");
                }
            }
            out.println("퀴즈 완료! 총점: " + score + "점");
            System.out.println("클라이언트와의 연결이 종료되었습니다. 점수: " + score);
        } catch (IOException e) {
            System.out.println("클라이언트 연결 오류: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

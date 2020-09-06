import org.apache.commons.codec.binary.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class Second {
    private static DataOutputStream dos;
    private static String user;
    private static String pass;
    private static BufferedReader consoleReader;
    private static AtomicBoolean success = new AtomicBoolean(true);
    private static boolean loginSuccess = false;

    public static void main(String[] args) throws Exception {
        consoleReader = new BufferedReader(new InputStreamReader(System.in));
        makeConnection();
        while(!loginSuccess) {
            getCredentials();
            login();
        }

        String repeat = "yes";
        while (repeat.equals("yes")) {
            sendMail();
            System.out.println("Email was successfully sent! Do you want so send another email? [yes/no]");
            repeat = consoleReader.readLine();
        }
        send("QUIT\r\n");
        Thread.sleep(2000);
        System.exit(0);
    }

    private static void makeConnection() throws Exception {
        SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault()).createSocket("smtp.gmail.com", 465);
        final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        (new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("SERVER: " + line);
                    if (line.matches("535.+")) {
                        success.set(false);
                        System.out.println("Seems like you credentials are not correct. Try again!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
        dos = new DataOutputStream(sock.getOutputStream());
        send("EHLO smtp.gmail.com\r\n");
    }

    private static void getCredentials() throws Exception {
        System.out.println("NOTE: in order to login to gmail, you should have allow less secure apps option on!");
        System.out.println("Enter your username (e.g. test@gmail.com): ");
        user = consoleReader.readLine();
        System.out.println("Enter your password: ");
        pass = consoleReader.readLine();
    }

    private static void login() throws Exception {
        String username = Base64.encodeBase64String(user.getBytes(StandardCharsets.UTF_8));
        String password = Base64.encodeBase64String(pass.getBytes(StandardCharsets.UTF_8));

        send("AUTH LOGIN\r\n");
        send(username + "\r\n");
        send(password + "\r\n");
        loginSuccess = success.get();
    }

    private static void sendMail() throws Exception {
        System.out.println("Enter email address you want to send an email to: ");
        String recipient = consoleReader.readLine();
        send("MAIL FROM:<" + user + ">\r\n");
        send("RCPT TO:<" + recipient + ">\r\n");
        send("DATA\r\n");
        send("Subject: Does it work?\r\n");
        send("It works!\r\n");
        send(".\r\n");
    }

    private static void send(String s) throws Exception {
        dos.writeBytes(s);
        System.out.println("CLIENT: " + s);
        Thread.sleep(1000);
    }
}

package com.xmum.forum;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SubServerDB {

    private static List<String> posts = new ArrayList<String>();
    private static JTextArea textArea;

    public static void main(String[] args) {
        int port = 12345;

        // 启动 Swing UI 线程
        SwingUtilities.invokeLater(() -> createAndShowGUI());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);



            while (true) {
                try (Socket socket = serverSocket.accept();
                     ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

                    // 读取客户端发送的 List<String>
                    List<String> receivedList = (List<String>) objectInputStream.readObject();
                    System.out.println("Received list: " + receivedList);

                    synchronized (posts) {
                        posts = receivedList;
                        updateTextArea();
                        broadcast();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Posts Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    private static void updateTextArea() {
        SwingUtilities.invokeLater(() -> {
            textArea.setText("");  // 清空当前内容
            for (String post : posts) {
                textArea.append(post + "\n");
            }
        });
    }

    private static void broadcast() {
        String host = "localhost";
        int[] ports = {12346, 12347}; // 添加更多端口号以发送数据

        for (int port : ports) {
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

                // 创建并发送 List<String>
                objectOutputStream.writeObject(posts);
                objectOutputStream.flush();

                System.out.println("Data sent successfully to port " + port);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

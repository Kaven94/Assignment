package com.xmum.forum;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class ServerApplication extends JFrame {

    private JTextArea logArea;
    private static ServerApplication instance;
    private static  List<String> posts = new ArrayList<>();

    public ServerApplication() {
        setTitle("Server Logs");
        setSize(1200, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Simhei", Font.PLAIN, 36));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        JButton clearButton = new JButton("Clear Logs");
        clearButton.setPreferredSize(new Dimension(240, 60));
        clearButton.setFont(new Font("Microsoft Yahei", Font.BOLD, 36));
        clearButton.setBackground(Color.BLACK);
        clearButton.setForeground(Color.CYAN);
        clearButton.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
        clearButton.setFocusPainted(false);
        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                clearButton.setBackground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                clearButton.setBackground(Color.BLACK);
            }
        });
        clearButton.addActionListener(e -> logArea.setText(""));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            instance = new ServerApplication();
            instance.setVisible(true);
        });

        while (instance == null) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            instance.log("Server started, waiting for connections...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (Exception e) {
            instance.log("Server error: " + e.getMessage());
            e.printStackTrace();
        }

      /*  try (ServerSocket serverSocketBroadcast = new ServerSocket(8081)) {
            instance.log("Server started, waiting for connections...");
            while (true) {
                Socket socket = serverSocketBroadcast.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (Exception e) {
            instance.log("Server error: " + e.getMessage());
            e.printStackTrace();
        }*/
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof ScheduledPost) {
                        ScheduledPost scheduledPost = (ScheduledPost) obj;
                        out.writeObject("Success");

                        // 使用 SwingWorker 在后台线程中处理 sleep 操作
                        long delay = scheduledPost.getScheduledTime();
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                sleep(delay);
                                return null;
                            }

                            @Override
                            protected void done() {
                                // 获取当前时间
                                instance.log("Scheduled post received: " + scheduledPost.getContent());
                                synchronized (posts) {
                                    posts.add(scheduledPost.getContent());
                                }
                                instance.broadcast();
                            }
                        }.execute();
                    } else if (obj instanceof Post) {
                        Post post = (Post) obj;
                        instance.log("Received post: " + post.getContent());
                        synchronized (posts) {
                            posts.add(post.getContent());
                        }
                        out.writeObject("Success");
                        instance.broadcast();
                    } else {
                        out.writeObject("Failure");
                        instance.log("Failed to process object");
                    }
                }
            } catch (Exception e) {
                instance.log("");
            }
                try {
                    socket.close();
                } catch (Exception e) {
                    instance.log("Error closing socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private void broadcast() {
            String host = "localhost";
            int port = 12345;


            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {

                // 创建并发送 List<String>
                objectOutputStream.writeObject(posts);
                objectOutputStream.flush();

                // 接收服务器返回的 List<String>
                List<String> receivedList = (List<String>) objectInputStream.readObject();
                System.out.println("Received list from server: " + receivedList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}


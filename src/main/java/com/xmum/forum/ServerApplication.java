package com.xmum.forum;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerApplication extends JFrame {

    private JTextArea logArea;
    private static List<ScheduledPost> scheduledPosts = new ArrayList<>();
    private static ServerApplication instance;

    public ServerApplication() {
        setTitle("Server Logs");
        setSize(1200, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        // Additional button to clear logs
        JButton clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(e -> logArea.setText(""));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        // Ensure the instance is created and visible before starting the server socket
        SwingUtilities.invokeLater(() -> {
            instance = new ServerApplication();
            instance.setVisible(true);
        });

        // Wait until instance is initialized
        while (instance == null) {
            try {
                Thread.sleep(100);
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
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                Object obj = in.readObject();
                if (obj instanceof ScheduledPost) {
                    ScheduledPost scheduledPost = (ScheduledPost) obj;
                    scheduledPosts.add(scheduledPost);
                    new Thread(new ScheduledPostPublisher(scheduledPost)).start();
                    out.writeObject("Success");
                    instance.log("Scheduled post received: " + scheduledPost.getContent());
                } else if (obj instanceof Post) {
                    Post post = (Post) obj;
                    instance.log("Received post: " + post.getContent());
                    out.writeObject("Success");
                } else {
                    out.writeObject("Failure");
                    instance.log("Failed to process object");
                }
            } catch (Exception e) {
                instance.log("Client handler error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static class ScheduledPostPublisher implements Runnable {
        private ScheduledPost scheduledPost;

        public ScheduledPostPublisher(ScheduledPost scheduledPost) {
            this.scheduledPost = scheduledPost;
        }

        @Override
        public void run() {
            try {
                long delay = scheduledPost.getScheduledTime() - System.currentTimeMillis();
                if (delay > 0) {
                    Thread.sleep(delay);
                }
                instance.log("Publishing scheduled post: " + scheduledPost.getContent());
                // Handle publishing post to the database
            } catch (InterruptedException e) {
                instance.log("Scheduled post publishing interrupted: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

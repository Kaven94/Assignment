package com.xmum.forum;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static java.lang.Thread.sleep;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientApplication extends JFrame {

    private JTextArea postContent;
    private JButton postButton;
    private JButton scheduleButton;
    private JTextField scheduleTime;
    private JTextArea displayArea;
    private JTextArea allPostsArea;


    public ClientApplication(long threadId) {

        setTitle("XMUM Forum - User " + threadId);
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        postContent = new JTextArea(5, 30);
        postButton = new JButton("Post");
        scheduleButton = new JButton("Schedule");
        scheduleTime = new JTextField("Enter time in ms", 15);
        displayArea = new JTextArea(5, 40);
        displayArea.setEditable(false);
        allPostsArea = new JTextArea(25,40);
        displayArea.setEditable(false);

        postButton.addActionListener(new PostButtonListener());
        scheduleButton.addActionListener(new ScheduleButtonListener());

        // Add focus listener to scheduleTime
        scheduleTime.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (scheduleTime.getText().equals("Enter time in ms")) {
                    scheduleTime.setText("");
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (scheduleTime.getText().isEmpty()) {
                    scheduleTime.setText("Enter time in ms");
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Create Post"));
        inputPanel.add(new JScrollPane(postContent), BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(postButton);
        controlPanel.add(scheduleTime);
        controlPanel.add(scheduleButton);

        inputPanel.add(controlPanel, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.NORTH);

        add(new JScrollPane(displayArea), BorderLayout.CENTER);


        // 添加用于显示所有帖子区域
        JPanel allPostsPanel = new JPanel(new BorderLayout());
        allPostsPanel.setBorder(BorderFactory.createTitledBorder("All Posts"));
        allPostsPanel.add(new JScrollPane(allPostsArea), BorderLayout.CENTER);
        add(allPostsPanel, BorderLayout.SOUTH);

        // 启动一个线程来接收来自服务器的帖子
        new Thread(new ReceivePosts()).start();
    }

    private class PostButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String content = postContent.getText();
            sendPost(content, null);
        }
    }

    private class ScheduleButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String content = postContent.getText();
            String timeText = scheduleTime.getText();

            // 验证 scheduleTime 是否为空或无效
            if (timeText == null || timeText.trim().isEmpty() || !timeText.matches("\\d+")) {
                JOptionPane.showMessageDialog(null, "Please enter a valid time in milliseconds.", "Invalid Time", JOptionPane.ERROR_MESSAGE);
            }

            sendPost(content, timeText);
        }
    }

    private void sendPost(String content, String scheduleTime) {

        // 验证 content 是否为空或只有空白字符
        if (content == null || content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter post content.", "Empty Content", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Post post;
            if (scheduleTime == null) {
                scheduleTime = "0";
                post = new Post(content);
            } else {
                post = new ScheduledPost(content, Long.parseLong(scheduleTime));
            }

            out.writeObject(post);


            // 显示发送成功提示
            String response = (String) in.readObject();
            if (response.equals("Success")) {
                JOptionPane.showMessageDialog(this, "Post sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                sleep(Long.parseLong(scheduleTime));

                // 获取当前时间
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedNow = now.format(formatter);


                displayArea.append(formattedNow + " >> " + content + "\n");

            } else {
                JOptionPane.showMessageDialog(this, "Failed to send post!", "Error", JOptionPane.ERROR_MESSAGE);
            }



        } catch (Exception ex) {
            ex.printStackTrace();
            // 显示发送失败提示
            JOptionPane.showMessageDialog(this, "Failed to send post!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ReceivePosts implements Runnable {
        @Override
        public void run() {
            try (Socket socket = new Socket("localhost", 8080);
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                while (true) {
                    Post post = (Post) in.readObject();
                    SwingUtilities.invokeLater(() -> {
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                        String formattedNow = now.format(formatter);
                        allPostsArea.append(formattedNow + " >> " + post.getContent() + "\n");
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        final ExecutorService executorService = Executors.newFixedThreadPool(5); // 创建线程池

        executorService.submit(() -> {
            long threadId = Thread.currentThread().getId();
            SwingUtilities.invokeLater(() -> {
                ClientApplication app1 = new ClientApplication(threadId);
                app1.setVisible(true);
            });
        });

        executorService.submit(() -> {
            long threadId = Thread.currentThread().getId();
            SwingUtilities.invokeLater(() -> {
                ClientApplication app2 = new ClientApplication(threadId);
                app2.setVisible(true);
            });
        });

        executorService.shutdown();
    }
}

package com.xmum.forum;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class ClientApplication extends JFrame {

    private final long threadId;
    private JTextArea postContent;
    private JButton postButton;
    private JButton scheduleButton;
    private JTextField scheduleTime;
    private JTextArea displayArea;
    private JTextArea allPostsArea;
    private static List<String> posts = new ArrayList<>();
    private static final Lock lock = new ReentrantLock(); // 添加锁

    public ClientApplication(long threadId, int receivePort) {
        this.threadId = threadId;

        setTitle("XMUM Forum - User " + threadId);
        setSize(1600, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel userInfomation = new JLabel("Current User: User " + threadId);
        userInfomation.setFont(new Font("Arial", Font.BOLD, 24));
        userInfomation.setForeground(Color.RED);

        postContent = new JTextArea(5, 30);
        postContent.setFont(new Font("Arial", Font.PLAIN, 24));
        postContent.setBackground(new Color(135,206,250));
        postContent.setLineWrap(true);

        postButton = new JButton("Post");
        postButton.setPreferredSize(new Dimension(80, 40));
        postButton.setFont(new Font("Arial", Font.BOLD, 20));
        postButton.setBackground(Color.LIGHT_GRAY);
        postButton.setForeground(Color.BLACK);
        postButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        postButton.setFocusPainted(false);
        postButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                postButton.setBackground(Color.GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                postButton.setBackground(Color.LIGHT_GRAY);
            }
        });

        scheduleButton = new JButton("Schedule");
        scheduleButton.setPreferredSize(new Dimension(120, 40));
        scheduleButton.setFont(new Font("Arial", Font.BOLD, 20));
        scheduleButton.setBackground(Color.LIGHT_GRAY);
        scheduleButton.setForeground(Color.BLACK);
        scheduleButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        scheduleButton.setFocusPainted(false);
        scheduleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                scheduleButton.setBackground(Color.GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                scheduleButton.setBackground(Color.LIGHT_GRAY);
            }
        });

        scheduleTime = new JTextField(" Enter time in ms", 10);
        scheduleTime.setFont(new Font("Arial", Font.BOLD, 18));
        scheduleTime.setPreferredSize(new Dimension(200, 30));
        scheduleTime.setBackground(Color.WHITE);
        scheduleTime.setForeground(Color.BLACK);
        scheduleTime.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));


        displayArea = new JTextArea(15, 40);
        displayArea.setFont(new Font("Arial", Font.PLAIN, 24));
        displayArea.setBackground(new Color(144,238,144));
        displayArea.setLineWrap(true);
        displayArea.setEditable(false);

        allPostsArea = new JTextArea(25,40);
        allPostsArea.setFont(new Font("Arial", Font.PLAIN, 24));
        allPostsArea.setBackground(new Color(144,238,144));
        allPostsArea.setLineWrap(true);
        allPostsArea.setEditable(false);

        postButton.addActionListener(new PostButtonListener());
        scheduleButton.addActionListener(new ScheduleButtonListener());

        // Add focus listener to scheduleTime
        scheduleTime.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (scheduleTime.getText().equals(" Enter time in ms")) {
                    scheduleTime.setText("");
                    scheduleTime.setBackground(Color.LIGHT_GRAY);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (scheduleTime.getText().isEmpty()) {
                    scheduleTime.setText(" Enter time in ms");
                    scheduleTime.setBackground(Color.WHITE);
                }
            }
        });

        JPanel UserPanel = new JPanel(new BorderLayout());
        UserPanel.add(userInfomation, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout());
        TitledBorder titledBorder1 = BorderFactory.createTitledBorder("Create Post");
        titledBorder1.setTitleFont(titledBorder1.getTitleFont().deriveFont(Font.BOLD, 16));
        inputPanel.setBorder(titledBorder1);
        inputPanel.add(new JScrollPane(postContent), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(postButton);
        controlPanel.add(scheduleTime);
        controlPanel.add(scheduleButton);

        inputPanel.add(controlPanel, BorderLayout.SOUTH);
        UserPanel.add(inputPanel, BorderLayout.CENTER);
        add(UserPanel, BorderLayout.WEST);

        JPanel displayPanel = new JPanel(new BorderLayout());

        JPanel yourPostsPanel = new JPanel(new BorderLayout());
        TitledBorder titledBorder2 = BorderFactory.createTitledBorder("Your Posts");
        titledBorder2.setTitleFont(titledBorder2.getTitleFont().deriveFont(Font.BOLD, 16));
        yourPostsPanel.setBorder(titledBorder2);
        yourPostsPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
        displayPanel.add(yourPostsPanel, BorderLayout.NORTH);

        // 添加用于显示所有帖子区域
        JPanel allPostsPanel = new JPanel(new BorderLayout());
        TitledBorder titledBorder3 = BorderFactory.createTitledBorder("All Posts");
        titledBorder3.setTitleFont(titledBorder3.getTitleFont().deriveFont(Font.BOLD, 16));
        allPostsPanel.setBorder(titledBorder3);
        allPostsPanel.add(new JScrollPane(allPostsArea), BorderLayout.CENTER);
        displayPanel.add(allPostsPanel, BorderLayout.CENTER);

        add(displayPanel, BorderLayout.CENTER);
        // 启动一个线程来接收来自服务器的帖子
        new Thread(new ReceivePosts(receivePort)).start();
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
                return;
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
                post = new Post("User " + threadId + ": " + content);
            } else {
                post = new ScheduledPost("User " + threadId + ": " + content, Long.parseLong(scheduleTime));
            }

            out.writeObject(post);


            // 显示发送成功提示
            String response = (String) in.readObject();
            if (response.equals("Success")) {
                JOptionPane.showMessageDialog(this, "Post sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // 使用 SwingWorker 在后台线程中处理 sleep 操作
                long delay = Long.parseLong(scheduleTime);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        sleep(delay);
                        return null;
                    }

                    @Override
                    protected void done() {
                        // 获取当前时间
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                        String formattedNow = now.format(formatter);
                        displayArea.append(formattedNow + " >> " + content + "\n");
                    }
                }.execute();

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

        private int port;

        public ReceivePosts(int port) {
            this.port = port;
        }
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {

                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        ) {
                        System.out.println(port + "received");
                        // 读取数据端发送的 List<String>
                        List<String> receivedList = (List<String>) objectInputStream.readObject();
                        lock.lock(); // 锁定
                        try {
                            posts = receivedList;
                            updateTextArea();
                        } finally {
                            lock.unlock(); // 解锁
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTextArea() {
        SwingUtilities.invokeLater(() -> {
            allPostsArea.removeAll(); // Clear current content
            allPostsArea.setLayout(new BoxLayout(allPostsArea, BoxLayout.Y_AXIS));
            lock.lock(); // Lock

            try {
                for (String post : posts) {
                    JPanel postPanel = new JPanel();
                    postPanel.setLayout(new BorderLayout());

                    // Set a fixed preferred size for each post panel
                    postPanel.setPreferredSize(new Dimension(allPostsArea.getWidth(), 10));

                    // Add border
                    postPanel.setBorder(new LineBorder(Color.BLACK, 1));

                    JTextArea postText = new JTextArea(post);
                    postText.setEditable(false);

                    JButton viewButton = new JButton("View");
                    viewButton.addActionListener(e -> viewPostDetail(post));

                    postPanel.add(postText, BorderLayout.CENTER);
                    postPanel.add(viewButton, BorderLayout.EAST);

                    allPostsArea.add(postPanel);
                }
            } finally {
                lock.unlock(); // Unlock
            }
            allPostsArea.revalidate();
            allPostsArea.repaint();
        });
    }

    private void viewPostDetail(String post) {
        JFrame postDetailFrame = new JFrame("Post Detail");
        postDetailFrame.setSize(400, 300);
        postDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea postDetailText = new JTextArea(post);
        postDetailText.setEditable(false);

        postDetailFrame.add(new JScrollPane(postDetailText));
        postDetailFrame.setVisible(true);
    }


    public static void main(String[] args) {
        final ExecutorService executorService = Executors.newFixedThreadPool(5); // 创建线程池

        executorService.submit(() -> {
            long threadId = Thread.currentThread().getId();
            SwingUtilities.invokeLater(() -> {
                ClientApplication app1 = new ClientApplication(threadId, 12346);
                app1.setVisible(true);
            });
        });

        executorService.submit(() -> {
            long threadId = Thread.currentThread().getId();
            SwingUtilities.invokeLater(() -> {
                ClientApplication app2 = new ClientApplication(threadId, 12347);
                app2.setVisible(true);
            });
        });

        executorService.shutdown();
    }
}

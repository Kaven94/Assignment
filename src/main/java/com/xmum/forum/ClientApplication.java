package com.xmum.forum;

import com.xmum.forum.service.ForumService;
import com.xmum.forum.Listeners.ButtonListeners;
import com.xmum.forum.service.impl.ForumServiceImpl;

import javax.swing.*;
import java.awt.*;

public class ClientApplication extends JFrame {

    private JTextArea postContent;
    private JButton postButton;
    private JButton scheduleButton;
    private JTextField scheduleTime;
    private JTextArea displayArea;
    private ForumService forumService;

    public ClientApplication() {
        forumService = new ForumServiceImpl();

        setTitle("XMUM Forum");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        postContent = new JTextArea(5, 30);
        postButton = new JButton("Post");
        scheduleButton = new JButton("Schedule");
        scheduleTime = new JTextField("Enter time in ms", 15);
        displayArea = new JTextArea(10, 40);
        displayArea.setEditable(false);

        postButton.addActionListener(new ButtonListeners.PostButtonListener(postContent, displayArea, forumService));
        scheduleButton.addActionListener(new ButtonListeners.ScheduleButtonListener(postContent, scheduleTime, displayArea, forumService));

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(postContent), BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(postButton);
        controlPanel.add(scheduleTime);
        controlPanel.add(scheduleButton);

        inputPanel.add(controlPanel, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientApplication app = new ClientApplication();
            app.setVisible(true);
        });
    }
}

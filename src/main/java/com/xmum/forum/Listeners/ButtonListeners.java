package com.xmum.forum.Listeners;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;
import com.xmum.forum.service.ForumService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.Thread.sleep;

public class ButtonListeners {
    
    public static class PostButtonListener implements ActionListener {
        private JTextArea postContent;
        private JTextArea displayArea;
        private ForumService forumService;
        
        public PostButtonListener(JTextArea postContent, JTextArea displayArea, ForumService forumService) {
            this.postContent = postContent;
            this.displayArea = displayArea;
            this.forumService = forumService;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String content = postContent.getText();
            Post post = new Post(content);
            String response = forumService.postMessage(post);
            if ("Success".equals(response)) {
                JOptionPane.showMessageDialog(null, "Post sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                displayArea.append("Posted: " + content + "\n");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to send post!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class ScheduleButtonListener implements ActionListener {
        private JTextArea postContent;
        private JTextField scheduleTime;
        private JTextArea displayArea;
        private ForumService forumService;

        public ScheduleButtonListener(JTextArea postContent, JTextField scheduleTime, JTextArea displayArea, ForumService forumService) {
            this.postContent = postContent;
            this.scheduleTime = scheduleTime;
            this.displayArea = displayArea;
            this.forumService = forumService;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String content = postContent.getText();
            String time = scheduleTime.getText();
            ScheduledPost scheduledPost = new ScheduledPost(content, Long.parseLong(time));
            String response = forumService.schedulePost(scheduledPost);
            if ("Success".equals(response)) {
                JOptionPane.showMessageDialog(null, "Post scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                try {
                    sleep(Long.parseLong(time));
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                displayArea.append("Scheduled: " + content + " at " + time + "ms\n");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to schedule post!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

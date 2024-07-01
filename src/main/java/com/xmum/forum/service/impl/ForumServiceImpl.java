package com.xmum.forum.service.impl;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;
import com.xmum.forum.service.ForumService;


public class ForumServiceImpl implements ForumService {

    @Override
    public String postMessage(Post post) {
        System.out.println("Posting message: " + post.getContent());
        return "Success";
    }

    @Override
    public String schedulePost(ScheduledPost scheduledPost) {
        System.out.println("Scheduling post: " + scheduledPost.getContent() + " at " + scheduledPost.getScheduledTime());
        return "Success";
    }
}

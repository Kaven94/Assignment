package com.xmum.forum.service;

import com.xmum.forum.pojo.Post;
import com.xmum.forum.pojo.ScheduledPost;

public interface ForumService {

    String postMessage(Post post);
    String schedulePost(ScheduledPost scheduledPost);
}

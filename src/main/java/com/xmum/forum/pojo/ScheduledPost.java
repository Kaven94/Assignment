package com.xmum.forum.pojo;

public class ScheduledPost extends Post {

    private long scheduledTime;

    public ScheduledPost(String content, long scheduledTime) {
        super(content);
        this.scheduledTime = scheduledTime;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }
}

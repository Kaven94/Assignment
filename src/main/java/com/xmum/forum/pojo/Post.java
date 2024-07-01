package com.xmum.forum.pojo;

import java.io.Serializable;

public class Post implements Serializable {
    private String content;

    public Post(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

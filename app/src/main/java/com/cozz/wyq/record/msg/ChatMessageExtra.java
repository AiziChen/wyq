package com.cozz.wyq.record.msg;

public class ChatMessageExtra {
    private ChatMessage extra;
    private String content;

    public ChatMessage getExtra() {
        return extra;
    }

    public void setExtra(ChatMessage extra) {
        this.extra = extra;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

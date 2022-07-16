package com.cozz.wyq.pojo;

import androidx.annotation.Keep;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

@Keep
public class Group {
    @JSONField(name = "groupName")
    private String groupName;
    @JSONField(name = "groupId")
    private String groupId;
    @JSONField(name = "userId1")
    private String userId1;
    @JSONField(name = "userId2")
    private String userId2;
    @JSONField(name = "delayStart")
    private int delayStart;
    @JSONField(name = "delayEnd")
    private int delayEnd;
    @JSONField(name = "msgs")
    private List<String> msgs;
    @JSONField(name = "enabled")
    private boolean enabled;

    public Group() {
        groupName = null;
        groupId = null;
        userId1 = null;
        userId2 = null;
        delayStart = 0;
        delayEnd = 0;
        msgs = new ArrayList<>();
        enabled = true;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public int getDelayStart() {
        return delayStart;
    }

    public void setDelayStart(int delayStart) {
        this.delayStart = delayStart;
    }

    public int getDelayEnd() {
        return delayEnd;
    }

    public void setDelayEnd(int delayEnd) {
        this.delayEnd = delayEnd;
    }

    public List<String> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<String> msgs) {
        this.msgs = msgs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

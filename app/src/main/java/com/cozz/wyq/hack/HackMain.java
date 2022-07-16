package com.cozz.wyq.hack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.FileObserver;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.cozz.wyq.MainActivity;
import com.cozz.wyq.pojo.Group;
import com.cozz.wyq.record.DefaultMessage;
import com.cozz.wyq.record.msg.ChatMessage;
import com.cozz.wyq.tools.DiskTool;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HackMain {
    private static final String TAG = "HackMain";
    private static Object v2TIMConnectManagerInstance = null;
    private static Object mUser = null;
    private static Group[] groups = null;
    private static FileObserver observer = null;
    private static final Random r = new Random();

    public static void hackEntry(XC_LoadPackage.LoadPackageParam lpparam, String processName) {
        Class<?> DefaultMessageClazz = XposedHelpers.findClass("com.im.yunjian.model.DefaultMessage", lpparam.classLoader);
        Class<?> MessageManagerClazz = XposedHelpers.findClass("com.im.yunjian.sdk.v2.V2TIMMessageManager", lpparam.classLoader);
        Class<?> ChatMessageClazz = XposedHelpers.findClass("com.yunjian.wyq.bean.message.ChatMessage", lpparam.classLoader);
        Class<?> TimeUtilsClazz = XposedHelpers.findClass("com.yunjian.wyq.utils.f2", lpparam.classLoader);

        XposedHelpers.findAndHookMethod("com.im.yunjian.sdk.message.MessageCenter$1", lpparam.classLoader,
                "onReceiveNewMessage", List.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        List defaultMsgs = (List) param.args[0];
                        for (Object msg : defaultMsgs) {
                            String msgStr = JSONObject.toJSONString(msg);
                            // instance msg: type == 1
                            if (msgStr.contains("\"type\":1")) {
                                DefaultMessage defaultMessage = JSONObject.parseObject(msgStr, DefaultMessage.class);
                                if (defaultMessage.getContent() != null) {
//                                    XposedBridge.log("msg: " + defaultMessage.getContent());
                                    org.json.JSONObject jsonObject = new org.json.JSONObject(defaultMessage.getContent());
                                    org.json.JSONObject chatMsg = new org.json.JSONObject(jsonObject.getString("extra"));
                                    if (mUser != null) {
                                        String mUserId = (String) XposedHelpers.callMethod(mUser, "getUserId");
                                        String mNickName = (String) XposedHelpers.callMethod(mUser, "getNickName");
                                        if ((chatMsg.optBoolean("isGroup", false) || chatMsg.optBoolean("group", false))
                                                && chatMsg.optInt("type", 1) == ChatMessage.TYPE_TEXT) {
                                            String fromUserId = chatMsg.getString("fromUserId");
//                                            String fromUserName = chatMsg.getString("fromUserName");
                                            String toUserId = chatMsg.getString("toUserId");
                                            // iPhone version is not contains `roomJid`
//                                            String roomJid = chatMsg.getRoomJid();
                                            String roomJid = toUserId;
                                            String content = chatMsg.getString("content");
                                            if (!mUserId.equals(fromUserId) && groups != null && groups.length > 0) {
                                                for (Group g : groups) {
                                                    int delayStart = g.getDelayStart();
                                                    int delayEnd = g.getDelayEnd();
                                                    String groupId = g.getGroupId();
                                                    String userId1 = g.getUserId1();
                                                    String userId2 = g.getUserId2();
                                                    List<String> lines = g.getMsgs();
                                                    boolean enabled = g.isEnabled();
                                                    boolean flag;
                                                    if ((userId1 == null || TextUtils.isEmpty(userId1))
                                                            && (userId2 == null || TextUtils.isEmpty(userId2))) {
                                                        flag = true;
                                                    } else {
                                                        if (fromUserId.equals(userId1) || fromUserId.equals(userId2)) {
                                                            flag = true;
                                                        } else {
                                                            flag = false;
                                                        }
                                                    }
                                                    if (roomJid.equals(groupId) && flag && lines.size() > 0 && enabled) {
                                                        for (String line : lines) {
                                                            String[] msgs = line.split("\\|");
                                                            if (msgs.length > 1 && content.contains(msgs[0])) {
                                                                int delay = r.nextInt(delayEnd - delayStart + 1);
                                                                int index = r.nextInt(msgs.length - 1) + 1;
                                                                new Thread(() -> {
                                                                    try {
                                                                        Thread.sleep(delay * 1000);
                                                                    } catch (InterruptedException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Object rsChatMsg = XposedHelpers.newInstance(ChatMessageClazz);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setType", 1);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setContent", msgs[index]);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setFromUserId", mUserId);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setFromUserName", mNickName);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setTimeSend", XposedHelpers.callStaticMethod(TimeUtilsClazz, "E"));
                                                                    XposedHelpers.callMethod(rsChatMsg, "setToUserId", roomJid);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setDeleteTime", -1);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setIsEncrypt", 0);
                                                                    String replaceAll = UUID.randomUUID().toString().replaceAll("-", "");
                                                                    XposedHelpers.callMethod(rsChatMsg, "setPacketId", replaceAll);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setMessageId", replaceAll);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setGroup", true);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setMsgType", 1);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setReSendCount", 5);
                                                                    XposedHelpers.callMethod(rsChatMsg, "setRoomJid", roomJid);
                                                                    if (v2TIMConnectManagerInstance != null) {
                                                                        Class<?> aaa = XposedHelpers.findClass("com.im.yunjian.sdk.v2.V2TIMConnectManager$6", lpparam.classLoader);
                                                                        Object dmsg = XposedHelpers.callStaticMethod(DefaultMessageClazz, "toDefaultMessage", rsChatMsg);
                                                                        Object managerInstance = XposedHelpers.callStaticMethod(MessageManagerClazz, "getInstance");
                                                                        XposedHelpers.callMethod(managerInstance, "sendMessage", dmsg, XposedHelpers.newInstance(aaa, v2TIMConnectManagerInstance, rsChatMsg));
//                                                                        XposedBridge.log(JSONObject.toJSONString(defaultMessage));
                                                                    }
                                                                }).start();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
//                synchronized (XposedHelpers.getObjectField(param.thisObject, "mLockObject")) {
//                }
                    }
                });
//        XposedHelpers.findAndHookMethod("com.im.yunjian.sdk.v2.V2TIMManagerImpl$4", lpparam.classLoader, "onReceiveNewMessage", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
////                synchronized (XposedHelpers.getObjectField(param.thisObject, "mLockObject")) {
////                }
//                XposedBridge.log("list: " + param.args[0]);
//            }
//        });
//        XposedHelpers.findAndHookMethod("com.im.yunjian.sdk.v2.V2TIMConnectManager", lpparam.classLoader,
//                "sendMessage", ChatMessageClazz, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        super.beforeHookedMethod(param);
//                        Object chatMsg = param.args[0];
//                    }
//                });
        XposedHelpers.findAndHookConstructor("com.im.yunjian.sdk.v2.V2TIMConnectManager", lpparam.classLoader, Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
//                Object context = param.args[0];
                v2TIMConnectManagerInstance = param.thisObject;
            }
        });
//        XposedHelpers.findAndHookMethod("com.yunjian.wyq.utils.log.LogUtils", lpparam.classLoader, "e",
//                String.class, String.class, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        super.beforeHookedMethod(param);
//                        XposedBridge.log(param.args[0] + ", " + param.args[1]);
//                    }
//                });

        XposedHelpers.findAndHookMethod("com.yunjian.wyq.helper.LoginSecureHelper", lpparam.classLoader,
//                P(CoreManager lVar, Context context, User user, AsyncUtils.C19904a aVar, Runnable runnable, AbstractC17273m mVar, String str, String str2, byte[] bArr, String str3)
                "P", XposedHelpers.findClass("com.yunjian.wyq.ui.base.l", lpparam.classLoader),
                Context.class, XposedHelpers.findClass("com.yunjian.wyq.bean.User", lpparam.classLoader),
                XposedHelpers.findClass("com.yunjian.wyq.utils.o$a", lpparam.classLoader),
                Runnable.class, XposedHelpers.findClass("com.yunjian.wyq.helper.LoginSecureHelper$m", lpparam.classLoader),
                String.class, String.class, byte[].class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        mUser = param.args[2];
                    }
                });

        XposedHelpers.findAndHookMethod("com.yunjian.wyq.ui.message.multi.RoomInfoActivity$g", lpparam.classLoader,
                "e", XposedHelpers.findClass("com.yunjian.wyq.utils.o$a", lpparam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Object roomInfoActivity = XposedHelpers.getObjectField(param.thisObject, "a");
                        Object mucRoom = XposedHelpers.getObjectField(roomInfoActivity, "a");
                        List mucRoomMembers = (List) XposedHelpers.callMethod(mucRoom, "getMembers");
                        LinkedList<String> usersInfo = new LinkedList<>();
                        usersInfo.add("群id-" + XposedHelpers.callMethod(mucRoom, "getJid"));
                        for (Object roomMember : mucRoomMembers) {
                            int role = (int) XposedHelpers.callMethod(roomMember, "getRole");
                            // 群主或群管
                            if (role == 1 || role == 2) {
                                usersInfo.add(XposedHelpers.callMethod(roomMember, "getNickName") + "-" + XposedHelpers.callMethod(roomMember, "getUserId"));
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>((Activity) roomInfoActivity, android.R.layout.simple_list_item_1, usersInfo.toArray(new String[0]));
                        XposedHelpers.callMethod(roomInfoActivity, "runOnUiThread", (Runnable) () -> {
                            new AlertDialog.Builder((Activity) roomInfoActivity)
                                    .setTitle("点击复制群组+用户ID")
                                    .setAdapter(adapter, (dialogInterface, i) -> {
                                        String item = adapter.getItem(i);
                                        String[] user = item.split("-");
                                        String nickName = user[0];
                                        String userId = user[1];
                                        try {
                                            ClipboardManager clipboard = (ClipboardManager) ((Activity) roomInfoActivity).getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText(nickName, userId);
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText((Activity) roomInfoActivity, "复制成功", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText((Activity) roomInfoActivity, "复制失败：" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .show();
                        });
                    }
                });
        XposedHelpers.findAndHookMethod("com.yunjian.wyq.ui.MainActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
//                XposedBridge.log(DiskTool.getConfigPath() + "/" + MainActivity.GROUPS_CONFIG_FILE);
                groups = JSONObject.parseObject(DiskTool.readFile(MainActivity.GROUPS_CONFIG_FILE), Group[].class);
                observer = new FileObserver(DiskTool.getConfigPath(), FileObserver.ALL_EVENTS) {
                    int count = 0;

                    @Override
                    public void onEvent(int evt, @Nullable String file) {
                        if (MainActivity.GROUPS_CONFIG_FILE.equals(file)) {
                            if ((evt == FileObserver.MODIFY || evt == FileObserver.CREATE) && count++ == 1) {
                                count = 0;
//                                XposedBridge.log(file + ", " + DiskTool.getConfigPath() + "/" + MainActivity.GROUPS_CONFIG_FILE);
                                groups = JSONObject.parseObject(DiskTool.readFile(MainActivity.GROUPS_CONFIG_FILE), Group[].class);
                            }
                        }
                    }
                };
                observer.startWatching();
            }
        });
    }
}

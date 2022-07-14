package com.cozz.wyq.hack;

import com.alibaba.fastjson.JSONObject;
import com.cozz.wyq.record.DefaultMessage;
import com.cozz.wyq.record.msg.ChatMessage;
import com.cozz.wyq.record.msg.ChatMessageExtra;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HackMain {
    private static final String TAG = "HackMain";

    public static void hackEntry(XC_LoadPackage.LoadPackageParam lpparam, String processName) {
        Class<?> DefaultMessageClazz = XposedHelpers.findClass("com.im.yunjian.model.DefaultMessage", lpparam.classLoader);
        Class<?> MessageManagerClazz = XposedHelpers.findClass("com.im.yunjian.sdk.v2.V2TIMMessageManager", lpparam.classLoader);

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
                            ChatMessageExtra msgExtra = JSONObject.parseObject(defaultMessage.getContent(), ChatMessageExtra.class);
                            ChatMessage chatMsg = msgExtra.getExtra();
                            if (chatMsg.isGroup() && chatMsg.getMsgType() == ChatMessage.TYPE_TEXT && !chatMsg.isMySend()) {
                                String fromUserId = chatMsg.getFromUserId();
                                String fromUserName = chatMsg.getFromUserName();
                                String toUserId = chatMsg.getToUserId();
                                String roomJid = chatMsg.getRoomJid();
                                String content = chatMsg.getContent();
                                XposedBridge.log(content);
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
        XposedHelpers.findAndHookMethod("com.im.yunjian.sdk.v2.V2TIMConnectManager", lpparam.classLoader,
                "sendMessage", XposedHelpers.findClass("com.yunjian.wyq.bean.message.ChatMessage", lpparam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Object chatMsg = param.args[0];
//                        Class<?> aaa = XposedHelpers.findClass("com.im.yunjian.sdk.v2.V2TIMConnectManager$6", lpparam.classLoader);
////                        XposedBridge.log(JSONObject.toJSONString(chatMsg));
//                        Object defaultMessage = XposedHelpers.callStaticMethod(DefaultMessageClazz, "toDefaultMessage", chatMsg);
//                        Object managerInstance = XposedHelpers.callStaticMethod(MessageManagerClazz, "getInstance");
//                        XposedHelpers.callMethod(managerInstance, "sendMessage", defaultMessage, XposedHelpers.newInstance(aaa));
                    }
                });
        XposedHelpers.findAndHookMethod("com.yunjian.wyq.utils.log.LogUtils", lpparam.classLoader, "e",
                String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log(param.args[0] + ", " + param.args[1]);
                    }
                });
    }
}

package se.kth.csc.stayawhile.api.websocket;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import se.kth.csc.stayawhile.api.User;

public class Websocket {
    private Socket socket;
    private String mQueueName;

    public Websocket(String queueName) {
        this.mQueueName = queueName;
        try {
            socket = IO.socket("http://queue.csc.kth.se/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("listen", mQueueName);
            }
        });
    }

    public void connect() {
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
    }

    public void sendStopHelp(User user) {
        try {
            System.out.println("send stopHelp " + user);
            JSONObject obj = new JSONObject();
            obj.put("ugKthid", user.getUgKthid());
            obj.put("queueName", mQueueName);
            socket.emit("stopHelp", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendHelp(User user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("ugKthid", user.getUgKthid());
            obj.put("queueName", mQueueName);
            System.out.println("send help " + obj);
            socket.emit("help", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendKick(User user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", user.getJSON());
            obj.put("queueName", mQueueName);
            socket.emit("kick", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendLock() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            socket.emit("lock", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUnlock() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            socket.emit("unlock", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject getMessageObject(String message) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("queueName", mQueueName);
        obj.put("message", message);
        return obj;
    }

    public void sendBroadcast(String message) {
        try {
            JSONObject obj = getMessageObject(message);
            socket.emit("broadcast", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBroadcastFaculty(String message) {
        try {
            JSONObject obj = getMessageObject(message);
            socket.emit("broadcastFaculty", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageUser(String message, String ugKthid) {
        try {
            JSONObject obj = getMessageObject(message);
            obj.put("ugKthid", ugKthid);
            socket.emit("messageUser", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFlag(String message, String ugKthid) {
        try {

            JSONObject obj = getMessageObject(message);
            obj.put("ugKthid", ugKthid);
            socket.emit("flag", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCompletion(String message, String ugKthid) {
        try {

            JSONObject obj = getMessageObject(message);
            obj.put("ugKthid", ugKthid);
            socket.emit("completion", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBadLocation(User student) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", student.getJSON());
            obj.put("queueName", mQueueName);
            obj.put("type", "unknown");
            socket.emit("badLocation", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void onJoin(Emitter.Listener listener) {
        socket.on("join", listener);
    }

    public void onLeave(Emitter.Listener listener) {
        socket.on("leave", listener);
    }

    public void onUpdate(Emitter.Listener listener) {
        socket.on("update", listener);
    }

    public void onHelp(Emitter.Listener listener) {
        socket.on("help", listener);
    }

    public void onStopHelp(Emitter.Listener listener) {
        socket.on("stopHelp", listener);
    }

    public void onMsg(Emitter.Listener listener) {
        socket.on("msg", listener);
    }

    public void onConnect(Emitter.Listener listener) {
        socket.on(Socket.EVENT_CONNECT, listener);
    }
}

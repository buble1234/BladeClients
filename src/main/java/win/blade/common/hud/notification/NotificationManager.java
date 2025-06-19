package win.blade.common.hud.notification;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.hud.notification.impl.InfoNotification;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public void add(String content, NotificationType type, long delay) {
        Notification notification = null;

        switch (type) {
            case INFO:
                notification = new InfoNotification(content, delay, notifications.size());
                break;
        }

        if (notification == null) return;
        notifications.add(notification);
    }

    public void render(DrawContext context) {
        if (notifications.isEmpty()) return;

        int i = 0;
        for (Notification notification : notifications) {
            notification.update(i);
            notification.render(context);
            i++;
        }

        notifications.removeIf(Notification::isFinished);
    }
}
package win.blade.common.ui;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.ui.impl.InfoNotification;
import win.blade.common.ui.impl.SuccessNotification;
import win.blade.common.ui.impl.ErrorNotification;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public void add(String content, NotificationType type, long delay) {
        Notification notification = switch (type) {
            case INFO -> new InfoNotification(content, delay, notifications.size());
            case SUCCESS -> new SuccessNotification(content, delay, notifications.size());
            case ERROR -> new ErrorNotification(content, delay, notifications.size());
        };

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
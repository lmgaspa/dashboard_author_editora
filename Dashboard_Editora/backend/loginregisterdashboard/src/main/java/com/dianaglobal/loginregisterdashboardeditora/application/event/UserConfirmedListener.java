// src/main/java/com/dianaglobal/loginregister/application/event/UserConfirmedListener.java
package com.dianaglobal.loginregisterdashboardeditora.application.event;

import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;

public interface UserConfirmedListener {
    void onUserConfirmed(User user, String plaintextPassword);
}

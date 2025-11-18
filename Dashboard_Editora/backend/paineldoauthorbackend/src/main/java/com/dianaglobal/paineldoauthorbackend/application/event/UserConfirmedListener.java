// src/main/java/com/dianaglobal/paineldoauthor/application/event/UserConfirmedListener.java
package com.dianaglobal.paineldoauthorbackend.application.event;

import com.dianaglobal.paineldoauthorbackend.domain.model.User;

public interface UserConfirmedListener {
    void onUserConfirmed(User user, String plaintextPassword);
}

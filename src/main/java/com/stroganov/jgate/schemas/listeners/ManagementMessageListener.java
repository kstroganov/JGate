package com.stroganov.jgate.schemas.listeners;

import com.stroganov.jgate.Application;
import ru.micexrts.cgate.messages.*;

/**
 * Created by stroganov on 14.05.2016.
 */
public interface ManagementMessageListener {
    int onOpen(Application.JGateListener listener, OpenMessage message);
    int onClose(Application.JGateListener listener, CloseMessage message);
    int onTransactionBegin(Application.JGateListener listener, TnBeginMessage message);
    int onTransactionCommit(Application.JGateListener listener, TnCommitMessage message);
    int onLine(Application.JGateListener listener, P2ReplOnlineMessage message);
    int onLifeNnumChange(Application.JGateListener listener, P2ReplLifeNumMessage message);
    int onClearDeleted(Application.JGateListener listener, P2ReplClearDeletedMessage message);
    int onReplState(Application.JGateListener listener, P2ReplStateMessage message);
}

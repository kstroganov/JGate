package com.stroganov.jgate;

import com.stroganov.jgate.Application;
import com.stroganov.jgate.ManagementMessageListener;
import ru.micexrts.cgate.*;
import ru.micexrts.cgate.messages.*;

/**
 * Created by stroganov on 14.05.2016.
 */
public abstract class StreamDataMessageSubscriber<T extends ManagementMessageListener> implements ISubscriber {
    private Application.JGateListener jgateListener;
    protected final T userListener;

    public StreamDataMessageSubscriber(T listener) { userListener = listener; }

    protected abstract int onMessage(Application.JGateListener jgateListener, StreamDataMessage message);

    @Override
    public int onMessage(Connection connection, Listener listener, Message message) {
        int messageType = message.getType();
        switch (messageType) {
            case MessageType.MSG_OPEN:
                return userListener.onOpen(jgateListener, (OpenMessage) message);
            case MessageType.MSG_CLOSE:
                return userListener.onClose(jgateListener, (CloseMessage) message);
            case MessageType.MSG_STREAM_DATA:
                return onMessage(jgateListener, (StreamDataMessage) message);
            case MessageType.MSG_P2REPL_REPLSTATE:
                return userListener.onReplState(jgateListener, (P2ReplStateMessage) message);
            case MessageType.MSG_P2REPL_LIFENUM:
                return userListener.onLifeNnumChange(jgateListener, (P2ReplLifeNumMessage) message);
            case MessageType.MSG_P2REPL_CLEARDELETED:
                return userListener.onClearDeleted(jgateListener, (P2ReplClearDeletedMessage) message);
            case MessageType.MSG_P2REPL_ONLINE:
                return userListener.onLine(jgateListener, (P2ReplOnlineMessage) message);
            case MessageType.MSG_TN_BEGIN:
                return userListener.onTransactionBegin(jgateListener, (TnBeginMessage) message);
            case MessageType.MSG_TN_COMMIT:
                return userListener.onTransactionCommit(jgateListener, (TnCommitMessage) message);
        }
        return ErrorCode.OK;
    }

    public void setJGateListener(Application.JGateListener jgateListener) {
        this.jgateListener = jgateListener;
    }
}

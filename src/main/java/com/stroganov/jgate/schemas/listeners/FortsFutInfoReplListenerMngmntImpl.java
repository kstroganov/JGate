package com.stroganov.jgate.schemas.listeners;

import com.stroganov.jgate.Application;
import ru.micexrts.cgate.ErrorCode;
import ru.micexrts.cgate.messages.*;

/**
 * Created by gorgon on 5/19/16.
 */
public abstract class FortsFutInfoReplListenerMngmntImpl implements FortsFutInfoReplListener {
    @Override
    public int onOpen(Application.JGateListener listener, OpenMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onClose(Application.JGateListener listener, CloseMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onTransactionBegin(Application.JGateListener listener, TnBeginMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onTransactionCommit(Application.JGateListener listener, TnCommitMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onLine(Application.JGateListener listener, P2ReplOnlineMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onLifeNnumChange(Application.JGateListener listener, P2ReplLifeNumMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onClearDeleted(Application.JGateListener listener, P2ReplClearDeletedMessage message) {
        return ErrorCode.OK;
    }

    @Override
    public int onReplState(Application.JGateListener listener, P2ReplStateMessage message) {
        return ErrorCode.OK;
    }
}

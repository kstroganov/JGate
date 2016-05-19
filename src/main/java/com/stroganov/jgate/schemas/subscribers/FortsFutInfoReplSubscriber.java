package com.stroganov.jgate.schemas.subscribers;

import com.stroganov.jgate.Application;
import ru.micexrts.cgate.*;
import ru.micexrts.cgate.messages.*;

import com.stroganov.jgate.schemas.listeners.FortsFutInfoReplListener;

public final class FortsFutInfoReplSubscriber extends StreamDataMessageSubscriber<FortsFutInfoReplListener> {
    public final static String listenerSettings = "p2repl://FORTS_FUTINFO_REPL;scheme=|FILE|fut_info.ini|FortsFutInfoRepl";

    public FortsFutInfoReplSubscriber(FortsFutInfoReplListener listener) { super(listener); }

    @Override
    protected int onMessage(Application.JGateListener jgateListener, StreamDataMessage message) {
        switch (message.getMsgIndex()) {
            case com.stroganov.jgate.schemas.FortsFutInfoRepl.fut_sess_contents.TABLE_INDEX:
                com.stroganov.jgate.schemas.FortsFutInfoRepl.fut_sess_contents msg = new com.stroganov.jgate.schemas.FortsFutInfoRepl.fut_sess_contents();
                msg.setData(message.getData());
                try {
                    userListener.onMessage(jgateListener, msg);
                } finally {
                    msg.setData(null);
                }
                break;
        }
        return ErrorCode.OK;
    }

}

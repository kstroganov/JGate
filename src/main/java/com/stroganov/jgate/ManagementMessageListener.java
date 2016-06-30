package com.stroganov.jgate;

import ru.micexrts.cgate.messages.*;

/**
 * Created by stroganov on 14.05.2016.
 */
public interface ManagementMessageListener {
    // The message is delivered at the moment of data stream activation. This event surely
    // occurs before receiving of any data on this subscription. For data streams,
    // delivery of the message means that the data scheme was agreed and is ready to use.
    int onOpen(Application.JGateListener listener, OpenMessage message);
    // The message is delivered at the moment of data stream closure. Delivery of the message
    // means that the stream was closed by the user or the system.
    int onClose(Application.JGateListener listener, CloseMessage message);
    // Means the moment when receiving of the next data block starts. Along with the next message,
    // may be used by the program logic for data integrity control.
    int onTransactionBegin(Application.JGateListener listener, TnBeginMessage message);
    // Means the moment when receiving of the next data block is completed. By the moment this
    // message is delivered, it may be safely assumed that data received under this subscription
    // are consistent and reflect the inter-synchronized tables.
    int onTransactionCommit(Application.JGateListener listener, TnCommitMessage message);
    // Stream switching to the online mode — it means that receiving of the initial snapshot
    // was completed.
    int onLine(Application.JGateListener listener, P2ReplOnlineMessage message);
    // The scheme life number was changed. This message means that previous data, which were
    // received regarding the stream are not up-to-date and should be deleted. This will be
    // accompanied by retranslation of data on the new data scheme life number.
    int onLifeNnumChange(Application.JGateListener listener, P2ReplLifeNumMessage message);
    // Mass deletion of outdated data was performed.
    int onClearDeleted(Application.JGateListener listener, P2ReplClearDeletedMessage message);
    // The message indicates the state of data stream; it is sent before closure of the stream.
    // The 'message'’ contains the line, which indicates the encoded state of the data stream
    // as of the moment the message is delivered — the data scheme, table revision numbers and
    // the scheme life number are saved as for the time of receiving the last 'TnCommitMessage'
    // message. This line may be to open listener as the ‘replstate’ parameter on the same stream
    // on the next time which will provide continuation of data receiving upon shutdown of the
    // stream.
    int onReplState(Application.JGateListener listener, P2ReplStateMessage message);
}

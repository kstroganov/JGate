package com.stroganov.jgate.schemas.builder;

import com.stroganov.jgate.Application;
import com.stroganov.jgate.schemas.listeners.*;
import com.stroganov.jgate.schemas.subscribers.*;
import ru.micexrts.cgate.CGateException;

/**
 * Created by stroganov on 16.05.2016.
 */
public class SchemasBuilder {
    final Application.DataFeedFabric dfFabric;

    public SchemasBuilder(Application.DataFeedFabric dfFabric) {
        this.dfFabric = dfFabric;
    }
    public SchemasBuilder applyFortsFutInfoRepl(String listenerName,
                                                String cgateListenerOpenSettings,
                                                FortsFutInfoReplListener listener) throws CGateException {
        dfFabric.createListener(String.format("%s;name=%s", FortsFutInfoReplSubscriber.listenerSettings, listenerName),
                                cgateListenerOpenSettings,
                                new FortsFutInfoReplSubscriber(listener));
        return this;
    }
}

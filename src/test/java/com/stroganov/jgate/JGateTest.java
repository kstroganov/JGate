package com.stroganov.jgate;

import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.LinkedList;

import ru.micexrts.cgate.CGate;
import ru.micexrts.cgate.CGateException;
import ru.micexrts.cgate.ErrorCode;

import com.stroganov.jgate.schemas.FortsFutInfoRepl;
import com.stroganov.jgate.schemas.listeners.FortsFutInfoReplListenerMngmntImpl;

/**
 * Created by stroganov on 18.05.2016.
 */
public class JGateTest {
    @Test
    public void fortsFutInfoTest() throws CGateException, InterruptedException, IllegalAccessException {
        LinkedList<Integer> futIsinList = new LinkedList<>();
        Application app = Application.createInstance(new Application.Configurator() {
            @Override
            public void configure(Application.Builder builder) throws CGateException {
                builder.newConnection(new Application.ConnectionSettings(
                        "FortsFutInfoTestConnection", Application.ConnectionType.tcp, "127.0.0.1", 4001
                )).applyFortsFutInfoRepl(
                        "FortsFutInfoReplListener",
                        null,
                        new FortsFutInfoReplListenerMngmntImpl() {
                            @Override
                            public int onMessage(Application.JGateListener listener, FortsFutInfoRepl.fut_sess_contents message) {
                                futIsinList.add(message.get_isin_id());
                                return ErrorCode.OK;
                            }
                        });
            }

            @Override
            public String getCGateSettings() {
                return "ini=jgatetest.ini;key=11111111";
            }

            @Override
            public String getAppName() {
                return "JGate.fortsFutInfoTest";
            }
        });
        app.run();
        Thread.sleep(3000);
        app.stop();
        assertFalse(futIsinList.isEmpty());
    }
}

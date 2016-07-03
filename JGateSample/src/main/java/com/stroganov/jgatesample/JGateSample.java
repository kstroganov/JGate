package com.stroganov.jgatesample;

import com.stroganov.jgate.Application;
import com.stroganov.jgatesample.schemas.FortsFutInfoRepl;
import com.stroganov.jgatesample.schemas.builder.SchemasBuilder;
import com.stroganov.jgatesample.schemas.listeners.FortsFutInfoReplListenerImpl;
import ru.micexrts.cgate.CGateException;
import ru.micexrts.cgate.ErrorCode;
import ru.micexrts.cgate.P2TypeParser;

/**
 * Created by stroganov on 30.06.2016.
 */
public class JGateSample {
    public static void main(String[] args) throws CGateException, InterruptedException {
        Application app = Application.createInstance(new Application.Configurator() {
            @Override
            public void configure(Application.Builder builder) throws CGateException {
                builder.newConnection(new Application.ConnectionSettings(
                        "JGateSampleConnection", Application.ConnectionType.tcp, "127.0.0.1", 4001),
                        SchemasBuilder.getSchemasBuilderFabric()
                ).applyFortsFutInfoRepl(
                        "JGateSampleListener",
                        "FORTS_FUTINFO_REPL",
                        null,
                        new FortsFutInfoReplListenerImpl() {
                            @Override
                            public int onMessage(Application.JGateListener listener, FortsFutInfoRepl.fut_sess_contents message) {
                                System.out.printf("%-60s %-10d %-10d\n", message.get_name(), message.get_isin_id(), message.get_state());
                                return ErrorCode.OK;
                            }

                            @Override
                            public int onMessage(Application.JGateListener listener, FortsFutInfoRepl.sys_events message) {
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
                return "JGateSample";
            }
        });
        P2TypeParser.setCharset("Windows-1251");
        System.out.println("Active FORTS futures:");
        System.out.printf("%-60s %-10s %-10s\n", "Name", "ISIN", "State");
        app.run();
        Thread.sleep(3000);
        app.stop();
    }
}

package com.stroganov.jgate;

import com.stroganov.jgate.schemas.builder.SchemasBuilder;
import com.stroganov.jgate.schemas.subscribers.StreamDataMessageSubscriber;
import ru.micexrts.cgate.*;

import java.util.HashMap;
import java.util.LinkedList;

public final class Application {

    /////////////////////////////////////////////////////////////
    //
    // Configuration
    //
    public interface Configurator {
        void configure(Builder builder) throws CGateException;

        String getCGateSettings();

        String getAppName();
    }

    enum ConnectionType {tcp, lrpcq}

    /////////////////////////////////////////////////////////////
    //
    // ConnectionSettings
    //
    public static class ConnectionSettings {
        public ConnectionSettings(String connName, ConnectionType type, String host, int port) {
            this(connName, type, host, port, 3000);
        }
        public ConnectionSettings(String connName, ConnectionType type, String host, int port, int timeout) {
            this(connName, type, host, port, timeout, null);
        }
        public ConnectionSettings(String connName, ConnectionType type, String host, int port, int timeout, String localPass) {
            this.connName = connName;
            this.type = type;
            this.host = host;
            this.port = port;
            this.timeout = timeout;
            this.localPass = localPass;
        }
        public final String connName;
        public final ConnectionType type;
        public final String host;
        public final int port;
        public final String localPass;
        public final int timeout;
    }

    /////////////////////////////////////////////////////////////
    //
    // Builder
    //
    public interface Builder {
        SchemasBuilder newConnection(ConnectionSettings settings) throws CGateException;
    }

    public interface DataFeedFabric {
        void createListener(String createSettings, String openSettings, StreamDataMessageSubscriber subscriber) throws CGateException;

        void createPublisher();

        void createPublishListener();
    }

    private interface DataFeed {
        void process() throws CGateException;
        String getName();
        void dispose() throws CGateException;
    }

    /////////////////////////////////////////////////////////////
    //
    // JGateConnection
    //
    public class JGateConnection extends Thread {
        private final Connection connection;
        private volatile boolean exitFlag = false;
        private CGateException runtimeException;
        private final LinkedList<DataFeed> dataFeeds = new LinkedList<>();

        private JGateConnection(String settings) throws CGateException {
            connection = new Connection(settings);
        }

        private void logException(DataFeed df, CGateException e) {
            CGate.logError(String.format("CGateException occured while processing '%s' dataFeed on connection '%s'. %s.",
                                         df.getName(),
                                         connection.getName(),
                                         e.getMessage()));
        }
        private void logException(DataFeed df, Exception e) {
            CGate.logError(String.format("Exception occured while processing '%s' dataFeed on connection '%s'. Error message: '%s'.",
                                         df.getName(),
                                         connection.getName(),
                                         e.getMessage()));
        }

        @Override
        public void run() {
            try {
                while (!exitFlag) {
                    switch (connection.getState()) {
                        case ru.micexrts.cgate.State.ERROR:
                            connection.close();
                            break;
                        case ru.micexrts.cgate.State.CLOSED:
                            try {
                                connection.open();
                            } catch (CGateException e) {
                                CGate.logError(String.format("Failed to open connection '%s'. %s. Waiting 1000 msec. before new attemption.",
                                                             connection.getName(),
                                                             e.getMessage()));
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ee) {
                                    // Do nothing
                                }
                            }
                            break;
                        case ru.micexrts.cgate.State.ACTIVE:
                            int result = connection.process(1);
                            if (result != ErrorCode.OK && result != ErrorCode.TIMEOUT)
                                CGate.logInfo(String.format("WARNING: connection state request failed with code: 0x%X.", result));
                            for (DataFeed df : dataFeeds) {
                                try {
                                    df.process();
                                } catch (CGateException e) {
                                    logException(df, e);
                                } catch (Exception e) {
                                    logException(df, e);
                                }
                            }
                            break;
                    }
                }
            } catch (CGateException e) {
                runtimeException = e;
            } finally {
                for (DataFeed df : dataFeeds) {
                    try {
                        df.dispose();
                    } catch (CGateException e) {
                        logException(df, e);
                    }
                }
            }
        }

        private void dispose() throws InterruptedException, CGateException {
            exitFlag = true;
            try {
                this.join();
            } finally {
                try {
                    connection.close();
                } finally {
                    connection.dispose();
                }
            }
        }

        private <T extends DataFeed> T addDataFeed(T dataFeed) {
            dataFeeds.add(dataFeed);
            return dataFeed;
        }

        public Exception getRuntimeException() {
            return runtimeException;
        }

        public boolean isErrorState() {
            return exitFlag && runtimeException != null;
        }
    }

    /////////////////////////////////////////////////////////////
    //
    // JGateListener
    //
    public class JGateListener implements DataFeed {
        private Listener listener;
        private String openSettings;

        private JGateListener(Connection connection, String settings, String openSettings, ISubscriber subscriber) throws CGateException {
            listener = new Listener(connection, settings, subscriber);
            this.openSettings = openSettings;
        }

        @Override
        public void process() throws CGateException {
            int state = listener.getState();
            switch (state) {
                case State.ERROR:
                    listener.close();
                    break;
                case State.CLOSED:
                    if (openSettings == null)
                        listener.open();
                    else
                        listener.open(openSettings);
                    break;
            }
        }

        @Override
        public String getName() { return listener.getName(); }

        @Override
        public void dispose() throws CGateException {
            try {
                if (listener.getState() != State.CLOSED)
                    listener.close();
            } finally {
                listener.dispose();
            }
        }

        public void setOpenSettings(String openSettings) { this.openSettings = openSettings; }
    }

    private static volatile boolean exitFlag = false;
    private static volatile boolean cleanedUp = false;

    private HashMap<String, JGateConnection> connections = new HashMap<>();
    private String cgateSettings;

    private Application(String cgateSettings) throws IllegalAccessException, CGateException {
        if (cgateSettings == null || cgateSettings.isEmpty())
            throw new IllegalAccessException("Parameter 'cgateSettings' can't be null or empty");
        CGate.open(cgateSettings);
        this.cgateSettings = cgateSettings;
    }

    private void runConnections() {
        for (JGateConnection connection : connections.values()) {
            connection.start();
        }
    }

    private void destroyConnections() {
        for (JGateConnection connection : connections.values()) {
            try {
                connection.dispose();
            } catch (CGateException e) {
                CGate.logError(String.format("CGateException occured while disposing connection '%s'. %s.",
                                             connection.getName(),
                                             e.getMessage(),
                                             e.getErrCode()));
            } catch (InterruptedException e) {
                CGate.logError(String.format("InterruptedException occured while disposing connection '%s'. Error message: '%s'.",
                        connection.getName(),
                        e.getMessage()));
            }
        }
    }

    public static Application createInstance(Configurator config) throws CGateException, IllegalAccessException {
        if (config == null)
            throw new IllegalArgumentException("Parameter 'config' can't be null");
        Application app = new Application(config.getCGateSettings());
        config.configure(new Builder() {
            @Override
            public SchemasBuilder newConnection(ConnectionSettings settings) throws CGateException {
                if (settings == null)
                    throw new IllegalArgumentException("Parameter 'settings' can't be null");
                if (settings.type == null)
                    throw new IllegalArgumentException("Parameter 'settings.type' can't be null");
                if (settings.host == null || settings.host.isEmpty())
                    throw new IllegalArgumentException("Parameter 'settings.host' can't be null or empty");
                StringBuilder sb = new StringBuilder("p2").append(settings.type.toString())
                                                          .append("://")
                                                          .append(settings.host)
                                                          .append(":")
                                                          .append(settings.port)
                                                          .append(";app_name=")
                                                          .append(config.getAppName())
                                                          .append(";name=")
                                                          .append(settings.connName);
                if (settings.localPass != null && !settings.localPass.isEmpty())
                    sb.append(";local_pass=").append(settings.localPass);
                if (settings.timeout > 0)
                    sb.append(settings.type == ConnectionType.tcp ? ";timeout=" : ";local_timeout=").append(settings.timeout);
                JGateConnection newConn = app.new JGateConnection(sb.toString());
                app.connections.put(settings.connName, newConn);
                return new SchemasBuilder(new DataFeedFabric() {
                    @Override
                    public void createListener(String createSettings,
                                               String openSettings,
                                               StreamDataMessageSubscriber subscriber) throws CGateException {
                        subscriber.setJGateListener(newConn.addDataFeed(app.new JGateListener(newConn.connection,
                                                                                              createSettings,
                                                                                              openSettings,
                                                                                              subscriber)));
                    }

                    @Override
                    public void createPublisher() {

                    }

                    @Override
                    public void createPublishListener() {

                    }
                });
            }
        });
        return app;
    }

    public void run() throws CGateException {
        runConnections();
    }

    public void stop() throws CGateException {
        destroyConnections();
        CGate.close();
    }
}
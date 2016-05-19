# JGate
##Synopsys
The general purpose of the project is to simplify work with MOEX Spectra P2 CGate library which provides access to FORTS (Futures & Options on RTS) market on [MOEX](http://moex.com/en/) via Spectra P2 CGate library.
##Getting Started
First of all it is required to configure the application. JGate uses builder pattern fo this purpose. You need to implement 'com.stroganov.jgate.Application.Configurator' interface. For example:

'''Java
public class MyApp extends FortsFutInfoReplListenerMngmntImpl implements Application.Configurator {
    @Override
    public void configure(Application.Builder builder) throws CGateException {
        builder.newConnection(
                new Application.ConnectionSettings(
                        "FortsFutInfoConnection", Application.ConnectionType.tcp, "127.0.0.1", 4001
                )
        ).applyFortsFutInfoRepl("FortsFutInfoReplListener", null, this);
    }

    @Override
    public String getCGateSettings() {
        return "ini=jgatetest.ini;key=11111111";
    }

    @Override
    public String getAppName() {
        return "JGate.fortsFutInfoTest";
    }

    @Override
    public int onMessage(Application.JGateListener listener, FortsFutInfoRepl.fut_sess_contents msg) {
        System.out.println(String.format("Isin: %n; Description: %s", msg.get_isin_id(), msg.get_name()));
        return ErrorCode.OK;
    }

    public static void main(String[] args) {
        try {
            Application app = Application.createInstance(new MyApp());
            app.run();
            Thread.sleep(3000);
            app.stop();
        } catch (CGateException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}'''

Let's look on function 'void configure(com.stroganov.jgate.Application.Builder builder)'. Work with exchange's data streams entirely configures here. In the example we create new connection with appropriated settings using 'newConnection' function of 'com.stroganov.jgate.Application.Builder' interface. After this, we apply scheme to the connection using 'apply' family function. Here we provide listener name, listener options string (can be null) and reference to object which implements listener interface:

'''Java
    @Override
    public int onMessage(Application.JGateListener listener, FortsFutInfoRepl.fut_sess_contents msg) {
        System.out.println(String.format("Isin: %n; Description: %s", msg.get_isin_id(), msg.get_name()));
        return ErrorCode.OK;
    }
'''

Here we will receive messages from exchange's datastream.
Then we create instance of com.stroganov.jgate.Application class and run it:

'''Java
    Application app = Application.createInstance(new MyApp());
    app.run();
    Thread.sleep(3000);
    app.stop();
'''

That's all you need to start work with MOEX FORTS exchange.

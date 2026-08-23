import database.DataSeeder;
import database.DatabaseManager;
import factorypattern.FactoryDemo;
import proxy.ProxyDemo;
import chainofresponsibility.*;
import bridge.BridgeDemo;
import observer.ObserverDemo;

public class Main {

    public static void main(String[] args) {

        DatabaseManager.getInstance();
        new DataSeeder().seedAll();
        System.out.println("Database ready.\n");

        FactoryDemo.demo();

        ProxyDemo.run();

        CORDemo.main(args);

        BridgeDemo.run();

        ObserverDemo.run();

    }
}

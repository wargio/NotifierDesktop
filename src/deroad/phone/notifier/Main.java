package deroad.phone.notifier;

import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

public class Main extends Application {

    final static String appName = "Notifier";
    static String ip = null;
    static String hostname = null;
    static TrayIcon trayIcon = null;
    static PopupMenu trayMenu = null;
    static ServerSocket server = null;
    static Thread listener = null;
    static Boolean keepRunning = true;
    static HashMap<Integer, ClientInfo> clients = new HashMap<Integer, ClientInfo>();
    static int logging = 0;

    private final static Runnable notificationHandler = new Runnable() {
        @Override

        public void run() {
            Logger logger = new Logger("Handler");
            boolean running = true;
            logger.log("Listen thread ok", Logger.Level.DEBUG);
            try {
                do {
                    Socket socket = null;
                    synchronized (server) {
                        if (server == null) {
                            break;
                        }
                    }
                    try {
                        socket = server.accept();
                        String ip = socket.getInetAddress().getHostAddress();
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        Data data = (Data) in.readObject();
                        ClientInfo cinfo = null;
                        if (data != null && data.getAccess() != 0) {
                            cinfo = clients.get(data.getAccess());
                            if (cinfo != null && (data.getWho() != null || data.getMessage() != null)) {
                                logger.log(cinfo.getHostname() + ": " + data.getWho() + " - " + data.getMessage(), Logger.Level.DEBUG);
                                trayIcon.displayMessage(
                                        cinfo.getHostname() + ": " + data.getWho(),
                                        data.getMessage(),
                                        TrayIcon.MessageType.INFO
                                );
                            } else if (cinfo != null && data.getWho() == null && data.getMessage() == null) {
                                clients.remove(cinfo.getAccess());
                                String message = cinfo.getHostname() + " is now disconnected.";
                                trayIcon.displayMessage(
                                        appName,
                                        message,
                                        TrayIcon.MessageType.WARNING
                                );
                                logger.log(message, Logger.Level.WARNING);
                            } else {
                                logger.log("Bad access for ip: " + ip, Logger.Level.ERROR);
                            }
                        } else if (data != null) {
                            logger.log("New Client: " + data.getMessage() + " " + ip, Logger.Level.WARNING);
                            cinfo = new ClientInfo(ip, data.getMessage());
                            while (clients.containsKey(cinfo.getAccess())) {
                                cinfo.setAccess();
                            }
                            clients.put(cinfo.getAccess(), cinfo);
                            out.writeObject(new Data("", hostname, cinfo.getAccess()));

                            trayIcon.displayMessage(
                                    appName,
                                    data.getMessage() + " is now connected!",
                                    TrayIcon.MessageType.WARNING
                            );
                        } else {
                            logger.log("Bad data for ip: " + ip + " " + data, Logger.Level.ERROR);
                        }
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    } catch (Exception e) {
                        logger.log("Exception " + e.getMessage(), Logger.Level.ERROR);
                    }
                } while (true);
            } catch (Exception e) {
                logger.log("Exception " + e.getMessage(), Logger.Level.ERROR);
            }
            logger.log("Listen thread down", Logger.Level.DEBUG);
        }
    };

    private void createSystemTray() {
        Logger logger = new Logger("SystemTray");
        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("data/tray.png"));
        trayMenu = new PopupMenu();

        //1t menuitem for popupmenu
        MenuItem client = new MenuItem("Known clients.");
        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String cli = "";
                for (int access : clients.keySet()) {
                    ClientInfo cinfo = clients.get(access);
                    cli += cinfo.getHostname() + ": " + cinfo.getIp() + " (" + (new Date(cinfo.getTimestamp())) + ")" + "\n";
                }
                if (cli.length() == 0) {
                    cli = "No clients.";
                }
                JOptionPane.showMessageDialog(null, "Clients:\n" + cli);
            }
        });
        trayMenu.add(client);

        //2t menuitem for popupmenu
        MenuItem action = new MenuItem("About " + appName);
        action.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Created by deroad.\n\nHostname: " + hostname + "\nIP: " + ip);
            }
        });
        trayMenu.add(action);
        //3nd menuitem of popupmenu
        MenuItem close = new MenuItem("Close " + appName);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    listener.interrupt();
                    if (!server.isClosed()) {
                        server.close();
                    }
                    synchronized (server) {
                        server = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
        trayMenu.add(close);

        //setting tray icon
        trayIcon = new TrayIcon(image, appName, trayMenu);
        //adjust to default size as per system recommendation 
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(hostname + ": " + ip);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        logger.log("System tray ok", Logger.Level.DEBUG);

    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(appName);
        createSystemTray();
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 1 && args[0].equals("-d")) {
            Logger.setLevel(Logger.Level.DEBUG);
        }

        Logger logger = new Logger("Main");
        logger.log("Starting " + appName, Logger.Level.ERROR);

        //checking for support
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            ip = InetAddress.getLocalHost().getHostAddress();

            server = new ServerSocket(10550);
            listener = new Thread(notificationHandler);
            listener.start();
        } catch (Exception e) {
            logger.log("This pc is not connected to any network.", Logger.Level.ERROR);
            logger.log(e.getStackTrace().toString(), Logger.Level.ERROR);
            return;
        }
        logger.log("Now working with the tray", Logger.Level.WARNING);
        if (!SystemTray.isSupported()) {
            logger.log("System tray is not supported !!! ", Logger.Level.ERROR);
            return;
        }
        launch(args);
    }//end of main

}//end of class

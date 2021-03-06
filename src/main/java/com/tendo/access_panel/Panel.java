package com.tendo.access_panel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main Panel class
 */
public class Panel {
    public static final int TIMEOUT_AUTHORIZE = 10000;
    public static final int TIMEOUT_UDP = 100000;
    public static final int EVENT_LENGTH = 230;

    public static void main(String[] args) {
        try {
            Panel panel = new Panel();

            final String usage = "Usage: java -jar access-panel-jar-with-dependencies.jar host palm_port recv code";
            if (args.length != 4) {
                System.out.println(usage);
                return;
            }
            
            panel.exec(args[0], Short.parseShort(args[1]), Short.parseShort(args[2]), args[3]);
        } catch (IOException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setLogLevel(Level level) {
        Logger log = LogManager.getLogManager().getLogger("");
        for (Handler h : log.getHandlers()) {
            h.setLevel(level);
        }
    }

    /**
     * This method connects to registration socket, starts connection socket
     * listening thread and sends registration information
     */
    private void exec(String host, short port, short rport, String msg) throws IOException {
        setLogLevel(Level.INFO);
        
        Socket socket = new Socket();
        socket.setSoTimeout(TIMEOUT_AUTHORIZE);
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_AUTHORIZE);
        Logger.getLogger("Access-Panel").log(Level.INFO, "Connected to registration socket");

        new Thread(new RegProcessor(socket, rport)).start();

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 0);

        Logger.getLogger("Access-Panel").log(Level.INFO, "Sending registration data");
        System.arraycopy(msg.getBytes(), 0, message, 0, msg.getBytes().length);
        System.arraycopy(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(rport).array(), 0, message, 8, 2);
        socket.getOutputStream().write(message);

        System.in.read();
        UdpProcessor.get().stop();
    }
}

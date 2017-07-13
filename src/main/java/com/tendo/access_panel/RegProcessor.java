package com.tendo.access_panel;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registration receiving thread
 */
public class RegProcessor implements Runnable{
    private Socket socket;
    private short rport;
    
    public RegProcessor(Socket socket, short rport) {
        this.socket = socket;
        this.rport = rport;
    }

    @Override
    public void run() {
        try  {
            byte[] bytes = new byte[4];
            socket.getInputStream().read(bytes, 0, bytes.length);
            int result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
            
            Logger.getLogger("Access-Panel").log(Level.INFO, "registration result " + result);
            if (result == 1) {
                Logger.getLogger("Access-Panel").log(Level.INFO, "Host registered on TUCBI controller");
                UdpProcessor udp = UdpProcessor.get();
                udp.setRecvPort(rport);
                new Thread(udp).start();
            }
            else {
                Logger.getLogger("Access-Panel").log(Level.SEVERE, "Failed to register on the controller");
            }
        } catch (IOException ex) {
            Logger.getLogger(RegProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(RegProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

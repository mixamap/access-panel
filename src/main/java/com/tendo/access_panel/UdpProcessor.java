package com.tendo.access_panel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements UDP Receiving thread functionality
 */
public class UdpProcessor implements Runnable {
    private static UdpProcessor udpProcessor;

    private byte[] receiveData;
    private DatagramPacket receivePacket;
    private DatagramSocket udpSocket;
    private boolean keepRunning = true;
    private short recvPort;

    private UdpProcessor() throws SocketException, UnknownHostException {
        receiveData = new byte[Panel.EVENT_LENGTH];
        Arrays.fill(receiveData, (byte) 0);
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
    }
    
    private void printEvent(String text, InetAddress addr) {
        if (text.length() != Panel.EVENT_LENGTH) {
            System.out.println("Inalid format");
        }
        else {
            System.out.printf("IP of sender: %s%n", addr.getHostAddress());
            System.out.printf("Personal number: %s%n", text.substring(0, 20));
            System.out.printf("Name: %s%n", text.substring(20, 70));
            System.out.printf("Surname: %s%n", text.substring(90, 160));
            System.out.printf("Patronymic: %s%n", text.substring(160, 230));
        }
    }

    @Override
    public void run() {
        try {
            Logger.getLogger("Access-Panel").log(Level.INFO, "Starting UDP recv message loop");
            System.out.println("Press \"Enter\" for exit");
            
            udpSocket = new DatagramSocket(recvPort);
            udpSocket.setSoTimeout(Panel.TIMEOUT_UDP);
            while (keepRunning) {
                udpSocket.receive(receivePacket);
                printEvent(new String(receivePacket.getData()), receivePacket.getAddress());
            }
        } catch (IOException ex) {
            if (keepRunning) {
                Logger.getLogger(UdpProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        finally {
            if (udpSocket != null) {
                udpSocket.close();
            }
        }
    }

    public static UdpProcessor get() throws SocketException, UnknownHostException {
        if (udpProcessor == null) {
            synchronized (UdpProcessor.class) {
                if (udpProcessor == null) {
                    udpProcessor = new UdpProcessor();
                }
            }
        }
        
        return udpProcessor;
    }
    
    public void stop() {
        keepRunning = false;
        udpSocket.close();
    }

    public void setRecvPort(short recvPort) {
        this.recvPort = recvPort;
    }
}

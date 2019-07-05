package top.nihil;


import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

@Log
@Accessors(chain = true)
@AllArgsConstructor
public class DNSRelayServer {
    private InetSocketAddress listenAddress, remoteDNS;
    private Hosts hosts;

    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket(53, listenAddress.getAddress());
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while (true) {
            socket.receive(packet);
            DNSMessage receiveMessage = new DNSMessage(data);
            String ipAddress = hosts.getHostMap().getOrDefault(receiveMessage.getQuestion().getQNAME(), "");

            if (!ipAddress.equals("") && receiveMessage.getHeader().getQDCOUNT() == 1) {
                DNSMessage responseMessage = new DNSMessage();
                int flags = 0;
                if (ipAddress.equals("0.0.0.0")) {
                    flags = 0x8580;
                    log.info(String.format("%s is forbidden", receiveMessage.getQuestion().getQNAME()));
                } else {
                    flags = 0x8583;
                    log.info(String.format("host %s is in the host file", receiveMessage.getQuestion().getQNAME()));
                }

                responseMessage.setHeader(
                        receiveMessage.getHeader().getID(),
                        flags,
                        receiveMessage.getHeader().getQDCOUNT(),
                        1,
                        1,
                        0);

                responseMessage.setQuestion(receiveMessage.getQuestion());

                responseMessage.addAnswer(0xc00c,
                        receiveMessage.getQuestion().getQTYPE(),
                        receiveMessage.getQuestion().getQCLASS(),
                        3600 * 24,
                        4,
                        ipAddress);

                responseMessage.addAuthority(0xc00c,
                        6,
                        receiveMessage.getQuestion().getQCLASS(),
                        3600 * 24,
                        0,
                        null);

                byte[] responseBytes = responseMessage.getResponseByteArray();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);

            } else {
                DatagramSocket remoteDNSSocket = new DatagramSocket();
                log.info(String.format("Can't find the host %s", receiveMessage.getQuestion().getQNAME()));
                DatagramPacket sendPacket = new DatagramPacket(data, packet.getLength(), remoteDNS.getAddress(), remoteDNS.getPort());
                remoteDNSSocket.send(sendPacket);

                byte[] receivedData = new byte[1024];
                DatagramPacket remoteReceivePacket = new DatagramPacket(receivedData, receivedData.length);
                remoteDNSSocket.receive(remoteReceivePacket);

                DatagramPacket responsePacket = new DatagramPacket(receivedData, receivedData.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
                remoteDNSSocket.close();
            }
        }
    }

}

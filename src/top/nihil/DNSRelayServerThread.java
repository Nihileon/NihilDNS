package top.nihil;

import lombok.Data;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.*;

@Log
@Data
public class DNSRelayServerThread implements Runnable {
    byte[] data;
    Hosts hosts;
    InetSocketAddress clientAddress;
    InetSocketAddress remoteDNS;

    DNSRelayServerThread(DatagramPacket packet, Hosts hosts, InetSocketAddress remoteDNS) {
        data = packet.getData();
        clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
        this.hosts = hosts;
        this.remoteDNS = remoteDNS;
    }

    @Override
    public void run() {

        DNSMessage receiveMessage = new DNSMessage(data);
        String ipAddress = hosts.getHostMap().getOrDefault(receiveMessage.getQuestion().getQNAME(), "");
        if (!ipAddress.equals("") && receiveMessage.getHeader().getQDCOUNT() == 1) {
            DNSMessage responseMessage = new DNSMessage();
            int flags = 0;
            if (ipAddress.equals("0.0.0.0")) {
                //1000 0101 1000 0111
                //response, Authoritative Answer, Recursion Desired, Recursion Available, Refused
                flags = 0x8585;
                log.info(String.format("%s is forbidden", receiveMessage.getQuestion().getQNAME()));
            } else {
                //1000 0101 1000 0000
                //response, Authoritative Answer, Recursion Desired, Recursion Available, No error condition
                flags = 0x8580;
                log.info(String.format("host %s is mapped to %s", receiveMessage.getQuestion().getQNAME(), ipAddress));
            }

            responseMessage.setHeader(
                    receiveMessage.getHeader().getID(),
                    flags,
                    receiveMessage.getHeader().getQDCOUNT(),
                    1,
                    1,
                    0);

            responseMessage.setQuestion(
                    receiveMessage.getQuestion());

            responseMessage.addAnswer(
                    0xc00c,
                    receiveMessage.getQuestion().getQTYPE(),
                    receiveMessage.getQuestion().getQCLASS(),
                    1200,
                    4,
                    ipAddress);

            responseMessage.addAuthority(
                    0xc00c,
                    DNSResourceRecord.QTYPE_SOA,
                    receiveMessage.getQuestion().getQCLASS(),
                    1200,
                    0,
                    null);

            byte[] responseBytes = responseMessage.getResponseByteArray();
            DatagramPacket responsePacket = new DatagramPacket(
                    responseBytes, responseBytes.length, clientAddress);
            synchronized (DNSRelayServer.mLock) {
                try {
                    DNSRelayServer.socket.send(responsePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                DatagramSocket remoteDNSSocket = new DatagramSocket();
                //超时
                remoteDNSSocket.setSoTimeout(2000);
//                log.info(String.format("Can't find the host %s", receiveMessage.getQuestion().getQNAME()));
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, remoteDNS);
                remoteDNSSocket.send(sendPacket);
                byte[] receivedData = new byte[1024];
                DatagramPacket remoteReceivePacket = new DatagramPacket(receivedData, receivedData.length);
                try {
                    remoteDNSSocket.receive(remoteReceivePacket);
                    remoteDNSSocket.close();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
                DatagramPacket responsePacket = new DatagramPacket(receivedData, receivedData.length, clientAddress);
                synchronized (DNSRelayServer.mLock) {
                    DNSRelayServer.socket.send(responsePacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

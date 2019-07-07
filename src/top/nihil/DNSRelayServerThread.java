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
    InetSocketAddress client;
    InetSocketAddress remoteDNS;

    DNSRelayServerThread(DatagramPacket packet, Hosts hosts, InetSocketAddress remoteDNS) {
        data = packet.getData();
        client = new InetSocketAddress(packet.getAddress(), packet.getPort());
        this.hosts = hosts;
        this.remoteDNS = remoteDNS;
    }

    @Override
    public void run() {
        DNSMessage clientMessage = new DNSMessage(data);
        String ipAddress = hosts.getHostMap()
                .getOrDefault(clientMessage.getQuestion().getQNAME(), "");

        if (!ipAddress.equals("") && clientMessage.getHeader().getQDCOUNT() == 1)
            inHostResponse(clientMessage, ipAddress);
        else
            notInHostResponse(clientMessage);
    }

    private void inHostResponse(DNSMessage clientMessage, String ipAddress) {
        DNSMessage responseMessage = new DNSMessage();

        int flags = 0;
        if (ipAddress.equals("0.0.0.0")) {
            //1000 0101 1000 0111
            //response, Authoritative Answer, Recursion Desired, Recursion Available, Refused
            flags = 0x8585;
            log.info(String.format("%s is forbidden", clientMessage.getQuestion().getQNAME()));
        } else {
            //1000 0101 1000 0000
            //response, Authoritative Answer, Recursion Desired, Recursion Available, No error condition
            flags = 0x8580;
            log.info(String.format("host %s is mapped to %s", clientMessage.getQuestion().getQNAME(), ipAddress));
        }

        responseMessage.setHeader(
                clientMessage.getHeader().getID(),
                flags,
                clientMessage.getHeader().getQDCOUNT(),
                1,
                1,
                0);

        responseMessage.setQuestion(
                clientMessage.getQuestion());

        responseMessage.addAnswer(
                0xc00c,
                clientMessage.getQuestion().getQTYPE(),
                clientMessage.getQuestion().getQCLASS(),
                1200,
                4,
                ipAddress);

        responseMessage.addAuthority(
                0xc00c,
                DNSResourceRecord.QTYPE_SOA,
                clientMessage.getQuestion().getQCLASS(),
                1200,
                0,
                null);

        byte[] responseBytes = responseMessage.getResponseByteArray();
        DatagramPacket responsePacket = new DatagramPacket(
                responseBytes, responseBytes.length, client);
        synchronized (DNSRelayServer.mLock) {
            try {
                DNSRelayServer.socket.send(responsePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void notInHostResponse(DNSMessage clientMessage) {
//      log.info(String.format("Can't find %s in default host file", clientMessage.getQuestion().getQNAME()));
        byte[] result;
        result = DNSRelayServer.cache.getCache(clientMessage);

        try {
            if (result == null) {
                DatagramSocket remoteDNSSocket = new DatagramSocket();
                //timeout
                remoteDNSSocket.setSoTimeout(2000);
                DatagramPacket sendRemoteDNSPacket = new DatagramPacket(data, data.length, remoteDNS);
                remoteDNSSocket.send(sendRemoteDNSPacket);

                result = new byte[1024];
                DatagramPacket remoteReceivePacket = new DatagramPacket(result, result.length);
                remoteDNSSocket.receive(remoteReceivePacket);
                remoteDNSSocket.close();

                DNSMessage responseMessage = new DNSMessage(result);
                int HeadQuesLength = clientMessage.getHeaderAndQuestionLength();
                if (Converter.byteArrayToUnsignedShort(result, HeadQuesLength + 2) ==
                        DNSResourceRecord.QTYPE_A) {
                    responseMessage.setDnsMessageBytes(result);
                    DNSRelayServer.cache.addCache(responseMessage);
                }
            }

            DatagramPacket responsePacket = new DatagramPacket(result, result.length, client);
            synchronized (DNSRelayServer.mLock) {
                DNSRelayServer.socket.send(responsePacket);
            }
        } catch (SocketTimeoutException e) {
            log.info("Remote DNS receive timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

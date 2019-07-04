package top.nihil;

import com.oracle.tools.packager.Log;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

public class DNSRelayServer {
    private InetSocketAddress listenAddress, remoteDNS;
    private Hosts hosts;

    public DNSRelayServer(InetSocketAddress listenAddress, InetSocketAddress remoteDNS, Hosts hosts) {
        this.listenAddress = listenAddress;
        this.remoteDNS = remoteDNS;
        this.hosts = hosts;
    }

    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket(listenAddress);
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while (true) {
            socket.receive(packet);
            int offset = 0;
            DNSHeader dnsHeader = new DNSHeader();
            DNSQuestion dnsQuestion = new DNSQuestion();
            dnsHeader.setID(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            dnsHeader.setFlags(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2))).flagsTobits();
            offset += 2;
            dnsHeader.setQDCOUNT(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            dnsHeader.setANCOUNT(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            dnsHeader.setNSCOUNT(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            dnsHeader.setARCOUNT(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            if (dnsHeader.getQDCOUNT() > 0) {
                String domainName = Converter.byteArrayToDomainName(null, data, offset);
                dnsQuestion.setQNAME(domainName);
                offset += domainName.length() + 2;
                dnsQuestion.setQTYPE(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
                offset += 2;
                dnsQuestion.setQCLASS(Converter.byteArrayToShort(Arrays.copyOfRange(data, offset, offset + 2)));
                offset += 2;
            } else {
                Log.info("No QDCOUNT error");
            }

            String ipAddress = hosts.getHostMap().getOrDefault(dnsQuestion.getQNAME(), "");
            Log.info(ipAddress);
            if (!ipAddress.equals("") && dnsHeader.getQDCOUNT() == 1) {
                int flags = 0;
                if (ipAddress.equals("0.0.0.0")) {
                    flags = 0x8580;
                }else {
                    flags = 0x8583;
                }

                DNSHeader responseHeader = new DNSHeader(
                        dnsHeader.getID(),flags,dnsHeader.getQDCOUNT(),1,1,0);
                byte[] headerBytes = responseHeader.toByteArray();
                byte[] questionBytes = dnsQuestion.toByteArray();
                DNSResourceRecord responseAnswer = new DNSResourceRecord(
                        0xc00c, dnsQuestion.getQTYPE(), dnsQuestion.getQCLASS(), 3600*24, (short) 4, ipAddress);
                byte[] answerBytes = responseAnswer.toByteArray();
                DNSResourceRecord responseAuth = new DNSResourceRecord(0xc00c,  6, dnsQuestion.getQCLASS(), 3600*24, (short) 0 , null);
                byte[] authBytes = responseAuth.toByteArray();
                byte[] responseBytes = new byte[headerBytes.length+questionBytes.length+answerBytes.length+authBytes.length];
                int responseOffset = 0;
                System.arraycopy(headerBytes,0,responseBytes,responseOffset,headerBytes.length);
                responseOffset+=headerBytes.length;
                System.arraycopy(questionBytes,0,responseBytes,responseOffset,questionBytes.length);
                responseOffset+=questionBytes.length;
                if(!ipAddress.equals("0.0.0.0")){
                    System.arraycopy(answerBytes,0,responseBytes,responseOffset,answerBytes.length);
                    responseOffset+=answerBytes.length;
                }
                System.arraycopy(authBytes,0,responseBytes,responseOffset,authBytes.length);
                DatagramPacket responsePacket = new DatagramPacket(responseBytes,responseBytes.length,packet.getAddress(),packet.getPort());
                socket.send(responsePacket);

            } else {
                DatagramSocket remoteDNSSocket = new DatagramSocket(remoteDNS);
                DatagramPacket sendPacket = new DatagramPacket(data, packet.getLength());
                remoteDNSSocket.send(sendPacket);
                byte[] receivedData = new byte[1024];
                DatagramPacket internetReceivedPacket = new DatagramPacket(receivedData, receivedData.length);
                remoteDNSSocket.receive(internetReceivedPacket);

                DatagramPacket responsePacket = new DatagramPacket(receivedData, internetReceivedPacket.getLength(), listenAddress);
                socket.send(responsePacket);
                remoteDNSSocket.close();
            }
        }
    }

}

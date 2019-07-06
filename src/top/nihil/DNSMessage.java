package top.nihil;

import lombok.Data;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    +---------------------+
    |        Header       |
    +---------------------+
    |       Question      | the question for the name server
    +---------------------+
    |        Answer       | RRs answering the question
    +---------------------+
    |      Authority      | RRs pointing toward an authority
    +---------------------+
    |      Additional     | RRs holding additional information
    +---------------------+
 */
@Data
@Log
public class DNSMessage {
    /*
    \todo  id 不对，需要将 message 里面的 id 修改为正确的部分。
     */
    private DNSHeader header;
    private DNSQuestion question;
    private List<DNSResourceRecord> answers;
    private List<DNSResourceRecord> authorities;
    private int headerAndQuestionLength;
    byte[] dnsMessageBytes;

    DNSMessage() {
        header = new DNSHeader();
        question = new DNSQuestion();
        answers = new ArrayList<>();
        authorities = new ArrayList<>();
    }

    public DNSMessage(byte[] data) {
        header = new DNSHeader();
        question = new DNSQuestion();
        answers = new ArrayList<>();
        authorities = new ArrayList<>();

        int offset = 0;
        header.setID(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
        offset += 2;
        header.setFlags(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2))).flagsToBits();
        offset += 2;
        header.setQDCOUNT(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
        offset += 2;
        header.setANCOUNT(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
        offset += 2;
        header.setNSCOUNT(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
        offset += 2;
        header.setARCOUNT(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
        offset += 2;
        if (header.getQDCOUNT() > 0) {
            String domainName = Converter.byteArrayToDomainName(null, data, offset);
            question.setQNAME(domainName);
            offset += domainName.length() + 2;
            question.setQTYPE(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            question.setQCLASS(Converter.byteArrayToUnsignedShort(Arrays.copyOfRange(data, offset, offset + 2)));
            offset += 2;
            headerAndQuestionLength = offset;
        } else {
            log.info("No QDCOUNT error");
        }
    }

    void setHeader(int ID, int flags, int QDCOUNT, int ANCOUNT, int NSCOUNT, int ARCOUNT) {
        header.setID(ID);
        header.setFlags(flags).flagsToBits();
        header.setQDCOUNT(QDCOUNT);
        header.setANCOUNT(ANCOUNT);
        header.setNSCOUNT(NSCOUNT);
        header.setARCOUNT(ARCOUNT);
    }

    void addAnswer(int NAME, int TYPE, int CLASS, long TTL, int RDLENGTH, String RDATA) {
        DNSResourceRecord dnsAnswer = new DNSResourceRecord(NAME, TYPE, CLASS, TTL, RDLENGTH, RDATA);
        answers.add(dnsAnswer);
    }

    void addAuthority(int NAME, int TYPE, int CLASS, long TTL, int RDLENGTH, String RDATA) {
        DNSResourceRecord dnsAuthority = new DNSResourceRecord(NAME, TYPE, CLASS, TTL, RDLENGTH, RDATA);
        authorities.add(dnsAuthority);
    }

    byte[] getResponseByteArray() {
        int length = 0;
        byte[] headerBytes = header.toByteArray();
        length += headerBytes.length;

        byte[] questionBytes = question.toByteArray();
        length += questionBytes.length;

        assert answers.size() == header.getANCOUNT();
        List<byte[]> answersList = new ArrayList<>();
        for (DNSResourceRecord rr : answers) {
            byte[] temp = rr.toByteArray();
            answersList.add(temp);
            length += temp.length;
        }

        assert authorities.size() == header.getNSCOUNT();
        List<byte[]> authoritiesList = new ArrayList<>();
        for (DNSResourceRecord rr : authorities) {
            byte[] temp = rr.toByteArray();
            authoritiesList.add(temp);
            length += temp.length;
        }

        byte[] response = new byte[length];
        int offset = 0;

        System.arraycopy(headerBytes, 0, response, offset, headerBytes.length);
        offset += headerBytes.length;

        System.arraycopy(questionBytes, 0, response, offset, questionBytes.length);
        offset += questionBytes.length;

        for (byte[] ans : answersList) {
            System.arraycopy(ans, 0, response, offset, ans.length);
            offset += ans.length;
        }

        for (byte[] aut : authoritiesList) {
            System.arraycopy(aut, 0, response, offset, aut.length);
            offset += aut.length;
        }

        return response;
    }

    void setMessageBytesID(int ID) {
        System.arraycopy(Converter.shortToByteArray(ID), 0, dnsMessageBytes, 0, 2);
    }
}

package top.nihil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;

import java.util.Iterator;
import java.util.LinkedList;

@Log
public class Cache {

    @AllArgsConstructor
    @Data
    private
    class Item {
        private DNSMessage message;
        private long timeout;
    }

    private final LinkedList<Cache.Item> items = new LinkedList<>();

    private long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    byte[] getCache(DNSMessage message) {
        DNSQuestion question = message.getQuestion();
        DNSHeader header = message.getHeader();
        long currentTime = getCurrentTime();
//        log.info(String.format("%d", items.size()));

        Iterator<Cache.Item> iter = items.iterator();
        while (iter.hasNext()) {
            Cache.Item item = iter.next();
            if (item.getTimeout() < currentTime) {
                iter.remove();
                continue;
            }
            DNSQuestion itemQuestion = item.getMessage().getQuestion();
            if (question.getQNAME().equals(itemQuestion.getQNAME()) &&
                    question.getQTYPE() == itemQuestion.getQTYPE() &&
                    question.getQCLASS() == itemQuestion.getQCLASS()) {
                log.info("hit cache");
                item.getMessage().setMessageBytesID(header.getID());
                return item.getMessage().getDnsMessageBytes();
            }
        }
        return null;
    }

    void addCache(DNSMessage message) {
        DNSQuestion question = message.getQuestion();
        long currentTime = getCurrentTime();

        Iterator<Cache.Item> iter = items.iterator();
        while (iter.hasNext()) {
            DNSQuestion itemQuestion = iter.next().getMessage().getQuestion();

            if (question.getQNAME().equals(itemQuestion.getQNAME()) &&
                    question.getQTYPE() == itemQuestion.getQTYPE() &&
                    question.getQCLASS() == itemQuestion.getQCLASS()) {
                log.info("update cache");
                iter.remove();
            }
        }

        long itemTimeout = Converter.byteArrayToUnsignedInt(
                                message.getDnsMessageBytes(),
                                message.getHeaderAndQuestionLength() + 6
                            ) + getCurrentTime();
        Cache.Item item = new Cache.Item(message, itemTimeout);
        items.add(item);
    }
}

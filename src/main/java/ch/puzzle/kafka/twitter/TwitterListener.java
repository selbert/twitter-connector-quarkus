package ch.puzzle.kafka.twitter;

import io.vertx.core.impl.ConcurrentHashSet;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@ApplicationScoped
public class TwitterListener implements StatusListener, Publisher<String> {

    private Set<Subscriber<? super String>> subscribers = new ConcurrentHashSet<>();

    private final Logger logger = Logger.getLogger(TwitterListener.class.getName());


    public void onStatus(Status status) {
        String text = status.getText();
        String filtered = Optional.ofNullable(text)
                .map(this::filterHashTags)
                .map(this::filterSymbols)
                .orElse("");
        logger.info("subscribers: " + subscribers.size() + ", text: " + text + " -> " + filtered);
        try {
            subscribers.forEach(s -> s.onNext(filtered));
        } catch (Exception e) {
            logger.severe("onNextException: " + e.getMessage());
        }
    }

    private String filterSymbols(String s) {
        return s;
    }

    private String filterHashTags(String s) {
        return s.replaceAll("\\#[a-zA-Z]+\\b","");
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        logger.info("onDeletionNotice: " + statusDeletionNotice);
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        logger.info("onTrackLimitationNotice: " + numberOfLimitedStatuses);
    }

    public void onScrubGeo(long l, long l1) {
        logger.info("onScrubGeo: l: " + l + "; l1: " + l1);
    }

    public void onStallWarning(StallWarning stallWarning) {
        logger.info("onStallWarning: " + stallWarning.getMessage());
    }

    public void onException(Exception ex) {
        logger.severe("onException: " + ex.getMessage());
    }

    @Override
    public void subscribe(Subscriber<? super String> subscriber) {
        logger.info("New subscriber: " + subscriber);
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {

            }

            @Override
            public void cancel() {
                removeSubscriber(subscriber);
            }
        });
        subscribers.add(subscriber);
    }

    private void removeSubscriber(Subscriber<? super String> subscriber) {
        logger.info("Removed subscriber: " + subscriber);
        subscribers.remove(subscriber);
    }
}
package ch.puzzle.kafka.twitter;

import io.reactivex.Flowable;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TweetsPusher {

    @ConfigProperty(name = "twitter4j.oauth.consumerKey")
    String consumerKey;
    @ConfigProperty(name = "twitter4j.oauth.consumerSecret")
    String consumerSecret;
    @ConfigProperty(name = "twitter4j.oauth.accessToken")
    String accessToken;
    @ConfigProperty(name = "twitter4j.oauth.accessTokenSecret")
    String accessTokenSecret;
    @ConfigProperty(name = "application.filters")
    String filters;

    @Inject
    TwitterListener listener;

    private TwitterStream twitterStream;

    @PostConstruct
    public void init() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance()
                .addListener(listener)
                .filter(filters.split(","));
    }

    @PreDestroy
    public void cleanup() {
        twitterStream.shutdown();
    }

    @Outgoing("chatmessages")
    public Flowable<String> generate() {
        return Flowable.fromPublisher(listener);
    }

}
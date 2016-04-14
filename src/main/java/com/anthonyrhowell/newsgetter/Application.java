package com.anthonyrhowell.newsgetter;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Application extends SpeechletRequestStreamHandler {



    public Application() {
        super(new NewsSpeechlet(), getAppIds());
    }

    private static Set<String> getAppIds() {
        Set appIds = new HashSet<>();
        appIds.add("amzn1.echo-sdk-ams.app.e073dcb1-6645-4fc5-a6dd-ffa65687cf52");
        return appIds;
    }

    public static void main(String[] args) throws IOException, FeedException {

    }


}

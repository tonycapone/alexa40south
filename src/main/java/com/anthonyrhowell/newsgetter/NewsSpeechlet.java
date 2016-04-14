package com.anthonyrhowell.newsgetter;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class NewsSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(NewsSpeechlet.class);
    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("GetHeadlines".equals(intentName)) {
            return getHelloResponse();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse getHelloResponse()  {

        String speechText = null;
        try {
            speechText = parseFeed();

        } catch (FeedException e) {
            e.printStackTrace();
            log.error(String.valueOf(e));
            speechText = "error";
        } catch (IOException e) {
            e.printStackTrace();
            log.error(String.valueOf(e));
            speechText = "error";
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("FortySouth");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {

    }

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Forty South News getter";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("FortySouth");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private String  parseFeed() throws IOException, FeedException {
        URL feedUrl = new URL("http://40southnews.com/feed/");

        SyndFeedInput feedInput = new SyndFeedInput();
        SyndFeed feed = feedInput.build(new XmlReader(feedUrl));

        StringBuilder stringBuilder = new StringBuilder();

        feed.getEntries().forEach(syndEntry -> stringBuilder.append(syndEntry.getTitle()).append(", "));

        return stringBuilder.toString();

    }

}

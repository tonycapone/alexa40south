package com.anthonyrhowell.newsgetter;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

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
            return getHeadlines();
        } else if ("ReadHeadline".equals(intentName)){
            if(intent.getSlot("Index").getValue() != null && !intent.getSlot("Index").getValue().equals("")){
                return getHeadlineByIndex(intent.getSlot("Index").getValue());
            } else if (intent.getSlot("Token").getValue() != null && !intent.getSlot("Token").getValue().equals("")){
                return getHeadlineByContains(intent.getSlot("Token"));
            }
            return getSpeechletResponse("Something went wrong. Sorry bro");
        }
        else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse getHeadlineByContains(Slot token) {

        Optional<SyndEntry> entry = null;
        try {
            List<SyndEntry> entries = parseFeed();
            entry = entries.stream().filter(eachEntry -> eachEntry.getTitle().toLowerCase().contains(token.getValue())).findFirst();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        }
        return getSpeechletResponse(entry.isPresent() ? entry.get().getDescription().getValue() : "Sorry, no headline matched that description");
    }

    private SpeechletResponse getHeadlineByIndex(String index) {
        SyndEntry syndEntry = null;
        try {
             syndEntry = parseFeed().get(Integer.valueOf(index) + -1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        }

        return getSpeechletResponse(syndEntry.getDescription().getValue());
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {

    }

    private SpeechletResponse getHeadlines()  {

        String speechText = null;
        try {
            speechText = getHeadlinesAsString();

        } catch (FeedException e) {
            e.printStackTrace();
            log.error(String.valueOf(e));
            speechText = "error";
        } catch (IOException e) {
            e.printStackTrace();
            log.error(String.valueOf(e));
            speechText = "error";
        }
        return getSpeechletResponse(speechText);


    }

    private SpeechletResponse getSpeechletResponse(String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("FortySouth");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private String getHeadlinesAsString() throws IOException, FeedException {
        String speechText;List<SyndEntry> syndEntries = parseFeed();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Here are the top headlines: ");
        syndEntries.forEach(syndEntry -> stringBuilder.append(syndEntry.getTitle()).append(". "));
        speechText = stringBuilder.toString();
        return speechText;
    }

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to Forty South News";

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

    private List<SyndEntry> parseFeed() throws IOException, FeedException {
        URL feedUrl = new URL("http://40southnews.com/feed/");

        SyndFeedInput feedInput = new SyndFeedInput();
        SyndFeed feed = feedInput.build(new XmlReader(feedUrl));

        return feed.getEntries();

    }

}

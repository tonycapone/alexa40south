package com.anthonyrhowell.newsgetter;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewsSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(NewsSpeechlet.class);
    private String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private String WEEK_DATE_FORMAT = "yyy-'W'w";

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
            return getHeadlines(intent);
        } else if ("ReadHeadline".equals(intentName)){
            if(checkSlot(intent, "Index")){
                return getHeadlineByIndex(intent.getSlot("Index").getValue());
            } else if (checkSlot(intent, "Ordinal")){
                return getHeadlineByOrdinal(intent.getSlot("Ordinal").getValue());
            }
            return getSpeechletResponse("Something went wrong. Sorry bro");
        }
        else {
            throw new SpeechletException("Invalid Intent");
        }
    }


    private boolean checkSlot(Intent intent, String slotName) {
        return intent.getSlot(slotName).getValue() != null && !intent.getSlot(slotName).getValue().equals("");
    }
    private SpeechletResponse getHeadlineByOrdinal(String ordinal) {
        return getHeadlineByIndex(Ordinal.getInt(ordinal));
    }

    private SpeechletResponse getHeadlineByIndex(String index) {
        return getHeadlineByIndex(Integer.valueOf(index));
    }

    private SpeechletResponse getHeadlineByIndex(int index) {
        SyndEntry syndEntry = null;
        try {
            syndEntry = parseFeed().get(index + -1);
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

    private SpeechletResponse getHeadlines(Intent intent)  {

        return getSpeechletResponse(getHeadlinesAsString(intent));

    }

    private SpeechletResponse getSpeechletResponse(String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("FortySouth");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
        log.info("Returning speech: \n" + speechText);
        return SpeechletResponse.newTellResponse(speech, card).should;
    }

    private String getHeadlinesAsString(Intent intent) {
        String speechText;
        List<SyndEntry> syndEntries = null;
        try {
            syndEntries = parseFeed();
        } catch (IOException e) {
            e.printStackTrace();
            return "Sorry, I was not able to contact 40 South News";
        } catch (FeedException e) {
            e.printStackTrace();
            return "Sorry, I was not able to contact 40 South News";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Date forDate = getDate(intent);
        PrettyTime p = new PrettyTime(DateUtils.addDays(DateTime.now().toDate(), -1));
        stringBuilder.append("Here are the headlines");
        if (forDate.after(DateUtils.addDays(DateTime.now().toDate(), -7))) {
            stringBuilder.append(" from " + p.format(forDate));
        }
        stringBuilder.append(". ");
        syndEntries.stream().filter(entry -> entry.getPublishedDate()
                .after(forDate))
                .forEach(syndEntry -> stringBuilder.append(syndEntry.getTitle())
                        .append(". "));
        speechText = stringBuilder.toString();
        return speechText;
    }

    private Date getDate(Intent intent) {
        String date = intent.getSlot("Date").getValue();

        // If date not specified, get all headlines
        if (date == null){
            log.info("No date specified; returning all headlines");
            return new Date(1);
        }
        return parseDateFromString(date);
    }

    private Date parseDateFromString(String dateString)  {
        String format = DEFAULT_DATE_FORMAT;
        if(dateString.contains("W")){
            format = WEEK_DATE_FORMAT;
        }

        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            Date date = df.parse(dateString);
            log.info("Returning headlines since " + date.toString());
            return date;
        } catch (ParseException e) {
            log.error("Unable to parse date " + dateString, e);
            return null;
        }
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

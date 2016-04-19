package com.anthonyrhowell.newsgetter;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class NewsSpeechletTest {

    private NewsSpeechlet newsSpeechlet = new NewsSpeechlet();

    @Test
    public void testGetHeadlines() throws Exception {
        IntentRequest intentRequest = mock(IntentRequest.class);
        Session session = Session.builder().withSessionId("test").build();

        Map<String, Slot> slots =  new HashMap<>();
        slots.put("Date", Slot.builder().withName("Date").build());

        Intent intent = Intent.builder().withName("GetHeadlines").withSlots(slots).build();
        when(intentRequest.getIntent()).thenReturn(intent);

        SpeechletResponse speechletResponse = newsSpeechlet.onIntent(intentRequest, session);

    }

    @Test
    public void testReadHeadlineByIndex() throws Exception {
        IntentRequest intentRequest = mock(IntentRequest.class);
        Session session = Session.builder().withSessionId("test").build();

        Map<String, Slot> slots =  new HashMap<>();
        slots.put("Index", Slot.builder().withName("Index").withValue("2").build());

        Intent intent = Intent.builder().withName("ReadHeadline").withSlots(slots).build();
        when(intentRequest.getIntent()).thenReturn(intent);

        SpeechletResponse speechletResponse = newsSpeechlet.onIntent(intentRequest, session);
    }

    @Test
    public void testReadHeadlineByOrdinal() throws Exception {
        IntentRequest intentRequest = mock(IntentRequest.class);
        Session session = Session.builder().withSessionId("test").build();

        HashMap<String, String> slotMap = new HashMap<>();
        slotMap.put("Ordinal", "1st");
        Map<String, Slot> slots = buildSlotsForIntent("ReadHeadline", slotMap);

        Intent intent = Intent.builder().withName("ReadHeadline").withSlots(slots).build();
        when(intentRequest.getIntent()).thenReturn(intent);

        SpeechletResponse speechletResponse = newsSpeechlet.onIntent(intentRequest, session);
    }

    @Test
    public void testGetHeadlinesByDate() throws Exception {
        IntentRequest intentRequest = mock(IntentRequest.class);
        Session session = Session.builder().withSessionId("test").build();

        Map<String, Slot> slots =  new HashMap<>();
        slots.put("Date", Slot.builder().withName("Date").withValue("2016-4-14").build());
        Intent intent = Intent.builder().withName("GetHeadlines").withSlots(slots).build();
        when(intentRequest.getIntent()).thenReturn(intent);

        SpeechletResponse speechletResponse = newsSpeechlet.onIntent(intentRequest, session);
    }

    @Test
    public void testGetHeadlinesByWeek() throws Exception {
        IntentRequest intentRequest = mock(IntentRequest.class);
        Session session = Session.builder().withSessionId("test").build();

        HashMap<String, String> slotMap = new HashMap<>();
        slotMap.put("Date", "2016-W15");
        Map<String, Slot> slots = buildSlotsForIntent("GetHeadlines", slotMap);

        Intent intent = Intent.builder().withName("GetHeadlines").withSlots(slots).build();
        when(intentRequest.getIntent()).thenReturn(intent);

        SpeechletResponse speechletResponse = newsSpeechlet.onIntent(intentRequest, session);
    }

    private Map<String, Slot> buildSlotsForIntent(String intent, Map<String, String> slotAndValue) {
        Map<String, Slot> slots =  new HashMap<>();
        if(intent.equals("GetHeadlines")){
            slots.put("Date", Slot.builder().withName(slotAndValue.get("Date")).withValue(slotAndValue.get("Date")).build());
        } else if (intent.equals("ReadHeadline")){
            slots.put("Index", Slot.builder().withName("Index").withValue(slotAndValue.get("Index")).build());
            slots.put("Ordinal", Slot.builder().withName("Ordinal").withValue(slotAndValue.get("Ordinal")).build());
        }
        return slots;
    }




}
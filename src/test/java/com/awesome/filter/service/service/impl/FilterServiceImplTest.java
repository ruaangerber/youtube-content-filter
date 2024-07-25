package com.awesome.filter.service.service.impl;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.openai.OpenAiChatModel;

import com.awesome.filter.service.model.internal.FilteredText;
import com.awesome.filter.service.model.request.FilterRequest;
import com.awesome.filter.service.service.WordlistService;

import io.github.thoroldvix.api.YoutubeTranscriptApi;

class FilterServiceImplTest {

    private FilterServiceImpl filterService;

    @Mock
    private OpenAiChatModel chatModel;

    @Mock
    private YoutubeTranscriptApi youtubeTranscriptApi;

    @Mock
    private WordlistService wordlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterService = new FilterServiceImpl(chatModel, youtubeTranscriptApi, wordlistService);
    }

    @AfterEach
    void tearDown() {
        filterService = null;
    }

    @Test
    void isAllowed_givenListOfEntries_whenAnEntryIsTrue_thenReturnFalse() {
        Assertions.assertFalse(filterService.isAllowed(List.of(
            FilteredText.builder().blocked(Boolean.FALSE).build(),
            FilteredText.builder().blocked(Boolean.TRUE).build())),
            "Expected the presence of a TRUE value to cause FALSE to be returned");
    }

    @Test
    void isAllowed_givenListOfEntries_whenAllEntryAreTrue_thenReturnFalse() {
        Assertions.assertFalse(filterService.isAllowed(List.of(
            FilteredText.builder().blocked(Boolean.TRUE).build(),
            FilteredText.builder().blocked(Boolean.TRUE).build())),
            "Expected the presence of a TRUE value to cause FALSE to be returned");
    }

    @Test
    void isAllowed_givenListOfEntries_whenAllEntryAreFalse_thenReturnTrue() {
        Assertions.assertTrue(filterService.isAllowed(List.of(
            FilteredText.builder().blocked(Boolean.FALSE).build(),
            FilteredText.builder().blocked(Boolean.FALSE).build())),
            "Expected the presence of no TRUE value to cause TRUE to be returned");
    }

    @Test
    void getVideoId_whenVideoIdIsAMatch_thenReturnVideoId() {
        var response = filterService.getVideoId(FilterRequest.builder().url("https://www.youtube.com/watch?v=R3DzIqNOstY").build());

        Assertions.assertEquals("R3DzIqNOstY", response);
    }

}

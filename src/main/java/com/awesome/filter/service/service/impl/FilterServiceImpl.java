package com.awesome.filter.service.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import com.awesome.filter.service.model.internal.FilteredText;
import com.awesome.filter.service.model.request.FilterRequest;
import com.awesome.filter.service.model.response.FilterResponse;
import com.awesome.filter.service.service.FilterService;
import com.awesome.filter.service.service.WordlistService;

import io.github.thoroldvix.api.TranscriptContent;
import io.github.thoroldvix.api.TranscriptRetrievalException;
import io.github.thoroldvix.api.YoutubeTranscriptApi;
import io.github.thoroldvix.api.TranscriptContent.Fragment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilterServiceImpl implements FilterService {
    
    private final OpenAiChatModel chatModel;

    private final YoutubeTranscriptApi youtubeTranscriptApi;

    private final WordlistService wordlistService;

    private static final String AI_SYSTEM_MESSAGE = "You are a content moderator that only responds with \"true\" if the content is allowed, or \"false\" if the content is not allowed, with a reason separated by a comma with no spaces. The text contains only partial phrases, so look at the context within the phrase. \"incomplete content\" and variations thereof must not be used as a reason for blocking content. Ignore content that is scientific. Be lenient towards incomplete content.";

    private static final String AI_USER_MESSAGE = "Moderate the following content and respond as instructed if it is suitable for a child of age 12: \"%s\"";

    private static final String YOUTUBE_REGEX = "(v=[^&.]+)";

    @Override
    public FilterResponse filter(final FilterRequest request) {

        if (Objects.nonNull(request.getUrl()) || Objects.nonNull(request.getVideoId())) {

            var videoId = getVideoId(request);

            if (Objects.isNull(videoId)) {
                return FilterResponse.builder().allowed(Boolean.TRUE).build();
            }

            TranscriptContent content;

            try {
                content = youtubeTranscriptApi.getTranscript(videoId, "en");
            } catch (TranscriptRetrievalException exception) {
                throw new RuntimeException("Could not get transcript", exception);
            }

            log.info("Found transcript for videoId: [{}]: \n{}", videoId, getContent(content.getContent()));

            var hits = content.getContent().parallelStream().flatMap((fragment) -> 
                wordlistService.match(fragment.getText()).stream()).toList();

            var deduplicatedHits = hits.stream().filter(distinctByKey((hit) -> hit.getText())).toList();

            var aiHits = deduplicatedHits.stream().filter((hit) -> hit.getBlocked())
                .map((hit) -> applyAiFilter(hit.getText())).toList();
                
            var isAllowed = isAllowed(aiHits);

            log.info("isAllowed: [{}]", isAllowed);

            return FilterResponse.builder().allowed(isAllowed).matches(
                aiHits.stream().filter((hit) -> hit.getBlocked()).map((hit) -> hit.getText()).toList()).build();
        }

        return FilterResponse.builder().allowed(Boolean.TRUE).build();
    }

    protected boolean isAllowed(final List<FilteredText> entries) {
        return !entries.stream().collect(Collectors.groupingBy(FilteredText::getBlocked)).keySet().stream().anyMatch((blocked) -> Boolean.TRUE.equals(blocked));
    }

    protected static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        var map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private FilteredText applyAiFilter(final String text) {
        var systemMessage = new SystemMessage(AI_SYSTEM_MESSAGE);
        var userMessage = new UserMessage(AI_USER_MESSAGE);

        var prompt = new Prompt(List.of(systemMessage, userMessage));
        var response = chatModel.call(prompt);

        if (Objects.nonNull(response.getResult().getOutput().getContent())) {
            log.info("Response from OpenAI for text: [{}]: [{}]", text, response.getResult().getOutput().getContent());

            var responseFragments = response.getResult().getOutput().getContent().split(",");

            if ("true".equals(responseFragments[0])) {
                return FilteredText.builder().blocked(Boolean.FALSE).build();
            }
        }

        return FilteredText.builder().blocked(Boolean.TRUE).text(text).build();
    }

    protected String getVideoId(final FilterRequest request) {
        if (Objects.nonNull(request.getVideoId())) {
            return request.getVideoId();
        }

        var pattern = Pattern.compile(YOUTUBE_REGEX);
        var matcher = pattern.matcher(request.getUrl());

        log.info("Filtering URL: [{}]", request.getUrl());

        if (!request.getUrl().contains("v=")) {
            return null;
        }

        if (matcher.find()) {
            return matcher.group(0).replace("v=", "");
        }

        return null;
    }

    private String getContent(final List<Fragment> fragments) {
        return fragments.stream().map(Fragment::getText).collect(Collectors.joining(","));
    }

}

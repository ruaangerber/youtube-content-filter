package com.awesome.filter.service.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.awesome.filter.service.model.internal.FilteredText;
import com.awesome.filter.service.service.WordlistService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WordlistServiceImpl implements WordlistService {

    private static final String FILE = "src/main/resources/words.txt";

    private static final List<String> WORDS;

    static {
        try {
            WORDS = Arrays.asList(Files.readString(Path.of(FILE)).split(","));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load wordlist", exception);
        }
    }

    @Override
    public List<FilteredText> match(final String text) {
        return WORDS.parallelStream().filter((word) -> {
            var contain = Arrays.stream(text.split(" ")).filter((textWord) -> textWord.toUpperCase().equals(word.toUpperCase())).toList();
            if (!contain.isEmpty()) {
                log.info("Match: [{}]", word);
            }
            return !contain.isEmpty();
        }).map((word) -> FilteredText.builder().blocked(Boolean.TRUE).text(text).word(word).build()).toList();
    }
    
}

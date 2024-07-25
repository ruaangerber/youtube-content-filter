package com.awesome.filter.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.thoroldvix.api.YoutubeTranscriptApi;
import io.github.thoroldvix.internal.TranscriptApiFactory;

@Configuration
public class YoutubeFilterConfig {
    
    @Bean
    public YoutubeTranscriptApi youtubeTranscriptApi() {
        return TranscriptApiFactory.createDefault();
    }

}

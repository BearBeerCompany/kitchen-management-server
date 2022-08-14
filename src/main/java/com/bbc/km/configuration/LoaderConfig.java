package com.bbc.km.configuration;

import com.bbc.km.service.StatsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class LoaderConfig {

    private final StatsService statsService;

    public LoaderConfig(StatsService statsService) {
        this.statsService = statsService;
    }

    @EventListener
    private void onBoot(ContextRefreshedEvent ctx) {
        if(!statsService.existToday()) {
            statsService.create();
        }
    }
}

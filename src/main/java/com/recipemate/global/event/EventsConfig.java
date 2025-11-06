package com.recipemate.global.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EventsConfig implements InitializingBean {

    private final ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Events.setPublisher(applicationContext);
    }
}

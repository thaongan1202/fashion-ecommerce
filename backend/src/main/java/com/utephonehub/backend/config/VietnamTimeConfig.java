package com.utephonehub.backend.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class VietnamTimeConfig {

    public static final String ZONE_ID = "Asia/Ho_Chi_Minh";

    @PostConstruct
    public void applyDefaultZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONE_ID));
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer vietnamDateTimeCustomizer() {
        return builder -> {
            builder.timeZone(TimeZone.getTimeZone(ZONE_ID));
            builder.serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

                @Override
                public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value == null) {
                        gen.writeNull();
                        return;
                    }
                    gen.writeString(value.format(formatter) + "+07:00");
                }
            });
        };
    }
}

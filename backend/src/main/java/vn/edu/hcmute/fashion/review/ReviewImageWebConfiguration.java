package vn.edu.hcmute.fashion.review;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ReviewImageWebConfiguration implements WebMvcConfigurer {
    private final ReviewImageStorageService storage;

    public ReviewImageWebConfiguration(ReviewImageStorageService storage) { this.storage = storage; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/reviews/**")
                .addResourceLocations(storage.storageDirectory().toUri().toString() + "/");
    }
}

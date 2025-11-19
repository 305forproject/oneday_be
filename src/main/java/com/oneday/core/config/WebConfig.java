package com.oneday.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${file.upload.base-dir}")
	private String uploadBaseDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/images/**")
			.addResourceLocations("file:" + uploadBaseDir + "/");
	}
}

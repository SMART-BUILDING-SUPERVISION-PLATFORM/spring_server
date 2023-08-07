package snust.sbsp.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import snust.sbsp.common.interceptor.Interceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new Interceptor())
			.excludePathPatterns("/api/crew/auth/*")
			.excludePathPatterns("/api/company");
	}
}

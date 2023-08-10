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

//	@Bean
//	public FilterRegistrationBean<CustomCorsFilter> customCorsFilter() {
//		FilterRegistrationBean<CustomCorsFilter> registrationBean
//			= new FilterRegistrationBean<>();
//
//		registrationBean.setFilter(new CustomCorsFilter());
//		registrationBean.setOrder(1);
//		registrationBean.setName("First-CustomCorsFilter");
//
//		return registrationBean;
//	}
}

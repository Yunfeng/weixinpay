package cn.buk.pay.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by yfdai on 2016/12/3.
 */
@Configuration
@PropertySource(value="classpath:config.properties")
@ComponentScan(basePackages = {"cn.buk"},
        excludeFilters = {
            @ComponentScan.Filter(type= FilterType.ANNOTATION, value = EnableWebMvc.class)
        })
public class RootConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}

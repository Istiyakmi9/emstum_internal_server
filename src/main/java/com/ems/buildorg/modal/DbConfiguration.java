package com.ems.buildorg.modal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class DbConfiguration {
    String driver;
    String url;
    String username;
    String password;
    String catalog;
}

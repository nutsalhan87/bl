package ru.itmo.hub.core.model.primary;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.itmo.hub.core.model.DataSourceProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "primary.datasource")
public class PrimaryDataSourceProperties implements DataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String db;
}

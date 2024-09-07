package ru.itmo.hub.core.model.comments;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.itmo.hub.core.model.DataSourceProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "comments.datasource")
public class CommentsDataSourceProperties implements DataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String db;
}

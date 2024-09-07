package ru.itmo.hub.core.model.comments;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import ru.itmo.hub.util.DataSourceUtils;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;

@Configuration
@DependsOn("transactionManager")
@EnableJpaRepositories(basePackages = "ru.itmo.hub.core.model.comments",
        entityManagerFactoryRef = "commentsEntityManager"
)
@EnableConfigurationProperties(CommentsDataSourceProperties.class)
class CommentsConfig {
    private final CommentsDataSourceProperties commentsDataSourceProperties;
    private final XADataSource xaDataSource;
    private final String hbm2ddl;

    public CommentsConfig(CommentsDataSourceProperties commentsDataSourceProperties,
                          @Value("${hibernate.hbm2ddl.auto}") String hbm2ddl) throws SQLException {
        this.commentsDataSourceProperties = commentsDataSourceProperties;
        this.xaDataSource = DataSourceUtils.xaDataSource(commentsDataSourceProperties);
        this.hbm2ddl = hbm2ddl;
    }

    @Bean(name = "commentsDataSource")
    public DataSource commentsDataSource() {
        return DataSourceUtils.dataSource(this.xaDataSource, "comments");
    }

    @Bean(name = "commentsEntityManager")
    @DependsOn({"transactionManager", "commentsDataSource"})
    public LocalContainerEntityManagerFactoryBean commentsEntityManager(@Qualifier("commentsDataSource") DataSource dataSource) {
        return DataSourceUtils.entityManager(hbm2ddl,
                commentsDataSourceProperties,
                dataSource,
                this.getClass().getPackageName(),
                "commentsPersistenceUnit");
    }
}

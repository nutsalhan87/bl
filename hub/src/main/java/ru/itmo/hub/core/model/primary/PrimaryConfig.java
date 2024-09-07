package ru.itmo.hub.core.model.primary;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import ru.itmo.hub.util.DataSourceUtils;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;

@Configuration
@DependsOn("transactionManager")
@EnableJpaRepositories(basePackages = "ru.itmo.hub.core.model.primary",
        entityManagerFactoryRef = "primaryEntityManager"
)
@EnableConfigurationProperties(PrimaryDataSourceProperties.class)
class PrimaryConfig {
    private final PrimaryDataSourceProperties primaryDataSourceProperties;
    private final XADataSource xaDataSource;
    private final String hbm2ddl;

    public PrimaryConfig(PrimaryDataSourceProperties primaryDataSourceProperties,
                         @Value("${hibernate.hbm2ddl.auto}") String hbm2ddl) throws SQLException {
        this.primaryDataSourceProperties = primaryDataSourceProperties;
        this.xaDataSource = DataSourceUtils.xaDataSource(primaryDataSourceProperties);
        this.hbm2ddl = hbm2ddl;
    }

    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        return DataSourceUtils.dataSource(this.xaDataSource, "primary");
    }

    @Primary
    @Bean(name = "primaryEntityManager")
    @DependsOn({"transactionManager", "primaryDataSource"})
    public LocalContainerEntityManagerFactoryBean primaryEntityManager(@Qualifier("primaryDataSource") DataSource dataSource) {
        return DataSourceUtils.entityManager(hbm2ddl,
                primaryDataSourceProperties,
                dataSource,
                this.getClass().getPackageName(),
                "primaryPersistenceUnit");
    }
}

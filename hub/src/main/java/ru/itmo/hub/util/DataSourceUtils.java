package ru.itmo.hub.util;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.xa.PGXADataSource;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import ru.itmo.hub.config.transaction.AtomikosJtaPlatform;
import ru.itmo.hub.core.model.DataSourceProperties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DataSourceUtils {
    private static final Random random = new Random();

    public static XADataSource xaDataSource(@NonNull DataSourceProperties dataSourceProperties) throws SQLException {
        var db = dataSourceProperties.getDb().toUpperCase();
        return switch (db) {
            case "H2" -> {
                var jdbcDataSource = new JdbcDataSource();
                jdbcDataSource.setUrl(dataSourceProperties.getUrl());
                jdbcDataSource.setUser(dataSourceProperties.getUsername());
                jdbcDataSource.setPassword(dataSourceProperties.getPassword());
                yield jdbcDataSource;
            }
            case "POSTGRESQL" -> {
                var pgxaDataSource = new PGXADataSource();
                pgxaDataSource.setUrl(dataSourceProperties.getUrl());
                pgxaDataSource.setUser(dataSourceProperties.getUsername());
                pgxaDataSource.setPassword(dataSourceProperties.getPassword());
                yield pgxaDataSource;
            }
            default -> throw new SQLException();
        };
    }

    public static DataSource dataSource(@NonNull XADataSource xaDataSource, @NonNull String uniqueResourceNamePrefix) {
        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setMaxPoolSize(10); // TODO: разобраться, какое значение ставить
        atomikosDataSourceBean.setXaDataSource(xaDataSource);
        atomikosDataSourceBean.setUniqueResourceName(uniqueResourceNamePrefix + random.nextInt());
        return atomikosDataSourceBean;
    }

    public static LocalContainerEntityManagerFactoryBean entityManager(@NonNull String hbm2ddl,
                                                                       @NonNull DataSourceProperties dataSourceProperties,
                                                                       @NonNull DataSource dataSource,
                                                                       @NonNull String packagesToScan,
                                                                       @NonNull String persistenceUnitName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.transaction.jta.platform", AtomikosJtaPlatform.class.getName());
        properties.put("jakarta.persistence.transactionType", "JTA");
        properties.put("hibernate.hbm2ddl.auto", hbm2ddl);

        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.valueOf(dataSourceProperties.getDb().toUpperCase()));

        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setJtaDataSource(dataSource);
        entityManager.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        entityManager.setPackagesToScan(packagesToScan);
        entityManager.setPersistenceUnitName(persistenceUnitName);
        entityManager.setJpaPropertyMap(properties);
        return entityManager;
    }
}

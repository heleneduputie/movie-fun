package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${VCAP_SERVICES}") String vcap){
        return new DatabaseServiceCredentials(vcap);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return dataSource;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter mySqlHibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        mySqlHibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        mySqlHibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        mySqlHibernateJpaVendorAdapter.setGenerateDdl(true);
        return mySqlHibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean movieLocalContainerEntityManagerFactoryBean(
            @Qualifier("moviesDataSource") DataSource dataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("movieFactory");
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumLocalContainerEntityManagerFactoryBean(
            @Qualifier("albumsDataSource") DataSource dataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("albumFactory");
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager moviePlatformTransactionManager(EntityManagerFactory movieLocalContainerEntityManagerFactoryBean){

        return new JpaTransactionManager(movieLocalContainerEntityManagerFactoryBean);
    }

    @Bean
    public PlatformTransactionManager albumPlatformTransactionManager(EntityManagerFactory albumLocalContainerEntityManagerFactoryBean){
        return new JpaTransactionManager(albumLocalContainerEntityManagerFactoryBean);
    }
}

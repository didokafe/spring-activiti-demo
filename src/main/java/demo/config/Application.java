package demo.config;

import demo.component.PhotoService;
import demo.model.Photo;
import doge.photo.DogePhotoManipulator;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.spring.integration.ActivitiInboundGateway;
import org.activiti.spring.integration.IntegrationActivityBehavior;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "demo")
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages="demo.repository")
//@EntityScan(basePackages = "demo")
public class Application {

    @Configuration
    static class MvcConfiguration extends WebMvcConfigurerAdapter {
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/").setViewName("upload");
        }
    }

    @Configuration
    static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/approve").hasAuthority("photoReviewers")
                    .antMatchers("/").authenticated()
                    .and()
                    .csrf().disable()
                    .httpBasic();
        }
    }

    @Bean
    IntegrationActivityBehavior activitiDelegate(ActivitiInboundGateway activitiInboundGateway) {
        return new IntegrationActivityBehavior(activitiInboundGateway);
    }

    @Bean
    ActivitiInboundGateway inboundGateway(ProcessEngine processEngine) {
        return new ActivitiInboundGateway(processEngine, "processed", "userId", "photo", "photos");
    }

    @Bean
    IntegrationFlow inboundProcess(ActivitiInboundGateway activitiInboundGateway, PhotoService photoService) {
        return IntegrationFlows
                .from(activitiInboundGateway)
                .handle(
                        new GenericHandler<ActivityExecution>() {
                            @Override
                            public Object handle(ActivityExecution execution, Map<String, Object> headers) {

                                Photo photo = (Photo) execution.getVariable("photo");
                                Long photoId = photo.getId();
                                System.out.println("integration: handling execution " + headers.toString());
                                System.out.println("integration: handling photo #" + photoId);

                                photoService.dogifyPhoto(photo);

                                return MessageBuilder.withPayload(execution)
                                        .setHeader("processed", (Object) true)
                                        .copyHeaders(headers).build();
                            }
                        }
                )
                .get();
    }

    @Bean
    DogePhotoManipulator dogePhotoManipulator() {
        DogePhotoManipulator dogePhotoManipulator = new DogePhotoManipulator();
        dogePhotoManipulator.addTextOverlay("pivotal", "abstractfactorybean", "java");
        dogePhotoManipulator.addTextOverlay("spring", "annotations", "boot");
        dogePhotoManipulator.addTextOverlay("code", "semicolonfree", "groovy");
        dogePhotoManipulator.addTextOverlay("clean", "juergenized", "spring");
        dogePhotoManipulator.addTextOverlay("workflow", "activiti", "BPM");
        return dogePhotoManipulator;
    }

    /*@Bean
    CommandLineRunner init(IdentityService identityService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {

                // install groups & users
                Group approvers = group("photoReviewers");
                Group uploaders = group("photoUploaders");

                User joram = user("approver", "Joram", "Barrez");
                identityService.createMembership(joram.getId(), approvers.getId());
                identityService.createMembership(joram.getId(), uploaders.getId());

                User josh = user("uploader", "Josh", "Long");
                identityService.createMembership(josh.getId(), uploaders.getId());
            }

            private User user(String userName, String f, String l) {
                User u = identityService.newUser(userName);
                u.setFirstName(f);
                u.setLastName(l);
                u.setPassword("password");
                identityService.saveUser(u);
                return u;
            }

            private Group group(String groupName) {
                Group group = identityService.newGroup(groupName);
                group.setName(groupName);
                group.setType("security-role");
                identityService.saveGroup(group);
                return group;
            }
        };

    }*/

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/activiti?serverTimezone=UTC&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        HibernateJpaVendorAdapter hibernateJpa = new HibernateJpaVendorAdapter();
        hibernateJpa.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        hibernateJpa.setShowSql(true);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("demo.model");
        emf.setJpaVendorAdapter(hibernateJpa);
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.ejb.naming_strategy","org.hibernate.cfg.ImprovedNamingStrategy");
        jpaProperties.put("javax.persistence.validation.mode","none");
        jpaProperties.put("hibernate.hbm2ddl.auto","create");
        emf.setJpaProperties(jpaProperties);
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager txnMgr = new JpaTransactionManager();
        txnMgr.setEntityManagerFactory(entityManagerFactory().getObject());
        return txnMgr;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

package us.dot.its.jpo.ode.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import us.dot.its.jpo.ode.api.accessors.postgres.UserRepository;
import us.dot.its.jpo.ode.api.asn1.DecoderManager;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableWebMvc
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"us.dot.its.jpo.ode.api", "us.dot.its.jpo.geojsonconverter.validator"})
public class ConflictApiApplication extends SpringBootServletInitializer {

    @Autowired DecoderManager manager;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ConflictApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConflictApiApplication.class, args);
        System.out.println("Started Conflict Monitor API");
        System.out.println("Conflict Monitor API docs page found here: http://localhost:8081/swagger-ui/index.html");
        System.out.println("Startup Complete"); 
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                ConflictMonitorApiProperties props = new ConflictMonitorApiProperties();
//                registry.addMapping("/**").allowedOrigins(props.getCors());
//                // registry.addMapping("/**").allowedMethods("*");
//            }
//        };
//    }

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Bean 
    public void test(){
        System.out.println("Found Users: " + userRepository.count());
        // List<User> users = userRepository.findAll();

        // for(User user: users){
        //     System.out.println(user);
        // }
        String queryString = String.format(
            "SELECT new us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole(u.email, o.name, r.name) " +
            "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
            "JOIN Organizations o on uo.organization_id = o.organization_id "+
            "JOIN Roles r on uo.role_id = r.role_id " +
            "where u.email = '%s' ", "test@gmail.com");

        System.out.println(queryString);

        TypedQuery<UserOrgRole> query 
            = entityManager.createQuery(queryString, UserOrgRole.class);
        List<UserOrgRole> resultList = query.getResultList();

        System.out.println(resultList.size());
        for(UserOrgRole userOrgRole: resultList){
            System.out.println(userOrgRole);
        }
    }

    
    
}

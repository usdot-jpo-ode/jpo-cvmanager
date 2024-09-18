package us.dot.its.jpo.ode.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import us.dot.its.jpo.ode.api.asn1.DecoderManager;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.services.PostgresService;

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

    @Autowired PostgresService service;

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


    @Bean
    public void test(){
        List<String> ips = service.getAllowedRSUIPByEmail("test@gmail.com");
        for(String ip : ips){
            System.out.println(ip);
        }

        List<UserOrgRole> userOrgRoles = service.findUserOrgRoles("test@gmail.com");
        for(UserOrgRole userOrgRole : userOrgRoles){
            System.out.println(userOrgRole);
        }

        List<Integer> intersectionIds = service.getAllowedIntersectionIdByEmail("test@gmail.com");
        for(Integer id : intersectionIds){
            System.out.println(id);
        }
        
    }
}

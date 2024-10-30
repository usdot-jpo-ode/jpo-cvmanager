package com.cvmanager.auth.provider.user;

import java.sql.Connection;
import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cvmanager.auth.provider.user.CustomUserStorageProviderConstants.*;

public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {
    private static final Logger log = LoggerFactory.getLogger(CustomUserStorageProviderFactory.class);    
    protected final List<ProviderConfigProperty> configMetadata;
    
    public CustomUserStorageProviderFactory() {
        log.debug("CustomUserStorageProviderFactory created");
        
        
        // Create config metadata
        configMetadata = ProviderConfigurationBuilder.create()
          .property()
            .name(CONFIG_KEY_JDBC_DRIVER)
            .label("JDBC Driver Class")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("org.h2.Driver")
            .helpText("Fully qualified class name of the JDBC driver")
            .add()
          .property()
            .name(CONFIG_KEY_JDBC_URL)
            .label("JDBC URL")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("jdbc:h2:mem:customdb")
            .helpText("JDBC URL used to connect to the user database")
            .add()
          .property()
            .name(CONFIG_KEY_DB_USERNAME)
            .label("Database User")
            .type(ProviderConfigProperty.STRING_TYPE)
            .helpText("Username used to connect to the database")
            .add()
          .property()
            .name(CONFIG_KEY_DB_PASSWORD)
            .label("Database Password")
            .type(ProviderConfigProperty.STRING_TYPE)
            .helpText("Password used to connect to the database")
            .secret(true)
            .add()
          .property()
            .name(CONFIG_KEY_VALIDATION_QUERY)
            .label("SQL Validation Query")
            .type(ProviderConfigProperty.STRING_TYPE)
            .helpText("SQL query used to validate a connection")
            .defaultValue("select 1")
            .add()
          .build();   
          
    }

    @Override
    public CustomUserStorageProvider create(KeycloakSession ksession, ComponentModel model) {
        log.debug("creating new CustomUserStorageProvider");
        return new CustomUserStorageProvider(ksession,model);
    }

    @Override
    public String getId() {
        log.debug("getId()");
        return "custom-user-provider";
    }

    
    // Configuration support methods
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        
       try (Connection c = CustomUserStorageProvider.getConnection(config)) {
           log.debug("Testing connection..." );
           c.createStatement().execute(config.get(CONFIG_KEY_VALIDATION_QUERY));
           log.debug("Connection OK !" );
       }
       catch(Exception ex) {
           log.warn("Unable to validate connection: ex={}", ex.getMessage());
           throw new ComponentValidationException("Unable to validate database connection",ex);
       }
    }
}

package org.example.actividad.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@Profile("h2")
public class H2Config {
    @Bean
    public DataSource dataSource(
            @Value("${backstage.datasource.url}") String url,
            @Value("${backstage.datasource.username}") String usuario,
            @Value("${backstage.datasource.password}") String clave) {
        var fuente = new DriverManagerDataSource(url, usuario, clave);
        fuente.setDriverClassName("org.h2.Driver");
        // CREATE IF NOT EXISTS conserva los datos al volver a iniciar.
        new ResourceDatabasePopulator(new ClassPathResource("db/schema-h2.sql")).execute(fuente);
        return fuente;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource fuente) {
        return new JdbcTemplate(fuente);
    }
}

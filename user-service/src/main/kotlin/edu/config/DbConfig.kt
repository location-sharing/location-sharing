package edu.config

import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import com.github.jasync.sql.db.postgresql.pool.PostgreSQLConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DbConfig {



    @Bean
    fun connectionFactory() {
        return Jasync
    }
}
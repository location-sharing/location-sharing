package edu

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
class UserServiceApplicationTests {

    @Test
    fun contextLoads() {
    }


    @Test
    fun cors() {

        val client = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:8082")
            .build()

        val response: WebTestClient.ResponseSpec = client.options()
            .uri("/cors-enabled-endpoint")
            .header("Origin", "http://any-origin.com")
            .header("Access-Control-Request-Method", "PUT")
            .exchange()

        response.expectHeader()
            .valueEquals("Access-Control-Allow-Origin", "*")
        response.expectHeader()
            .valueEquals("Access-Control-Allow-Methods", "PUT")
        response.expectHeader()
            .exists("Access-Control-Max-Age")
    }
}

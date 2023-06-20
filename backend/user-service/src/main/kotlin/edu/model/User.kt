package edu.model

import jakarta.persistence.*
import java.util.*

@Table(name = "users")
@Entity
class User() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(unique = true, nullable = false, length = 255)
    lateinit var username: String

    @Column(unique = true, nullable = false, length = 255)
    lateinit var email: String

    @Column(nullable = false, length = 255)
    lateinit var password: String

    constructor(
        id: UUID?,
        username: String,
        email: String,
        password: String
    ) : this() {
        this.id = id
        this.username = username
        this.email = email
        this.password = password
    }
}
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

    @Column(nullable = false, length = 255)
    lateinit var password: String

    constructor(
        id: UUID?,
        username: String,
        password: String
    ) : this() {
        this.id = id
        this.username = username
        this.password = password
    }
}
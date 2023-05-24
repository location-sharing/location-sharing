package edu.model

import jakarta.persistence.*
import java.util.*

@Table(name = "groups")
@Entity
class Group() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(unique = true, nullable = false, length = 255)
    lateinit var name: String

    constructor(
        id: UUID?,
        username: String,
    ) : this() {
        this.id = id
        this.name = username
    }
}
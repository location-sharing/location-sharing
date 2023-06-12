package edu.repository.model

import jakarta.persistence.*
import java.util.*

@Table(name = "Users")
@Entity
class User() {

    @Id
    lateinit var id: UUID

    @Column(nullable = false, unique = true)
    lateinit var username: String

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    lateinit var groups: MutableSet<Group>

    constructor(
        id: UUID,
        username: String,
    ) : this() {
        this.id = id
        this.username = username
    }
}
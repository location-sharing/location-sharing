package edu.repository.model

import jakarta.persistence.*
import java.util.*

@Table(name = "Users")
@Entity
class User() {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var name: String

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    lateinit var groups: MutableSet<Group>

    constructor(
        id: UUID,
        name: String,
    ) : this() {
        this.id = id
        this.name = name
    }
}
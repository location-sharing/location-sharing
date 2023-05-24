package edu.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.util.*

@Table(name = "Users")
@Entity
class User {
    @Id
    var id: UUID? = null

    @ManyToMany(mappedBy = "users")
    lateinit var groups: Set<Group>
}
package edu.repository.model

import jakarta.persistence.*
import java.util.*

@Table(name = "groups")
@Entity
class Group() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(nullable = false, length = 255)
    lateinit var name: String

    @Column(nullable = false, length = 255)
    lateinit var ownerId: String

    @ManyToMany(
        cascade = [CascadeType.ALL],
        fetch = FetchType.EAGER
    )
    @JoinTable(
        name = "GroupUsers",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    lateinit var users: MutableSet<User>

    constructor(
        id: UUID? = null,
        name: String,
        ownerId: String,
        users: MutableSet<User> = mutableSetOf()
    ) : this() {
        this.id = id
        this.name = name
        this.ownerId = ownerId
        this.users = users
    }
}
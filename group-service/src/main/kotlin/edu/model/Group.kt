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

    // userId of the owner
//    @Column(nullable = false, length = 255)
//    lateinit var ownerId: String

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "GroupUsers",
        joinColumns = [JoinColumn(name = "id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    lateinit var users: Set<User>

    constructor(
        id: UUID?,
        username: String,
//        ownerId: String
    ) : this() {
        this.id = id
        this.name = username
//        this.ownerId = ownerId
    }
}
package edu.repository

import edu.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: CrudRepository<User, UUID> {

    fun findByUsername(username: String): Optional<User>
}
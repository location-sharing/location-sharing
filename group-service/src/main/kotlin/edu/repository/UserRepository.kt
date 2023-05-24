package edu.repository

import edu.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository: CrudRepository<User, UUID> {
    fun findGroupsById(id: UUID)
}
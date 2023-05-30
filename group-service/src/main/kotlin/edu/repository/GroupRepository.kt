package edu.repository

import edu.repository.model.Group
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository: CrudRepository<Group, UUID> {

    fun findByIdAndOwnerId(id: UUID, ownerId: String): Optional<Group>

    fun existsByIdAndOwnerId(id: UUID, ownerId: String): Boolean

    fun deleteByIdAndOwnerId(id: UUID, ownerId: String)
}
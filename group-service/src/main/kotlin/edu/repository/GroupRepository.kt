package edu.repository

import edu.model.Group
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GroupRepository: CrudRepository<Group, UUID>
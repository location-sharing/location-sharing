package edu.repository

import edu.repository.model.Group
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository: CrudRepository<Group, UUID>
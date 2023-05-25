package edu.controller

import edu.dto.GroupDto
import edu.dto.UserDto
import edu.mapper.GroupMapper
import edu.mapper.UserMapper
import edu.service.GroupService
import edu.service.UserGroupService
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupUserController(
    val groupService: GroupService,
    val userGroupService: UserGroupService
){

    @GetMapping("/{id}/users")
    suspend fun getGroupUsers(@PathVariable id: String): ResponseEntity<List<UserDto>> {
        val users = groupService.getGroupUsers(id)
        return ResponseEntity.ok(
            UserMapper.from(users)
        )
    }

    @GetMapping
    suspend fun getUserGroups(@RequestParam userId: String): ResponseEntity<List<GroupDto>> {
        val groups = userGroupService.findUserGroups(userId)
        return ResponseEntity.ok(
            GroupMapper.from(groups)
        )
    }

    @PostMapping("/{groupId}/users")
    suspend fun addGroupUser(
        @PathVariable groupId: String,
        @RequestParam userId: String
    ): ResponseEntity<GroupDto> {
        val group = groupService.addGroupUser(groupId, userId)
        return ResponseEntity.ok(
            GroupMapper.from(group)
        )
    }

    @DeleteMapping("/{groupId}/users")
    suspend fun removeGroupUser(
        @PathVariable groupId: String,
        @RequestParam userId: String
    ): ResponseEntity<Nothing> {
        groupService.removeGroupUser(groupId, userId)
        return ResponseEntity.noContent().build()
    }
}
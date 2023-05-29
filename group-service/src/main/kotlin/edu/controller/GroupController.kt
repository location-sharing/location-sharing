package edu.controller

import edu.dto.GroupCreateDto
import edu.dto.GroupDto
import edu.dto.GroupUpdateDto
import edu.mapper.GroupMapper
import edu.service.GroupService
import edu.service.UserGroupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    val groupService: GroupService,
    val userGroupService: UserGroupService,
) {

    @PostMapping
    suspend fun postGroup(@RequestBody createGroup: GroupCreateDto): ResponseEntity<GroupDto> {
        val group = groupService.create(createGroup)
        return ResponseEntity.ok(
            GroupMapper.from(group)
        )
    }

    @GetMapping("/{id}")
    suspend fun getGroup(@PathVariable id: String): ResponseEntity<GroupDto> {
        val group = groupService.findById(id)
        return ResponseEntity.ok(
            GroupMapper.from(group)
        )
    }

    @PatchMapping("/{id}")
    suspend fun updateGroup(
        @PathVariable id: String,
        @RequestBody updateDto: GroupUpdateDto
    ): ResponseEntity<GroupDto> {
        val group = groupService.patch(id, updateDto)
        return ResponseEntity.ok(
            GroupMapper.from(group)
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteGroup(@PathVariable id: String): ResponseEntity<Nothing> {
        groupService.delete(id)
        return ResponseEntity.noContent().build()
    }

//    @GetMapping("/{id}/users")
//    suspend fun getGroupUsers(@PathVariable id: String): ResponseEntity<List<UserDto>> {
//        val users = groupService.getGroupUsers(id)
//        return ResponseEntity.ok(
//            UserMapper.from(users)
//        )
//    }
//
//    @GetMapping
//    suspend fun getUserGroups(@RequestParam userId: String): ResponseEntity<List<GroupDto>> {
//        val groups = userGroupService.findUserGroups(userId)
//        return ResponseEntity.ok(
//            GroupMapper.from(groups)
//        )
//    }
}
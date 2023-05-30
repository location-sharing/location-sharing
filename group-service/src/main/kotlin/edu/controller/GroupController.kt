package edu.controller

import edu.dto.*
import edu.mapper.GroupMapper
import edu.security.filters.AuthenticatedUser
import edu.service.GroupService
import edu.service.UserGroupService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    val groupService: GroupService,
    val userGroupService: UserGroupService,
) {

    @GetMapping
    suspend fun getUserGroups(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<GroupDto>> {
        val groups = userGroupService.findUserGroups(user.id)
        return ResponseEntity.ok(
            GroupMapper.toDtoList(groups)
        )
    }

    @GetMapping("/{id}")
    suspend fun getUserGroup(
        @PathVariable id: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<GroupDetailDto> {
        val group = userGroupService.findUserGroup(user.id, id)
        return ResponseEntity.ok(
            GroupMapper.toDetailDto(group)
        )
    }

    @PostMapping
    suspend fun postGroup(
        @RequestBody createGroup: GroupCreateDto,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<GroupDetailDto> {
        val group = groupService.create(createGroup, user.id)
        return ResponseEntity.ok(
            GroupMapper.toDetailDto(group)
        )
    }

    @PatchMapping("/{id}")
    suspend fun updateGroup(
        @PathVariable id: String,
        @RequestBody updateDto: GroupUpdateDto,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<GroupDetailDto> {
        val group = groupService.patch(id, updateDto, user.id)
        return ResponseEntity.ok(
            GroupMapper.toDetailDto(group)
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteGroup(
        @PathVariable id: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        groupService.delete(id, user.id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{groupId}/users")
    suspend fun addGroupUser(
        @PathVariable groupId: String,
        @RequestParam userId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        groupService.addGroupUser(groupId, user.id, userId)
        return ResponseEntity.accepted().build()
    }

    @DeleteMapping("/{groupId}/users")
    suspend fun removeGroupUser(
        @PathVariable groupId: String,
        @RequestParam userId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        groupService.removeGroupUser(groupId, user.id, userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}")
    suspend fun changeOwner(
        @PathVariable id: String,
        @RequestParam userId: String,
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<Void> {
        groupService.changeOwner(id, user.id, userId)
        return ResponseEntity.accepted().build()
    }
}
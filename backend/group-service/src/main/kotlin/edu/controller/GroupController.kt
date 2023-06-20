package edu.controller

import edu.dto.GroupCreateDto
import edu.dto.GroupDetailDto
import edu.dto.GroupDto
import edu.dto.GroupPatchDto
import edu.mapper.GroupMapper
import edu.security.jwt.AuthenticatedUser
import edu.service.GroupService
import edu.service.UserGroupService
import edu.service.exception.ValidationException
import edu.util.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    val groupService: GroupService,
    val userGroupService: UserGroupService,
) {

    val log = logger()

    @GetMapping
    suspend fun getUserGroups(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<GroupDto>> {
        val groups = userGroupService.findUserGroupsById(user.id)
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
        @RequestBody updateDto: GroupPatchDto,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<GroupDto> {
        val group = groupService.patch(id, updateDto, user.id)
        return ResponseEntity.ok(
            GroupMapper.toDto(group)
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteGroup(
        @PathVariable id: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        // switch to IO threads here because this has to be a transaction
        // run on the same thread all the way to the end
        withContext(Dispatchers.IO) {
            groupService.delete(id, user.id)
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{groupId}/users")
    suspend fun addGroupUser(
        @PathVariable groupId: String,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) username: String?,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        if (userId != null) {
            groupService.addGroupUserById(groupId, user.id, userId)
        } else if (username != null) {
            groupService.addGroupUserByUsername(groupId, user.id, username)
        } else {
            log.debug("userId or username URL parameter must be specified")
            throw ValidationException("Either userId or username URL parameters must be specified.")
        }
        return ResponseEntity.accepted().build()
    }

    @DeleteMapping("/{groupId}/users")
    suspend fun removeGroupUser(
        @PathVariable groupId: String,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) username: String?,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        if (userId != null) {
            groupService.removeGroupUserById(groupId, user.id, userId)
        } else if (username != null) {
            groupService.removeGroupUserByUsername(groupId, user, username)
        } else {
            log.debug("userId or username URL parameter must be specified")
            throw ValidationException("Either userId or username URL parameters must be specified.")
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/owner")
    suspend fun changeOwner(
        @PathVariable id: String,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) username: String?,
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<Void> {
        if (userId != null) {
            groupService.changeOwnerById(id, user.id, userId)
        } else if (username != null) {
            groupService.changeOwnerByUsername(id, user, username)
        } else {
            log.debug("userId or username URL parameter must be specified")
            throw ValidationException("Either userId or username URL parameters must be specified.")
        }
        return ResponseEntity.accepted().build()
    }
}
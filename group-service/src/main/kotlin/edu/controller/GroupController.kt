package edu.controller

import edu.dto.GroupCreateDto
import edu.dto.GroupDto
import edu.dto.GroupUpdateDto
import edu.service.GroupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    val groupService: GroupService
) {

    @PostMapping
    suspend fun postGroup(@RequestBody createGroup: GroupCreateDto): ResponseEntity<GroupDto> {
        val group = groupService.create(createGroup)
        return ResponseEntity.ok(group)
    }

    @GetMapping("/{id}")
    suspend fun getGroup(@PathVariable id: String): ResponseEntity<GroupDto> {
        val group = groupService.findById(id)
        return ResponseEntity.ok(group)
    }

    @PatchMapping("/{id}")
    suspend fun updateGroup(
        @PathVariable id: String,
        @RequestBody updateDto: GroupUpdateDto
    ): ResponseEntity<GroupDto> {
        val group = groupService.patch(id, updateDto)
        return ResponseEntity.ok(group)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteGroup(@PathVariable id: String) {
        groupService.delete(id)
    }
}
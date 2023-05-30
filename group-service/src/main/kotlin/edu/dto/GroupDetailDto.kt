package edu.dto

data class GroupDetailDto(
    val id: String,
    val name: String,
    val ownerId: String,
    val users: List<UserDto>
)
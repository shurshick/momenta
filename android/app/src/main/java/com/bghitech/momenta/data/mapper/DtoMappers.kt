package com.bghitech.momenta.data.mapper

import com.bghitech.momenta.data.local.entity.CachedChallengeEntity
import com.bghitech.momenta.data.local.entity.CachedPostEntity
import com.bghitech.momenta.data.remote.dto.*
import com.bghitech.momenta.domain.model.*

fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    avatarKey = avatarKey,
    email = email
)

fun ChallengeDto.toDomain(): Challenge = Challenge(
    id = id,
    date = date,
    title = title,
    description = description,
    endsAt = endsAt,
    userPosted = userPosted,
    participantsCount = participantsCount
)

fun PostDto.toDomain(): Post = Post(
    id = id,
    user = user?.toDomain() ?: User(id = "", username = "", displayName = null, avatarUrl = null, avatarKey = null, email = null),
    mediaType = mediaType,
    previewUrl = previewUrl ?: "",
    thumbUrl = thumbUrl,
    caption = caption,
    country = country,
    city = city,
    likesCount = likesCount,
    commentsCount = commentsCount,
    viewsCount = viewsCount,
    createdAt = createdAt ?: "",
    isLiked = isLiked,
    isMine = isMine,
    canDelete = canDelete
)

fun ProfileDto.toDomain(): Profile = Profile(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    avatarKey = avatarKey,
    bio = bio,
    momentsCount = momentsCount,
    streakCount = streakCount,
    likesCount = likesCount,
    recentPosts = recentPosts.map { it.toDomain() }
)

fun RecentPostDto.toDomain(): Post = Post(
    id = id,
    user = User(id = "", username = "", displayName = null, avatarUrl = null, avatarKey = null, email = null),
    mediaType = "image",
    previewUrl = previewUrl ?: "",
    thumbUrl = thumbUrl,
    caption = null,
    country = null,
    city = null,
    likesCount = 0,
    commentsCount = 0,
    viewsCount = 0,
    createdAt = createdAt ?: "",
    isLiked = false
)

fun Challenge.toCachedEntity(): CachedChallengeEntity = CachedChallengeEntity(
    id = id,
    date = date,
    title = title,
    description = description,
    endsAt = endsAt,
    userPosted = userPosted,
    participantsCount = participantsCount
)

fun CachedChallengeEntity.toDomain(): Challenge = Challenge(
    id = id,
    date = date,
    title = title,
    description = description,
    endsAt = endsAt,
    userPosted = userPosted,
    participantsCount = participantsCount
)

fun Post.toCachedEntity(): CachedPostEntity = CachedPostEntity(
    id = id,
    username = user.username,
    displayName = user.displayName,
    avatarUrl = user.avatarUrl,
    avatarKey = user.avatarKey,
    challengeDate = "",
    mediaType = mediaType,
    previewUrl = previewUrl,
    thumbUrl = thumbUrl,
    caption = caption,
    country = country,
    city = city,
    likesCount = likesCount,
    commentsCount = commentsCount,
    viewsCount = viewsCount,
    createdAt = createdAt,
    isLiked = isLiked,
    isMine = isMine,
    canDelete = canDelete
)

fun CachedPostEntity.toDomain(): Post = Post(
    id = id,
    user = User(id = "", username = username, displayName = displayName, avatarUrl = avatarUrl, avatarKey = avatarKey, email = null),
    mediaType = mediaType,
    previewUrl = previewUrl,
    thumbUrl = thumbUrl,
    caption = caption,
    country = country,
    city = city,
    likesCount = likesCount,
    commentsCount = commentsCount,
    viewsCount = viewsCount,
    createdAt = createdAt,
    isLiked = isLiked,
    isMine = isMine,
    canDelete = canDelete
)

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    postId = postId,
    user = user.toDomain(),
    text = text,
    createdAt = createdAt ?: "",
    isMine = isMine,
    canDelete = canDelete
)

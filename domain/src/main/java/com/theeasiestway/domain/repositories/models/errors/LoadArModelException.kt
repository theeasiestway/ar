package com.theeasiestway.domain.repositories.models.errors

/**
 * Created by Alexey Loboda on 06.02.2022
 */
class LoadArModelException(
    message: String?,
    cause: Throwable?
): Exception(message, cause)
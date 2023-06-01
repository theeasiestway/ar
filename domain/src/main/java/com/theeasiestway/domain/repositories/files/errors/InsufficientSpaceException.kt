package com.theeasiestway.domain.repositories.files.errors

/**
 * Created by Alexey Loboda on 01.05.2023
 */
class InsufficientSpaceException(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause)
package com.theeasiestway.domain.exceptions.files

/**
 * Created by Alexey Loboda on 01.05.2023
 */
class FileAlreadyExistsException(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause)
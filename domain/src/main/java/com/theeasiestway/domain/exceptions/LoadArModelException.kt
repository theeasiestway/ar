package com.theeasiestway.domain.exceptions

/**
 * Created by Alexey Loboda on 06.02.2022
 */
class LoadArModelException(
    message: String?,
    cause: Throwable?
) : Exception(message, cause)
package com.theeasiestway.domain.exceptions

/**
 * Created by Alexey Loboda on 06.02.2022
 */
class LoadArModelException: Exception {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
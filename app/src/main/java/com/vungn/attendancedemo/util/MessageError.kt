package com.vungn.attendancedemo.util

import java.util.Random

data class MessageError(val message: String, val code: Int = Random().nextInt())
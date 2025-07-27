package pl.techbrewery.sam.kmp.utils

import java.sql.SQLIntegrityConstraintViolationException

class IgnoredException: Exception()
class ItemAlreadyOnListException(message: String): SQLIntegrityConstraintViolationException(message)
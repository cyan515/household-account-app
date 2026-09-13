package cyan0515.householdAccount.model.user

class UserAlreadyExistsException(
    cause: Throwable? = null
) : RuntimeException("User already exists", cause)

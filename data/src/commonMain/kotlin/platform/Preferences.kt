package platform

expect fun prefSet(key: String, value: String)
expect fun prefGet(key: String): String?
expect fun prefRemove(key: String)

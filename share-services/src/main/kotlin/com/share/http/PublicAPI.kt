package com.share.http

interface PublicAPIData

class EmptyDataRequest: PublicAPIData
class CustomAPIResponse<T : PublicAPIData?> : PublicAPIResponse<T> {
    //  @Nullable
    @get:Deprecated("")
    @set:Deprecated("")
    var userContext: String? = null

    //  @NotNullable
    var context: APIContext? = null

    constructor()
    constructor(data: T) : super(data)

}

class CustomAPIRequest<T : PublicAPIData?> : PublicAPIRequest<T> {
    constructor()
    constructor(data: T) : super(data)
    constructor(data: T, fields: List<String?>?) : super(data, fields)
}


open class PublicAPIRequest<T : PublicAPIData?> {
    // `data` is a reserved word
    //  @NotNullable
    var data: T? = null
        protected set

    // `fields` is a reserved word
    //  @NotNullable
    var fields: List<String>? = null
        protected set

    constructor()
    constructor(data: T) {
        this.data = data
    }

    @Suppress("UNCHECKED_CAST")
    constructor(data: T, fields: List<String?>?) {
        this.data = data
        this.fields = fields as List<String>?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PublicAPIRequest<*>) {
            return false
        }
        if (if (data != null) data!! != other.data else other.data != null) {
            return false
        }
        return if (fields != null) fields == other.fields else other.fields == null
    }

    override fun hashCode(): Int {
        var result = if (data != null) data.hashCode() else 0
        result = 31 * result + if (fields != null) fields.hashCode() else 0
        return result
    }
}

open class PublicAPIResponse<T : PublicAPIData?> {
    // `data` is a reserved word
    //  @NotNullable
    private var data: T? = null

    protected constructor()
    constructor(data: T) {
        this.data = data
    }
}

class APIContext {
    //  @Nullable
    var nonce = ""
}

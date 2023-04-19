package com.droidappproject.letschat.model

class User {
    var uid: String? = null
    var name: String? = null
    var bio: String? = null
    var phoneNumber: String? = null
    var profileImage: String? = null

    constructor() {}
    constructor(
        uid: String?,
        name: String?,
        bio: String?,
        phoneNumber: String?,
        profileImage: String?
    ) {
        this.uid = uid
        this.name = name
        this.bio = bio
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
    }
}
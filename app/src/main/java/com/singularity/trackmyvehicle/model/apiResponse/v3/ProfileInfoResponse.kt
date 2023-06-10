package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 10/27/21.
 */
class ProfileInfoResponse (
        @SerializedName("Response")
        var profileResponse : ProfileSuccessResponse? = null
) : Serializable{

    class ProfileSuccessResponse (
            @SerializedName("ID")
            var id : String? = "",

            @SerializedName("Email")
            var email : String? = "",

            @SerializedName("SignInName")
            var signInName : String? = "",

            @SerializedName("NameFirst")
            var nameFirst : String? = "",

            @SerializedName("NameMiddle")
            var nameMiddle : String? = "",

            @SerializedName("NameLast")
            var nameLast : String? = "",

            @SerializedName("PhoneMobile")
            var mobileNumber : String? = "",

            @SerializedName("Picture")
            var picture : String? = "",

            @SerializedName("PictureThumbnail")
            var pictureThumbnail : String? = "",

            @SerializedName("GroupName")
            var groupName : String? = "",

            @SerializedName("GroupIdentifierHighest")
            var groupIdentifierHighest : String? = ""


    ) : Serializable
}
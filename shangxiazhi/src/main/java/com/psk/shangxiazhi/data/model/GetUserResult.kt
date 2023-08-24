package com.psk.shangxiazhi.data.model

/*
{
    "msg":"success",
    "code":0,
    "user":{
        "patientId":125057,
        "hospitalId":50,
        "rfid":null,
        "name":"朱勇测",
        "identifyType":"身份证",
        "identifyNumber":"510502198009260412",
        "gender":1,
        "birthday":"1980/09/26",
        "address":null,
        "telephone":"18584658926",
        "regDate":"2022/11/04",
        "adderId":3544,
        "height":170,
        "weight":80,
        "hospitalizationNum":"8544225696",
        "bedNum":"2",
        "isSmoke":0,
        "hemoglobin":null,
        "baricIndex":null,
        "lipids":null,
        "relateWeight":null,
        "bodySurfaceArea":null,
        "doctorId":3544,
        "doctorName":"朱勇",
        "disease":null,
        "status":1,
        "remark":null,
        "diseasearea":null,
        "visitNum":null,
        "emergencyContact":null,
        "emergencyContactPhone":null,
        "avatar":null,
        "isVip":0,
        "age":42,
        "hospitalName":"重庆普施康"
    }
}
 */
data class GetUserResult(
    val code: Int,
    val msg: String,
    val user: User
)

data class User(
    val name: String,
)

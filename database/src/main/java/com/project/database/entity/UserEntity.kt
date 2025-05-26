package com.project.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project.core.Address
import com.project.core.Company
import com.project.core.Geo
import com.project.core.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val city: String,
    val street: String,
    val suite: String,
    val zipcode: String,
    val lat: String,
    val lng: String,
    val phone: String,
    val website: String,
    val companyName: String,
    val companyCatchPhrase: String,
    val companyBs: String
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    username = username,
    email = email,
    city = address.city,
    street = address.street,
    suite = address.suite,
    zipcode = address.zipcode,
    lat = address.geo.lat,
    lng = address.geo.lng,
    phone = phone,
    website = website,
    companyName = company.name,
    companyCatchPhrase = company.catchPhrase,
    companyBs = company.bs
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    username = username,
    email = email,
    address = Address(
        street = street,
        suite = suite,
        city = city,
        zipcode = zipcode,
        geo = Geo(
            lat = lat,
            lng = lng
        )
    ),
    phone = phone,
    website = website,
    company = Company(
        name = companyName,
        catchPhrase = companyCatchPhrase,
        bs = companyBs
    )
)
package com.project.network.dto

import com.google.gson.annotations.SerializedName
import com.project.core.Address
import com.project.core.Company
import com.project.core.Geo
import com.project.core.User

data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val address: AddressDto,
    val phone: String,
    val website: String,
    val company: CompanyDto
)

data class AddressDto(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: GeoDto
)

data class GeoDto(
    val lat: String,
    val lng: String
)

data class CompanyDto(
    val name: String,
    @SerializedName("catchPhrase")
    val catchPhrase: String,
    val bs: String
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    username = username,
    email = email,
    address = address.toDomain(),
    phone = phone,
    website = website,
    company = company.toDomain()
)

fun AddressDto.toDomain(): Address = Address(
    street = street,
    suite = suite,
    city = city,
    zipcode = zipcode,
    geo = geo.toDomain()
)

fun GeoDto.toDomain(): Geo = Geo(
    lat = lat,
    lng = lng
)

fun CompanyDto.toDomain(): Company = Company(
    name = name,
    catchPhrase = catchPhrase,
    bs = bs
)
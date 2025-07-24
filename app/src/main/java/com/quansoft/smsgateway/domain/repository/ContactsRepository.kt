package com.quansoft.smsgateway.domain.repository

interface ContactsRepository {
    fun findContactName(phoneNumber: String): String?
}
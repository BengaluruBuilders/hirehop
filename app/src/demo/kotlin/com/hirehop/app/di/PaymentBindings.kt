package com.hirehop.app.di

import com.hirehop.core.data.repository.StoredUsageAllowance
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.offline.OfflinePaymentGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface PaymentBindings {
    @Binds
    fun bindPaymentGateway(impl: OfflinePaymentGateway): PaymentGateway

    @Binds
    fun bindUsageAllowance(impl: StoredUsageAllowance): UsageAllowance
}

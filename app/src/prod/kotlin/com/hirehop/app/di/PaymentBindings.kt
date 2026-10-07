package com.hirehop.app.di

import com.hirehop.app.AppStartTask
import com.hirehop.app.billing.GooglePlayBilling
import com.hirehop.app.billing.PlayBilling
import com.hirehop.app.billing.PurchaseRestorer
import com.hirehop.app.billing.RemotePaymentGateway
import com.hirehop.app.billing.WalletUsageAllowance
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.domain.PaymentGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
interface PaymentBindings {
    @Binds
    fun bindPaymentGateway(impl: RemotePaymentGateway): PaymentGateway

    @Binds
    fun bindUsageAllowance(impl: WalletUsageAllowance): UsageAllowance

    @Binds
    fun bindPlayBilling(impl: GooglePlayBilling): PlayBilling

    @Binds
    @IntoSet
    fun bindPurchaseRestorer(impl: PurchaseRestorer): AppStartTask
}

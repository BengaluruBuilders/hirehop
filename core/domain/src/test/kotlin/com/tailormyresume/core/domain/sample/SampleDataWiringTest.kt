package com.tailormyresume.core.domain.sample

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.di.DomainModule
import com.tailormyresume.core.domain.offline.OfflinePaymentGateway
import org.junit.Test
import java.lang.reflect.Modifier
import javax.inject.Inject

class SampleDataWiringTest {

    private val constructor = OfflineSampleDataController::class.java.constructors.single()

    @Test
    fun theControllerTakesTheOfflinePaymentGatewayAndNeverAFlavourBoundOne() {
        val parameterTypes = constructor.parameterTypes.toList()

        assertThat(parameterTypes).contains(OfflinePaymentGateway::class.java)
        assertThat(parameterTypes).doesNotContain(PaymentGateway::class.java)
    }

    @Test
    fun onlyTheDomainModuleCanBuildTheController() {
        assertThat(constructor.isAnnotationPresent(Inject::class.java)).isFalse()
    }

    @Test
    fun theDomainModuleDoesNotBindTheControllerToWhateverTheFlavourInjects() {
        val abstractSampleBindings = DomainModule::class.java.declaredMethods.filter {
            Modifier.isAbstract(it.modifiers) && it.returnType == SampleDataController::class.java
        }

        assertThat(abstractSampleBindings).isEmpty()
    }
}

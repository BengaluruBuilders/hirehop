package com.hirehop.app.ui

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RootViewModelStoresTest {

    private val stores = RootViewModelStores()

    private class Probe : ViewModel() {
        var cleared = false

        override fun onCleared() {
            cleared = true
        }
    }

    private fun probeIn(root: NavigationRoot): Probe =
        ViewModelProvider.create(
            stores.storeOf(root),
            viewModelFactory { initializer { Probe() } },
        )[Probe::class.java]

    @Test
    fun leavingTheFirstRunRootClearsItsViewModels() {
        val firstRunProbe = probeIn(NavigationRoot.FirstRun)

        stores.keepOnly(NavigationRoot.Main)

        assertThat(firstRunProbe.cleared).isTrue()
    }

    @Test
    fun leavingTheFirstRunRootKeepsTheViewModelsOfTheMainRoot() {
        val mainProbe = probeIn(NavigationRoot.Main)
        probeIn(NavigationRoot.FirstRun)

        stores.keepOnly(NavigationRoot.Main)

        assertThat(mainProbe.cleared).isFalse()
        assertThat(probeIn(NavigationRoot.Main)).isSameInstanceAs(mainProbe)
    }

    @Test
    fun returningToTheFirstRunRootGivesNewViewModels() {
        val before = probeIn(NavigationRoot.FirstRun)
        stores.keepOnly(NavigationRoot.Main)

        stores.keepOnly(NavigationRoot.FirstRun)

        assertThat(probeIn(NavigationRoot.FirstRun)).isNotSameInstanceAs(before)
    }

    @Test
    fun rootOwnerUsesItsOwnStoreAndTheParentFactory() {
        val parent = ParentOwner()
        val store = stores.storeOf(NavigationRoot.Main)

        val owner = RootViewModelStoreOwner(parent, store)

        assertThat(owner.viewModelStore).isSameInstanceAs(store)
        assertThat(owner.viewModelStore).isNotSameInstanceAs(parent.viewModelStore)
        assertThat(owner.defaultViewModelProviderFactory).isSameInstanceAs(parent.defaultViewModelProviderFactory)
    }

    @Test
    fun clearingTheHolderClearsEveryRoot() {
        val first = probeIn(NavigationRoot.FirstRun)
        val main = probeIn(NavigationRoot.Main)

        stores.releaseAll()

        assertThat(first.cleared).isTrue()
        assertThat(main.cleared).isTrue()
    }

    private class ParentOwner : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = ViewModelStore()
        override val defaultViewModelProviderFactory: ViewModelProvider.Factory =
            viewModelFactory { initializer { Probe() } }
    }
}

package di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import viewmodel.DeviceListViewModel
import viewmodel.LoginViewModel
import viewmodel.RomQueryViewModel

/** Koin bindings for the presentation layer (ViewModels). */
val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RomQueryViewModel)
    viewModelOf(::DeviceListViewModel)
}

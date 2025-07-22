package pl.techbrewery.sam

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import pl.techbrewery.sam.di.appModules
import pl.techbrewery.sam.features.timber.DebugLogsTree
import pl.techbrewery.sam.kmp.database.initKmpModule
import pl.techbrewery.sam.kmp.di.kmpModules
import timber.log.Timber
import kotlin.getValue

class SamApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKmpModule(this)
        startKoin {
            androidContext(this@SamApp)
            modules(kmpModules + appModules)
        }
        Timber.plant(DebugLogsTree())
    }
}
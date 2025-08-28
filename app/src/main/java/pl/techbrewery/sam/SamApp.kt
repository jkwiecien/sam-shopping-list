package pl.techbrewery.sam

import android.app.Application
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import pl.techbrewery.sam.di.appModules
import pl.techbrewery.sam.kmp.database.initKmpModule
import pl.techbrewery.sam.kmp.di.kmpModules
import pl.techbrewery.sam.kmp.utils.SAMAntilog

class SamApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKmpModule(this)
        startKoin {
            androidContext(this@SamApp)
            modules(kmpModules + appModules)
        }
        Napier.base(SAMAntilog())
    }
}
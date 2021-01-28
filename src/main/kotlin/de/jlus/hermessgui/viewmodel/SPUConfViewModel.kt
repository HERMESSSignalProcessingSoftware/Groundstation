package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.SPUConfig
import tornadofx.ItemViewModel


/**
 * The ViewModel connecting an SPUConfig model with the SPUConfigTab
 */
class SPUConfViewModel(initItem: SPUConfig): ItemViewModel<SPUConfig>(initItem) {
    // general SPU settings
    val confName = bind(SPUConfig::confNameProperty)
    val ovrdMode = bind(SPUConfig::ovrdModeProperty)
    val sleep = bind(SPUConfig::sleepProperty)
    // dms adc settings
    val dmsOffsetCalInit = bind(SPUConfig::dmsOffsetCalInitProperty)
    val dmsFullscaleCalInit = bind(SPUConfig::dmsFullscaleCalInitProperty)
    val dmsSamplerate = bind(SPUConfig::dmsSamplerateProperty)
    val dmsPGA = bind(SPUConfig::dmsPGAProperty)
    // pt100 adc settings
    val pt100OffsetCalInit = bind(SPUConfig::pt100OffsetCalInitProperty)
    val pt100FullscaleCalInit = bind(SPUConfig::pt100FullscaleCalInitProperty)
    val pt100Samplerate = bind(SPUConfig::pt100SamplerateProperty)
    val pt100PGA = bind(SPUConfig::pt100PGAProperty)
    // data storage settings
    val storeMeasurementsEnabled = bind(SPUConfig::storeMeasurementsEnabledProperty)
    val storeMetadataEnabled = bind(SPUConfig::storeMetadataEnabledProperty)
    val storeStartOnLo = bind(SPUConfig::storeStartOnLOProperty)
    val storeStartOnSOE = bind(SPUConfig::storeStartOnSOEProperty)
    val storeStartOnSODS = bind(SPUConfig::storeStartOnSODSProperty)
    val storeStopOnLo = bind(SPUConfig::storeStopOnLOProperty)
    val storeStopOnSOE = bind(SPUConfig::storeStopOnSOEProperty)
    val storeStopOnSODS = bind(SPUConfig::storeStopOnSODSProperty)
    val storeMinTime = bind(SPUConfig::storeMinTimeProperty)
    val storeMaxTime = bind(SPUConfig::storeMaxTimeProperty)
    // TM settings
    val tmEnabled = bind(SPUConfig::tmEnabledProperty)
}
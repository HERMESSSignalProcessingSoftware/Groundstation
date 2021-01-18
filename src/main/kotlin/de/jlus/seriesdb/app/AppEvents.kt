package de.jlus.seriesdb.app

import de.jlus.seriesdb.view.MainTab
import tornadofx.FXEvent

object WindowFirstShow : FXEvent()
class OpenedProjectTab(val tab: MainTab) : FXEvent()
class ClosedProjectTab(val tab: MainTab) : FXEvent()
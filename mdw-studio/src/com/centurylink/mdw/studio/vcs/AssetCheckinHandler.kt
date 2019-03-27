package com.centurylink.mdw.studio.vcs

import com.centurylink.mdw.studio.MdwSettings
import com.centurylink.mdw.studio.action.AssetVercheck
import com.centurylink.mdw.studio.proj.ProjectSetup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import java.util.function.Consumer

class AssetCheckinHandlerFactory : CheckinHandlerFactory() {

    override fun createHandler(checkinPanel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return AssetCheckinHandler(checkinPanel.project, checkinPanel)
    }
}

class AssetCheckinHandler(private val project: Project, private val checkinPanel: CheckinProjectPanel) : CheckinHandler() {

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent? {
        project.getComponent(ProjectSetup::class.java)?.let { projectSetup: ProjectSetup ->
            if (projectSetup.isMdwProject) {
                return BooleanCommitOption(checkinPanel, "Check MDW Asset Versions", true,
                        { MdwSettings.instance.isAssetVercheckBeforeCommit },
                        Consumer { b -> MdwSettings.instance.isAssetVercheckBeforeCommit = b })
            }
        }
        return null
    }

    override fun beforeCheckin(): ReturnResult {
        project.getComponent(ProjectSetup::class.java)?.let { projectSetup: ProjectSetup ->
            if (projectSetup.isMdwProject && MdwSettings.instance.isAssetVercheckBeforeCommit) {
                AssetVercheck(projectSetup).performCheck()
            }
        }

        return ReturnResult.COMMIT
    }
}
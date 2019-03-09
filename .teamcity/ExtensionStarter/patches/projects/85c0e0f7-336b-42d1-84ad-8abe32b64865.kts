package ExtensionStarter.patches.projects

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the project with uuid = '85c0e0f7-336b-42d1-84ad-8abe32b64865' (id = 'ExtensionStarter')
accordingly, and delete the patch script.
*/
changeProject(uuid("85c0e0f7-336b-42d1-84ad-8abe32b64865")) {
    params {
        add {
            param("env.GLOBAL_ACCOUNT_NAME", "customer1_70a0b3c7-3f29-4b30-afea-f6e173520cd0")
        }
        add {
            param("env.EVENTS_SERVICE_HOST", "ec2-34-221-206-45.us-west-2.compute.amazonaws.com")
        }
    }
}
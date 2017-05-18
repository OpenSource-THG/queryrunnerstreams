node() {
    withEnv(["APP=java-project-template"]) {
        stage('Checkout') {
            checkout([
                $class: 'GitSCM',
                branches: [[name: "${GIT_TAG}"]],
                extensions: [
                    [$class: 'WipeWorkspace'],
                    [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: false]
                ],
                userRemoteConfigs: [
//                  @repository_group
                    [url: "git@gitlab.io.thehut.local:thg-common/${APP}.git",
                     credentialsId: 'jenkins-ssh']]
                ])
        }

        def tag = env.GIT_TAG
        currentBuild.displayName = tag

        stage('Release') {
//          @service_name
            sh('helm upgrade -i --kube-context gb1_kube --values config/live/values.yaml java-project-template ./kubernetes-chart')
        }
    }
}
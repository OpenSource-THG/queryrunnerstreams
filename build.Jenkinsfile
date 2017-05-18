#!groovy

node() {

    env.WORKSPACE = pwd()
//    @repository_name
    withEnv(["APP=java-project-template"]) {
    try {
        stage('Checkout') {
            checkout([
            $class: 'GitSCM',
            branches: [[name: 'develop']],
            extensions: [
                [$class: 'WipeWorkspace'],
                [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: false]
            ],
            userRemoteConfigs: [
//              @repository_group
                [url: "git@gitlab.io.thehut.local:thg-common/${APP}.git",
                 credentialsId: 'jenkins-ssh']]
            ])
        }

        stage('Compile and test') {
            sh "./gradlew clean build"
        }

        TAG="develop"

        docker.withRegistry('https://artifactory.io.thehut.local:5000/', 'artifactory.io.thehut.local:5000') {

            stage('Build Image'){
//              @repository_group, @image_name
                sh "docker build -t artifactory.io.thehut.local:5000/thg-common/${APP}:${TAG} ."
            }

            stage('Archive Image') {
//              @repository_group, @image_name
                sh "docker push artifactory.io.thehut.local:5000/thg-common/${APP}:${TAG} "
            }
        }

        currentBuild.result = "SUCCESS"

        } catch (err) {
          junit 'report.xml'
          currentBuild.result = "FAILURE"
      }
    }
}

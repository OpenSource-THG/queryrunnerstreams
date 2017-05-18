node() {

//  @repository_name
    withEnv(["APP=java-project-template"]) {
        stage('Checkout') {
            checkout([
                $class: 'GitSCM',
                branches: [[name: 'develop']],
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

        stage('Compile and test') {
            sh "mvn clean package"
        }

        stage('Input')
        choice = new ChoiceParameterDefinition('VERSION_TYPE', ['patch', 'minor', 'major'] as String[], 'This project uses Semantic Versioning. Please refer to http://semver.org/')
        def selectedType = input(id: 'selectedType', message: 'Select one', parameters: [choice])
        echo("Selected " + selectedType)

        stage('Create Tag') {
            sh("./build-scripts/helm-tag.sh -t ${selectedType}")
        }

        TAG=sh(returnStdout: true, script: "git describe --abbrev=0 --tags").trim()
        currentBuild.displayName = "${TAG}"

        docker.withRegistry('https://artifactory.io.thehut.local:5000/', 'artifactory.io.thehut.local:5000') {
            stage('Build Image') {
//              @repository_group
                docker.build("thg-common/${APP}:${TAG}").push()
                docker.build("thg-common/${APP}:latest").push()
            }
        }
    }

}
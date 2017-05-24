#!groovy

node {
  deleteDir()

  stage('checkout') {
    checkout([
      $class: 'GitSCM',
      branches: [[name: 'develop']],
      extensions: [
        [$class: 'WipeWorkspace'],
        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false,
          recursiveSubmodules: true, reference: '', trackingSubmodules: false]
      ],
      userRemoteConfigs: [
        [url: "git@gitlab.io.thehut.local:thg-common/fuzzyduck.git", credentialsId: 'jenkins-ssh']]
      ])
  }

  stage('Build') {
    try {
      sh './gradlew clean -Pfindbugs.xml build'
    } finally {
      step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/*.xml'])
      step([$class: 'FindBugsPublisher', pattern: 'build/reports/findbugs/main.xml'])
    }
  }

  def selectedType

  stage('Input') {
    choice = new ChoiceParameterDefinition('VERSION_TYPE', ['patch', 'minor', 'major'] as String[],
      'This project uses Semantic Versioning. Please refer to http://semver.org/')
    selectedType = input(
      id: 'selectedType',
      message: 'Select a version type',
      ok: 'Deploy',
      abort: 'Abort',
      parameters: [choice]
    )
    echo("Selected " + selectedType)
  }

  stage('Create Tag') {
    sh("./scripts/tag.sh -t ${selectedType}")
    TAG_VERSION=sh(returnStdout: true, script: "git describe --abbrev=0 --tags").trim()
    echo "New tag version ${TAG_VERSION}"
  }

  stage('Publish Artifact') {
    echo "Publishing artifact"
    sh "./gradlew artifactoryPublish -PprojectVersion=${TAG_VERSION}"
  }
}
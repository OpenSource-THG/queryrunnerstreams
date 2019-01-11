#!groovy

node {
  deleteDir()

  stage('Checkout') {
    checkout scm
    sh 'git checkout master && git pull'
  }

  stage('Test') {
    sh './gradlew clean test check'
  }

  stage('Build') {
    sh './gradlew clean build'
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

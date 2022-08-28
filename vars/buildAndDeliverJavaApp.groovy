import groovy.transform.Field

@Field
Map defaults = [
    run_tests: true,
]

def call(Map config) {
    pipeline {
        agent {
            docker {
                image 'maven:3-alpine'
            }
        }
        stages {
            stage('AUTOMATED PIPELINE') {
                when {
                    branch 'master'
                }
                stages {
                    stage('Build with tests') {
                        when {
                            equals expected: true, actual: config.run_tests
                        }
                        steps {
                            sh 'mvn -B clean package'
                        }
                    }
                    stage('Build without tests') {
                        when {
                            equals expected: false, actual: config.run_tests
                        }
                        steps {
                            sh 'mvn -B -DskipTests clean package'
                        }
                    }
                    stage('Test') {
                        steps {
                            sh 'mvn test'
                        }
                        post {
                            always {
                                junit 'target/surefire-reports/*.xml'
                            }
                        }
                    }
                    stage('Deliver') {
                        steps {
                            sh 'chmod +x ./scripts/deliver.sh && ./scripts/deliver.sh'
                        }
                    }
                }
            }
        }
    }
}